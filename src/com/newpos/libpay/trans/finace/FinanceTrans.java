package com.newpos.libpay.trans.finace;

import android.content.Context;

import com.android.newpos.dataapi.DataApiImpl;
import com.android.newpos.libemv.EMVISRCode;
import com.android.newpos.libemv.PBOCCardInfo;
import com.android.newpos.libemv.PBOCException;
import com.android.newpos.libemv.PBOCListener;
import com.android.newpos.libemv.PBOCOnlineResult;
import com.android.newpos.libemv.PBOCPin;
import com.android.newpos.libemv.PBOCPinRet;
import com.android.newpos.libemv.PBOCPinType;
import com.android.newpos.libemv.PBOCTransProperty;
import com.android.newpos.libemv.PBOCUtil;
import com.android.newpos.libemv.PBOCode;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.device.pinpad.PinResult;
import com.newpos.libpay.device.pinpad.PinType;
import com.newpos.libpay.device.pinpad.PinpadManager;
import com.newpos.libpay.device.printer.PrintManager;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Trans;
import com.newpos.libpay.trans.Type;
import com.newpos.libpay.trans.manager.reversal.RevesalTrans;
import com.newpos.libpay.trans.manager.script.ScriptTrans;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.SlotType;
import com.pos.device.printer.PrintTask;
import com.pos.device.printer.Printer;

import javax.xml.parsers.FactoryConfigurationError;

import cn.desert.newpos.payui.base.PayApplication;

/**
 * Created by zhouqiang on 2017/12/11.
 * @author zhouqiang
 * Finanzas Transaccion
 */
public class FinanceTrans extends Trans {

	/**
	 * modo de entrada de servicio
	 * @ {@link ServiceEntryMode}
	 */
	protected ServiceEntryMode inputMode ;

	/**
	 * transacción si es necesario guardar
	 */
	protected boolean isSaveLog;

	/**
	 * transacción si necesita PIN o si existe PIN
	 */
	protected boolean isPinExist;

	/**
	 * transacción si necesita reversión
	 */
	protected boolean isReversal;

	/**
	 * si necesita preprocesamiento, como reversión, envío de script, etc.
	 */
	protected boolean isProcPreTrans;

	/**
	 * si es necesario posprocesamiento después de la transacción finalizada
	 */
	protected boolean isProcSuffix;

	/**
	 * ya sea permitir retroceso
	 */
	protected boolean isFallBack;

	/**
	 * si necesita GAC2
	 */
	protected boolean isNeedGAC2;

	/**
	 * si transacción con tarjeta de débito
	 */
	protected boolean isDebit;

	/**
	 * si es necesario imprimir recibo
	 */
	protected boolean isNeedPrint;

	/**
	 * registro de transacciones
	 */
	protected TransLog transLog;

	/**
	 * datos de registro de transacciones
	 */
	protected TransLogData data;

	/**
	 * @param ctx context @{@link Context}
	 * @param transEname transaction name @{@link com.newpos.libpay.trans.Type}
     */
	public FinanceTrans(Context ctx, String transEname , TransInterface tt) {
		super(ctx, transEname , tt);
		transLog = TransLog.getInstance();
		iso8583.setHasMac(true);
		setTraceNoInc(true);
	}

	/**
	 * procesar algunos datos especiales antes en línea
     */
	protected void setSpecialDatas() {
		EntryMode = ISOUtil.padleft(String.valueOf(inputMode.getVal()), 2, '0');
		if (isPinExist) {
			CaptureCode = "12";
			EntryMode += "10";
		}else {
			EntryMode += "20";
		}
		if (isPinExist || Track2 != null || Track3 != null) {
			if(isPinExist) {
				SecurityInfo = "2";
			} else {
				SecurityInfo = "0";
			}
			if (cfg.isSingleKey()) {
				SecurityInfo += "0";
			} else {
				SecurityInfo += "6";
			}
			if (cfg.isTrackEncrypt()) {
				SecurityInfo += "10000000000000";
			} else {
				SecurityInfo += "00000000000000";
			}
		}
		appendField60("048");
	}

