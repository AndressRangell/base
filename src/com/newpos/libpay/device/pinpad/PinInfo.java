package com.newpos.libpay.device.pinpad;

/**
 * Created by zhouqiang on 2017/3/17.
 * @author zhouqiang
 * ingrese la información del resultado del PIN
 */

public class PinInfo {
    /**
     * ingrese el resultado del PIN
     * @ {@link PinResult}
     */
    private PinResult result ;

    /**
     * código de error
     */
    private int errno ;

    /**
     * Bloqueo de PIN
     */
    private byte[] pinblock ;

    public PinResult getResult() {
        return result;
    }

    public void setResult(PinResult result) {
        this.result = result;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public byte[] getPinblock() {
        return pinblock;
    }

    public void setPinblock(byte[] pinblock) {
        this.pinblock = pinblock;
    }
}
