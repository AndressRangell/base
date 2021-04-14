package com.newpos.libpay.device.scanner;

import android.app.Activity;
import android.content.SyncAdapterType;

import com.android.desert.keyboard.InputManager;
import com.newpos.libpay.Logger;
import com.newpos.libpay.trans.Tcode;

/**
 * Created by zhouqiang on 2017/7/7.
 * @author zhouqiang
 * ScannerManager
 */

public class ScannerManager {

    private static ScannerManager instance ;
    private static InputManager.Style mode ;
    private static InnerScanner scanner ;

    public static final int SUCCESS = 0x102 ;

    private ScannerManager(){}

    public static ScannerManager getInstance(Activity activity , InputManager.Style m){
        mode = m ;
        scanner = new InnerScanner(activity);
        if(null == instance){
            instance = new ScannerManager();
        }
        return instance ;
    }

    private QRCListener listener ;

    public void getQRCode(int timeout , QRCListener l){
        Logger.debug("ScannerManager>>getQRCode>>timeout="+timeout);
        scanner.initScanner();
        final QRCInfo info = new QRCInfo();
        if(null == l){
            info.setResultFalg(false);
            info.setErrno(Tcode.SCAN_CODE_FAIL);
            listener.callback(info);
        }else {
            //TODO 20170707 ZQ
            this.listener = l ;
            scanner.startScan(timeout, new InnerScannerListener() {
                @Override
                public void onScanResult(int retCode, byte[] data) {
                    if(SUCCESS == retCode){
                        info.setResultFalg(true);
                        info.setQrc(new String(data));
                    }else {
                        info.setResultFalg(false);
                        info.setErrno(Tcode.SCAN_CODE_FAIL);
                    }
                    listener.callback(info);
                }
            });
//            scanner.startScan(timeout, new OnScanListener() {
//                @Override
//                public void onScanResult(int i, byte[] bytes) {
//                    if(0 == i){
//                        info.setResultFalg(true);
//                        info.setQrc(new String(bytes));
//                    }else {
//                        info.setResultFalg(false);
//                        info.setErrno(i);
//                    }
//                    listener.callback(info);
//                }
//            });
        }
    }
}
