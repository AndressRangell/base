package com.newpos.libpay.trans.finace.forload;

import android.content.Context;

import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.finace.FinanceTrans;

/**
 * Created by zhouqiang on 2017/4/27.
 * @author zhouqiang
 * Credit For Load
 */
public class CreditForLoad extends FinanceTrans implements TransPresenter{

    public CreditForLoad(Context ctx, String transEname, TransInterface tt) {
        super(ctx, transEname, tt);
    }

    @Override
    public void start() {

    }

    @Override
    public ISO8583 getISO8583() {
        return null;
    }
}
