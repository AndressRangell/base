package com.newpos.libpay.presenter;

import android.app.Activity;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputManager;
import com.android.newpos.pay.R;
import com.newpos.libpay.PaySdk;
import com.newpos.libpay.PaySdkException;
import com.newpos.libpay.device.card.CardInfo;
import com.newpos.libpay.device.card.CardListener;
import com.newpos.libpay.device.card.CardManager;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.device.pinpad.PinResult;
import com.newpos.libpay.device.pinpad.PinType;
import com.newpos.libpay.device.pinpad.PinpadListener;
import com.newpos.libpay.device.pinpad.PinpadManager;
import com.newpos.libpay.device.scanner.QRCInfo;
import com.newpos.libpay.device.scanner.QRCListener;
import com.newpos.libpay.device.scanner.ScannerManager;
import com.newpos.libpay.device.user.OnUserResultListener;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.global.TMConstants;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.finace.ServiceEntryMode;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.beeper.Beeper;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.SlotType;

import java.util.Locale;

/**
 * Created by zhouqiang on 2017/4/25.
 * @author zhouqiang
 * MVP --> P
 * Recolección y distribución de datos, procesamiento lógico.
 */

public class TransImplement implements TransInterface {

    /**
     * app UI(MVP --> v)
     */
    private TransView transView = null ;

    /**
     * Activity
     */
    private Activity mActivity = null ;

    /**
     * confirmar o cancelar oyente,
     * 0------confirmar
     * 1------cancelar
     */
    private int mRet = 0 ;

    /**
     * transaction type(Bank card, QR code(Alipay/Wechat pay))
     */
    private InputManager.Style payStyle ;

    /**
     * archivo de configuración de terminal
     */
    private TMConfig config ;

    /**
     *
     */
    private int timeout ;

    /**
     * @param activity Activity
     * @param tv app UI(MVP --> v)
     */
    public TransImplement(Activity activity , TransView tv){
        this.transView = tv ;
        this.mActivity = activity ;
        this.config = TMConfig.getInstance();
        this.timeout = config.getTimeout();
    }

    /**
     * bloqueo de objeto
     */
    private Object o = new byte[0] ;

    /**
     * notificar
     */
    private void listenNotify(){
        synchronized (o){
            o.notify();
        }
    }

    /**
     * bloquear
     */
    private void funWait(){
        synchronized (o){
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * el usuario confirma o cancela el oyente
     */
    final OnUserResultListener listener = new OnUserResultListener() {
        @Override
        public void confirm(InputManager.Style style) {
            mRet = 0 ;
            payStyle = style ;
            listenNotify();
        }

        @Override
        public void cancel() {
            mRet = 1 ;
            listenNotify();
        }
    };

    @Override
    public InputInfo getInput(InputManager.Mode type) {
        transView.showInputView(timeout , type , listener);
        funWait();
        InputInfo info = new InputInfo();
        if(mRet == 1){
            info.setResultFlag(false);
            info.setErrno(Tcode.USER_CANCEL);
        }else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(type));
            info.setNextStyle(payStyle);
        }
        return info;
    }

    @Override
    public CardInfo getCard(int mode) {
        transView.showCardView(timeout , mode);
        if(config.isVocie()) {
            PAYUtils.playVoice(mActivity, R.raw.swipecard);
        }
        final CardInfo cInfo = new CardInfo() ;
        CardManager cardManager = CardManager.getInstance(mode);
        cardManager.getCard(timeout, new CardListener() {
            @Override
            public void callback(CardInfo cardInfo) {
                cInfo.setResultFalg(cardInfo.isResultFalg());
                cInfo.setNfcType(cardInfo.getNfcType());
                cInfo.setCardType(cardInfo.getCardType());
                cInfo.setTrackNo(cardInfo.getTrackNo());
                cInfo.setCardAtr(cardInfo.getCardAtr());
                cInfo.setErrno(cardInfo.getErrno());
                listenNotify();
            }
        });
        funWait();
        return cInfo;
    }

