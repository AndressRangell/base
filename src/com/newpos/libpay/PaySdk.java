package com.newpos.libpay;

import android.app.Activity;
import android.content.Context;

import com.android.newpos.libemv.PBOCManager;
import com.android.newpos.libemv.PBOCPin;
import com.newpos.libpay.device.card.CardManager;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.global.TMConstants;
import com.newpos.libpay.paras.EmvAidInfo;
import com.newpos.libpay.paras.EmvCapkInfo;
import com.newpos.libpay.presenter.TransImplement;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.presenter.TransView;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Type;
import com.newpos.libpay.trans.finace.auth.PreAuth;
import com.newpos.libpay.trans.finace.auth.PreAuthComplete;
import com.newpos.libpay.trans.finace.auth.PreAuthCompleteVoid;
import com.newpos.libpay.trans.finace.auth.PreAuthVoid;
import com.newpos.libpay.trans.finace.ecquery.ECEnquiryTrans;
import com.newpos.libpay.trans.finace.query.EnquiryTrans;
import com.newpos.libpay.trans.finace.quickpass.QuickPassTrans;
import com.newpos.libpay.trans.finace.refund.RefundTrans;
import com.newpos.libpay.trans.finace.revocation.VoidTrans;
import com.newpos.libpay.trans.finace.sale.SaleTrans;
import com.newpos.libpay.trans.finace.scan.ScanRefund;
import com.newpos.libpay.trans.finace.scan.ScanSale;
import com.newpos.libpay.trans.finace.scan.ScanVoid;
import com.newpos.libpay.trans.finace.transfer.TransferTrans;
import com.newpos.libpay.trans.manager.down.DparaTrans;
import com.newpos.libpay.trans.manager.logon.LogonTrans;
import com.newpos.libpay.trans.manager.logout.LogoutTrans;
import com.newpos.libpay.trans.manager.settle.SettleTrans;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.SDKManager;
import com.pos.device.SDKManagerCallback;

/**
 * Created by zhouqiang on 2017/4/25.
 * @author zhouqiang
 * pay SDK
 */
public class PaySdk {

    /**
     * Patrón de diseño singleton
     */
    private static PaySdk mInstance = null ;

    /**
     * contexto
     */
    private Context mContext = null ;

    private Activity mActivity = null ;

    /**
     * modo MVP
     */
    private TransPresenter presenter = null ;

    /**
     * Identificar si pago sdk inicializado
     */
    private static boolean isInit = false ;

    /**
     * la función de devolución de llamada de PaySdk
     */
    private PaySdkListener mListener = null ;

    /**
     * Archivo de recursos de PaySdk
     * @link @{@link String}
     */
    private String cacheFilePath = null ;

    /**
     * ruta del archivo de parámetros de terminal
     * @link @{@link String}
     */
    private String paraFilepath = null ;

    public Context getContext() throws PaySdkException {
        if(this.mContext == null){
            throw new PaySdkException(PaySdkException.PARA_NULL);
        }
        return mContext ;
    }

    public PaySdk setActivity(Activity activity){
        this.mActivity = activity ;
        return mInstance ;
    }

    public PaySdk setParaFilePath(String path){
        this.paraFilepath = path ;
        return mInstance ;
    }

    public String getParaFilepath(){
        return this.paraFilepath ;
    }

    public PaySdk setCacheFilePath(String path){
        this.cacheFilePath = path ;
        return mInstance ;
    }

    public String getCacheFilePath(){
        return this.cacheFilePath ;
    }

    private PaySdk(){}

    public PaySdk setListener(PaySdkListener listener){
        this.mListener = listener ;
        return mInstance ;
    }

    public static PaySdk getInstance(){
        if(mInstance == null){
            mInstance = new PaySdk();
        }
        return mInstance ;
    }

    public void init(Context context) throws PaySdkException {
        this.mContext = context ;
        this.init();
    }

    public void init(Context context , PaySdkListener listener) throws PaySdkException {
        this.mContext = context ;
        this.mListener = listener ;
        this.init();
    }

    public void init() throws PaySdkException {
        System.out.println("init->start.....");
        if(this.mContext == null){
            throw new PaySdkException(PaySdkException.PARA_NULL);
        }

        if(this.paraFilepath == null || !this.paraFilepath.endsWith("properties")){
            this.paraFilepath = TMConstants.DEFAULTCONFIG ;
        }

        if(this.cacheFilePath == null){
            this.cacheFilePath = mContext.getFilesDir() + "/" ;
        }else if(!this.cacheFilePath.endsWith("/")){
            this.cacheFilePath += "/" ;
        }

        TMConfig.setRootFilePath(this.cacheFilePath);
        System.out.println("init->paras files path:" + this.paraFilepath);
        System.out.println("init->cache files will be saved in:" + this.cacheFilePath);
        System.out.println("init->pay sdk will run based on:" + (TMConfig.getInstance().getBankid()==1?"UNIONPAY":"CITICPAY") );
        if(!TMConfig.getInstance().isOnline()){
            PAYUtils.copyAssetsToData(this.mContext , EmvAidInfo.FILENAME);
            PAYUtils.copyAssetsToData(this.mContext , EmvCapkInfo.FILENAME);
        }
        SDKManager.init(mContext, new SDKManagerCallback() {
            @Override
            public void onFinish() {
                isInit = true ;
                System.out.println("init->success");
                if(mListener!=null){
                    mListener.success();
                }
            }
        });
    }

