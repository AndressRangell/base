package com.newpos.libpay.device.scanner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.newpos.pay.R;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.trans.Tcode;
import com.pos.device.scanner.OnScanListener;
import com.pos.device.scanner.Scanner;

/**
 * InnerScanner
 * @author
 */

public class InnerScanner {

    private InnerScannerListener mOnScannedListener ;
    private boolean isContinueScan = false;
    private boolean isBackCamera = true;
    private boolean isBeep = true;
    private boolean isTorchOn = false;
    private RelativeLayout previewLayout;
    private RelativeLayout back_cameraLayout;
    private RelativeLayout preview_cameraLayout;
    private ImageView mask;
    private LinearLayout leftArrowLayout;
    private LinearLayout rightArrowLayout;
    private View cameraPreviewScanS = null;
    private LinearLayout scannerLayout = null;
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams params = null;
    private int timeOut = 60;
    private TranslateAnimation translateAnimation;

    private static final int CALLBACK_SCANS = 4;
    private static final int INIT_SCANS = 5;
    private static final int START_SCANS = 6;
    private static final int STOP_SCANS = 7;
    private static final int STOP_SCANS_TIMEOUT = 8;
    private static final int STOP_SCANS_OTHER = 9;
    private static final int CHANGE_CAMEMA = 10;

    private FrameLayout mContentRl;

    private long lastClickTime = 0;

    /**
     * detener busqueda
     */
    private boolean isStop = false;

    /**
     * escáner de inicio
     */
    private boolean isInit = false;

    private boolean isAddWindow = false;

    private Activity MActivity ;

