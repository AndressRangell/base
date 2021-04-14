package com.newpos.libpay.helper.iso8583;

import android.content.Context;

import com.newpos.libpay.Logger;
import com.newpos.libpay.device.pinpad.PinpadManager;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.global.TMConstants;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * ISO8583
 * @author
 */
public class ISO8583 {

	//Nota: el campo 0 es el tipo de mensaje
	//el campo 1 es mapa de bits
	private boolean isHasMac = false;
	private int fieldNum; // longitud del campo
	private FieldAttr[] listFieldAttr; // attr de campo
	private String[] listFieldData; // todos los datos de campo

	private String[] listFieldRespData;// datos del campo de respuesta
	private String tpdu; // tpdu
	private String header; // encabezado del paquete
	private String rspHeader; // encabezado de respuesta del servidor
	private String rspTpdu; //respuesta tupu del servidor

	private Context mContext ;

	private TransLogData LogData = new TransLogData();

	public TransLogData getLogData() {
		return LogData;
	}

	public String getRspHeader() {
		return rspHeader;
	}

	/**
	 * @param context tpdu cabecera
	 */
	public ISO8583(Context context, String tpdu, String header) {
		this.mContext = context ;
		fieldNum = Integer.parseInt(PAYUtils.lodeConfig(context, TMConstants.ISO8583, "filedNum"));
		int attrLen = fieldNum + 3;
		listFieldAttr = new FieldAttr[attrLen];
		SetAttr(context, fieldNum);
		listFieldData = new String[fieldNum + 1];// 0~64
		listFieldRespData = new String[fieldNum + 1];// 0~64
		this.tpdu = tpdu;
		this.header = header;
		this.mContext = context ;
	}

	/**
	 * establecer cada atributo de campo
	 * @param context
	 * @param totalFiled
     */
	private void SetAttr(Context context, int totalFiled) {
		Properties pro = PAYUtils.lodeConfig(context, TMConstants.ISO8583);
		listFieldAttr[0] = getAttr(pro, "tpdu");
		listFieldAttr[1] = getAttr(pro, "header");
		for (int i = 0; i <= totalFiled; i++) {
			listFieldAttr[i + 2] = getAttr(pro, i + "");
		}
	}

	/**
	 * obtener el atributo de campo del archivo de configuración
	 * @param pro objeto de archivo de configuración
	 * @param proName nombre
	 * @return campo atributo
	 */
	private FieldAttr getAttr(Properties pro, String proName) {
		String prop = pro.getProperty(proName);
		if (prop == null) {
			return null;
		}
		String[] propGroup = prop.split(",");
		FieldAttr attr = new FieldAttr();
		int data_len = Integer.parseInt(propGroup[1]) ;
		attr.setDataLen(data_len);
		attr.setDataType(Integer.parseInt(propGroup[2]));
		attr.setLenAttr(Integer.parseInt(propGroup[0]));
		attr.setLenType(Integer.parseInt(propGroup[3]));

		return attr;
	}

	/**
	 * establecer datos de campo
	 * @param fieldNo campo No.
	 * @param data dato del campo
	 * @return
	 */
	public int setField(int fieldNo, String data) {
		if (fieldNo > fieldNum) {
			return -1;
		}
		listFieldData[fieldNo] = data;
		return 0;
	}

	/**
	 * establecer datos de campo de respuesta
	 * @param fieldNo
	 * @param data
     * @return
     */
	private int setRspField(int fieldNo, String data) {
		if (fieldNo > fieldNum) {
			return -1;
		}
		Logger.debug("receive field " + fieldNo + ":" + data);
		listFieldRespData[fieldNo] = data;
		return 0;
	}

	/**
	 * obtener datos del campo de respuesta por el campo NO.
	 * @param fieldNo campo NO.
	 * @return
	 */
	public String getfield(int fieldNo) {
		if (fieldNo > fieldNum) {
			return null;
		}
		return listFieldRespData[fieldNo];
	}

