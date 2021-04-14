package com.newpos.libpay.device.printer;

import java.util.Locale;

/**
 * Created by zhouqiang on 2017/4/4.
 * Imprimir clase constante
 * @author zhouqiang
 */

public class PrintRes {

    static boolean zh = Locale.getDefault().getLanguage().equals("zh");

    /**
     * Cadena de recursos
     */
    public interface CH{
        public static final String WANNING = zh?"Advertencia: este firmware es una versión de prueba y no se puede utilizar con fines comerciales. Las transacciones en esta versión pueden poner en peligro la seguridad de la tarjeta del titular de la tarjeta." : "Warning:Debug firmware,use for commercial forbidden,it will be hurt the benefit of cardholder through this version.";
        public static final String MERCHANT_COPY = zh?"Talón de comerciante     MERCHANT COPY" :"MERCHANT COPY";
        public static final String CARDHOLDER_COPY = zh?"Talón del titular de la tarjeta     CARDHOLDER COPY" :"CARDHOLDER COPY";
        public static final String BANK_COPY = zh?"Talón de banco     BANK COPY" :"BANK COPY";
        public static final String MERCHANT_NAME = zh?"Nombre del Negocio(MERCHANT NAME):" :"MERCHANT NAME:";
        public static final String MERCHANT_ID = zh?"Identificación del comerciante(MERCHANT NO):" :"MERCHANT NO:";
        public static final String TERNIMAL_ID = zh?"Número de terminal(TERMINAL NO):" :"TERMINAL NO:";
        public static final String OPERATOR_NO = zh?"Número de operador(OPERATOR NO):" :"OPERATOR NO";
        public static final String CARD_NO = zh?"número de tarjeta(CARD NO):" :"CARD NO:";
        public static final String SCANCODE = zh?"Código de pago(PayCode):" :"PayCode:" ;
        public static final String ISSUER = zh?"Editor(ISSUER):  Banco CITIC" :"ISSUER : China Bank";
        public static final String ISSUER2 = zh?"Editor(ISSUER):" :"ISSUER:";
        public static final String ACQUIRER = zh?"Banco adquirente(ACQ):  UnionPay Business" :"ACQ : Unionpay";
        public static final String ACQUIRER2 = zh?"Banco adquirente(ACQ):" :"ACQ:";
        public static final String TRANS_AAC = zh?"Texto cifrado de la aplicación(AAC):" :"AAC:";
        public static final String TRANS_AAC_ARQC = zh?"Transacción en línea" :"ARQC";
        public static final String TRANS_AAC_TC = zh?"Transacción sin conexión" :"TC";
        public static final String TRANS_TYPE = zh?"tipo de transacción(TXN. TYPE):" :"TXN. TYPE :";
        public static final String CARD_EXPDATE = zh?"Periodo de validez de la tarjeta(EXP. DATE):" :"EXP. DATE:";
        public static final String BATCH_NO = zh?"número de lote(BATCH NO):" :"BATCH NO:";
        public static final String VOUCHER_NO = zh?"Número de Vales(VOUCHER NO):" :"VOUCHER NO:";
        public static final String AUTH_NO = zh?"Código de Autorización(AUTH NO):" :"AUTH NO:";
        public static final String DATE_TIME = zh?"Fecha y hora(DATE/TIME):" :"DATE/TIME:";
        public static final String REF_NO = zh?"Número de Referencia de la Transacción(REF. NO):" :"REF. NO:";
        public static final String AMOUNT = zh?"Monto(AMOUNT):" :"AMOUNT:";
        public static final String EC_AMOUNT = zh?"Saldo de caja electrónico(AMOUNT):" :"EC AMOUNT:";
        public static final String CARD_AMOUNT = zh?"Balance de tarjeta(AMOUNT):" :"AMOUNT:";
        public static final String RMB = zh?"RMB:" :"$:";
        public static final String REFERENCE = zh?"Observaciones/REFERENCE" :"REFERENCE";
        public static final String REPRINT = zh?"***** Reimprimir *****" :"***** REPRINT *****";
        public static final String CARDHOLDER_SIGN = zh?"Firma del titular" :"CardHolder Signature";
        public static final String AGREE_TRANS = zh?"Acepto la transacción anterior" :"I agree these transaction above";
        public static final String SETTLE_SUMMARY = zh?"Liquidación total" :"Settle Sum Receipt";
        public static final String SETTLE_LIST = zh?"Tipos de/TYPE      Numero de transacciones/SUM      Monto/AMOUNT" :"TYPE      SUM      AMOUNT";
        public static final String SETTLE_INNER_CARD = zh?"Tarjeta interna: nivel de conciliación" :"Inner card；Reconciliation";
        public static final String SETTLE_OUTER_CARD = zh?"Comodín: nivel de reconciliación" :"Outer card:Reconciliation";
        public static final String SETTLE_DETAILS = zh?"Declaración de establecimiento" :"Settle Detail Receipt";
        public static final String SETTLE_DETAILS_LIST = zh?"Número de vale, tipo, código de autorización, monto, número de tarjeta" :"VOUCHER     TYPE     AUTHNO     AMOUNT    CARDNO";
        public static final String DETAILS = zh?"Detalles de la transacción" :"Transaction Details";
    }


    /**
     * Cadena de tipo trans
     */
    public static final String[] TRANS = {
            zh? "Consulta de saldo": "BALANCE",
            zh? "Consumo": "SALES",
            zh? "Cancelación de consumo": "VOID",
            zh? "Consulta electrónica de saldo de caja": "EC BALANCE",
            zh? "Quick Spending": "QUICK PASS",
            zh? "Asentamiento": "SETTLE",
            zh? "Autorización previa": "AUTH",
            zh? "Autorización previa completa": "AUTH COMPLETE",
            zh? "Revocación completa de la autorización previa": "COMPLETE VOID",
            zh? "Revocación de autorización previa": "AUTH VOID",
            zh? "Volver": "REFUND",
            zh? "Transferir": "TRANSFER",
            zh? "Cargando": "CREFORLOAD",
            zh? "Mención circular": "DEBFORLOAD",
            zh? "Iniciar sesión": "LOGON",
            zh? "Cerrar sesión": "LOGOUT",
            zh? "Parameter public key download": "DOWN PARA",
            zh? "Consumo de código de escaneo": "SCAN SALE",
            zh? "Escanear código QR Deshacer": "SCAN VOID",
            zh? "Escanear código QR Devolver": "SCAN REFUND",
    };

    /**
     * tipo de transacción estándar
     */
    public static final String[] STANDRAD_TRANS_TYPE = {
            "BALANCE",
            "SALES",
            "VOID",
            "EC BALANCE",
            "QUICK PASS",
            "SETTLE",
            "AUTH",
            "AUTH COMPLETE",
            "COMPLETE VOID",
            "AUTH VOID",
            "REFUND",
            "TRANSFER",
            "CREFORLOAD",
            "DEBFORLOAD",
            "LOGON",
            "LOGOUT",
            "DOWN PARA",
            "SCAN SALE",
            "SCAN VOID",
            "SCAN REFUND",
    };
}
