package cn.desert.newpos.payui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Creado por zhouqiang el 2018/2/1.
 * @author zhouqiang
 */
public class LocaleChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            PayApplication.getInstance().exit();
        }
    }
}
