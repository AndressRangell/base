package com.newpos.libpay.trans.finace.query;

import android.content.Context;

import com.android.newpos.libemv.PBOCTag9c;
import com.android.newpos.libemv.PBOCTransFlow;
import com.android.newpos.libemv.PBOCTransProperty;
import com.android.newpos.libemv.PBOCUtil;
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
 * Created by zhouqiang on 2017/10/11.
 * @author zhouqiang
 * balance inquiry
 */
public class EnquiryTrans extends FinanceTrans implements TransPresenter {

	public EnquiryTrans(Context ctx, String transEname , TransInterface tt) {
		super(ctx, transEname , tt);
		isTraceNoInc = true;
		isSaveLog = false;
		isReversal = false;
		isProcPreTrans = true;
		isProcSuffix = true;
		isFallBack = cfg.isCheckICC();
		isDebit = true;
		isNeedPrint = false ;
	}

	@Override
	public ISO8583 getISO8583() {
		return iso8583;
	}

	@Override
	public void start() {
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
			ret = handleMAGPin();
			if(ret!=0){
				transInterface.showError(false , ret);
				return;
			}
			prepareOnline();
		}else {
			PBOCTransProperty property = new PBOCTransProperty();
			property.setTag9c(PBOCTag9c.enquiry);
			property.setTraceNO(Integer.parseInt(cfg.getTraceNo()));
			property.setFirstEC(false);
			property.setForceOnline(true);
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
			Logger.debug("EnquiryTrans->PBOCOde:"+code);
			handlePBOCode(code);
		}
		return;
	}

	private void handlePBOCode(int code){
		if(code != PBOCode.PBOC_REQUEST_ONLINE){
			transInterface.showError(false , code);
			return;
		}
		if(inputMode == ServiceEntryMode.NFC){
			int ret = transInterface.confirmCardNO(PBOCUtil.getPBOCCardInfo().getCardNO());
			if(ret!=0){
				transInterface.showError(false , Tcode.USER_CANCEL);
				return;
			}
			ret = handleNFCPin();
			if(ret!=0){
				transInterface.showError(false , ret);
				return;
			}
		}
		setICCData();
		prepareOnline();
	}
}
