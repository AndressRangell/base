package com.newpos.libpay.trans.finace;

/**
 * Created by zhouqiang on 2017/12/11.
 * @author zhouqiang
 * servicio ingrese al modo del campo 22
 */
public enum ServiceEntryMode {
    /**
     * Introduzca manualmente el número de tarjeta
     */
    HAND(1),

    /**
     * modo de banda magnética
     */
    MAG(2),

    /**
     * insertar modo de tarjeta
     */
    ICC(5),

    /**
     * modo sin contacto
     */
    NFC(7),

    /**
     * escanear el modo de código QR
     */
    QRC(9);

    private int val ;

    public int getVal(){
        return this.val ;
    }

    private ServiceEntryMode(int val){
        this.val = val ;
    }
}
