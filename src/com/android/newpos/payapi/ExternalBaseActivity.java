package com.android.newpos.payapi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import cn.desert.newpos.payui.master.MasterControl;
import cn.desert.newpos.payui.transrecord.HistoryTrans;

public class ExternalBaseActivity extends Activity {
    protected static final String PKG = "com.android.newpos.pay" ;
    protected static final String MASTER_CONTROL = "cn.desert.newpos.payui.master.MasterControl" ;
    protected static final String PRINT_CONTROL = "cn.desert.newpos.payui.transrecord.HistoryTrans" ;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    protected void start(String transType){
        Bundle bundle = new Bundle();
        bundle.putString(MasterControl.TRANS_KEY, transType);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName(PKG, MASTER_CONTROL);
        intent.setComponent(cn);
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);
    }

    protected void startQueryPrint(String action){
        ComponentName cn = new ComponentName(PKG , PRINT_CONTROL);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(cn);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra(HistoryTrans.EVENTS , action);
        startActivityForResult(intent , 0);
    }
}
