package com.newpos.libpay.trans.manager.down;

import android.content.Context;

import com.android.newpos.libemv.PBOCManager;
import com.newpos.libpay.Logger;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.global.TMConstants;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.paras.EmvAidInfo;
import com.newpos.libpay.paras.EmvCapkInfo;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Type;
import com.newpos.libpay.trans.manager.ManageTrans;
import com.newpos.libpay.utils.ISOException;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouqiang on 2017/12/11.
 * @author zhouqiang
 * download parameters
 */
public class DparaTrans extends ManageTrans implements TransPresenter {

	public DparaTrans(Context ctx , String transEName  , TransInterface tt) {
		super(ctx, transEName , tt);
		isTraceNoInc = false ;
	}

	@Override
	public ISO8583 getISO8583() {
		return iso8583;
	}

	@Override
	public void start() {
		transInterface.handling(Tcode.EMV_AID_DOWNLOADING);
		if(cfg.isOnline()){
			int retVal = DownloadAid();
			if(retVal!=0){
				transInterface.showError(false , retVal);
				return;
			}
			transInterface.handling(Tcode.EMV_CAPK_DOWNLOAING);
			retVal = DownloadCapk();
			if(retVal!=0){
				transInterface.showError(false , retVal);
				return;
			}
			if(aidQueryList.size() > 0 && capkQueryList.size() > 0){
				transInterface.trannSuccess(Tcode.EMV_DOWNLOADING_SUCC);
			}else if(aidQueryList.size() > 0 && capkQueryList.size() <= 0){
				transInterface.showError(false , Tcode.NO_CAPK_NEED_DOWNLOAD);
			}else if(aidQueryList.size() <= 0 && capkQueryList.size() > 0){
				transInterface.showError(false , Tcode.NO_AID_NEED_DOWNLOAD);
			}else{
				transInterface.showError(false , Tcode.NO_AID_CAPK_DOWNLOAD);
			}

		}else {
			Logger.debug("load capk&aid offline");

			PAYUtils.copyAssetsToData(context , TMConstants.LOCALAID);
			PAYUtils.copyAssetsToData(context , TMConstants.LOCALCAPK);

			transInterface.trannSuccess(Tcode.EMV_DOWNLOADING_SUCC);
		}
	}

	private List<byte[]> capkQueryList;
	private List<byte[]> aidQueryList;
	private EmvAidInfo emvAidInfo;
	private EmvCapkInfo emvCapkInfo;


	private void setFields() {
		iso8583.clearData();
		if (MsgID != null){
			iso8583.setField(0, MsgID);
		}
		iso8583.setField(41, TermID); // 41
		if (MerchID != null){
			iso8583.setField(42, MerchID);// 42
		}
		if (Field60 != null){
			iso8583.setField(60, Field60);// 60
		}
		if (Field62 != null){
			iso8583.setField(62, Field62);// 62
		}
	}

	/**
	 * query CAPK from server
	 * QUERY_EMV_CAPK=0820,,,00,372
	 * DOWNLOAD_EMV_CAPK=0800,,,00,370
	 * DOWNLOAD_EMV_CAPK_END=0800,,,00,371
	 * QUERY_EMV_PARAM=0820,,,00,382
	 * DOWNLOAD_EMV_PARAM=0800,,,00,380
	 * DOWNLOAD_EMV_PARAM_END=0800,,,00,381
	 * @throws ISOException
	 */
	private int queryEMVCapk() throws ISOException {
		int reciveCount = 0;
		int offset = 0;
		int totalLen = 0;
		byte[] b1 = { (byte) 0x9f, 0x06, 0x05 };
		byte[] b2 = { (byte) 0x9f, 0x22, 0x01 };
		byte[] b3 = { (byte) 0xdf, 0x05 };
		byte[] temp = null;
		TransEName = Type.QUERY_EMV_CAPK ;
		setFixedDatas();
		// set field 62
		while (true) {
			offset = 0;
			Field62 = ISOUtil.byte2hex(("1" + ISOUtil.padleft(reciveCount + "", 2, '0')).getBytes());
			setFields();
			int retVal = OnLineTrans();
			if (retVal != 0) {
				return retVal;
			}

			String rspCode = iso8583.getfield(39);
			if (null == rspCode) {
				return Tcode.RECEIVE_DATA_FAIL;
			}
			if(!rspCode.equals(RSP_00_SUCCESS)){
				return formatRsp(rspCode) ;
			}
			String str62 = iso8583.getfield(62);
			if (null == str62) {
				return Tcode.RECEIVE_DATA_FAIL;
			}
			byte[] field62 = ISOUtil.str2bcd(str62, false);
			totalLen = field62.length;
			if (field62[0] == 0x30) {
				//end
				break;
			}
			offset++;
			while (offset < totalLen) {
				temp = new byte[6];
				if (!ISOUtil.memcmp(b1, 0, field62, offset, 3)) {
					return -1;
				}
				offset += 3;
				System.arraycopy(field62, offset, temp, 0, 5);
				offset += 5;

				if (!ISOUtil.memcmp(b2, 0, field62, offset, 3)) {
					return -1;
				}
				offset += 3;
				System.arraycopy(field62, offset, temp, 5, 1);
				offset++;

				if (!ISOUtil.memcmp(b3, 0, field62, offset, 2)) {
					return -1;
				}
				offset += 2;
				offset += field62[offset] + 1; // skip check expire date
				capkQueryList.add(temp);
				reciveCount++;
			}
			if (field62[0] != 0x32) {
				// end
				break;
			}
		}
		return 0;
	}

