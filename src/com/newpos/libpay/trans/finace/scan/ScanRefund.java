package com.newpos.libpay.trans.finace.scan;

import android.content.Context;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputManager;
import com.newpos.libpay.device.printer.PrintManager;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Type;
import com.newpos.libpay.trans.finace.FinanceTrans;
import com.newpos.libpay.trans.finace.ServiceEntryMode;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.printer.Printer;

/**
 * Created by zhouqiang on 2017/11/14.
 * @author zhouqiang
 * reembolso de escaneo
 */
public class ScanRefund extends FinanceTrans implements TransPresenter {

    public ScanRefund(Context ctx, String transEname , TransInterface tt) {
        super(ctx, transEname , tt);
        isTraceNoInc = true;
        isSaveLog = true;
        isReversal = true;
        isProcPreTrans = true;
        isProcSuffix = true;
        isFallBack = cfg.isCheckICC();
        isDebit = true;
        isNeedPrint = true ;
    }

    @Override
    public ISO8583 getISO8583() {
        return iso8583 ;
    }

    @Override
    public void start() {
        InputInfo info = transInterface.getInput(InputManager.Mode.PASSWORD);
        if(!info.isResultFlag()){
            transInterface.showError(false , info.getErrno());
            return;
        }
        String master_pass = info.getResult();
        if(!master_pass.equals(cfg.getMasterPass())){
            transInterface.showError(false , Tcode.MASSTER_PASS_ERROR);
            return ;
        }
        info = transInterface.getInput(InputManager.Mode.VOUCHER);
        if(!info.isResultFlag()){
            transInterface.showError(false , info.getErrno());
            return;
        }
        String tn = info.getResult() ;
        info = transInterface.getInput(InputManager.Mode.REFERENCE);
        if(!info.isResultFlag()){
            transInterface.showError(false , info.getErrno());
            return ;
        }
        TransLog log = TransLog.getInstance() ;
        data = log.searchTransLogByTraceNo(tn);
        if(data==null || data.getIsVoided() || !data.getEName().equals(Type.SCANSALE) || !data.getRRN().equals(info.getResult())){
            transInterface.showError(false , Tcode.CANNOT_FIND_TRANS);
            return;
        }
        int retVal = transInterface.confirmTransInfo(data);
        if(0 != retVal){
            transInterface.showError(false , Tcode.USER_CANCEL);
            return;
        }
        Amount = data.getAmount();
        inputMode = ServiceEntryMode.QRC ;
        RRN = data.getRRN();
        AuthCode = data.getAuthCode();
        Field61 = data.getBatchNo()+data.getTraceNo();
        if(isSaveLog){
            TransLogData d = setScanData(data.getPan());
            transLog.saveLog(d);
        }
        cfg.incTraceNo();
        if(isNeedPrint){
            retVal = printTrans() ;
        }
        if(retVal!=0){
            transInterface.showError(false , retVal);
            return;
        }
        data.setVoided(true);
        TransLog.getInstance().updateTransLog(
                TransLog.getInstance().getCurrentIndex(data),data);
        transInterface.trannSuccess(Tcode.SCAN_REFUND_SUCCESS ,
                PAYUtils.getStrAmount(Amount));
        return;
    }
}
