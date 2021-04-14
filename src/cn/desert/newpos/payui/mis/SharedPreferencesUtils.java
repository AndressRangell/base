package cn.desert.newpos.payui.mis;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    /**
     * Nombre de archivo guardado en el teléfono
     */
    private static final String FILE_NAME = "share_date";



    /**
     * Cómo guardar datos，Necesitamos obtener el tipo específico de datos guardados，Luego llame a diferentes métodos de ahorro según el tipo
     * @param context
     * @param key
     * @param object
     */
    public static void setParam(Context context , String key, String object){
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, object);
        editor.commit();
    }


    /**
     * Obtén el método para guardar los datos，Obtenemos el tipo específico de datos guardados de acuerdo con el valor predeterminado，
     * Luego llame al método relativo para obtener el valor
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static String getParam(Context context , String key, String defaultObject){
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(key,defaultObject);
    }

    public static boolean isMis(Context context){
        return "true".equals(SharedPreferencesUtils.getParam(context , "mis" , String.valueOf(false)));
    }

    public static void setMis(Context context , boolean isMis){
        SharedPreferencesUtils.setParam(context , "mis" , String.valueOf(isMis));
    }

    public static String misAmount(Context context){
        return SharedPreferencesUtils.getParam(context , "mis_amount" , "");
    }

    public static void setMisAmount(Context context , String amount){
        SharedPreferencesUtils.setParam(context , "mis_amount" , amount);
    }

    public static String misChannel(Context context){
        return SharedPreferencesUtils.getParam(context , "mis_channel" , "");
    }

    public static void setMisChannel(Context context , String channel){
        SharedPreferencesUtils.setParam(context , "mis_channel" , channel);
    }
}
