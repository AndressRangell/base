package com.newpos.libpay.trans.manager.logon;

import android.content.Context;

import com.newpos.libpay.Logger;
import com.newpos.libpay.device.pinpad.PinpadKeytem;
import com.newpos.libpay.device.pinpad.PinpadKeytype;
import com.newpos.libpay.device.pinpad.PinpadManager;
import com.newpos.libpay.device.pinpad.WorkKeyinfo;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Type;
import com.newpos.libpay.trans.manager.ManageTrans;
import com.newpos.libpay.trans.manager.down.DparaTrans;
import com.newpos.libpay.utils.ISOUtil;
import com.pos.device.ped.Ped;

/**
 * Created by zhouqiang on 2017/12/11.
 * @author zhouqiang
 * terminal sign in
 */

public class LogonTrans extends ManageTrans implements TransPresenter{

	public LogonTrans(Context ctx , String transEN , TransInterface tt) {
		super(ctx, transEN , tt);
		isTraceNoInc = false ;
	}

	@Override
	public ISO8583 getISO8583() {
		return iso8583;
	}

	@Override
	public void start() {
		if(!cfg.isOnline()){
			transInterface.handling(Tcode.TERMINAL_LOGON);
			int retVal = SignInOffline() ;
			if( retVal  == 0){
				transInterface.trannSuccess(Tcode.LOGON_SUCCESS);
			}else {
				transInterface.showError(false , Tcode.UNKNOWN_ERROR);
			}
			return;
		}
		int retVal = sign() ;
		if(retVal!=0){
			transInterface.showError(false , Tcode.UNKNOWN_ERROR);
			return;
		}
		if(pbocManager.getAllEmvParas().size()!=0){
			transInterface.trannSuccess(Tcode.LOGON_SUCCESS);
			return;
		}
		DparaTrans dTrans = new DparaTrans(context , Type.DOWNPARA , transInterface);
		transInterface.handling(Tcode.EMV_AID_DOWNLOADING);
		retVal = dTrans.DownloadAid();
		if(retVal!=0){
			transInterface.showError(false , retVal);
			return;
		}
		transInterface.handling(Tcode.EMV_CAPK_DOWNLOAING);
		retVal = dTrans.DownloadCapk();
		if(retVal!=0){
			transInterface.showError(false , retVal);
			return;
		}
		transInterface.trannSuccess(Tcode.LOGON_DOWN_SUCCESS);
		return;
	}

	private int sign(){
		transInterface.handling(Tcode.TERMINAL_LOGON);
		int retVal = SignIn();
		if(retVal!=0){
			return retVal ;
		}
		return 0 ;
	}