	/**
	 * configurar algunos datos de la tarjeta IC
	 */
	protected void setICCData(){
		Logger.debug("==FinanceTrans->setICCData==");
		PBOCCardInfo info = PBOCUtil.getPBOCCardInfo() ;
		Pan = info.getCardNO();
		ExpDate = info.getExpDate();
		Track2 = info.getCardTrack2();
		Track1 = info.getCardTrack1();
		Track3 = info.getCardTrack3();
		PanSeqNo = info.getCardSeqNo();
		ICCData = PBOCUtil.getF55Data(PBOCUtil.wOnlineTags);
	}

	/**
	 * establecer datos de transacciones en iso8583
	 */
	protected void setFields() {
        Logger.debug("==FinanceTrans->setFields==");

		if (MsgID != null) {
			iso8583.setField(0, MsgID);
		}
		if (Pan != null) {
			iso8583.setField(2, Pan);
		}
		if (ProcCode != null) {
			iso8583.setField(3, ProcCode);
		}
		if (Amount > 0) {
			String AmoutData = ISOUtil.padleft(Amount + "", 12, '0');
			iso8583.setField(4, AmoutData);
		}
		if (TraceNo != null) {
			iso8583.setField(11, TraceNo);
		}
		if (LocalTime != null) {
			iso8583.setField(12, LocalTime);
		}
		if (LocalDate != null) {
			iso8583.setField(13, LocalDate);
		}
		if (ExpDate != null) {
			iso8583.setField(14, ExpDate);
		}
		if (SettleDate != null) {
			iso8583.setField(15, SettleDate);
		}
		if (EntryMode != null) {
			iso8583.setField(22, EntryMode);
		}
		if (PanSeqNo != null) {
			iso8583.setField(23, PanSeqNo);
		}
		if (SvrCode != null) {
			iso8583.setField(25, SvrCode);
		}
		if (CaptureCode != null) {
			iso8583.setField(26, CaptureCode);
		}
		if (AcquirerID != null) {
			iso8583.setField(32, AcquirerID);
		}
		Logger.debug("Track2:"+Track2);
		Track2 = "6212163510102451610=4912120503" ;
		if (Track2 != null && cfg.isTrackEncrypt()) {
            Track2 = PinpadManager.getInstance().getEac(
					cfg.getMasterKeyIndex() , Track2);
			Logger.debug("Track2:"+Track2);
		}
		iso8583.setField(35, Track2);
		if (Track3 != null && cfg.isTrackEncrypt()) {
            Track3 = PinpadManager.getInstance().getEac(
					cfg.getMasterKeyIndex() , Track3);
		}
		iso8583.setField(36, Track3);
		if (RRN != null) {
			iso8583.setField(37, RRN);
		}
		if (AuthCode != null) {
			iso8583.setField(38, AuthCode);
		}
		if (RspCode != null) {
			iso8583.setField(39, RspCode);
		}
		if (TermID != null) {
			iso8583.setField(41, TermID);
		}
		if (MerchID != null) {
			iso8583.setField(42, MerchID);
		}
		if (Field44 != null) {
			iso8583.setField(44, Field44);
		}
		if (Field48 != null) {
			iso8583.setField(48, Field48);
		}
		if (CurrencyCode != null) {
			iso8583.setField(49, CurrencyCode);
		}
		if (PIN != null) {
			iso8583.setField(52, PIN);
		}
		if (SecurityInfo != null) {
			iso8583.setField(53, SecurityInfo);
		}
		if (ExtAmount != null) {
			iso8583.setField(54, ExtAmount);
		}
		if (ICCData != null) {
			iso8583.setField(55, ISOUtil.byte2hex(ICCData));
		}
		if (Field60 != null) {
			iso8583.setField(60, Field60);
		}
		if (Field61 != null) {
			iso8583.setField(61, Field61);
		}
		if (Field62 != null) {
			iso8583.setField(62, Field62);
		}
		if (Field63 != null) {
			iso8583.setField(63, Field63);
		}
	}

