package com.newpos.libpay.trans;

import android.content.Context;

import com.android.newpos.libemv.PBOCManager;
import com.newpos.libpay.Logger;
import com.newpos.libpay.PaySdk;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.global.TMConstants;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.helper.ssl.NetworkHelper;
import com.newpos.libpay.paras.EmvAidInfo;
import com.newpos.libpay.paras.EmvCapkInfo;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Created by zhouqiang on 2017/10/11.
 * @author zhouqiang
 * clase abstracta de transacción
 */
public abstract class Trans {

	/** ---------------------------campo definir inicio---------------------------*/
	/**
	 * 0* tipo de mensaje
	 */
	protected String MsgID;

	/**
	 * 2* Número de cuenta principal
	 */
	protected String Pan;

	/**
	 * 3* Código de procesamiento de transacciones
	 */
	protected String ProcCode;

	/**
	 * 4* Cantidad de transacciones
	 */
	protected long Amount;

	/**
	 * 11* Número de auditoría de seguimiento del sistema
	 */
	protected String TraceNo;

	/**
	 * 12* Hora local de transacción
	 */
	protected String LocalTime;

	/**
	 * 13* Fecha local de transacción
	 */
	protected String LocalDate;

	/**
	 * 14* Fecha de vencimiento
	 */
	protected String ExpDate;

	/**
	 * 15 Fecha de liquidación
	 */
	protected String SettleDate;

	/**
	 * 22* Modo de entrada de punto de servicio
	 */
	protected String EntryMode;

	/**
	 * 23* Número de secuencia de la tarjeta
	 */
	protected String PanSeqNo;

	/**
	 * 25* Modo de condición de punto de servicio
	 */
	protected String SvrCode;

	/**
	 * 26* Código de captura de PIN de punto de servicio
	 */
	protected String CaptureCode;

	/**
	 * 32* Código de identificación de la institución adquirente
	 */
	protected String AcquirerID;

	/**
	 * Datos de la pista 1 (sin uso)
	 */
	protected String Track1;

	/**
	 * 35* Datos de la pista 2
	 */
	protected String Track2;

	/**
	 * 36* Datos de la pista 3
	 */
	protected String Track3;

	/**
	 * 37* Número de referencia de recuperación
	 */
	protected String RRN;

	/**
	 * 38* Código de respuesta de identificación de autorización
	 */
	protected String AuthCode;

	/**
	 * 39* Código de respuesta
	 */
	protected String RspCode;

	/**
	 * 41* Identificación del terminal del aceptador de tarjetas
	 */
	protected String TermID;

	/**
	 * 42* Código de identificación del aceptador de la tarjeta
	 */
	protected String MerchID;

	/**
	 * 44* Datos de respuesta adicionales
	 */
	protected String Field44;

	/**
	 * 48* Datos adicionales - Privada
	 */
	protected String Field48;

	/**
	 * 49* Código de moneda de transacción
	 */
	protected String CurrencyCode;

	/**
	 * 52* Datos PIN (bloqueo de PIN)
	 */
	protected String PIN;

	/**
	 * 53* Información de control relacionada con la seguridad
	 */
	protected String SecurityInfo;

	/**
	 * 54* Balance de Cuenta
	 */
	protected String ExtAmount;

	/**
	 * 55* Datos relacionados con el sistema de tarjeta de circuito integrado
	 */
	protected byte[] ICCData = null ;

	/**
	 * 58* PBOC_ELECTRONIC_DATA
	 */
	protected String Field58 ;

	/**
	 * 60* Campo 60 Reservada Privada
	 * —— longitud de campo		        											N3
	 * ——60.1  codigo tipo de mensaje		    									N2
	 * ——60.2  número de lote		        										N6
	 * ——60.3  Código de información de gestión de red								N3
	 * ——60.4  capacidad terminal	      											N1
	 * ——60.5  Código de condición ICC												N1
	 * ——60.6  Soporte para deducciones parciales y marcas de saldo de devolución. 	N1
	 * ——60.7  tipo de saldo 														N3
	 *
	 * Nota: para obtener más información detallada, consulte QCUP009.1-2014
	 */
	protected String Field60;
	/**
	 * si usa 60.1 y 60.1 de la traducción original
	 */
	protected boolean isUseOrg_603_601 = false;
	/**
	 * campo 60.1
	 */
	protected String F60_1;
	/**
	 * campo 60.2
	 */
	protected String BatchNo;
	/**
	 * campo 60.3
	 */
	protected String F60_3;

