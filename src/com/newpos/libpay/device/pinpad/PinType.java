package com.newpos.libpay.device.pinpad;

import com.pos.device.ped.RsaPinKey;

/**
 * Created by zhouqiang on 2017/12/12.
 */

public class PinType {
    private boolean isOnline ;

    /**
     * número de tarjeta
     */
    private String cardNO ;

    /**
     * cantidad de transacción
     */
    private String amount ;


    /**
     * reingresar recuentos de PIN fuera de línea
     */
    private int counts ;

    /**
     * tipo de PIN fuera de línea
     * 0-----texto claro
     * 1-----texto cifrado
     */
    private int type ;

    /**
     * la clave del pin de cifrado fuera de línea
     */
    private RsaPinKey pinKey ;

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getCardNO() {
        return cardNO;
    }

    public void setCardNO(String cardNO) {
        this.cardNO = cardNO;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getCounts() {
        return counts;
    }

    public void setCounts(int counts) {
        this.counts = counts;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public RsaPinKey getPinKey() {
        return pinKey;
    }

    public void setPinKey(RsaPinKey pinKey) {
        this.pinKey = pinKey;
    }
}