	/**
	 * iniciar la transacción PBOC
	 * @param property propiedad de transacción
	 * @return
     */
	protected int startPBOC(PBOCTransProperty property){
		transInterface.handling(Tcode.PROCESSING);
		int pboccode ;
		try {
			pboccode = pbocManager.startPBOC(property , listener);
		} catch (PBOCException e) {
			pboccode = PBOCode.PBOC_UNKNOWN_ERROR ;
		}
		Pan = PBOCUtil.getPBOCCardInfo().getCardNO();
		return pboccode ;
	}

	/**
	 * handle ingrese el PIN de la tarjeta sin contacto
	 * @return
     */
	protected int handleNFCPin(){
		if(!pbocManager.isCardSupportedOnlinePIN()){
			return 0 ;
		}
		PinType type = new PinType();
		type.setOnline(true);
		type.setCardNO(PBOCUtil.getPBOCCardInfo().getCardNO());
		type.setAmount(String.valueOf(Amount));
		PinInfo info = transInterface.getPinpadPin(type);
		PinResult result = info.getResult() ;
		if(result != PinResult.SUCCESS){
			return info.getErrno() ;
		}
		byte[] pin = info.getPinblock() ;
		if(pin == null){
			isPinExist = false ;
		}else {
			isPinExist = true ;
			PIN = ISOUtil.hexString(pin);
		}
		return 0 ;
	}

	/**
	 * handle datos de la tarjeta de banda magnética
	 * @return
     */
	protected int handleMAGData(String[] tracks){
		String data1 = null;
		String data2 = null;
		String data3 = null;
		int msgLen = 0;
		if (tracks[0].length() > 0 && tracks[0].length() <= 80) {
			data1 = new String(tracks[0]);
		}
		if (tracks[1].length() >= 13 && tracks[1].length() <= 37) {
			data2 = new String(tracks[1]);
			if(!data2.contains("=")){
				return Tcode.SEARCH_CARD_FAIL ;
			}
			String judge = data2.substring(0, data2.indexOf('='));
			if(judge.length() < 13 || judge.length() > 19){
				return Tcode.SEND_DATA_FAIL ;
			}
			if (data2.indexOf('=') != -1) {
				msgLen++;
			}
		}
		if (tracks[2].length() >= 15 && tracks[2].length() <= 107) {
			data3 = new String(tracks[2]);
		}
		if (msgLen == 0) {
			return Tcode.SEARCH_CARD_FAIL ;
		}
		if (!isFallBack) {
			int splitIndex = data2.indexOf("=");
			if (data2.length() - splitIndex < 5) {
				return Tcode.SEARCH_CARD_FAIL ;
			}
			char iccChar = data2.charAt(splitIndex + 5);
			if (iccChar == '2' || iccChar == '6') {
				return Tcode.IC_NOT_FALLBACK ;
			}
		}
		Pan = data2.substring(0, data2.indexOf('='));
		Track2 = data2;
		Track3 = data3;
		return 0 ;
	}

	/**
	 * handle ingrese el PIN de la tarjeta de banda magnética
	 * @return
     */
	protected int handleMAGPin(){
		PinType type = new PinType();
		type.setOnline(true);
		type.setCardNO(Pan);
		type.setAmount(String.valueOf(Amount));
		PinInfo info = transInterface.getPinpadPin(type);
		PinResult result = info.getResult() ;
		if(result != PinResult.SUCCESS){
			return info.getErrno();
		}
		byte[] pin = info.getPinblock() ;
		if(pin == null){
			isPinExist = false ;
		}else {
			isPinExist = true ;
			PIN = ISOUtil.hexString(pin);
		}
		return 0 ;
	}

