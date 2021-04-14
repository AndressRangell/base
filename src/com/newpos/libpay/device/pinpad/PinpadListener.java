package com.newpos.libpay.device.pinpad;

/**
 * Created by zhouqiang on 2017/3/17.
 * @author zhouqiang
 * Monitorización de contraseña de entrada de teclado de contraseña
 */

public interface PinpadListener {
    void callback(PinInfo info);
}