	/**
	 * query AID from server
	 * @return
	 * @throws ISOException
     */
	private int queryEMVParam() throws ISOException {
		int reciveCount = 0;
		int offset = 0;
		int totalLen = 0;
		byte[] temp = null;
		int aidLen = 0;
		TransEName = Type.QUERY_EMV_PARAM ;
		setFixedDatas();
		// 设置62域
		while (true) {
			offset = 0;
			Field62 = ISOUtil.byte2hex(("1" + ISOUtil.padleft(reciveCount + "",2, '0')).getBytes());
			setFields();
			int retVal = OnLineTrans();
			if (retVal != 0) {
				return retVal;
			}
			// 缓存
			String rspCode = iso8583.getfield(39);
			if (null == rspCode) {
				return Tcode.RECEIVE_DATA_FAIL;
			}
			if(!rspCode.equals(RSP_00_SUCCESS)){
				return formatRsp(rspCode);
			}
			String str62 = iso8583.getfield(62);
			if (null == str62) {
				return Tcode.RECEIVE_DATA_FAIL;
			}
			byte[] field62 = ISOUtil.str2bcd(str62, false);
			totalLen = field62.length;
			if (field62[0] == 0x30) {
				// end
				break;
			}
			offset++;
			while (offset < totalLen) {
				if (!ISOUtil.memcmp(new byte[] { (byte) 0x9f, 0x06 }, 0, field62, offset, 2)) {
					return -1;
				}
				offset += 2;

				aidLen = field62[offset++];
				temp = new byte[aidLen];
				System.arraycopy(field62, offset, temp, 0, aidLen);
				offset += aidLen;
				aidQueryList.add(temp);
				reciveCount++;
			}
			if (field62[0] != 0x32) {
				// end
				break;
			}
		}
		return 0;
	}

	/**
	 * download CAPK from server
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
     */
	private int downEMVCapk() throws IOException {
		TransEName = Type.DOWNLOAD_EMV_CAPK ;
		setFixedDatas();
		List<byte[]> capkList = new ArrayList<>();

		// set field 62
		for (byte[] item : capkQueryList) {
			// 9F06 05 A000000333 9F22 01 02
			byte[] pack_result_rid = new byte[8];
			byte[] pack_result_index = new byte[4];
			byte[] result = new byte[12];
			int packLen = PAYUtils.pack_tlv_data(pack_result_rid, 0x9F06, 5, item, 0);
			System.arraycopy(pack_result_rid, 0, result, 0, packLen);
			packLen = PAYUtils.pack_tlv_data(pack_result_index, 0x9F22, 1, item, 5);
			System.arraycopy(pack_result_index, 0, result, pack_result_rid.length, packLen);
			Field62 = ISOUtil.byte2hex(result);
			setFields();
			int retVal = OnLineTrans();
			if (retVal != 0) {
				return retVal;
			}

			String rspCode = iso8583.getfield(39);
			if (null == rspCode) {
				return Tcode.RECEIVE_DATA_FAIL;
			}
			if(!rspCode.equals(RSP_00_SUCCESS)){
				return formatRsp(rspCode);
			}
			String str62 = iso8583.getfield(62);
			if (null == str62) {
				return Tcode.RECEIVE_DATA_FAIL;
			}
			byte[] field62 = ISOUtil.str2bcd(str62, false);
			if (field62[0] != 0x31) {
				// end
				break;
			}
			byte[] temp = new byte[field62.length-1];
			System.arraycopy(field62 , 1 , temp , 0 , temp.length);
			capkList.add(temp);
		}
		emvCapkInfo.setCapkList(capkList);
		return 0;
	}

