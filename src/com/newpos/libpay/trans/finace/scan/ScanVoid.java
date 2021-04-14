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
 * venta de escaneo nulo
 */
public class ScanVoid extends FinanceTrans implements TransPresenter{

    public ScanVoid(Context ctx, String transEname  , TransInterface tt) {
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
        return iso8583;
    }

    @Override
    public void start() {
        InputInfo info = transInterface.getInput(InputManager.Mode.PASSWORD);
        if(!info.isResultFlag()){
            transInterface.showError(false , info.getErrno());
            return ;
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
        TransLog log = TransLog.getInstance() ;
        data = log.searchTransLogByTraceNo(info.getResult());
        if(data==null || data.getIsVoided() || !data.getEName().equals(Type.SCANSALE)){
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
        if(retVal != 0){
            transInterface.showError(false , retVal);
            return;
        }
        data.setVoided(true);
        transInterface.trannSuccess(Tcode.SCAN_VOID_SUCCESS ,
                PAYUtils.getStrAmount(Amount));
        return;
    }
}
