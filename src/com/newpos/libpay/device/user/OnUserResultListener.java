package com.newpos.libpay.device.user;

import com.android.desert.keyboard.InputManager;

/**
 * Created by zhouqiang on 2017/4/25.
 * Interfaz de devolución de llamada unificada para vistas definidas por el usuario
 * @author zhouqiang
 */

public interface OnUserResultListener {
    /**
     * El usuario confirma la acción del paso anterior
     * @param type Temporalmente solo para métodos de pago, por favor llame primero para otros -1
     * @link @{@link com.android.desert.keyboard.InputManager.Style}
     * Entrada unificada
     */
    public void confirm(InputManager.Style type);

    /**
     * El usuario canceló el paso anterior
     * Entrada unificada
     */
    public void cancel();
}
