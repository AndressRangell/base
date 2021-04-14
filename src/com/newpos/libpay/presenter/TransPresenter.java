package com.newpos.libpay.presenter;

import com.newpos.libpay.helper.iso8583.ISO8583;

/**
 * Created by zhouqiang on 2017/3/15.
 * @author zhouqiang
 * Clase de interfaz de atributo de transacción
 */

public interface TransPresenter {
    /**
     * Iniciar el proceso de negociación de la interfaz MODELO
     * Los usuarios pueden iniciar un determinado proceso de transacción a través de esta interfaz
     */
    void start();

    /**
     * Obtenga objetos de mensaje empalmados durante la transacción
     * Los usuarios pueden utilizar este objeto para modificar y establecer dominios de transacciones relacionados.
     * {@link ISO8583}
     * @return
     */
    ISO8583 getISO8583();
}
