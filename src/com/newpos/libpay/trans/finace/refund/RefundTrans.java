package com.newpos.libpay.trans.finace.refund;

import android.content.Context;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputManager;
import com.android.newpos.libemv.PBOCTransFlow;
import com.android.newpos.libemv.PBOCTransProperty;
import com.android.newpos.libemv.PBOCode;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.card.CardInfo;
import com.newpos.libpay.device.card.CardType;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.finace.FinanceTrans;
import com.newpos.libpay.trans.finace.ServiceEntryMode;

/**
 * Created by zhouqiang on 2017/4/25.
 * @author zhouqiang
 * refund transaction
 */
public class RefundTrans extends FinanceTrans implements TransPresenter {

    public RefundTrans(Context ctx , String transEn , TransInterface tt){
        super(ctx , transEn , tt);
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
        Logger.debug("Refund >>>>>>>>>>>>>>>>>>>>>>>>>>>");
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
        CardInfo cardInfo = transInterface.getCard(
                CardType.INMODE_IC.getVal()|
                CardType.INMODE_NFC.getVal()|
                CardType.INMODE_MAG.getVal());
        if(!cardInfo.isResultFalg()){
            transInterface.showError(false , cardInfo.getErrno());
            return;
        }
        CardType type = cardInfo.getCardType() ;
        if(type == CardType.INMODE_MAG){
            inputMode = ServiceEntryMode.MAG ;
            int ret = handleMAGData(cardInfo.getTrackNo());
            if(ret!=0){
                transInterface.showError(false , ret);
                return;
            }
            afterCard();
        }else {
            PBOCTransProperty property = new PBOCTransProperty();
            property.setTag9c((byte) 0x01);
            property.setTraceNO(Integer.parseInt(cfg.getTraceNo()));
            property.setFirstEC(false);
            property.setForceOnline(true);
            if(type == CardType.INMODE_IC){
                inputMode = ServiceEntryMode.ICC ;
                property.setIcCard(true);
                property.setTransFlow(PBOCTransFlow.SIMPLE);
            }
            if(type == CardType.INMODE_NFC){
                inputMode = ServiceEntryMode.NFC ;
                property.setIcCard(false);
                if(cfg.isForcePboc()){
                    property.setTransFlow(PBOCTransFlow.FULL);
                }else {
                    property.setTransFlow(PBOCTransFlow.SIMPLE);
                }
            }
            int code = startPBOC(property);
            Logger.debug("RefundTrans->PBOCOde:"+code);
            if(code != PBOCode.PBOC_TRANS_SUCCESS && code != PBOCode.PBOC_REQUEST_ONLINE){
                transInterface.showError(false , code);
                return;
            }
            afterCard();
        }
        return;
    }

    private void afterCard(){
        InputInfo info = transInterface.getInput(InputManager.Mode.REFERENCE);
        if(!info.isResultFlag()){
            transInterface.showError(false , info.getErrno());
            return;
        }
        String refer = info.getResult() ;
        info = transInterface.getInput(InputManager.Mode.DATETIME);
        if(!info.isResultFlag()){
            transInterface.showError(false , info.getErrno());
            return;
        }
        String date = info.getResult();
        info = transInterface.getInput(InputManager.Mode.AMOUNT);
        if(!info.isResultFlag()){
            transInterface.showError(false , info.getErrno());
            return;
        }
        String amount = info.getResult();
        //Add by Andy Yuan
        RRN = refer;
        Field61 = "000000"+"000000"+date.substring(4);
        Amount = Long.parseLong(amount);
        Field63 = "000" ;
        prepareOnline();
    }
}
