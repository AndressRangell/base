package com.newpos.libpay.trans.manager;

import android.content.Context;

import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Trans;

/**
 * Created by zhouqiang on 2017/12/11.
 * @author zhouqiang
 * administrar la transacción
 */
public class ManageTrans extends Trans {
    /**
     * ManageTrans
     * @param ctx
     * @param transEname
     */
    public ManageTrans(Context ctx, String transEname , TransInterface tt) {
        super(ctx, transEname , tt);
        iso8583.setHasMac(true);
        setTraceNoInc(true);
    }

    /**
     * transacción en línea de administrar
     * @return @{@link Tcode}
     */
    protected int OnLineTrans() {
        if (connect() == -1) {
            return Tcode.SOCKET_FAIL;
        }
        if (send() == -1) {
            netWork.close();
            return Tcode.SEND_DATA_FAIL;
        }
        byte[] respData = receive();
        netWork.close();
        if (respData == null) {
            return Tcode.RECEIVE_DATA_FAIL;
        }

        int ret = iso8583.unPacketISO8583(respData);
        if (ret == 0 && isTraceNoInc) {
            cfg.incTraceNo() ;
        }
        return ret;
    }
}