	/**
	 * packet iso8583 data
	 * @return iso8583 data
	 */
	public byte[] packetISO8583() {
		byte[] temp = new byte[1024]; // búfer temporal
		byte[] bitmap = new byte[16];// mapa de bits
		byte[] bb = new byte[2048];// datos del paquete
		int lenAttr, lenType, dataType, dataMaxLen, headLen;
		int offset = 0;
		int dataLen = 0;
		int appResult = -1;

		// ***********************TPDU
		FieldAttr attr = listFieldAttr[0];
		appResult = appendHeader(attr, tpdu, temp);
		if (appResult < 0) {
			return null;
		}
		System.arraycopy(temp, 0, bb, offset, appResult);
		offset += appResult;
		// ***********************cabecera
		attr = listFieldAttr[1];
		appResult = appendHeader(attr, header, temp);
		if (appResult < 0) {
			return null;
		}
		System.arraycopy(temp, 0, bb, offset, appResult);
		offset += appResult;
		// ***********************tipo de mensaje
		attr = listFieldAttr[2];
		String fieldData = listFieldData[0];
		appResult = appendHeader(attr, fieldData, temp);
		if (appResult < 0) {
			return null;
		}
		System.arraycopy(temp, 0, bb, offset, appResult);
		offset += appResult;

		headLen = offset;// el desplazamiento del mapa de bits
		attr = listFieldAttr[3]; // atributo de mapa de bits
		if (attr.getDataType() == FieldTypesDefine.FIELDATTR_TYPE_ASCII) {
			offset += fieldNum / 4;// la longitud del mapa de bits
		}else {
			offset += fieldNum / 8;// la longitud del mapa de bits
		}

		if (fieldNum == 128){
			bitmap[0] = (byte) 0x80;
		}

		for (int i = 2; i < fieldNum; i++) {
			attr = listFieldAttr[i + 2];
			fieldData = listFieldData[i];

			// si los datos de campo o el atributo de campo son nulos, omita
			if (attr == null || fieldData == null) {
				continue;
			}

			bitmap[(i - 1) / 8] |= 0x80 >> ((i - 1) % 8);
			// obtener atributo de campo
			lenAttr = attr.getLenAttr();
			lenType = attr.getLenType();
			dataType = attr.getDataType();
			dataMaxLen = attr.getDataLen();
			// *********************** definir la longitud por atributo de datos de campo
			if (dataType == FieldTypesDefine.FIELDATTR_TYPE_BIN) {
				dataLen = fieldData.length() / 2;
			}else {
				dataLen = fieldData.length();
			}

			if (lenAttr == FieldTypesDefine.FIELDATTR_LEN_TYPE_NO) {
				if (dataLen != dataMaxLen) {
					Logger.debug("len != MaxLen fieldNum:" + i +",listFieldData["+i+"]="+listFieldData[i]);
					return null;
				}
			} else {
				if (dataLen > dataMaxLen) {
					Logger.debug("len > MaxLen  fieldNum:" + i +",listFieldData["+i+"]="+listFieldData[i]);
					return null;
				}
				if (lenType == FieldTypesDefine.FIELDATTR_TYPE_N) {
					temp = ISOUtil.int2bcd(dataLen, lenAttr);
					System.arraycopy(temp, 0, bb, offset, lenAttr);
					offset += lenAttr;
				} else if (lenType == FieldTypesDefine.FIELDATTR_TYPE_BIN) {
					//
				} else if (lenType == FieldTypesDefine.FIELDATTR_TYPE_ASCII) {
					//
				}
			}

			if (dataType == FieldTypesDefine.FIELDATTR_TYPE_BIN) {
				temp = ISOUtil.hex2byte(fieldData);
				System.arraycopy(temp, 0, bb, offset, dataLen);
			} else if (dataType == FieldTypesDefine.FIELDATTR_TYPE_ASCII) {
				System.arraycopy(fieldData.getBytes(), 0, bb, offset, dataLen);
			} else if (dataType == FieldTypesDefine.FIELDATTR_TYPE_CN) {
				temp = ISOUtil.str2bcd(fieldData, false, (byte) 0); // right pad
				dataLen = temp.length;
				System.arraycopy(temp, 0, bb, offset, dataLen);
			} else if (dataType == FieldTypesDefine.FIELDATTR_TYPE_N) {
				temp = ISOUtil.str2bcd(fieldData, true, (byte) 0);// left pad
				dataLen = temp.length;
				System.arraycopy(temp, 0, bb, offset, dataLen);
			}
			offset += dataLen;
		}
		if (isHasMac) {
			bitmap[fieldNum / 8 - 1] |= 0x01;
		}
		attr = listFieldAttr[3];
		if (attr.getDataType() == FieldTypesDefine.FIELDATTR_TYPE_ASCII) {
			System.arraycopy(ISOUtil.byte2hex(bitmap).getBytes(), 0, bb,
					headLen, fieldNum / 4);
		} else {
			System.arraycopy(bitmap, 0, bb, headLen, fieldNum / 8);
		}
		if (isHasMac) {
			// ******************************calcular mac
			byte[] mac ;
			if(TMConfig.getInstance().getStandard() == 2){
				mac = getCITICMAC(listFieldData);
			}else {
				mac = PinpadManager.getInstance().getMac(bb, headLen - 2, offset - headLen + 2);
			}
			// ******************************MAC
			if (mac != null) {
				System.arraycopy(mac, 0, bb, offset, 8);
			}else {
				return null;
			}
			offset += 8;
		}

		byte[] packet = new byte[offset];
		System.arraycopy(bb, 0, packet, 0, offset);
		return packet;
	}