	private void setFields() {
		if (MsgID != null) {
			iso8583.setField(0, MsgID);
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
		iso8583.setField(41, TermID);
		if (MerchID != null){
			iso8583.setField(42, MerchID);
		}
		if (Field60 != null){
			iso8583.setField(60, Field60);
		}
		if (Field62 != null){
			iso8583.setField(62, Field62);
		}
		if (Field63 != null){
			iso8583.setField(63, Field63);
		}
	}

	/**
	 * sign in
	 * @throws
	 **/
	public int SignIn() {
		TransEName = Type.LOGON ;
		setFixedDatas();
		iso8583.set62AttrDataType(2);
		iso8583.setField(11, cfg.getTraceNo());
		iso8583.setField(62,"53657175656E6365204E6F3132333230393832303030373031");
		String f60_3 ;
		if (cfg.isSingleKey()) {
			f60_3 = "001";
		} else if (cfg.isTrackEncrypt()) {
			f60_3 = "004";
		} else {
			f60_3 = "003";
		}
		Field60 = Field60.substring(0, 8) + f60_3;
		iso8583.setField(63, ISOUtil.padleft(cfg.getOprNo()+"",2,'0') + " ");
		setFields();
		int retVal = OnLineTrans();
		if (retVal != 0) {
			return retVal ;
		}
		String rspCode = iso8583.getfield(39);
		netWork.close();
		if(rspCode == null){
			return Tcode.RECEIVE_DATA_FAIL;
		}
		if(!rspCode.equals(RSP_00_SUCCESS)){
			return formatRsp(rspCode);
		}
		String str60 = iso8583.getfield(60);
		cfg.setBatchNo(Integer.parseInt(str60.substring(2 , 8))).save();
		Logger.debug("current batchNo = " + cfg.getBatchNo());
		String strField62 = iso8583.getfield(62);
		if (strField62 == null) {
			return Tcode.RECEIVE_DATA_FAIL;
		}
		byte[] field62 = ISOUtil.str2bcd(strField62, false);
		return setKey(field62);
	}

	/**
	 * just a demo for sign in offline
	 * @return
	 */
	public int SignInOffline() {
		byte[] keys = ISOUtil.str2bcd("1CF08008FD62A1E217153829C3A6E51C2A7B0CB84A187EE99C9D002BE1010250792913C4325EA56471657F39F8B3D6562CC515E0403BEB676CCCB22E" , false);
		return setKey(keys);
	}

	private int setKey(byte[] keyData) {
		WorkKeyinfo workKeyinfo = new WorkKeyinfo() ;
		workKeyinfo.setMasterKeyIndex(cfg.getMasterKeyIndex());
		workKeyinfo.setWorkKeyIndex(cfg.getMasterKeyIndex());
		workKeyinfo.setMode(Ped.KEY_VERIFY_KVC);
		workKeyinfo.setKeySystem(PinpadKeytem.MS_DES);

		byte[] temp ;
		int keyLen = 20;
		if(keyData.length!=60 && keyData.length!=40) {
			return -1;
		}
		//Inject PIN key
		temp = new byte[keyLen];
		System.arraycopy(keyData, 0, temp, 0, keyLen);
		long start = System.currentTimeMillis();
		workKeyinfo.setKeyType(PinpadKeytype.KEY_TYPE_PINK);
		workKeyinfo.setPrivacyKeyData(temp);
		int retVal = PinpadManager.loadWKey(workKeyinfo);
		Logger.debug("LogonTrans>>setKey>>PINK="+retVal);
		long end = System.currentTimeMillis();
		Logger.debug("LogonTrans>>setKey>>TIME="+(end - start));
		if (retVal != 0) {
			return retVal;
		}

		//Inject MAC key
		System.arraycopy(keyData, keyLen, temp, 0, keyLen);
		start = System.currentTimeMillis();
		workKeyinfo.setKeyType(PinpadKeytype.KEY_TYPE_MACK);
		if(cfg.getStandard() == 1){
			System.arraycopy(temp , 0 , temp , 8 , 8 );
		}
		workKeyinfo.setPrivacyKeyData(temp);
		retVal = PinpadManager.loadWKey(workKeyinfo);
		Logger.debug("LogonTrans>>setKey>>MACK="+retVal);
		end = System.currentTimeMillis();
		Logger.debug("LogonTrans>>setKey>>TIME="+(end - start));
		if (retVal != 0) {
			return retVal;
		}

		//inject EAC Key
		if(cfg.isTrackEncrypt()){
			System.arraycopy(keyData, keyLen*2, temp, 0, keyLen);
			start = System.currentTimeMillis();
			workKeyinfo.setKeyType(PinpadKeytype.KEY_TYPE_EAK);
			workKeyinfo.setPrivacyKeyData(temp);
			retVal = PinpadManager.loadWKey(workKeyinfo);
			Logger.debug("LogonTrans>>setKey>>EACK="+retVal);
			end = System.currentTimeMillis();
			Logger.debug("LogonTrans>>setKey>>TIME="+(end - start));
			if (retVal != 0) {
				return retVal;
			}
		}

		return 0;
	}
}