	/**
	 * download AID
	 * @return
     */
	private int downEMVParam() {
		TransEName = Type.DOWNLOAD_EMV_PARAM ;
		setFixedDatas();
		List<byte[]> aidList = new ArrayList<>() ;

		// set field 62
		for (byte[] item : aidQueryList) {
			// 9F06 07 A000000003 1010 aid
			byte[] pack_result_aid = new byte[item.length + 3];
			PAYUtils.pack_tlv_data(pack_result_aid, 0x9F06, item.length, item, 0);
			Field62 = ISOUtil.byte2hex(pack_result_aid);
			setFields();
			int retVal = OnLineTrans();
			if (retVal != 0) {
				return retVal;
			}

			String rspCode = iso8583.getfield(39);
			if (null == rspCode) {
				return Tcode.RECEIVE_DATA_FAIL;
			}
			if(!rspCode.equals(RSP_00_SUCCESS)){
				return formatRsp(rspCode);
			}
			String str62 = iso8583.getfield(62);
			if (null == str62) {
				return Tcode.RECEIVE_DATA_FAIL;
			}
			byte[] field62 = ISOUtil.str2bcd(str62, false);
			if (field62[0] != 0x31) {
				// end
				break;
			}
			byte[] temp = new byte[field62.length -1];
			System.arraycopy(field62 , 1 , temp , 0 , temp.length);
			aidList.add(temp);
		}
		emvAidInfo.setAidInfoList(aidList);
		return 0;
	}

	/**
	 *end of download CAPK
	 * @return
     */
	private int downEMVCapkEnd() {
		TransEName = Type.DOWNLOAD_EMV_CAPK_END ;
		setFixedDatas();
		Field62 = null ;
		setFields();
		int retVal = OnLineTrans();
		if (retVal != 0) {
			return retVal;
		}

//		String rspCode = iso8583.getfield(39);
//		if (null == rspCode || !rspCode.equals(RSP_00_SUCCESS)) {
//			return Tcode.T_receive_err;
//		}
//		String str62 = iso8583.getfield(62);
//		if (null == str62){
//			return Tcode.T_receive_err;
//		}
//		byte[] field62 = ISOUtil.str2bcd(str62, false);
//		Logger.debug("downEMVCapkEnd->field62:"+ISOUtil.byte2hex(field62));
//		if (field62[0] == 0x30) {
//			// No mas o finalmente
			return 0;
//		}
//		return -1 ;
	}

	/**
	 * end of download AID
	 * @return
     */
	private int downEMVParamEnd() {
		TransEName = Type.DOWNLOAD_EMV_PARAM_END ;
		setFixedDatas();
		Field62 = null ;
		setFields();
		int retVal = OnLineTrans();
		if (retVal != 0) {
			return retVal;
		}
//
//		String rspCode = iso8583.getfield(39);
//		if (null == rspCode || !rspCode.equals(RSP_00_SUCCESS)) {
//			return Tcode.T_receive_err;
//		}
//		String str62 = iso8583.getfield(62);
//		if (null == str62){
//			return -1;
//		}
//		byte[] field62 = ISOUtil.str2bcd(str62, false);
//		if (field62[0] == 0x30) {
//			// No mas o finalmente
			return 0;
//		}
//		return -1 ;
	}

	/**
	 * download AID
	 * @return
     */
	public int DownloadAid() {
		aidQueryList = new ArrayList<>();
		emvAidInfo = new EmvAidInfo();
		try {
			int retVal = queryEMVParam();
			if (retVal != 0) {
				return retVal ;
			}

			if(aidQueryList.size() > 0){
				retVal = downEMVParam();
				if(retVal != 0){
					return retVal ;
				}
				retVal = downEMVParamEnd();
				if(retVal!=0){
					return retVal;
				}
				// save
				try {
					PAYUtils.object2File(emvAidInfo, TMConfig.getRootFilePath() + EmvAidInfo.FILENAME);
					Logger.debug("save emv parameters successfully");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return Tcode.UNKNOWN_ERROR;
				} catch (IOException e) {
					e.printStackTrace();
					return Tcode.UNKNOWN_ERROR;
				}
			}
		} catch (ISOException e) {
			e.printStackTrace();
			return Tcode.UNKNOWN_ERROR ;
		}
		return 0 ;
	}

	/**
	 * download CAPK
     */
	public int DownloadCapk() {
		capkQueryList = new ArrayList<>();
		emvCapkInfo = new EmvCapkInfo();
		try {
			int retVal = queryEMVCapk();
			if (retVal != 0){
				return retVal ;
			}
			if(capkQueryList.size() > 0){
				retVal = downEMVCapk();
				if(retVal != 0){
					return retVal ;
				}
				retVal = downEMVCapkEnd();
				if (retVal != 0) {
					return retVal;
				}
				// save
				PAYUtils.object2File(emvCapkInfo, TMConfig.getRootFilePath() + EmvCapkInfo.FILENAME);
				Logger.debug("save capk infomation successfully");
			}
		} catch (ISOException e) {
			e.printStackTrace();
			return Tcode.UNKNOWN_ERROR ;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return Tcode.UNKNOWN_ERROR ;
		} catch (IOException e) {
			e.printStackTrace();
			return Tcode.UNKNOWN_ERROR ;
		}
		return 0 ;
	}
}