	/**
	 * 61* mensaje original
	 */
	protected String Field61;

	/**
	 * 62* Reservada privada
	 * Nota: para obtener más información detallada, consulte QCUP009.1-2014
	 */
	protected String Field62;

	/**
	 * 63* reservada privada
	 * Nota: para obtener más información detallada, consulte QCUP009.1-2014
	 */
	protected String Field63;

	/**
	 * 64* Código de autenticación de mensajes (MAC)
	 */
	protected String Field64;

	/** -----------------------campo definir fin-----------------------------*/


	/** -------------------------parámetros comunes de transacción--------------------*/
	/**
	 * Objeto de contexto
	 */
	protected Context context;

	/**
	 * ISO8583 objeto
	 */
	protected ISO8583 iso8583;

	/**
	 * Ayudante de red
	 */
	protected NetworkHelper netWork;

	/**
	 * configurar terminal
	 */
	protected TMConfig cfg;

	/**
	 * presentador del modo MVP
	 */
	protected TransInterface transInterface ;

	/**
	 * transacción nombre chino
	 */
	protected String TransCName;

	/**
	 * transacción nombre en inglés, consulte @{@link Type}
	 */
	protected String TransEName;

	/**
	 * si aumenta el bono NO.
	 */
	protected boolean isTraceNoInc;

	/**
	 * éxito si el código de respuesta (datos de respuesta en el campo 39) es "00".
	 */
	protected String RSP_00_SUCCESS = "00" ;

	/**
	 * Gerente de biblioteca de PBOC
	 */
	protected PBOCManager pbocManager ;
	/** ----------------------fin de los parámetros comunes de la transacción-------------------*/

	/***
	 * Trans object
	 * @param ctx Context @{@link Context}
	 * @param ename nombre de transaccion @{@link Type}
	 * @param tt presentador del modo MVP
	 */
	public Trans(Context ctx, String ename , TransInterface tt) {
		this.context = ctx ;
		this.TransEName = ename ;
		this.transInterface = tt ;
		this.pbocManager = PBOCManager.getInstance();
		this.pbocManager.setDEBUG(true);
		loadConfig();
		loadEMVConfig();
	}

	/**
	 * cargar parámetros de transacción desde la configuración del terminal
	 */
	private void loadConfig() {
		Logger.debug("==Trans->loadConfig==");

		cfg = TMConfig.getInstance();
		TermID = cfg.getTermID();
		MerchID = cfg.getMerchID();
		CurrencyCode = cfg.getCurrencyCode();
		BatchNo = ISOUtil.padleft("" + cfg.getBatchNo(), 6, '0');
		TraceNo = ISOUtil.padleft("" + cfg.getTraceNo(), 6, '0');

		//Init NetworkHelper
		boolean isPub = cfg.getPubCommun() ;
		String ip = isPub?cfg.getIp():cfg.getIP2();
		int port = Integer.parseInt(isPub?cfg.getPort():cfg.getPort2());
		netWork = new NetworkHelper(ip, port, cfg.getTimeout(), this.context);

		//Init ISO8583
		String tpdu = cfg.getTpdu();
		String header = cfg.getHeader();
		iso8583 = new ISO8583(this.context, tpdu, header);

		setFixedDatas();
	}

