package com.newpos.libpay.device.scanner;

/**
 * Created by zhouqiang on 2017/9/28.
 * @author
 * InnerScannerListener interface
 */

public interface InnerScannerListener {
    void onScanResult(int retCode, byte[] data);
}
