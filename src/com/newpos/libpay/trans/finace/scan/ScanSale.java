package com.newpos.libpay.trans.finace.scan;

import android.content.Context;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputManager;
import com.newpos.libpay.device.printer.PrintManager;
import com.newpos.libpay.device.scanner.QRCInfo;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.finace.FinanceTrans;
import com.newpos.libpay.trans.finace.ServiceEntryMode;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.printer.Printer;

/**
 * Created by zhouqiang on 2017/11/14.
 * @author zhouqiang
 * Venta de escaneo
 */
public class ScanSale extends FinanceTrans implements TransPresenter {

    public ScanSale(Context ctx, String transEname  , TransInterface tt) {
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
        InputInfo inputInfo = transInterface.getInput(InputManager.Mode.AMOUNT);
        if(!inputInfo.isResultFlag()){
            transInterface.showError(false , inputInfo.getErrno());
            return;
        }
        Amount = Long.parseLong(inputInfo.getResult());
        inputMode = ServiceEntryMode.QRC ;
        QRCInfo qrcInfo = transInterface.getQRCInfo(InputManager.Style.ALIPAY);
        if(!qrcInfo.isResultFalg()){
            transInterface.showError(false , qrcInfo.getErrno());
            return;
        }
        String paycode = qrcInfo.getQrc() ;
        if(isSaveLog){
            TransLogData data = setScanData(paycode);
            transLog.saveLog(data);
        }
        cfg.incTraceNo();
        int retVal = 0 ;
        if(isNeedPrint){
            retVal = printTrans() ;
        }
        if(retVal != 0){
            transInterface.showError(false , retVal);
            return;
        }
        transInterface.trannSuccess(Tcode.SCAN_PAY_SUCCESS ,
                PAYUtils.getStrAmount(Amount));
        return;
    }
}
