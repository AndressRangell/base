package com.android.newpos.payapi;

import android.os.Bundle;

import cn.desert.newpos.payui.IItem;

public class RPrintDetailsActivity extends ExternalBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startQueryPrint(IItem.ALLMENUS[4][2]);
    }
}