	private byte[] getCITICMAC(String[] packages){
		Logger.debug("==getCITICMAC==");
		//41 42   // 32 44  // 61
		int[] result_fileds = {0, 3, 4, 11, 12, 13, 25, 32, 38, 39,41,42,44,61} ;
		String temp = "" ;
		for (int i = 0 ; i < result_fileds.length ; i++){
			if(packages[result_fileds[i]]!=null){
				String str ;
				if(result_fileds[i] == 41 || result_fileds[i] == 42){
					byte[] bcd = ISOUtil.str2bcd(packages[result_fileds[i]] , false);
					int l = bcd.length*2 ;
					if(result_fileds[i] == 42){
						l -= 1 ;
					}
					str = ISOUtil.bcd2str(bcd , 0 , l , false);
				}else if(result_fileds[i] == 32 || result_fileds[i] == 44){
					int ll = packages[result_fileds[i]].length() ;
					if(ll < 10){
						str = "0"+ll+packages[result_fileds[i]];
					}else {
						str = ll + packages[result_fileds[i]];
					}
				}else if(result_fileds[i] == 61){
					int lll = packages[result_fileds[i]].length() ;
					if( lll < 16){
						str = packages[result_fileds[i]];
					}else {
						str = packages[result_fileds[i]].substring(0 , 16);
					}
				}else {
					str = packages[result_fileds[i]];
				}
				temp += str + " ";
			}
		}
		temp = temp.trim();
		Logger.debug("temp="+temp);
		String ascii = BCD2ASC(temp.getBytes()) ;
		Logger.debug("ascii = "+ascii);

		byte[] mac_in =  ISOUtil.hex2byte(ascii);
		Logger.debug("mac_in = "+ISOUtil.hexString(mac_in));
		Logger.debug("mac_in len = "+mac_in.length);
		byte[] mac = PinpadManager.getInstance().getMac(mac_in , 0 , mac_in.length);
		if(mac != null){
			Logger.debug("mac = "+ISOUtil.hexString(mac));
		}
		String bcd2ascii = BCD2ASC(ISOUtil.hexString(mac).getBytes()).substring(0 , 16);
		mac = ISOUtil.hex2byte(bcd2ascii);
		Logger.debug("mac ascii= "+ISOUtil.hexString(mac));

		return mac;
	}

