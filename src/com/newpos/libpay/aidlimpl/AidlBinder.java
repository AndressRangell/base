package com.newpos.libpay.aidlimpl;

import android.os.RemoteException;

import com.newpos.libpay.AIDL.TransAIDL;

import static com.newpos.libpay.aidlimpl.AidlLogger.LOGD ;

/**
 * Creado por zhouqiang el 9/12/2017.
 * @author zhouqiang
 */
public class AidlBinder extends TransAIDL.Stub {

    public AidlBinder(){

    }

    @Override
    public String sale(String json) throws RemoteException {
        LOGD("==sale==json:"+json);
        return null;
    }

    @Override
    public String sign(String json) throws RemoteException {
        LOGD("==sign==json:"+json);
        return null;
    }

    @Override
    public String down(String json) throws RemoteException {
        LOGD("==down==json:"+json);
        return null;
    }
}
