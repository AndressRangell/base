package com.newpos.libpay.device.scanner;

/**
 * Created by zhouqiang on 2017/7/7.
 * @author zhouqiang
 * informacion codigo QR
 */

public class QRCInfo {
    private boolean resultFalg ;

    /** codigo QR */
    private String qrc ;

    /** error no */
    private int errno ;

    public QRCInfo(){}

    public boolean isResultFalg() {
        return resultFalg;
    }

    public void setResultFalg(boolean resultFalg) {
        this.resultFalg = resultFalg;
    }

    public String getQrc() {
        return qrc;
    }

    public void setQrc(String qrc) {
        this.qrc = qrc;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }
}
