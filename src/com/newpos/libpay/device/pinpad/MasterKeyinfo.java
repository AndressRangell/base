package com.newpos.libpay.device.pinpad;

/**
 * Created by zhouqiang on 2017/3/15.
 * @author zhouqiang
 * Información de la llave maestra
 */

public class MasterKeyinfo {

    private int masterIndex ;
    private int keyType ;
    private byte[] plainKeyData ;
    private int keySystem ;

    public int getMasterIndex() {
        return masterIndex;
    }

    public void setMasterIndex(int masterIndex) {
        this.masterIndex = masterIndex;
    }

    public int getKeyType() {
        return keyType;
    }

    public void setKeyType(int keyType) {
        this.keyType = keyType;
    }

    public byte[] getPlainKeyData() {
        return plainKeyData;
    }

    public void setPlainKeyData(byte[] plainKeyData) {
        this.plainKeyData = plainKeyData;
    }

    public int getKeySystem() {
        return keySystem;
    }

    public void setKeySystem(int keySystem) {
        this.keySystem = keySystem;
    }
}
