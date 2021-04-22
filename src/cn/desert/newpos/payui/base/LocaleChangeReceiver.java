package cn.desert.newpos.payui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Creado por zhouqiang el 2018/2/1.
 * @author zhouqiang
 */

public class LocaleChangeReceiver extends BroadcastReceiver {

    /**
     * método que valida si recibimos un intent y su destino es el activity actual, de ser así cierra o finaliza el activity PayApplication
     * @param context contexto del activity
     * @param intent intent con la actividad de destino
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            PayApplication.getInstance().exit();
        }
    }

}