	/**
	 * prepararse en línea
	 */
	protected void prepareOnline(){
		transInterface.handling(Tcode.CONNECTING_CENTER);
		setSpecialDatas();
		int retVal ;
		if(cfg.isOnline()){
			retVal = OnlineTrans();
		}else {
			retVal = LocalPresentations();
		}
		clearPan();
		if(retVal!=0){
			Logger.debug("Trans:" + TransEName + ",error=" + retVal);
			transInterface.showError(false , retVal);
			return;
		}
		int code = 0 ;
		String additionalInfo = null ;
		switch (TransEName.replace("-" , " ")){
			case Type.SALE :
				code = Tcode.SALE_SUCCESS ;
				additionalInfo = PAYUtils.getStrAmount(Amount) ;
				break;
			case Type.VOID :
				code = Tcode.VOID_SUCCESS ;
				additionalInfo = PAYUtils.getStrAmount(Amount) ;
				data.setVoided(true);
				TransLog.getInstance().updateTransLog(
						TransLog.getInstance().getCurrentIndex(data),data);
				break;
			case Type.ENQUIRY :
				code = Tcode.ENQUIRY_SUCCESS ;
				additionalInfo = PAYUtils.getStrAmount(Amount) ;
				break;
			case Type.QUICKPASS :
				code = Tcode.QUICKPASS_SUCCESS ;
				additionalInfo = PAYUtils.getStrAmount(Amount) ;
				break;
			case Type.REFUND :
				code = Tcode.REFUND_SUCCESS ;
				additionalInfo = PAYUtils.getStrAmount(Amount) ;
				//data.setVoided(true);//modify by Andy
				//TransLog.getInstance().updateTransLog(
				//		TransLog.getInstance().getCurrentIndex(data),data);
				break;
			case Type.PREAUTH :
				code = Tcode.PREAUTH_SUCCESS ;
				additionalInfo = PAYUtils.getStrAmount(Amount) ;
				break;
			case Type.PREAUTHVOID :
				code = Tcode.PREAUTH_VOID_SUCCESS ;
				additionalInfo = PAYUtils.getStrAmount(Amount) ;
				//data.setVoided(true);
				//TransLog.getInstance().updateTransLog(
				//		TransLog.getInstance().getCurrentIndex(data),data);
				break;
			case Type.PREAUTHCOMPLETE :
				//data.setPreComp(true);//Modify by Andy
				//TransLog.getInstance().updateTransLog(
				//		TransLog.getInstance().getCurrentIndex(data),data);
				code = Tcode.PREAUTH_COMPLETE_SUCCESS ;
				additionalInfo = PAYUtils.getStrAmount(Amount) ;
				break;
			case Type.PREAUTHCOMPLETEVOID :
				data.setVoided(true);
				TransLog.getInstance().updateTransLog(
						TransLog.getInstance().getCurrentIndex(data),data);
				code = Tcode.COMPLETE_VOID_SUCCESS ;
				additionalInfo = PAYUtils.getStrAmount(Amount) ;
				break;
		}
		transInterface.trannSuccess(code , additionalInfo);

	}

	/**
	 * PBOC listener
	 */
	private final PBOCListener listener = new PBOCListener() {
		@Override
		public int dispMsg(int i, String s, int i1) {
			return 0;
		}

		@Override
		public int callbackSelApp(String[] strings) {
			return transInterface.choseAppList(strings);
		}

		@Override
		public int callbackCardNo(String s) {
			Pan = s ;
			return transInterface.confirmCardNO(s);
		}

		@Override
		public PBOCPin callbackEnterPIN(PBOCPinType pbocPinType) {
			PBOCPin pin = new PBOCPin();
			PinType type = new PinType();
			type.setAmount(String.valueOf(Amount));
			type.setCardNO(Pan);
			type.setOnline(pbocPinType.isOnlinePin());
			type.setCounts(pbocPinType.getOfflinePinCounts());
			type.setPinKey(pbocPinType.getPinKey());
			type.setType(pbocPinType.isPlainPin()?0:1);
			PinInfo info = transInterface.getPinpadPin(type);
			PinResult result = info.getResult() ;
			if(result == PinResult.SUCCESS){
				pin.setPbocPinRet(PBOCPinRet.SUCCESS);
			}if(result == PinResult.FAIL){
				pin.setPbocPinRet(PBOCPinRet.FAIL);
			}if(result == PinResult.NO_OPERATION){
				pin.setPbocPinRet(PBOCPinRet.NO_OPERATION);
				pin.setPinKeyIndex(cfg.getMasterKeyIndex());
			}
			pin.setPinBlock(info.getPinblock());
			pin.setErrno(info.getErrno());
			return pin;
		}

		@Override
		public int callbackVerifyCert(int i, String s) {
			return transInterface.confirmCardVerifyCert(s);
		}

		@Override
		public void pbocBeforeGPO() {
			transInterface.beforeGPO();
		}
	};

