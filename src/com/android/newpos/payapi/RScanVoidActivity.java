package com.android.newpos.payapi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.newpos.libpay.device.printer.PrintRes;

/**
 * Creado por zhouqiang el 7/5/2018.
 */

public class RScanVoidActivity extends ExternalBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        start(PrintRes.STANDRAD_TRANS_TYPE[18]);
    }
}
