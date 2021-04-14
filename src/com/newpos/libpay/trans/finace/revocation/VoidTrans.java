package com.newpos.libpay.trans.finace.revocation;

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
 * Created by zhouqiang on 2016/12/6.
 * @author zhouqiang
 * void trans
 */

public class VoidTrans extends FinanceTrans implements TransPresenter{

    public VoidTrans(Context ctx, String transEname , TransInterface tt) {
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
            return;
        }
        String master_pass = info.getResult();
        if(!master_pass.equals(cfg.getMasterPass())){
            transInterface.showError(false , Tcode.MASSTER_PASS_ERROR);
            return;
        }
        info = transInterface.getInput(InputManager.Mode.VOUCHER);
        if(!info.isResultFlag()){
            transInterface.showError(false , info.getErrno());
            return ;
        }
        Logger.debug("Input voucher is " + info.getResult());
        TransLog log = TransLog.getInstance() ;
        data = log.searchTransLogByTraceNo(info.getResult());
        if(data == null){
            Logger.debug("searchTransLogByTraceNo, can not find");
        }else{
            Logger.debug("data.getIsVoided() = " + data.getIsVoided());
            Logger.debug("data.getEName() = " + data.getEName());
        }
        //modify by Andy Yuan:!data.getIsVoided() -> data.getIsVoided()
        if(data==null || data.getIsVoided() || !data.getEName().equals(Type.SALE)){
            transInterface.showError(false , Tcode.CANNOT_FIND_TRANS);
            return;
        }

        int retVal = transInterface.confirmTransInfo(data);
        if(retVal!=0){
            transInterface.showError(false , Tcode.USER_CANCEL);
            return;
        }
        Amount = data.getAmount();
        RRN = data.getRRN();
        AuthCode = data.getAuthCode();
        Field61 = data.getBatchNo()+data.getTraceNo();
        Pan = data.getPan() ;
        //ExpDate = data.getExpDate();//modify by Andy Yuan
        //PanSeqNo = data.getPanSeqNo();//modify by Andy Yuan
        //ICCData = data.getICCData() ;//modify by Andy Yuan

        if(cfg.getRevocationCardSwitch()){
            CardInfo cardInfo = transInterface.getCard(
                    CardType.INMODE_IC.getVal()|
                    CardType.INMODE_NFC.getVal()|
                    CardType.INMODE_MAG.getVal());
            afterCard(cardInfo);
        }else {
            inputMode = ServiceEntryMode.HAND ;//add by Andy Yuan.
            prepareOnline();
        }
        return;
    }

    /**
     * procesamiento de la tarjeta de detección terminada
     * @param info
     */
    private void afterCard(CardInfo info){
        if(!info.isResultFalg()){
            transInterface.showError(false , info.getErrno());
            return ;
        }
        CardType type = info.getCardType() ;
        if(type == CardType.INMODE_MAG){
            inputMode = ServiceEntryMode.MAG ;
            int ret = handleMAGData(info.getTrackNo());
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
            Logger.debug("VoidTrans->PBOCOde:"+code);
            handlePBOCode(code);
        }
    }

    private void handlePBOCode(int code){
        if(code == PBOCode.PBOC_REQUEST_ONLINE && inputMode == ServiceEntryMode.NFC){
            if(cfg.getRevocationPassSwitch()){
                int ret = handleNFCPin();
                if(ret!=0){
                    transInterface.showError(false , ret);
                    return;
                }
            }
        }

        setICCData();
        prepareOnline();
    }
}
