package cn.desert.newpos.payui.simple;

import com.android.newpos.pay.R;

import java.util.Locale;

/**
 * Creado por zhouqiang el 11/11/2017.
 */

/**
 * esta clase contiene todos los logos del menu dentro de arreglos de drawables
 */
public class SecondMenu {
    public static final String IVKEY = "IVKEY" ;
    public static final String TVKEY = "TVKEY" ;

    public static int[] SCAN_ = {
            R.drawable.home2_scan_sale,
            R.drawable.home2_scan_void,
            R.drawable.home2_scan_refund
    } ;

    public static int[] PREAUTH_ = {
            R.drawable.home2_preauth_pre,
            R.drawable.home2_preauth_prevoid,
            R.drawable.home2_preauth_precom,
            R.drawable.home2_preauth_precomvoid,
    };

    public static int[] PRINT_ = {
            R.drawable.home2_print_query,
            R.drawable.home2_print_last,
            R.drawable.home2_print_all,
    } ;

    public static int[] MANA_ = {
            R.drawable.home2_mana_sign ,
            R.drawable.home2_mana_down,
            R.drawable.menu_manage_settle,
            R.drawable.home2_setting
    } ;

    public static int[] OTHERS_ = {
            Locale.getDefault().getLanguage().equals("zh")?R.drawable.home2_query:R.drawable.home2_query_en,
            Locale.getDefault().getLanguage().equals("zh")?R.drawable.home2_query_ec:R.drawable.home2_query_ec_en,
            R.drawable.home2_refund
    } ;
}
