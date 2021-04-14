package com.newpos.libpay.trans.finace.transfer;

import android.content.Context;

import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.finace.FinanceTrans;

/**
 * Created by zhouqiang on 2017/4/25.
 * @author zhouqiang
 * Transferir cuentas
 */

@Deprecated
public class TransferTrans extends FinanceTrans implements TransPresenter {

    public TransferTrans(Context ctx, String transEname, TransInterface tt) {
        super(ctx, transEname, tt);
    }

    @Override
    public ISO8583 getISO8583() {
        return iso8583;
    }

    @Override
    public void start() {

    }
}
