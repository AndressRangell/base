package com.newpos.libpay.trans.finace.ecquery;

import android.content.Context;

import com.android.newpos.libemv.PBOCECBalance;
import com.android.newpos.libemv.PBOCException;
import com.newpos.libpay.device.card.CardInfo;
import com.newpos.libpay.device.card.CardType;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.finace.FinanceTrans;
import com.newpos.libpay.utils.PAYUtils;

/**
 * Created by zhouqiang on 2017/8/30.
 * @author zhouqiang
 * inquiry EC balance
 */
public class ECEnquiryTrans extends FinanceTrans implements TransPresenter {

	public ECEnquiryTrans(Context ctx, String transEname , TransInterface tt) {
		super(ctx, transEname , tt);
	}

	@Override
	public ISO8583 getISO8583() {
		return iso8583;
	}

	@Override
	public void start() {
		CardInfo cardInfo = transInterface.getCard(
				CardType.INMODE_IC.getVal()|
				CardType.INMODE_NFC.getVal());
		if(!cardInfo.isResultFalg()){
			transInterface.showError(false , cardInfo.getErrno());
			return;
		}
		transInterface.handling(Tcode.PROCESSING);
		boolean isIC = cardInfo.getCardType()== CardType.INMODE_IC;
		try {
			PBOCECBalance ecBalance = pbocManager.readECBlance(isIC);
			transInterface.trannSuccess(Tcode.EC_ENQUIRY_SUCCESS ,
					PAYUtils.getStrAmount(ecBalance.getFirstCurrencyBalance()));
		} catch (PBOCException e) {
			transInterface.showError(false , Tcode.READ_ECAMOUNT_FAIL);
		}
		return;
	}
}
