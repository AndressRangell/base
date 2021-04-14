package cn.desert.newpos.payui;

import com.android.newpos.pay.R;

import java.util.Locale;

/**
 * @author zhouqiang
 * @email wy1376359644@163.com
 */

public interface IItem {

    boolean isZH = Locale.getDefault().getLanguage().equals("zh") ;

    /**
     * Icono de menú
     */
    public static final int[] HOMEIMSG = {
        //isZH?R.drawable.home2_cash:R.drawable.home2_cash_en,
        isZH?R.drawable.home3_scan:R.drawable.home3_scan,
        isZH?R.drawable.home3_auth:R.drawable.home3_auth,
        isZH?R.drawable.home3_void:R.drawable.home3_void,
        isZH?R.drawable.home3_print:R.drawable.home3_print,
        isZH?R.drawable.home3_manage:R.drawable.home3_manage,
        isZH?R.drawable.home3_others:R.drawable.home3_others,
    };

    /**
     * Nombre del menú de primer nivel
     */
    public static final String[] HOMEMENUS = {
        //isZH?"Recibir pago":"SALES",
        isZH?"Código de escaneo":"SCANS",
        isZH?"Transacción preautorizada":"AUTHS",
        isZH?"Cancelación de consumo":"VOID",
        isZH?"Impresión de consulta":"QUERY/PRINT",
        isZH?"Funcion de gerencia":"MANAGEMENTS",
        isZH?"otro":"OTHERS",
    };

    /**
     * Nombre del menú secundario
     */
    public static final String[][] ALLMENUS = {
            {
                isZH?"Recibir pago":"SALES"
            } ,
            {
                isZH?"Consumo de código de escaneo":"SCAN SALES" ,
                isZH?"Escanear código para cancelar":"SCAN VOID" ,
                isZH?"Escanear código para regresar":"SCAN REFUND"
            } ,
            {
                isZH?"Preautorización":"AUTH" ,
                isZH?"Revocación de preautorización":"AUTH VOID" ,
                isZH?"Autorización previa completada":"AUTH COMPLETE" ,
                isZH?"Revocación de preautorización completada":"COMPLETE VOID"
            } ,
            {
                isZH?"Cancelación de consumo":"VOID"
            } ,
            {
                isZH?"Consultar detalles de la transacción":"QUERY DETAILS" ,
                isZH?"Rehacer el último trazo":"REPRINT LAST" ,
                isZH?"Imprimir detalles de la transacción":"PRINT DETAILS"
            } ,
            {
                isZH?"Registrarse":"LOGON" ,
                isZH?"Descarga de clave pública de parámetros":"DOWN PARA" ,
                isZH?"Asentamiento":"SETTLE" ,
                isZH?"Configuración del programa":"SETTINGS"
            } ,
            {
                isZH?"Verificación de saldo":"BALANCE" ,
                isZH?"Consulta electrónica de saldo de caja":"EC BALANCE" ,
                isZH?"Regreso":"REFUND"
            } ,
    };

    /**
     * imágenes de consulta
     */
    public interface Home_Enquiry{
        public static final int[] imgs = {
                R.drawable.menu_query_ec,
                R.drawable.menu_query_cash} ;
    }

    /**
     * auth imágenes
     */
    public interface Home_Preauth{
        public static final int[] imgs = {
                R.drawable.menu_preauth,
                R.drawable.menu_preauth_void,
                R.drawable.menu_preauth_comp,
                R.drawable.menu_preauth_comp_void} ;
    }

    /**
     * administrar imágenes
     */
    public interface Home_Manage{
        public static final int[] imgs = {
                R.drawable.menu_manage_logon,
                R.drawable.menu_manage_logout,
                R.drawable.menu_manage_settle,
                R.drawable.menu_manage_download,
                R.drawable.menu_manage_history} ;
    }

