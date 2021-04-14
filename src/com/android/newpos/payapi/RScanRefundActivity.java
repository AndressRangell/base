package com.android.newpos.payapi;

import android.os.Bundle;

import com.newpos.libpay.device.printer.PrintRes;

public class RScanRefundActivity extends ExternalBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        start(PrintRes.STANDRAD_TRANS_TYPE[19]);
    }
}
