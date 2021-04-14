package com.newpos.libpay.device.card;

/**
 * Creado por zhouqiang el 12/12/2017.
 * @author zhouqiang
 * tipo de tarjeta
 */
public enum CardType {
    /**
     * modo de banda magnética
     */
    INMODE_MAG(0x02),

    /**
     * insertar modo de tarjeta con chip
     */
    INMODE_IC(0x08),

    /**
     * modo de tarjeta sin contacto
     */
    INMODE_NFC(0x10);

    private int val ;

    public int getVal(){
        return val ;
    }

    private CardType(int val){
        this.val = val ;
    }
}
