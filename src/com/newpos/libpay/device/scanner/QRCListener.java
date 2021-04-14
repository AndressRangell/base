package com.newpos.libpay.device.scanner;

/**
 * Created by zhouqiang on 2017/7/7.
 * @author zhouqiang
 * Devolución de llamada de la interfaz de código de escaneo
 */

public interface QRCListener {
    public void callback(QRCInfo info);
}