    @Override
    public PinInfo getPinpadPin(PinType type) {
        PinInfo pinInfo = transView.showEnterPinView(timeout , type , listener);
        if(config.isVocie()){
            PAYUtils.playVoice(mActivity , R.raw.enterpin);
        }
        final PinInfo result = new PinInfo() ;
        if(pinInfo.getResult() == PinResult.NO_OPERATION){
            PinpadManager.getInstance().getPin(timeout, type, new PinpadListener() {
                @Override
                public void callback(PinInfo info) {
                    result.setResult(info.getResult());
                    result.setErrno(info.getErrno());
                    result.setPinblock(info.getPinblock());
                    listenNotify();
                }
            });
        }else {
            result.setResult(pinInfo.getResult());
            result.setErrno(pinInfo.getErrno());
            result.setPinblock(pinInfo.getPinblock());
            listenNotify();
        }
        funWait();
        transView.showMsgInfo(timeout , getStatusInfo(String.valueOf(Tcode.PROCESSING)));
        return result;
    }

    @Override
    public int confirmCardNO(String cn) {
        transView.showCardNo(timeout, cn, listener);
        funWait();
        transView.showMsgInfo(timeout , getStatusInfo(String.valueOf(Tcode.PROCESSING)));
        return mRet;
    }

    @Override
    public int choseAppList(String[] list) {
        int ret = transView.showCardAppListView(timeout, list, listener);
        funWait();
        transView.showMsgInfo(timeout , getStatusInfo(String.valueOf(Tcode.PROCESSING)));
        return ret;
    }

    @Override
    public void beforeGPO() {
        transView.handleBeforceGPO(timeout);
    }

    @Override
    public int confirmTransInfo(TransLogData logData) {
        transView.showTransInfoView(timeout, logData, listener);
        funWait();
        transView.showMsgInfo(timeout , getStatusInfo(String.valueOf(Tcode.PROCESSING)));
        return mRet;
    }

    @Override
    public int confirmCardVerifyCert(String info) {
        transView.showCardVerifyCertView(timeout , info , listener);
        funWait();
        transView.showMsgInfo(timeout , getStatusInfo(String.valueOf(Tcode.PROCESSING)));
        return mRet;
    }

    @Override
    public QRCInfo getQRCInfo(final InputManager.Style mode) {
        transView.showQRCView(timeout , mode);
        final QRCInfo qinfo = new QRCInfo() ;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ScannerManager manager = ScannerManager.getInstance(mActivity , mode);
                manager.getQRCode(timeout, new QRCListener() {
                    @Override
                    public void callback(QRCInfo info) {
                        qinfo.setResultFalg(info.isResultFalg());
                        qinfo.setErrno(info.getErrno());
                        qinfo.setQrc(info.getQrc());
                        listenNotify();
                    }
                });
            }
        });
        funWait();
        return qinfo;
    }

    @Override
    public void handling(int status) {
        transView.showMsgInfo(timeout , getStatusInfo(String.valueOf(status)));
    }

    @Override
    public void trannSuccess(int code , String... args) {
        String info = getStatusInfo(String.valueOf(code)) ;
        if(args.length != 0){
            info += "\n"+args[0] ;
        }
        transView.showSuccess(timeout , info);
    }

    @Override
    public void showError(boolean isToast , int errcode) {
        transView.showError(timeout , isToast , getErrInfo(String.valueOf(errcode)));
    }

    @Override
    public int printerLackPaper() {
        transView.printerLackPaper(timeout , listener);
        funWait();
        transView.showMsgInfo(timeout , getStatusInfo(String.valueOf(Tcode.PROCESSING)));
        return mRet;
    }

    /** ===================================================================== */

    private String getStatusInfo(String status){
        try {
            String[] infos = Locale.getDefault().getLanguage().equals("zh")?
                    PAYUtils.getProps(PaySdk.getInstance().getContext(), TMConstants.CODE, status):
                    PAYUtils.getProps(PaySdk.getInstance().getContext(), TMConstants.CODE_EN, status);
            if(infos!=null){
                return infos[0];
            }
        }catch (PaySdkException pse){
            pse.printStackTrace();
        }
        if(Locale.getDefault().getLanguage().equals("zh")){
            return "Información desconocida" ;
        }else {
            return "Unknown error" ;
        }
    }

    private String getErrInfo(String status){
        try {
            String[] errs = Locale.getDefault().getLanguage().equals("zh")?
                    PAYUtils.getProps(PaySdk.getInstance().getContext(), TMConstants.CODE, status):
                    PAYUtils.getProps(PaySdk.getInstance().getContext(), TMConstants.CODE_EN, status);
            if(errs!=null){
                return errs[0];
            }
        }catch (PaySdkException pse){
            pse.printStackTrace();
        }
        if(Locale.getDefault().getLanguage().equals("zh")){
            return "未知错误" ;
        }else {
            return "Unknown error" ;
        }
    }
}
