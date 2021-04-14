package com.newpos.libpay.trans;

/**
 * Created by zhouqiang on 2017/3/26.
 * @author zhouqiang
 * Código de devolución de transacción
 */

public class Tcode {
    public static final int START = 300 ;

    /**
     * socket falló, verifique si la red está disponible
     */
    public static final int SOCKET_FAIL = START + 1;

    /**
     * envío de datos fallido, compruebe enviar datos o red
     */
    public static final int SEND_DATA_FAIL = START + 2;

    /**
     * la recepción de datos falló, verifique si la red está disponible
     */
    public static final int RECEIVE_DATA_FAIL = START + 3;

    /**
     * usuario cancelar
     */
    public static final int USER_CANCEL = START + 4;

    /**
     * la tarjeta de búsqueda falló
     */
    public static final int SEARCH_CARD_FAIL = START + 5;

    /**
     * escanear el código QR falló
     */
    public static final int SCAN_CODE_FAIL = START + 6;

    /**
     * impresión de recibo fallido
     */
    public static final int PRINT_FAIL = START + 7;

    /**
     * no puedo encontrar transacciones
     */
    public static final int CANNOT_FIND_TRANS = START + 8;

    /**
     * comprobar MAC falló
     */
    public static final int RECEIVE_MAC_ERROR = START + 9;

    /**
     * paquete ilegal, vuelva a intentarlo
     */
    public static final int ILLEGAL_PACKAGE = START + 10;

    /**
     * leer la cantidad de EC falló
     */
    public static final int READ_ECAMOUNT_FAIL = START + 11;

    /**
     * ninguna transacción necesita liquidación
     */
    public static final int BATCH_NO_TRANS = START + 12;

    /**
     * La tarjeta IC no tiene respaldo
     */
    public static final int IC_NOT_FALLBACK = START + 13;

    /**
     * Contraseña incorrecta para el supervisor
     */
    public static final int MASSTER_PASS_ERROR = START + 14;

    /**
     * Falta de liquidación
     */
    public static final int SETTLE_UPSEND_FAIL = START + 15;

    /**
     * la reversión falló
     */
    public static final int REVERSAL_FAIL = START + 16;

    /**
     * el monto del reembolso excede el límite
     */
    public static final int REFUND_AMOUNT_BEYOND = START + 17;

    /**
     * descargando CAPK
     */
    public static final int EMV_CAPK_DOWNLOAING = START + 18;

    /**
     * descargando AID
     */
    public static final int EMV_AID_DOWNLOADING = START + 19;

    /**
     * descargar exitosamente
     */
    public static final int EMV_DOWNLOADING_SUCC = START + 20;

    /**
     * Terminal está iniciando sesión
     */
    public static final int TERMINAL_LOGON = START + 21;

    /**
     * La terminal está desconectada
     */
    public static final int TERMINAL_LOGOUT = START + 22;

    /**
     * conectando
     */
    public static final int CONNECTING_CENTER = START + 23;

    /**
     * recibo de impresión
     */
    public static final int PRINTING_RECEPT = START + 24;

    /**
     * impresión de recibo de detalle de liquidación
     */
    public static final int PRINTING_DETAILS = START + 25;

    /**
     * enviando datos de reversión
     */
    public static final int SEND_REVERSAL = START + 26;

    /**
     * La impresora tiene poco papel, empaque el papel
     */
    public static final int PRINTER_LACK_PAPER = START + 27;

    /**
     * venta exitosa
     */
    public static final int SALE_SUCCESS = START + 28;

    /**
     * saldo de consulta exitoso
     */
    public static final int ENQUIRY_SUCCESS = START + 29;

    /**
     * vacío tener éxito
     */
    public static final int VOID_SUCCESS = START + 30;

    /**
     * consulta saldo EC exitoso
     */
    public static final int EC_ENQUIRY_SUCCESS = START + 31;

    /**
     * Pase rápido exitoso
     */
    public static final int QUICKPASS_SUCCESS = START + 32;

    /**
     * iniciar sesión correctamente
     */
    public static final int LOGON_SUCCESS = START + 33;

    /**
     * cerrar sesión correctamente
     */
    public static final int LOGOUT_SUCCESS = START + 34;

    /**
     * la transacción se está procesando
     */
    public static final int PROCESSING = START + 35;

    /**
     * enviar aviso de liquidación
     */
    public static final int SEND_SETTLE_NOTICE = START + 36;

    /**
     * enviar detalles de la transacción de liquidación
     */
    public static final int SEND_SETTLE_TRANS_DETAILS = START + 37;

    /**
     * enviar aviso de liquidación finalizada
     */
    public static final int SEND_SETTLE_FINISH_NOTICE = START + 38;

    /**
     * asentamiento exitoso
     */
    public static final int SETTLE_SUCCESS = START + 39;

    /**
     * enviando guión
     */
    public static final int SEND_SETTLE_SCRIPT = START + 40;

    /**
     * terminal iniciar sesión y descargar parámetros correctamente
     */
    public static final int LOGON_DOWN_SUCCESS = START + 41;

    /**
     * recibir datos del centro de la red
     */
    public static final int RECEIVE_CENtER_DATA = START + 42;

    /**
     * preautorización exitosa
     */
    public static final int PREAUTH_SUCCESS = START + 43;

    /**
     * completar la preautorización con éxito
     */
    public static final int PREAUTH_COMPLETE_SUCCESS = START + 44;

    /**
     * anular la autorización previa completa con éxito
     */
    public static final int COMPLETE_VOID_SUCCESS = START + 45;

    /**
     * void pre-auth exitosa
     */
    public static final int PREAUTH_VOID_SUCCESS = START + 46;

    /**
     * reembolso exitoso
     */
    public static final int REFUND_SUCCESS = START + 47;

    /**
     * escanear pagar con éxito
     */
    public static final int SCAN_PAY_SUCCESS = START + 48;

    /**
     * enviando datos al centro de la red
     */
    public static final int SEND_DATA_CENTER = START + 49;

    /**
     * nulo pago de escaneo exitoso
     */
    public static final int SCAN_VOID_SUCCESS = START + 50;

    /**
     * reembolso escaneo pagar exitosamente
     */
    public static final int SCAN_REFUND_SUCCESS = START + 51;

    /**
     * No requiere ninguna AID del servidor
     */
    public static final int NO_AID_NEED_DOWNLOAD = START + 52;

    /**
     * No requiere ningún CAPK del servidor
     */
    public static final int NO_CAPK_NEED_DOWNLOAD = START + 53;

    /**
     * sin ayuda y descarga capk
     */
    public static final int NO_AID_CAPK_DOWNLOAD = START + 54;

    /**
     * dispositivo de impresora ocupado
     */
    public static final int PRINTER_BUSY = START + 55;

    /**
     * impresora de alta temperatura
     */
    public static final int PRINTER_HIGH_TEMP = START + 56;

    /**
     * impresora sin batería
     */
    public static final int PRINTER_NO_BATTERY = START + 57;

    /**
     * impresión del estado de la impresora
     */
    public static final int PRINTER_STATUS_PRINT = START + 58;

    /**
     * la longitud del código de referencia debe ser 12
     */
    public static final int REFERENCE_LEN_12 = START + 59;

    /**
     * la longitud de la fecha debe ser 4
     */
    public static final int DATE_LEN_12 = START + 60;

    /**
     * transacción desconocida
     */
    public static final int UNKNOWN_TRANSACTION = 999 ;

    /**
     * error desconocido
     */
    public static final int UNKNOWN_ERROR = 999 ;
}
