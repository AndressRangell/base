package com.newpos.libpay.trans;

/**
 * Created by zhouqiang on 2017/12/11.
 * @author zhouqiang
 * tipo de transaccion
 */
public class Type {
    /**
     * INICIO DE SESIÓN (terminal de inicio de sesión)
     */
    public static final String LOGON = "LOGON" ;

    /**
     * CERRAR SESION (cierre de sesión de terminal)
     */
    public static final String LOGOUT = "LOGOUT" ;

    /**
     * DESCARGAR PARAMETROS (AIDs, CAPKs, BINs de tarjetas ...)
     */
    public static final String DOWNPARA = "DOWN PARA" ;

    /**
     * QUERY_EMV_CAPK, consulta CAPK desde el servidor
     */
    public static final String QUERY_EMV_CAPK = "QUERY_EMV_CAPK" ;

    /**
     * descargar CAPK del servidor
     */
    public static final String DOWNLOAD_EMV_CAPK = "DOWNLOAD_EMV_CAPK" ;

    /**
     * aviso de fin de descarga del servidor CAPK
     */
    public static final String DOWNLOAD_EMV_CAPK_END = "DOWNLOAD_EMV_CAPK_END" ;

    /**
     * consultar AID desde el servidor
     */
    public static final String QUERY_EMV_PARAM = "QUERY_EMV_PARAM" ;

    /**
     * descargar AID del servidor
     */
    public static final String DOWNLOAD_EMV_PARAM = "DOWNLOAD_EMV_PARAM" ;

    /**
     * aviso de fin de descarga del servidor AID
     */
    public static final String DOWNLOAD_EMV_PARAM_END = "DOWNLOAD_EMV_PARAM_END" ;

    /**
     * VENTA
     */
    public static final String SALE = "SALES" ;

    /**
     * saldo de consulta
     */
    public static final String ENQUIRY = "BALANCE" ;

    /**
     * venta nula
     */
    public static final String VOID = "VOID" ;

    /**
     * consultar saldo EC
     */
    public static final String EC_ENQUIRY = "EC BALANCE" ;

    /**
     * Venta de pase rápido
     */
    public static final String QUICKPASS = "QUICK PASS" ;

    /**
     * reembolso
     */
    public static final String REFUND = "REFUND" ;

    /**
     * pre-auth
     */
    public static final String PREAUTH = "AUTH" ;

    /**
     * void pre-auth
     */
    public static final String PREAUTHVOID = "AUTH VOID" ;

    /**
     * completo pre-auth
     */
    public static final String PREAUTHCOMPLETE = "AUTH COMPLETE" ;

    /**
     * void completo pre-auth
     */
    public static final String PREAUTHCOMPLETEVOID = "COMPLETE VOID" ;

    /**
     * Transferir cuentas
     */
    public static final String TRANSFER = "TRANSFER" ;

    public static final String CREFORLOAD = "CREFORLOAD" ;

    public static final String DEBFORLOAD = "DEBFORLOAD" ;

    /**
     * asentamiento
     */
    public static final String SETTLE = "SETTLE" ;

    /**
     * enviar todos los detalles de la transacción
     */
    public static final String UPSEND = "UPSEND" ;

    /**
     * INVERSIÓN
     */
    public static final String REVERSAL = "REVERSAL" ;

    /**
     * ENVIAR GUIÓN DEL EMISOR
     */
    public static final String SENDSCRIPT = "SENDSCRIPT" ;

    /**
     * escanear pagar
     */
    public static final String SCANSALE = "SCAN SALE" ;

    /**
     * pago de escaneo nulo
     */
    public static final String SCANVOID = "SCAN VOID" ;

    /**
     * pago de escaneo de reembolso
     */
    public static final String SCANREFUND = "SCAN REFUND" ;
}
