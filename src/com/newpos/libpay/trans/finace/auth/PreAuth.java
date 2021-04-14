package com.newpos.libpay.trans.finace.auth;

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
 * pre-auth
 */
public class PreAuth extends FinanceTrans implements TransPresenter {

    public PreAuth(Context ctx , String transEn , TransInterface tt){
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
        InputInfo inputInfo = transInterface.getInput(InputManager.Mode.AMOUNT);
        if(!inputInfo.isResultFlag()){
            transInterface.showError(false , inputInfo.getErrno());
            return;
        }
        Amount = Long.parseLong(inputInfo.getResult());
        CardInfo cardInfo = transInterface.getCard(CardType.INMODE_IC.getVal()|
                CardType.INMODE_NFC.getVal()|CardType.INMODE_MAG.getVal());
        if(!cardInfo.isResultFalg()){
            transInterface.showError(false , cardInfo.getErrno());
            return ;
        }
        CardType type = cardInfo.getCardType() ;
        if(type == CardType.INMODE_MAG){
            inputMode = ServiceEntryMode.MAG ;
            int ret = handleMAGData(cardInfo.getTrackNo());
            if(ret!=0){
                transInterface.showError(false , ret);
                return;
            }
            ret = transInterface.confirmCardNO(Pan);
            if(ret!=0){
                transInterface.showError(false , Tcode.USER_CANCEL);
                return;
            }
            ret = handleMAGPin();
            if(ret!=0){
                transInterface.showError(false , ret);
                return;
            }
            prepareOnline();
        }else {
            PBOCTransProperty property = new PBOCTransProperty();
            property.setTag9c((byte)0x03);
            property.setTraceNO(Integer.parseInt(cfg.getTraceNo()));
            property.setFirstEC(false);
            property.setForceOnline(true);
            property.setAmounts(Amount);
            property.setOtherAmounts(0);
            if(type == CardType.INMODE_IC){
                inputMode = ServiceEntryMode.ICC ;
                property.setIcCard(true);
                property.setTransFlow(PBOCTransFlow.FULL);
                isNeedGAC2 = true ;
            }
            if(type == CardType.INMODE_NFC){
                inputMode = ServiceEntryMode.NFC ;
                property.setIcCard(false);
                if(cfg.isForcePboc()){
                    property.setTransFlow(PBOCTransFlow.FULL);
                }else {
                    property.setTransFlow(PBOCTransFlow.QPASS);
                }
            }
            int code = startPBOC(property);
            Logger.debug("PreAuth->PBOCOde:"+code);
            handlePBOCode(code);
        }
        return;
    }

    private void handlePBOCode(int code){
        if(code == PBOCode.PBOC_REQUEST_ONLINE && inputMode == ServiceEntryMode.NFC){
            int ret = handleNFCPin();
            if(ret!=0){
                transInterface.showError(false , ret);
                return;
            }
        }

        setICCData();
        prepareOnline();
    }
}
