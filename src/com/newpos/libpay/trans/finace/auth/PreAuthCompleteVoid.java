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
import com.newpos.libpay.trans.Type;
import com.newpos.libpay.trans.finace.FinanceTrans;
import com.newpos.libpay.trans.finace.ServiceEntryMode;
import com.newpos.libpay.trans.translog.TransLog;

/**
 * Created by zhouqiang on 2017/4/25.
 * @'author zhouqiang
 * void complete pre-auth
 */

public class PreAuthCompleteVoid extends FinanceTrans implements TransPresenter {

    public PreAuthCompleteVoid(Context ctx , String transEn , TransInterface tt){
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
        info = transInterface.getInput(InputManager.Mode.VOUCHER );
        if(!info.isResultFlag()){
            transInterface.showError(false , info.getErrno());
            return ;
        }
        TransLog log = TransLog.getInstance() ;
        data = log.searchTransLogByTraceNo(info.getResult());
        if(data==null || !data.getEName().equals(Type.PREAUTHCOMPLETE) || data.getIsVoided()){
            transInterface.showError(false , Tcode.CANNOT_FIND_TRANS);
            return;
        }
        int retVal = transInterface.confirmTransInfo(data);
        if(0 != retVal){
            transInterface.showError(false , Tcode.USER_CANCEL);
            return;
        }
        Amount = data.getAmount();
        RRN = data.getRRN();
        AuthCode = data.getAuthCode();
        Field61 = data.getBatchNo()+data.getTraceNo();
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
            if(cfg.getRevocationPassSwitch()){
                ret = handleMAGPin();
                if(ret!=0){
                    transInterface.showError(false , ret);
                    return;
                }
            }
            prepareOnline();
        }else {
            PBOCTransProperty property = new PBOCTransProperty();
            property.setTag9c((byte) 0x03);
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
            Logger.debug("PreAuthVoid->PBOCOde:"+code);
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
