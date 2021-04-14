package com.newpos.libpay.trans.manager.logout;

import android.content.Context;

import com.newpos.libpay.Logger;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Type;
import com.newpos.libpay.trans.manager.ManageTrans;
import com.newpos.libpay.utils.ISOUtil;

/**
 * cerrar sesión en la terminal
 * @author zhouqiang
 */

@Deprecated
public class LogoutTrans extends ManageTrans implements TransPresenter{

	public LogoutTrans(Context ctx , String transEN , TransInterface tt) {
		super(ctx, transEN , tt);
		isTraceNoInc = false ;
	}

	@Override
	public ISO8583 getISO8583() {
		return iso8583;
	}

	@Override
	public void start() {
		transInterface.handling(Tcode.TERMINAL_LOGOUT);
		int retVal = Logout();
		if(retVal!=0){
			transInterface.showError(false , retVal);
			return;
		}
		transInterface.trannSuccess(Tcode.LOGOUT_SUCCESS);
		return;

	}

	/**
	 * desconectar
	 * @throws
	 **/
	public int Logout() {
		if(!cfg.isOnline()){
			return 0 ;
		}
		TransEName = Type.LOGOUT ;
		setFixedDatas();
		iso8583.clearData();
		iso8583.setField(0, MsgID);
		iso8583.setField(11, cfg.getTraceNo());
		iso8583.setField(41, cfg.getTermID());
		iso8583.setField(42, cfg.getMerchID());
		Logger.debug("Filed60 = "+Field60);
		iso8583.setField(60, Field60);
		iso8583.setField(63, ISOUtil.padleft(cfg.getOprNo()+"",2,'0') + " ");
		int retVal = OnLineTrans();
		Logger.debug("LogonTrans>>Logout>>OnLineTrans finish");
		if (retVal != 0) {
			return retVal ;
		}
		String rspCode = iso8583.getfield(39);
		netWork.close();
		if (rspCode != null && rspCode.equals("00")) {
			Logger.debug("LogoutTrans>>Logout>>sign out succeed");
			return 0 ;
		} else {
			if (rspCode == null) {
				return Tcode.RECEIVE_DATA_FAIL;
			} else {
				return formatRsp(rspCode);
			}
		}
	}
}