    /**
     * all menu title
     */
//    public interface Menus{
//        public static final String VOID = "Cancelación de consumo" ;
//        public static final String ENQUIRY = "Consulta" ;
//        public static final String ENQUIRY_1 = "Consulta de saldo" ;
//        public static final String ENQUIRY_2 = "Consulta electrónica de saldo de caja" ;
//        public static final String PREAUTH = "Transacción preautorizada" ;
//        public static final String PREAUTH_1 = "Autorización previa" ;
//        public static final String PREAUTH_2 = "Preautorización completada" ;
//        public static final String PREAUTH_3 = "Revocación de autorización previa" ;
//        public static final String PREAUTH_4 = "Revocación completa de preautorización" ;
//        public static final String MANAGER = "Gestión" ;
//        public static final String MANAGER_1 = "Iniciar sesión" ;
//        public static final String MANAGER_2 = "Cerrar sesión" ;
//        public static final String MANAGER_3 = "Liquidación" ;
//        public static final String MANAGER_4 = "Descarga de clave pública del parámetro" ;
//        public static final String MANAGER_5 = "Consulta de transacción histórica" ;
//        public static final String APPSTORE = "Rong store" ;
//        public static final String PROSETTING = "Configuración del programa" ;
//        public static final String SYSSETTING = "Configuración del sistema" ;
//        public static final String QUERYTRANSDETAILS = "Consultar detalles de la transacción" ;
//        public static final String REPRINTLAST = "Reimprimir el último trazo" ;
//        public static final String REPRINTALL = "Imprimir detalles de la transacción" ;
//    }

    public static final String[] MenusCN = {
            "Cancelación de consumo",
            "Preguntar",
            "Verificación de saldo",
            "Consulta electrónica de saldo de caja",
            "Transacción preautorizada",
            "Autorización previa",
            "Autorización previa completada",
            "Revocación de autorización previa",
            "Revocación completa de la autorización previa",
            "administración",
            "Registrarse",
            "Desconectar",
            "Asentamiento",
            "Descarga de clave pública de parámetros",
            "Consulta de transacciones históricas",
            "Tienda de finanzas",
            "Configuración del programa",
            "Ajustes del sistema",
            "Consultar detalles de la transacción",
            "Reproducir el último trazo",
            "Imprimir detalles de la transacción",
            "Consumo del código de escaneo",
            "Escanear código para cancelar",
            "Escanear devolución de código QR",
            "Regreso",
            "consumo",//Add by Andy Yuan
    };

    public static final String[] MenusEN = {
        "Void",
        "Balances",
        "Balance",
        "EC_Balance",
        "Pre-auths",
        "Preauth",
        "Pre-Completed",
        "Pre-Void",
        "Pre-CompVoid",
        "Management",
        "SignIn",
        "SignOut",
        "Settle",
        "DownParasCapks",
        "HistoryTrans",
        "RongAppstore",
        "AppSettings",
        "SysSettings",
        "QueryTransDetails",
        "ReprintLastTrans",
        "PrintTransDetails",
        "Scan-Sale",
        "Scan-Void",
        "Scan-Refund",
        "Refund",//Add by Andy Yuan
        "Sale",//Add by Andy Yuan
    };

    /**
     * imágenes de configuración
     */
    public interface Settings{
        public static final int[] IMGS = {
                R.drawable.icon_setting_communication,
                R.drawable.icon_setting_transpara,
                R.drawable.icon_setting_keyspara,
                R.drawable.icon_setting_maintainpdw,
                R.drawable.icon_setting_errlogs,
                R.drawable.icon_setting_privacy,
                R.drawable.icon_setting_deviceinfo
        };

        public static final int[] IMGS2 = {
                R.drawable.home2_setting_commun,
                R.drawable.home2_setting_trans,
                R.drawable.home2_setting_keys,
                R.drawable.home2_setting_privacy
        } ;
    }

    /**
     * lista de bancos
     */
    public interface BankList{
        public static final int[] IMGS = {
                R.drawable.huazirong,
                R.drawable.zhongguoyinlian,
                R.drawable.zhongguogongshang,
                R.drawable.zhongguojianshe,
                R.drawable.zhongguoyinhang,
                R.drawable.zhongguonongye,
                R.drawable.zhongguominsheng,
                R.drawable.zhongguoguangda,
                R.drawable.zhongguoyouzheng,
                R.drawable.zhongxinyinhang,
                R.drawable.zhaoshangyinhang,
                R.drawable.xingyeyinhang,
                R.drawable.pufayinhang,
                R.drawable.pipinganyinhang,
                R.drawable.jiaotongyinhang,
                R.drawable.huaxiayinhang,
                R.drawable.beijinyinhang
        };
    }

    public interface InputTitle{
        public static final int[] TITLEs = {
                R.string.please_input_amount,
                R.string.please_input_master_pass,
                R.string.please_input_trace_no,
                R.string.please_input_auth_code,
                R.string.please_input_data_time,
                R.string.please_input_reference,
        };
    }
}