	/**
	 * cargar la configuración de la transacción EMV
	 */
	private void loadEMVConfig(){
		String aidFilePath = TMConfig.getRootFilePath() + EmvAidInfo.FILENAME;
		Logger.debug("load aid from path = "+aidFilePath);
		File aidFile = new File(aidFilePath);
		if (aidFile.exists()) {
			try {
				EmvAidInfo aidInfo = (EmvAidInfo) PAYUtils.file2Object(aidFilePath);
				if (aidInfo != null && aidInfo.getAidInfoList() != null) {
					for (byte[] item : aidInfo.getAidInfoList()) {
						Logger.debug("load aid:"+ISOUtil.byte2hex(item));
						byte[] aid = new byte[item.length - 1];
						System.arraycopy(item , 1 , aid , 0 , aid.length);
						pbocManager.setEmvParas(aid);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		String capkFilePath = TMConfig.getRootFilePath()+ EmvCapkInfo.FILENAME;
		Logger.debug("load capk from path = "+capkFilePath);
		File capkFile = new File(capkFilePath);
		if (capkFile.exists()) {
			try {
				EmvCapkInfo capk = (EmvCapkInfo) PAYUtils.file2Object(capkFilePath);
				if (capk != null && capk.getCapkList() != null) {
					for (byte[] item : capk.getCapkList()) {
						Logger.debug("load capk:"+ISOUtil.byte2hex(item));
						pbocManager.setEmvCapks(item);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	protected void setFixedDatas() {
		Logger.debug("==Trans->setFixedDatas==");
		if (null == TransEName) {
			return;
		}
		Properties pro = PAYUtils.lodeConfig(context, TMConstants.TRANS);
		if (pro == null) {
			return;
		}
		String prop = pro.getProperty(TransEName);
		String[] propGroup = prop.split(",");
		if (!PAYUtils.isNullWithTrim(propGroup[0])){
			MsgID = propGroup[0];
		}else{
			MsgID = null;
		}
		if (!isUseOrg_603_601) {
			if (!PAYUtils.isNullWithTrim(propGroup[1])){
				ProcCode = propGroup[1];
			}else{
				ProcCode = null;
			}
		}
		if (!PAYUtils.isNullWithTrim(propGroup[2])){
			SvrCode = propGroup[2];
		}else{
			SvrCode = null;
		}
		if (!isUseOrg_603_601) {
			if (!PAYUtils.isNullWithTrim(propGroup[3])){
				F60_1 = propGroup[3];
			}else{
				F60_1 = null;
			}
		}
		if (!PAYUtils.isNullWithTrim(propGroup[4])) {
			F60_3 = propGroup[4];
		}else {
			F60_3 = null;
		}
		if (F60_1 != null && F60_3 != null){
			Field60 = F60_1 + cfg.getBatchNo() + F60_3;
		}
		try {
			TransCName = new String(propGroup[5].getBytes("ISO-8859-1"), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * establecer si aumentar el vale NO.
	 * @param isTraceNoInc
     */
	public void setTraceNoInc(boolean isTraceNoInc) {
		this.isTraceNoInc = isTraceNoInc;
	}

	/**
	 * añadir datos del campo 60
	 * @param f60
     */
	protected void appendField60(String f60) {
		Field60 = Field60 + f60;
	}

	/**
	 * formato de código de respuesta
	 * @param rsp
	 * @return @{@link Tcode}
	 */
	protected int formatRsp(String rsp){
		String[] stand_rsp = {"5A","5B","6A","A0","D1","D2","D3","D4","N6","N7"} ;
		int START = 200 ;
		boolean finded = false ;
		for (int i = 0 ; i < stand_rsp.length ; i++){
			if(stand_rsp[i].equals(rsp)){
				START += i ;
				finded = true ;
				break;
			}
		}
		if(finded){
			return START ;
		}else {
			return Integer.parseInt(rsp) ;
		}
	}

	/**
	 * crear socket y conectar
	 * @return
	 */
	protected int connect() {
		return netWork.Connect();
	}

	/**
	 * paquete de datos iso8583 y enviar
	 * @return
	 */
	protected int send() {
		byte[] pack = iso8583.packetISO8583();
		if (pack == null) {
			return -1;
		}
		Logger.debug(TransEName+"->send:"+ ISOUtil.hexString(pack));
		return netWork.Send(pack);
	}

	/**
	 * recibir datos
	 * @return
	 */
	protected byte[] receive() {
		byte[] recive = null;
		try {
			recive = netWork.Recive(2048, cfg.getTimeout());
		} catch (IOException e) {
			Logger.debug("receive->IOException:"+e.toString());
			return null;
		}
		if(recive!=null){
			Logger.debug(TransEName+"->receive:"+ISOUtil.hexString(recive));
		}
		return recive;
	}
}
