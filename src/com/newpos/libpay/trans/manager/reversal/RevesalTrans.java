package com.newpos.libpay.trans.manager.reversal;

import android.content.Context;

import com.newpos.libpay.Logger;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.manager.ManageTrans;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;

/**
 * Created by zhouqiang on 2017/12/11.
 * @author zhouqiang
 * transacción de reversión
 */
public class RevesalTrans extends ManageTrans {

	public RevesalTrans(Context ctx, String transEname) {
		super(ctx, transEname , null);
		isUseOrg_603_601 = true;
		iso8583.setHasMac(true);
		isTraceNoInc = false;
	}

	protected void setFields(TransLogData data) {
		if (MsgID != null) {
			iso8583.setField(0, MsgID);
		}
		if (data.getPan() != null) {
			iso8583.setField(2, data.getPan());
		}
		if (data.getProcCode() != null) {
			iso8583.setField(3, data.getProcCode());
		}
		if (data.getAmount() >= 0) {
			String AmoutData = "";
			AmoutData = ISOUtil.padleft(data.getAmount() + "", 12, '0');
			iso8583.setField(4, AmoutData);
		}
		if (data.getTraceNo() != null) {
			iso8583.setField(11, data.getTraceNo());
		}
		if (data.getExpDate() != null){
			iso8583.setField(14, data.getExpDate());
		}
		if (data.getEntryMode() != null) {
			iso8583.setField(22, data.getEntryMode());
		}
		if (data.getPanSeqNo() != null) {
			iso8583.setField(23, data.getPanSeqNo());
		}
		if (data.getSvrCode() != null){
			iso8583.setField(25, data.getSvrCode());
		}
		if (data.getAuthCode() != null) {
			iso8583.setField(38, data.getAuthCode());
		}
		if (data.getRspCode() != null) {
			iso8583.setField(39, data.getRspCode());
		}
		if (TermID != null) {
			iso8583.setField(41, TermID);
		}
		if (MerchID != null) {
			iso8583.setField(42, MerchID);
		}
		if (data.getCurrencyCode() != null) {
			iso8583.setField(49, data.getCurrencyCode());
		}
		if (data.getICCData() != null) {
			iso8583.setField(55, ISOUtil.byte2hex(data.getICCData()));
		}
		if (data.getField60() != null) {
			iso8583.setField(60, data.getField60());
		}
	}

	public int sendRevesal() {
		TransLogData data = TransLog.getReversal();
		setFields(data);
		int retVal = OnLineTrans();
		Logger.debug("RevesalTrans->sendRevesal:" + retVal);
		if (retVal == 0) {
			RspCode = iso8583.getfield(39);
			if (RspCode.equals(RSP_00_SUCCESS) ||
					RspCode.equals("12") ||
					RspCode.equals("25")) {
				return retVal;
			} else {
				data.setRspCode("06");
				TransLog.saveReversal(data);
				retVal = Tcode.REVERSAL_FAIL ;
			}
		} else if (retVal == Tcode.RECEIVE_MAC_ERROR) {
			data.setRspCode("A0");
			TransLog.saveReversal(data);
		} else if(retVal == Tcode.RECEIVE_DATA_FAIL){
			data.setRspCode("08");
			TransLog.saveReversal(data);
		} else if(retVal == Tcode.ILLEGAL_PACKAGE){
			data.setRspCode("08");
			TransLog.saveReversal(data);
		}
		return retVal;
	}
}
