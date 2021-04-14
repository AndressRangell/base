package com.newpos.libpay.device.pinpad;

/**
 * Created by zhouqiang on 2017/12/12.
 * @author zhouqiang
 * ingrese el resultado del PIN
 */
public enum PinResult {
    /**
     * ingrese el PIN exitosamente
     */
    SUCCESS(0),

    /**
     * Error al ingresar el PIN, como tiempo de espera, cancelar u otras.
     */
    FAIL(1),

    /**
     * No hagas nada, date cuenta por Presentador
     */
    NO_OPERATION(2);

    private int val ;

    public int getVal(){
        return val ;
    }

    private PinResult(int val){
        this.val = val ;
    }
}
