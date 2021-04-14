package cn.desert.newpos.payui.transrecord;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputManager;
import com.android.newpos.pay.R;
import com.newpos.libpay.device.card.CardInfo;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.device.pinpad.PinType;
import com.newpos.libpay.device.printer.PrintManager;
import com.newpos.libpay.device.scanner.QRCInfo;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.printer.PrintTask;
import com.pos.device.printer.Printer;

import cn.desert.newpos.payui.IItem;
import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.base.BaseActivity;
import cn.desert.newpos.payui.base.PayApplication;

/**
 * Creado por Andy Yuan el 18/1/2018.
 * Imprima la última transacción o imprima los detalles de la transacción
 */

public class PrintLastTrans extends BaseActivity implements TransInterface, View.OnClickListener{

    public static final String EVENTS = "EVENTS" ;
    public static final String LAST = "LAST" ;
    public static final String ALL = "ALL" ;
    public static final String ANY = "ANY" ;

    PrintManager manager = null;
    private static final int MSG_ERROR_MSG = 0x01;
    private static final int MSG_PRINT_OK = 0x02;
    private static final int MSG_USER_CANCEL = 0x03;

    private Button btnConfirm ;
    private Button btnCancel ;

    private int confirmRet ;
    private boolean isPrinted = true ;
    private boolean isPrinterBroken = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PayApplication.getInstance().addActivity(this);
        setReturnVisible(View.INVISIBLE);
        setSaveVisible(View.INVISIBLE);
        manager = PrintManager.getmInstance(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            String even = bundle.getString(PrintLastTrans.EVENTS);
            if(even.equals(LAST)){
                setNaviTitle(IItem.ALLMENUS[4][1]);
                TMConfig.setLockReturn(true);
                print(manager.buildTaskByTLD(TransLog.getInstance().getLastTransLog(),1 , true));
            }else if(even.equals(ALL)){
                setNaviTitle(IItem.ALLMENUS[4][2]);
                TMConfig.setLockReturn(true);
                print(manager.buildDetailsTask());
            }else if(even.equals(ANY)){
                setNaviTitle(getString(R.string.printing_receipt));
                TMConfig.setLockReturn(true);
                TransLogData data = TransLog.getInstance().searchTransLogByTraceNo(bundle.getString(HistoryDetail.TRACENO_KEY));
                if(data==null){
                    finish();
                    return;
                }
                print(manager.buildTaskByTLD(data ,1 , true));
            }else {
                showError(false , Tcode.UNKNOWN_TRANSACTION);
                finish();
            }
        }

    }

    @Override
    public void onClick(View view) {
        if(view.equals(btnCancel)){
            confirmRet = 1 ;
        }else {
            confirmRet = 0 ;
        }
        listenNotify();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TMConfig.setLockReturn(false);
            switch (msg.what) {
                case MSG_USER_CANCEL:
                case MSG_PRINT_OK:
                    PrintLastTrans.this.finish();
                    break;
                case MSG_ERROR_MSG:
                    showError(false , Tcode.PRINT_FAIL);
                    PrintLastTrans.this.finish();
                    break;
            }
        }
    };

    private void showHanding(String msg){
        TextView tv = (TextView) findViewById(R.id.handing_msginfo);
        tv.setText(msg);
        ImageView iv = (ImageView) findViewById(R.id.handing_img);
        iv.setImageBitmap(PAYUtils.getLogoByBankId(this , TMConfig.getInstance().getBankid()));
        WebView wv = (WebView) findViewById(R.id.handling_loading);
        wv.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
                "<img width=\"128\" height=\"128\" src='file:///android_asset/gif/loading.gif'/></div></body></html>", "text/html", "UTF-8",null);
    }

    /**
     * start printing task
     * @param task
     */
    private void print(final PrintTask task){
        new Thread(){
            @Override
            public void run() {
                int ret = 0 ;
                do {
                    handling(Tcode.PRINTING_RECEPT);
                    ret = manager.print(task);
                    if(Printer.PRINTER_STATUS_PAPER_LACK == ret){
                        int result = printerLackPaper();
                        if(1 == result){
                            isPrinted = false ;
                            break;
                        }
                    }else if(Printer.PRINTER_OK != ret){
                        isPrinterBroken = true ;
                        break;
                    }
                }while (ret == Printer.PRINTER_STATUS_PAPER_LACK) ;
                if(!isPrinted){
                    mHandler.sendEmptyMessage(MSG_USER_CANCEL);
                    return ;
                }
                if(isPrinterBroken){
                    mHandler.sendEmptyMessage(MSG_ERROR_MSG);
                    return;
                }
                mHandler.sendEmptyMessage(MSG_PRINT_OK);
            }
        }.start();
    }

    @Override
    public CardInfo getCard(int mode) {
        return null;
    }

    @Override
    public QRCInfo getQRCInfo(InputManager.Style mode) {
        return null;
    }

    @Override
    public PinInfo getPinpadPin(PinType type) {
        return null;
    }

    @Override
    public int confirmCardNO(String cn) {
        return 0;
    }

    @Override
    public int choseAppList(String[] list) {
        return 0;
    }

    @Override
    public void beforeGPO() {

    }

    @Override
    public void handling(int status) {
        final String info = PAYUtils.getStatusInfo(String.valueOf(status));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_handling);
                showHanding(info);
            }
        });
    }

    @Override
    public int confirmTransInfo(TransLogData logData) {
        return 0;
    }

    @Override
    public int confirmCardVerifyCert(String info) {
        return 0;
    }

    @Override
    public void trannSuccess(int code, String... args) {

    }

    @Override
    public void showError(boolean isToast, int errcode) {
        final String info = PAYUtils.getStatusInfo(String.valueOf(errcode));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIUtils.startResult(PrintLastTrans.this , false , info);
            }
        });
    }

    @Override
    public int printerLackPaper() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_printer_paper);
                btnConfirm = (Button) findViewById(R.id.printer_confirm);
                btnCancel = (Button) findViewById(R.id.printer_cancel);
                btnCancel.setOnClickListener(PrintLastTrans.this);
                btnConfirm.setOnClickListener(PrintLastTrans.this);
            }
        });
        funWait();
        return confirmRet ;
    }

    @Override
    public InputInfo getInput(InputManager.Mode type) {
        return null;
    }

    private Object o = new byte[0] ;

    /**
     * Notificar
     */
    private void listenNotify(){
        synchronized (o){
            o.notify();
        }
    }

    /**
     * bloquear
     */
    private void funWait(){
        synchronized (o){
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