    public InnerScanner(Activity activity){
        this.MActivity = activity ;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CALLBACK_SCANS:
                    try {
                        byte[] decodeByte = (byte[]) msg.obj;
                        if (mOnScannedListener != null) {
                            mOnScannedListener.onScanResult(ScannerManager.SUCCESS, decodeByte);
                            if(!isContinueScan) {
                                mOnScannedListener = null;
                            }
                        }
                        if (!isContinueScan) {
                            stopScanS();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case INIT_SCANS:
                    try {
                        isBackCamera = TMConfig.getInstance().isScanBack();
                        isContinueScan = false ;
                        isBeep = TMConfig.getInstance().isScanBeeper();
                        isTorchOn = TMConfig.getInstance().isScanTorchOn();
                        //Init scan UI
                        initLayoutAndWindow(isBackCamera);

                        Bundle realBundle = new Bundle();
                        realBundle.putBoolean(Scanner.SCANNER_CONTINUE_SCAN, isContinueScan);
                        realBundle.putBoolean(Scanner.SCANNER_IS_BACK_CAMERA, isBackCamera);
                        realBundle.putBoolean(Scanner.SCANNER_PLAY_BEEP,isBeep);
                        realBundle.putBoolean(Scanner.SCANNER_IS_TORCH_ON,isTorchOn);
                        cameraPreviewScanS = Scanner.getInstance().initScanner(MActivity, realBundle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case START_SCANS:
                    int timeoutScanS = (Integer) msg.obj;
                    startScanS(timeoutScanS,false);
                    break;
                case STOP_SCANS:
                    if (mOnScannedListener != null) {
                        mOnScannedListener.onScanResult(Tcode.SCAN_CODE_FAIL, "".getBytes());
                        mOnScannedListener = null;
                    }
                    stopScanS();
                    break;
                case STOP_SCANS_TIMEOUT:
                    if (mOnScannedListener != null) {
                        mOnScannedListener.onScanResult(Tcode.SCAN_CODE_FAIL, "".getBytes());
                        mOnScannedListener = null;
                    }
                    stopScanS();
                    break;
                case STOP_SCANS_OTHER:
                    if (mOnScannedListener != null) {
                        mOnScannedListener.onScanResult(Tcode.UNKNOWN_ERROR, "".getBytes());
                        mOnScannedListener = null;
                    }
                    stopScanS();
                    break;
                case CHANGE_CAMEMA:
                    //reset scanner parameter
                    Bundle b = new Bundle();
                    b.putBoolean(Scanner.SCANNER_CONTINUE_SCAN, isContinueScan);
                    b.putBoolean(Scanner.SCANNER_PLAY_BEEP,isBeep);
                    b.putBoolean(Scanner.SCANNER_IS_TORCH_ON,isTorchOn);
                    if(isBackCamera){
                        b.putBoolean(Scanner.SCANNER_IS_BACK_CAMERA, false);
                        isBackCamera = false;
                        //zq modify
//                        setPreview();
                    }else{
                        b.putBoolean(Scanner.SCANNER_IS_BACK_CAMERA, true);
                        isBackCamera = true;
                        //zq modify
//                        setBackView();
                    }
                    TMConfig.getInstance().setScanBack(isBackCamera).save();
                    try {
                        cameraPreviewScanS = Scanner.getInstance().initScanner(MActivity, b);
                        if(mOnScannedListener != null){
                            startScanS(timeOut,true);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * escaner de inicio
     */
    public void initScanner() {
        try {
            if(isStop){
                return;
            }

            isInit = true;
            resetCameraParam();
            handler.sendEmptyMessage(INIT_SCANS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * iniciar busqueda
     * @param timeout  se acabó el tiempo
     * @param listener InnerScannerListener
     */
    public void startScan(int timeout, InnerScannerListener listener) {
        if(listener==null){
            throw new IllegalArgumentException("Argument is null");
        }
        if(!isInit){
            listener.onScanResult(Tcode.UNKNOWN_ERROR , "".getBytes());
            return;
        }
        if(isStop){
            listener.onScanResult(Tcode.UNKNOWN_ERROR , "".getBytes());
            return;
        }
        isStop = true;
        if (timeout <= 0) {
            timeout = 600;
        }
        this.timeOut = timeout;
        mOnScannedListener = listener;
        handler.sendMessage(handler.obtainMessage(START_SCANS, timeout));
    }

    /**
     * detener busqueda
     */
    public void stopScan() {
        handler.sendMessage(handler.obtainMessage(STOP_SCANS));
        Scanner.getInstance().stopScan();
    }

    private void initLayoutAndWindow(boolean backCamera) {
        if (scannerLayout == null) {
            scannerLayout = (LinearLayout) LayoutInflater.from(MActivity).inflate(R.layout.saomasubmit, null);
            back_cameraLayout = (RelativeLayout)scannerLayout.findViewById(R.id.back_layout);
            preview_cameraLayout = (RelativeLayout)scannerLayout.findViewById(R.id.preview_layout);
            mask = (ImageView) scannerLayout.findViewById(R.id.capture_scan_line_real);
            mContentRl = (FrameLayout)scannerLayout.findViewById(R.id.frameLayout_view);
        }
        if (previewLayout == null) {
            previewLayout = (RelativeLayout) scannerLayout.findViewById(R.id.capture_preview);
            //zq modify
//            if(backCamera) {
//                setBackView();
//            }else{
//                setPreview();
//            }
            back_cameraLayout.setVisibility(View.VISIBLE);
            preview_cameraLayout.setVisibility(View.GONE);
            startAnim();
        }
        if (leftArrowLayout == null) {
            leftArrowLayout = (LinearLayout) scannerLayout.findViewById(R.id.top_left_layout);
            leftArrowLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFastClick()) {
                        //Prevent program crashes from continuous fast clicks
                    }else {
                        handler.sendMessage(handler.obtainMessage(STOP_SCANS));
                        Scanner.getInstance().stopScan();
                    }
                }
            });
        }
        if (rightArrowLayout == null) {
            rightArrowLayout = (LinearLayout) scannerLayout.findViewById(R.id.top_right_img);
            rightArrowLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isFastClick()) {
                        //Prevent program crashes from continuous fast clicks
                    }else {
                        handler.sendMessage(handler.obtainMessage(CHANGE_CAMEMA));
                    }
                }
            });
        }

        if (windowManager == null) {
            windowManager = (WindowManager) MActivity.getSystemService(Context.WINDOW_SERVICE);
        }
        if (params == null) {
            params = new WindowManager.LayoutParams();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_SECURE
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN;
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            params.gravity = Gravity.TOP | Gravity.CENTER;
        }
        scannerLayout.setFocusableInTouchMode(true);
        scannerLayout.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    handler.sendMessage(handler.obtainMessage(STOP_SCANS));
                    Scanner.getInstance().stopScan();
                    return true;
                }
                return false;
            }
        });
    }

    public synchronized boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 1000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    //zq add
    private void startAnim(){
        translateAnimation = new TranslateAnimation(0, 0, -600, 200);
        translateAnimation.setDuration(3000);
        translateAnimation.setInterpolator(MActivity, android.R.anim.linear_interpolator);
        translateAnimation.setRepeatMode(TranslateAnimation.RESTART);
        translateAnimation.setRepeatCount(TranslateAnimation.INFINITE);
        translateAnimation.setFillAfter(false);//Keep the current position after the end of the animation
        mask.startAnimation(translateAnimation);
    }

    private void setBackView(){
        if(back_cameraLayout != null && preview_cameraLayout != null && mask != null) {
            back_cameraLayout.setVisibility(View.VISIBLE);
            preview_cameraLayout.setVisibility(View.GONE);
            //animacion
            translateAnimation = new TranslateAnimation(0, 0, -600, 0);
            translateAnimation.setDuration(3000);
            translateAnimation.setInterpolator(MActivity, android.R.anim.linear_interpolator);
            translateAnimation.setRepeatMode(TranslateAnimation.RESTART);
            translateAnimation.setRepeatCount(TranslateAnimation.INFINITE);
            translateAnimation.setFillAfter(false);//Keep the current position after the end of the animation
            mask.startAnimation(translateAnimation);
        }
    }

    private void setPreview(){
        if(back_cameraLayout != null && preview_cameraLayout != null && mask != null) {
            preview_cameraLayout.setVisibility(View.VISIBLE);
            back_cameraLayout.setVisibility(View.GONE);
            try{
                if (mask.getAnimation() != null && mask.getAnimation().isInitialized()) {
                    //cerrar animacion
                    mask.clearAnimation();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void startScanS(int timeout,boolean isChangeCamera) {
        try {
            if(previewLayout != null && cameraPreviewScanS != null) {
                previewLayout.removeAllViews();
                previewLayout.addView(cameraPreviewScanS);
            }
            if(!isChangeCamera) {
                if(scannerLayout != null && params != null && windowManager != null) {
                    if(!isAddWindow) {
                        windowManager.addView(scannerLayout, params);
                        isAddWindow = true;
                    }else{
                        System.out.println("isAddwindow="+isAddWindow);
                    }
                }
            }
            Scanner.getInstance().startScan(timeout, new OnScanListener() {
                @Override
                public void onScanResult(int result, byte[] data) {
                    try {
                        if (result == 0) {
                            System.out.println("scan succeed, data：" + new String(data));
                            handler.sendMessage(handler.obtainMessage(CALLBACK_SCANS, data));
                        } else if (result == -1) {
                            System.out.println("user stop scan");
                            handler.sendMessage(handler.obtainMessage(STOP_SCANS));
                        } else if (result == -3) {
                            System.out.println("scan timeout");
                            handler.sendMessage(handler.obtainMessage(STOP_SCANS_TIMEOUT));
                        } else {
                            System.out.println("others error");
                            handler.sendMessage(handler.obtainMessage(STOP_SCANS_OTHER));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopScanS() {
        try {
            isInit = false;
            if(previewLayout != null) {
                previewLayout.removeAllViews();
            }
            if (scannerLayout != null && windowManager != null && isAddWindow) {
                scannerLayout.removeAllViews();
                windowManager.removeViewImmediate(scannerLayout);
                isAddWindow = false;
            }else{
                System.err.println("scannerLayout is null");
            }
            isStop = false;
        }catch (Exception e){
            isAddWindow = false;
        }
    }

    public void resetCameraParam(){

        mOnScannedListener = null;
        isContinueScan = false;
        isBackCamera = true;
        isBeep = true;
        isTorchOn = false;
        previewLayout = null;
        back_cameraLayout = null;
        preview_cameraLayout = null;
        mask = null;
        leftArrowLayout = null;
        rightArrowLayout = null;
        cameraPreviewScanS = null;
        scannerLayout = null;
        windowManager = null;
        params = null;
        timeOut = 60;
        translateAnimation = null;
        mContentRl = null;
    }

//    @Override
//    public void reset() {
//        LoggerUtils.LOGD("InnerScanner reset");
//        //Agregue un juicio, si el código de escaneo normalmente no está apagado, luego apáguelo nuevamente cuando cierre la sesión
//        if(isStop) {
//            LoggerUtils.LOGD("El barrido del muelle no se ha cerrado normalmente al cerrar la sesión");
//            handler.sendMessage(handler.obtainMessage(STOP_SCANS));
//        }
//        isInit = false;
//    }
}
