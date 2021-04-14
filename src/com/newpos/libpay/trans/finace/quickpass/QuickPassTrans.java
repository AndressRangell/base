package com.newpos.libpay.trans.finace.quickpass;

import android.content.Context;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputManager;
import com.android.newpos.libemv.PBOCTag9c;
import com.android.newpos.libemv.PBOCTransFlow;
import com.android.newpos.libemv.PBOCTransProperty;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.card.CardInfo;
import com.newpos.libpay.device.card.CardType;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.finace.FinanceTrans;
import com.newpos.libpay.trans.finace.ServiceEntryMode;

/**
 * Created by zhouqiang on 2017/9/9.
 * @author zhouqiang
 * Quick Pass transaction
 */
public class QuickPassTrans extends FinanceTrans implements TransPresenter {

	public QuickPassTrans(Context ctx, String transEname , TransInterface tt) {
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
		InputInfo info = transInterface.getInput(InputManager.Mode.AMOUNT);
		if(!info.isResultFlag()){
			transInterface.showError(false , info.getErrno());
			return;
		}
		Amount = Long.parseLong(info.getResult()) ;
		CardInfo cardInfo = transInterface.getCard(
				CardType.INMODE_NFC.getVal()|
				CardType.INMODE_IC.getVal());
		if(!cardInfo.isResultFalg()){
			transInterface.showError(false , info.getErrno());
			return;
		}
		PBOCTransProperty property = new PBOCTransProperty();
		property.setTag9c(PBOCTag9c.sale);
		property.setTraceNO(Integer.parseInt(cfg.getTraceNo()));
		property.setFirstEC(true);
		property.setForceOnline(false);
		property.setAmounts(Amount);
		property.setOtherAmounts(0);
		if(cardInfo.getCardType() == CardType.INMODE_IC){
			inputMode = ServiceEntryMode.ICC ;
			property.setIcCard(true);
			property.setTransFlow(PBOCTransFlow.FULL);
			isNeedGAC2 = true ;
		}
		if(cardInfo.getCardType() == CardType.INMODE_NFC){
			inputMode = ServiceEntryMode.NFC ;
			property.setIcCard(false);
			if(cfg.isForcePboc()){
				property.setTransFlow(PBOCTransFlow.FULL);
			}else {
				property.setTransFlow(PBOCTransFlow.QPASS);
			}
		}
		int code = startPBOC(property);
		Logger.debug("QuickPassTrans->PBOCOde:"+code);
		handlePBOCode(code);
		return;
	}

	private void handlePBOCode(int code){
		//if EC balance larger than transaction amount, get approve offline
		//otherwise, online transaction.
	}
}
