package com.newpos.libpay.trans.finace.sale;

import android.content.Context;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputManager;
import com.android.newpos.dataapi.DataApiImpl;
import com.android.newpos.libemv.PBOCTag9c;
import com.android.newpos.libemv.PBOCTransFlow;
import com.android.newpos.libemv.PBOCTransProperty;
import com.android.newpos.libemv.PBOCUtil;
import com.android.newpos.libemv.PBOCode;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.card.CardInfo;
import com.newpos.libpay.device.card.CardType;
import com.newpos.libpay.device.printer.PrintManager;
import com.newpos.libpay.device.scanner.QRCInfo;
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

import cn.desert.newpos.payui.base.PayApplication;

/**
 * Created by zhouqiang on 2017/12/11.
 * @author zhouqiang
 * sale transaction
 */

public class SaleTrans extends FinanceTrans implements TransPresenter {

	public SaleTrans(Context ctx, String transEname , TransInterface tt) {
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
		InputInfo inputInfo = transInterface.getInput(InputManager.Mode.AMOUNT);
		if(!inputInfo.isResultFlag()){
			transInterface.showError(false , inputInfo.getErrno());
			return;
		}
		Amount = Long.parseLong(inputInfo.getResult());
		if(inputInfo.getNextStyle() == InputManager.Style.UNIONPAY){
			unionpay();
		}else {
			scanpay(inputInfo);
		}
		return;
	}

	/**
	 * Transacción con tarjeta bancaria
	 */
	private void unionpay(){
		CardInfo cardInfo = transInterface.getCard(
				CardType.INMODE_IC.getVal()|
				CardType.INMODE_MAG.getVal()|
				CardType.INMODE_NFC.getVal());
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
			property.setTag9c(PBOCTag9c.sale);
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
			Logger.debug("SaleTrans->unionpay->PBOCOde:"+code);
			handlePBOCode(code);
		}
	}

	/**
	 * manejar la transacción PBOC
	 * @param code
     */
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

	/*
	  codigo QR de la transaccion
	 */
	private void scanpay(InputInfo info){
		inputMode = ServiceEntryMode.QRC ;
		TransEName = Type.SCANSALE ;
		QRCInfo qrcInfo = transInterface.getQRCInfo(info.getNextStyle()); ;
		if(!qrcInfo.isResultFalg()){
			transInterface.showError(false , qrcInfo.getErrno());
			return;
		}
		String paycode = qrcInfo.getQrc() ;
		if(isSaveLog){
			TransLogData data = setScanData(paycode);
			transLog.saveLog(data);
		}
		TransLog.clearReveral();
		int retVal = 0 ;
		if(isNeedPrint){
			retVal = printTrans() ;
		}
		if(retVal!=0){
			transInterface.showError(false , retVal);
			return;
		}
		transInterface.trannSuccess(Tcode.SCAN_PAY_SUCCESS ,
				PAYUtils.getStrAmount(Amount));
	}
}
