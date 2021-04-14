package com.newpos.libpay.trans.manager.script;

import android.content.Context;

import com.newpos.libpay.trans.manager.ManageTrans;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;

/**
 * Created by zhouqiang on 2017/12/11.
 * @author zhouqiang
 * transacción de secuencia de comandos del emisor
 */
public class ScriptTrans extends ManageTrans {

	public ScriptTrans(Context ctx, String transEname) {
		super(ctx, transEname , null);
		iso8583.setHasMac(true);
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
		if (TraceNo != null){
			iso8583.setField(11, TraceNo);
		}
		if (data.getEntryMode() != null) {
			iso8583.setField(22, data.getEntryMode());
		}
		if (data.getAcquirerID() != null) {
			iso8583.setField(32, data.getAcquirerID());
		}
		if (data.getRRN() != null){
			iso8583.setField(37, data.getRRN());
		}
		if (data.getAuthCode() != null) {
			iso8583.setField(38, data.getAuthCode());
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

		if (Field60 != null) {
			iso8583.setField(60, Field60);
		}

		Field61 = data.getTraceNo() + data.getBatchNo() + data.getLocalDate();
		iso8583.setField(61, Field61);
	}

	public int sendScriptResult(TransLogData data) {
		setFields(data);
		int ret = OnLineTrans();
		return ret;
	}
}