	/**
	 * transaccion Online
     * @return
     */
	protected int OnlineTrans() {
		int retVal = 0 ;
		setFields();
		if(isProcPreTrans){
			retVal = preTrans();
			if(retVal != 0){
				return retVal ;
			}
		}

		transInterface.handling(Tcode.CONNECTING_CENTER);
        if (connect() != 0){
			return Tcode.SOCKET_FAIL ;
		}

		if (isReversal) {
			Logger.debug("FinanceTrans->OnlineTrans->save Reversal");
			TransLogData Reveral = setReveralData();
			TransLog.saveReversal(Reveral);
		}

		transInterface.handling(Tcode.SEND_DATA_CENTER);
		if (send() != 0){
			netWork.close();
			return Tcode.SEND_DATA_FAIL ;
		}

		//aumentar el rastro no
		if (isTraceNoInc) {
			cfg.incTraceNo();
		}

		transInterface.handling(Tcode.RECEIVE_CENtER_DATA);
		byte[] respData = receive();
		netWork.close();
		if (respData == null){
			return Tcode.RECEIVE_DATA_FAIL ;
		}

		retVal = iso8583.unPacketISO8583(respData);
		if(retVal!=0){
			if(retVal == Tcode.RECEIVE_MAC_ERROR && isReversal){
				TransLogData newR = TransLog.getReversal() ;
				newR.setRspCode("A0");
				TransLog.clearReveral();
				TransLog.saveReversal(newR);
			}
			return retVal ;
		}

		RspCode = iso8583.getfield(39);
		AuthCode = iso8583.getfield(38);
		String strICC = iso8583.getfield(55);
		if (!PAYUtils.isNullWithTrim(strICC)){
			ICCData = ISOUtil.str2bcd(strICC, false);
		}else{
			ICCData = null ;
		}
		if(!RSP_00_SUCCESS.equals(RspCode)){
			TransLog.clearReveral();
			return formatRsp(RspCode);
		}

		if(isNeedGAC2){
			retVal = genAC2Trans();
			if(retVal != PBOCode.PBOC_TRANS_SUCCESS){
				return retVal ;
			}
		}

		//enviar script de emisor
		TransLogData data = TransLog.getScriptResult();
		if (data != null) {
			ScriptTrans script = new ScriptTrans(context, Type.SENDSCRIPT);
			int ret = script.sendScriptResult(data);
			if (ret == 0) {
				TransLog.clearScriptResult();
			}
		}

		// guardar registro de transacciones
		if (isSaveLog) {
			TransLogData logData = setLogData();
			transLog.saveLog(logData);
			//Agregar al servicio de datos
			try {
				PayApplication.getInstance().getDataApi().
						add(DataApiImpl.transfer(logData));
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		//borrar el registro de reversión después de que la transacción se haya realizado correctamente
		TransLog.clearReveral();

		if(TransEName.equals(Type.ENQUIRY)){
			String f54 = iso8583.getfield(54) ;
			if(!PAYUtils.isNullWithTrim(f54)){
				Amount = Long.parseLong(f54.substring(f54.indexOf('C')+1 , f54.length())) ;
			}else {
				return Tcode.RECEIVE_DATA_FAIL ;
			}
		}

		if(isNeedPrint){
			return printTrans();
		}

		return retVal ;
	}

	/**
	 * lidiar con la reversión
	 * @return
     */
	private int preTrans(){
		int retVal = 0 ;
		TransLogData revesalData = TransLog.getReversal();
		if (revesalData != null) {
			transInterface.handling(Tcode.SEND_REVERSAL);
			RevesalTrans revesal = new RevesalTrans(context, Type.REVERSAL);
			for (int i = 0; i < cfg.getReversalCount() ; i++) {
				retVal = revesal.sendRevesal();
				if(retVal == 0){
					//borrar el registro de reversión después de que la reversión se realice correctamente
					TransLog.clearReveral();
					break;
				}
			}
			//fin de inversión
			Logger.debug("preTrans->sendRevesal:"+retVal);
			if(retVal == Tcode.SOCKET_FAIL || retVal == Tcode.SEND_DATA_FAIL){
				//error
				return retVal ;
			}else {
				if(retVal != 0){
					//la reversión falló
					TransLog.clearReveral();
					return Tcode.REVERSAL_FAIL ;
				}
			}
		}
		return retVal ;
	}

	/**
	 * tratar con GAC2
	 * @return
     */
	private int genAC2Trans(){
		PBOCOnlineResult result = new PBOCOnlineResult();
		result.setField39(RspCode.getBytes());
		result.setFiled38(AuthCode.getBytes());
		result.setField55(ICCData);
		result.setResultCode(PBOCOnlineResult.ONLINECODE.SUCCESS);
		int retVal = pbocManager.afterOnlineProc(result);
		Logger.debug("genAC2Trans->afterOnlineProc:"+retVal);

		//Emitir resultado del acuerdo de script
		int isResult = pbocManager.getISResult();
		if(isResult != EMVISRCode.NO_ISR){
			// guardar el resultado de la secuencia de comandos del problema
			byte[] temp = new byte[256];
			int len = PAYUtils.pack_tags(PAYUtils.wISR_tags, temp);
			if (len > 0) {
				ICCData = new byte[len];
				System.arraycopy(temp, 0, ICCData, 0, len);
			} else{
				ICCData = null;
			}
			TransLogData scriptResult = setScriptData();
			TransLog.saveScriptResult(scriptResult);
		}

		if(retVal != PBOCode.PBOC_TRANS_SUCCESS){
			//La transacción de la tarjeta IC falló, si devuelve "00" en el campo 39,
			//actualizar el campo 39 como "06" en los datos de inversión
			TransLogData revesalData = TransLog.getReversal();
			if(revesalData!=null){
				revesalData.setRspCode("06");
				TransLog.saveReversal(revesalData);
			}
		}

		return retVal ;
	}

	/**
	 * establecer datos de registro de transacciones
	 * @return TransLog
	 */
	private TransLogData setLogData() {
		TransLogData LogData = new TransLogData();
		LogData.setPan(PAYUtils.getSecurityNum(Pan, 6, 4));
		LogData.setOprNo(cfg.getOprNo());
		LogData.setBatchNo(BatchNo);
		LogData.setEName(TransEName.replace("-" , " "));
		LogData.setOnline(true);
        LogData.setTraceNo(iso8583.getfield(11));
        LogData.setLocalTime(iso8583.getfield(12));
        LogData.setLocalDate(PAYUtils.getYear() + iso8583.getfield(13));
        LogData.setExpDate(iso8583.getfield(14));
        LogData.setSettleDate(iso8583.getfield(15));
        LogData.setEntryMode(iso8583.getfield(22));
        LogData.setPanSeqNo(iso8583.getfield(23));
        LogData.setAcquirerID(iso8583.getfield(32));
        LogData.setRRN(iso8583.getfield(37));
        LogData.setAuthCode(iso8583.getfield(38));
        LogData.setRspCode(iso8583.getfield(39));
        LogData.setField44(iso8583.getfield(44));
        LogData.setCurrencyCode(iso8583.getfield(49));
		LogData.setICCData(ICCData);
		LogData.setMode(inputMode);
		if(TransEName.equals(Type.ENQUIRY)){

		}else{
			LogData.setAmount(Long.parseLong(iso8583.getfield(4)));
			String field63 = iso8583.getfield(63);
			String IssuerName = field63.substring(0, 3);
			String ref = field63.substring(3, field63.length());
			LogData.setRefence(ref);
			LogData.setIssuerName(IssuerName);
		}
		return LogData;
	}

	/**
	 * establecer escaneo pagar datos de registro de transacciones
	 * @param code QR code
	 * @return
     */
	protected TransLogData setScanData(String code){
		TransLogData LogData = new TransLogData();
		LogData.setAmount(Amount);
		LogData.setPan(code);
		LogData.setOprNo(cfg.getOprNo());
		LogData.setBatchNo(BatchNo);
		LogData.setEName(TransEName.replace("-" , " "));
		LogData.setICCData(ICCData);
		LogData.setMode(inputMode);
		LogData.setLocalDate(PAYUtils.getYMD());
		LogData.setTraceNo(TraceNo);
		LogData.setOnline(true);
		LogData.setLocalTime(PAYUtils.getHMS());
		LogData.setSettleDate(PAYUtils.getYMD());
		LogData.setAcquirerID("12345678");
		LogData.setRRN("170907084952");
		LogData.setAuthCode("084952");
		LogData.setRspCode("00");
		LogData.setField44("0425       0461       ");
		LogData.setCurrencyCode("156");

		//Agregar al servicio de datos
		try {
			PayApplication.getInstance().getDataApi().
					add(DataApiImpl.transfer(LogData));
		}catch (Exception e){
			e.printStackTrace();
		}

		return LogData;
	}

	/**
	 * transacción fuera de línea
	 * @param ec_amount
     * @return
     */
//	protected int offlineAccept(String ec_amount){
//		if (isSaveLog) {
//			TransLogData LogData = new TransLogData();
//			if(TransEName.equals(Type.EC_ENQUIRY)){
//				LogData.setAmount(Long.parseLong(ec_amount));
//			}else {
//				LogData.setAmount(Amount);
//			}
//			LogData.setPan(PAYUtils.getSecurityNum(Pan, 6, 4));
//			LogData.setOprNo(cfg.getOprNo());
//			LogData.setEName(TransEName.replace("-" , " "));
//			LogData.setEntryMode(ISOUtil.padleft(inputMode + "", 2, '0')+"10");
//			LogData.setTraceNo(cfg.getTraceNo());
//			LogData.setBatchNo(cfg.getBatchNo());
//			LogData.setLocalDate(PAYUtils.getYear() + PAYUtils.getLocalDate());
//			LogData.setLocalTime(PAYUtils.getLocalTime());
//			LogData.setOnline(false);
//			LogData.setICCData(ICCData);
//			LogData.setMode(inputMode);
//			transLog.saveLog(LogData);
//			if(isTraceNoInc){
//				cfg.incTraceNo();
//			}
//		}
//		if(isNeedPrint){
//			return printTrans();
//		}
//		return 0 ;
//	}

	/**
	 * establecer datos de secuencia de comandos de emisión
	 * @return
     */
	private TransLogData setScriptData() {
		TransLogData LogData = new TransLogData();
		LogData.setPan(PAYUtils.getSecurityNum(Pan, 6, 4));
		LogData.setICCData(ICCData);
		LogData.setBatchNo(BatchNo);
        LogData.setAmount(Long.parseLong(iso8583.getfield(4)));
        LogData.setTraceNo(iso8583.getfield(11));
        LogData.setLocalTime(iso8583.getfield(12));
        LogData.setLocalDate(iso8583.getfield(13));
        LogData.setEntryMode(iso8583.getfield(22));
        LogData.setPanSeqNo(iso8583.getfield(23));
        LogData.setAcquirerID(iso8583.getfield(32));
        LogData.setRRN(iso8583.getfield(37));
        LogData.setAuthCode(iso8583.getfield(38));
        LogData.setCurrencyCode(iso8583.getfield(49));
		return LogData ;
	}

	/**
	 * establecer datos de reversión
	 * @return
     */
	private TransLogData setReveralData() {
		TransLogData LogData = new TransLogData();
		LogData.setPan(Pan);
		LogData.setProcCode(ProcCode);
		LogData.setAmount(Amount);
		LogData.setTraceNo(TraceNo);
		LogData.setExpDate(ExpDate);
		LogData.setEntryMode(EntryMode);
		LogData.setPanSeqNo(PanSeqNo);
		LogData.setSvrCode(SvrCode);
		LogData.setAuthCode(AuthCode);
		LogData.setRspCode("98");
		LogData.setCurrencyCode(CurrencyCode);
        byte[] temp = new byte[156];
        if (inputMode == ServiceEntryMode.ICC || inputMode == ServiceEntryMode.NFC) {
            int len = PAYUtils.pack_tags(PAYUtils.reversal_tag, temp);
            if (len > 0) {
                ICCData = new byte[len];
                System.arraycopy(temp, len, ICCData, 0, len);
                LogData.setICCData(ICCData);
            } else {
				ICCData = null;
			}
        }
		LogData.setField60(Field60);
		return LogData;
	}

	/**
	 * esta es una función de demostración, solo para transacciones fuera de línea
	 * @return
     */
	private int LocalPresentations(){
		if (isSaveLog) {
			TransLogData LogData = new TransLogData();
			LogData.setAmount(Amount);
			LogData.setPan(PAYUtils.getSecurityNum(Pan, 6, 4));
			LogData.setOprNo(cfg.getOprNo());
			LogData.setEName(TransEName.replace("-" , " "));
			LogData.setEntryMode(ISOUtil.padleft(inputMode + "", 2, '0')+"10");
			LogData.setTraceNo(cfg.getTraceNo());
			LogData.setBatchNo(cfg.getBatchNo());
			LogData.setLocalDate(PAYUtils.getYear() + PAYUtils.getLocalDate());
			LogData.setLocalTime(PAYUtils.getLocalTime());
			LogData.setRRN("123456789012");//Add by Andy Yuan
			LogData.setAuthCode(PAYUtils.getLocalTime());
			LogData.setOnline(false);
			LogData.setICCData(ICCData);
			LogData.setMode(inputMode);

			//Agregar al servicio de datos
			try {
				PayApplication.getInstance().getDataApi().
						add(DataApiImpl.transfer(LogData));
			}catch (Exception e){
				e.printStackTrace();
			}

			transLog.saveLog(LogData);
			if(isTraceNoInc){
				cfg.incTraceNo();
			}
		}
		if(isNeedPrint){
			return printTrans();
		}

		return 0 ;
	}

	/**
	 * iniciar la transacción de impresión
	 * @return
	 */
	protected int printTrans(){
		boolean isPrinted = true ;
		boolean isPrinterBroken = false ;
		PrintManager pm = PrintManager.getmInstance(context);
		for (int i = 0 ; i < cfg.getPrinterTickNumber() ; i ++){
			PrintTask task = pm.buildTaskByTLD(transLog.getLastTransLog() ,
					i , false);
			int ret = 0 ;
			do {
				transInterface.handling(Tcode.PRINTING_RECEPT);
				ret = pm.print(task);
				if(Printer.PRINTER_STATUS_PAPER_LACK == ret){
					int result = transInterface.printerLackPaper();
					if(1 == result){
						isPrinted = false ;
						break;
					}
				}else if(Printer.PRINTER_OK != ret){
					isPrinterBroken = true ;
					break;
				}
			}while (ret == Printer.PRINTER_STATUS_PAPER_LACK) ;
		}
		if(!isPrinted){
			//el usuario cancela la impresión
			return 0 ;
		}if(isPrinterBroken){
			return Tcode.PRINT_FAIL;
		}
		return 0 ;
	}

	/**
	 * borre los datos de la tarjeta, como el número de tarjeta, los datos de seguimiento, los datos de la tarjeta IC.
	 */
	protected void clearPan() {
		Pan = null;
		Track2 = null;
		Track3 = null;
		ICCData = null ;
		System.gc();
	}
}