    /**
     * liberar recursos del administrador de tarjetas
     */
    public void releaseCard(){
        if(isInit){
            CardManager.getInstance(0).releaseAll();
        }
    }

    /**
     * liberar recursos sdk
     */
    public void exit(){
        if(isInit){
            SDKManager.release();
            isInit = false ;
        }
    }

    /**
     * hacer coincidir el bloque con "-"
     * @param type
     * @return
     */
    private String matchBlock(String type){
        return type.replace(" " , "-") ;
    }

    public void startTrans(String transType , TransView tv) throws PaySdkException {
        if(this.mActivity == null){
            throw new PaySdkException(PaySdkException.PARA_NULL);
        }

        //Modificar por Andy Yuan
        TransInterface impl = new TransImplement(mActivity , tv);
        if(transType.equals(Type.SALE)){
            presenter = new SaleTrans(this.mContext , matchBlock(Type.SALE) , impl);
        }else if(transType.equals(Type.SCANSALE)){
            presenter = new ScanSale(this.mContext , matchBlock(Type.SCANSALE) , impl);
        }else if(transType.equals(Type.SCANVOID)){
            presenter = new ScanVoid(this.mContext , matchBlock(Type.SCANVOID) , impl);
        }else if(transType.equals(Type.SCANREFUND)){
            presenter = new ScanRefund(this.mContext , matchBlock(Type.SCANREFUND) , impl);
        }else if(transType.equals(Type.DOWNPARA)){
            presenter = new DparaTrans(this.mContext , matchBlock(Type.DOWNPARA) , impl);
        }else if(transType.equals(Type.LOGON)){
            presenter = new LogonTrans(this.mContext , matchBlock(Type.LOGON) , impl);
        }else if(transType.equals(Type.ENQUIRY)){
            presenter = new EnquiryTrans(this.mContext , matchBlock(Type.ENQUIRY) , impl);
        }else if(transType.equals(Type.VOID)){
            presenter = new VoidTrans(this.mContext , matchBlock(Type.VOID) , impl);
        }else if(transType.equals(Type.EC_ENQUIRY)){
            presenter = new ECEnquiryTrans(this.mContext , matchBlock(Type.EC_ENQUIRY) , impl);
        }else if(transType.equals(Type.QUICKPASS)){
            presenter = new QuickPassTrans(this.mContext , matchBlock(Type.QUICKPASS) , impl);
        }else if(transType.equals(Type.PREAUTH)){
            presenter = new PreAuth(this.mContext , matchBlock(Type.PREAUTH) , impl);
        }else if(transType.equals(Type.PREAUTHCOMPLETE)){
            presenter = new PreAuthComplete(this.mContext , matchBlock(Type.PREAUTHCOMPLETE) , impl);
        }else if(transType.equals(Type.PREAUTHCOMPLETEVOID)){
            presenter = new PreAuthCompleteVoid(this.mContext , matchBlock(Type.PREAUTHCOMPLETEVOID) , impl);
        }else if(transType.equals(Type.PREAUTHVOID)){
            presenter = new PreAuthVoid(this.mContext , matchBlock(Type.PREAUTHVOID) , impl);
        }else if(transType.equals(Type.SETTLE)){
            presenter = new SettleTrans(this.mContext , matchBlock(Type.SETTLE) , impl);
        }else if(transType.equals(Type.REFUND)){
            presenter = new RefundTrans(this.mContext , matchBlock(Type.REFUND) , impl);
        }else if(transType.equals(Type.TRANSFER)){
            presenter = new TransferTrans(this.mContext , matchBlock(Type.TRANSFER) , impl);
        }else if(transType.equals(Type.LOGOUT)){
            presenter = new LogoutTrans(this.mContext, matchBlock(Type.LOGOUT), impl);
        }else{
            impl.showError(false , Tcode.UNKNOWN_TRANSACTION);
            return ;
        }

        if(isInit){
            new Thread(){
                @Override
                public void run(){
                    presenter.start();
                }
            }.start();
        }else {
            throw new PaySdkException(PaySdkException.NOT_INIT);
        }
    }
}