	public final static char[] BToA = "0123456789abcdef".toCharArray() ;
	public static String BCD2ASC(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			int h = ((bytes[i] & 0xf0) >>> 4);
			int l = (bytes[i] & 0x0f);
			temp.append(BToA[h]).append( BToA[l]);
		}
		return temp.toString() ;
	}

	/**
	 * desempaquetar datos de respuesta del servidor
	 * @param data datos de respuesta del servidor
	 * @return 0: correcto, otros: error.response conjunto de datos en listfieldRespData
	 */
	public int unPacketISO8583(byte[] data) {
		Logger.debug("==ISO8583->unPacketISO8583");
		byte[] MacBlock = null;
		byte[] bitmap = new byte[16];
		int offset = 0;
		int lenAttr, lenType, dataMaxLen;
		int dataLen = 0;
		FieldAttr attr = new FieldAttr();
		int headLen = 0;
		// ******************************tpdu
		attr = listFieldAttr[0];
		offset += appRsp(-2, attr, data, offset, attr.getDataLen());
		// ******************************header
		attr = listFieldAttr[1];
		offset += appRsp(-1, attr, data, offset, attr.getDataLen());
		headLen = offset;
		// ******************************message type
		attr = listFieldAttr[2];
		offset += appRsp(0, attr, data, offset, attr.getDataLen());
		// ******************************bitmap
		if (listFieldAttr[3].getDataLen() != 8) {
			bitmap = new byte[16];
		}else {
			bitmap = new byte[8];
		}
		if (listFieldAttr[3].getDataType() == FieldTypesDefine.FIELDATTR_TYPE_ASCII) {
			// System.arraycopy(data, offset, bitmap, 0, bitmap.length);
			String strMap = new String(data, offset, bitmap.length);
			bitmap = ISOUtil.str2bcd(strMap, true);
			setRspField(1, strMap);
			offset += bitmap.length * 2;
		} else {
			System.arraycopy(data, offset, bitmap, 0, bitmap.length);
			setRspField(1, ISOUtil.byte2hex(bitmap));
			offset += bitmap.length;
		}

		for (int i = 2; i <= fieldNum; i++) {
			if (offset > data.length) {
				return Tcode.UNKNOWN_ERROR;
			}
			if (offset == data.length) {
				break;
			}
			if ((bitmap[(i - 1) / 8] & (0x80 >> ((i - 1) % 8))) == (byte) 0) {
				continue;
			}
			attr = listFieldAttr[i + 2];

			//si el atributo de campo es nulo, omita
			if(attr == null){
				continue;
			}

			dataMaxLen = attr.getDataLen();
			// dataType = attr.getDataType();
			lenAttr = attr.getLenAttr();
			lenType = attr.getLenType();

			// ******************************longitud
			if (lenAttr == FieldTypesDefine.FIELDATTR_LEN_TYPE_NO) {
				dataLen = dataMaxLen;
			} else {
				if (lenType == FieldTypesDefine.FIELDATTR_TYPE_N) {
					dataLen = ISOUtil.bcd2int(data, offset, lenAttr);
					offset += lenAttr;
				} else if (lenType == FieldTypesDefine.FIELDATTR_TYPE_ASCII) {
					dataLen = Integer
							.parseInt(new String(data, offset, lenAttr));
					offset += lenAttr;
				} else {
					dataLen = ISOUtil.byte2int(data, offset, lenAttr);
					offset += lenAttr;
				}
			}
			// ******************************dato
			if (i == 64) {
				if(TMConfig.getInstance().getStandard() == 2){
					MacBlock = getCITICMAC(listFieldRespData);
				}else {
					MacBlock = PinpadManager.getInstance().getMac(data, headLen, offset - headLen);
				}
			}
			if(data.length < offset || data.length < offset+dataLen){
				break;//error
			}else {
				int ret = appRsp(i, attr, data, offset, dataLen);
				if(ret == -1){
					break;
				}else {
					offset += ret ;
				}
			}
		}

		String recMac = listFieldRespData[64];
		if (null != recMac) {
			byte[] recBMac = recMac.getBytes();
			for (int i = 0; i < 4; i++) {
				if (MacBlock[i] != recBMac[i]) {
					return Tcode.RECEIVE_MAC_ERROR;
				}
			}
		}

		int sendMsgId = PAYUtils.Object2Int(listFieldData[0]);
		int reciveMsgId = PAYUtils.Object2Int(listFieldRespData[0]);
		String sendProCode = listFieldData[3];
		String reciveProCode = listFieldRespData[3];
		String sendTrackNo = listFieldData[11];
		String reciveTrackNo = listFieldRespData[11];
		String sendTermID = listFieldData[41];
		String reciveTermID = listFieldRespData[41];
		String sendMerchID = listFieldData[42];
		String reciveMerchID = listFieldRespData[42];

		if (reciveMsgId - sendMsgId != 10) {
			return Tcode.ILLEGAL_PACKAGE;
		}
		if (reciveTrackNo != null && !sendTrackNo.equals(reciveTrackNo)) {
			return Tcode.ILLEGAL_PACKAGE;
		}
		if (reciveProCode != null && !sendProCode.equals(reciveProCode)) {
			return Tcode.ILLEGAL_PACKAGE;
		}
		if (!sendTermID.equals(reciveTermID)) {
			return Tcode.ILLEGAL_PACKAGE;
		}
		if (!sendMerchID.equals(reciveMerchID)) {
			return Tcode.ILLEGAL_PACKAGE;
		}

		return 0;
	}


	public void set62AttrDataType(int type){
		listFieldAttr[64].setDataType(type);
	}


	public void clearData() {
		listFieldData = null;
		listFieldAttr = null;
		fieldNum = Integer.parseInt(PAYUtils.lodeConfig(mContext, TMConstants.ISO8583, "filedNum"));
		int attrLen = fieldNum + 3;
		listFieldAttr = new FieldAttr[attrLen];// tpdu header
		SetAttr(mContext, fieldNum);
		listFieldData = new String[fieldNum + 1];// 0~64
		listFieldRespData = new String[fieldNum + 1];// 0~64
	}

	public boolean isHasMac() {
		return isHasMac;
	}

	public void setHasMac(boolean isHasMac) {
		this.isHasMac = isHasMac;
	}

	/***
	 * @param attr atributo de campo
	 * @param content dato de campo
	 * @param bb bufer
	 * @return
	 */
	private int appendHeader(FieldAttr attr, String content, byte[] bb) {
		byte[] temp = null;
		int dataLen = -1;
		if (attr.getDataType() != FieldTypesDefine.FIELDATTR_TYPE_ASCII) {
			if (attr.getDataLen() != content.length()) {
				return -1;
			}
			temp = ISOUtil.str2bcd(content, false, (byte) 0);
			dataLen = temp.length;
			System.arraycopy(temp, 0, bb, 0, dataLen);
		} else {
			if (attr.getDataLen() != content.length()) {
				return -1;
			}
			dataLen = content.length();
			System.arraycopy(content.getBytes(), 0, bb, 0, dataLen);
		}
		return dataLen;
	}

	private int appRsp(int FieldNo,FieldAttr attr,byte[] data,int offset,int dataLen) {
		int rspOffset = 0;
		String temp = null;
		if (attr.getDataType() == FieldTypesDefine.FIELDATTR_TYPE_ASCII) {
			if (FieldNo == 63) {
				byte[] a = new byte[dataLen];
				System.arraycopy(data, offset, a, 0, dataLen);
				try {
					temp = new String(a, "gbk");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				temp = new String(data, offset, dataLen);
			}
			rspOffset = dataLen;
		} else if (attr.getDataType() == FieldTypesDefine.FIELDATTR_TYPE_BIN) {
			temp = ISOUtil.byte2hex(data, offset, dataLen);
			rspOffset = dataLen;
		} else {
			rspOffset = (dataLen + 1) / 2;
			//add
			if(data.length<=offset || data.length < offset+rspOffset){
				return -1 ;
			}else {
				temp = ISOUtil.byte2hex(data, offset, rspOffset);
			}
		}
		if (FieldNo == -1) {
			rspHeader = temp;
		}else if (FieldNo == -2) {
			rspTpdu = temp;
		}else {
			setRspField(FieldNo, temp);
		}
		return rspOffset;
	}
}