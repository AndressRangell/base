package com.newpos.libpay.aidlimpl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

/**
 * Creado por zhouqiang el 9/12/2017.
 */

public class AidlSevice extends Service {

    /**
     * Objeto AidlBinder
     */
    private AidlBinder aidlBinder = null ;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        aidlBinder = new AidlBinder() ;
        return aidlBinder ;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
