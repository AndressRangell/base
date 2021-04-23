package cn.desert.newpos.payui.transrecord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;

import com.android.newpos.pay.R;
import com.newpos.libpay.device.printer.PrintRes;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.trans.finace.ServiceEntryMode;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.base.BaseActivity;


/**
 * Creado por Andy Yuan
 * Requerir detalles de la transacción
 */

public class HistoryDetail extends BaseActivity {

    TextView cardno;
    TextView date;
    TextView merchantno;
    TextView refno;
    TextView terminalno;
    TextView tranctype;
    TextView voucherno;
    TextView head;
    TextView amount;
    TextView authno;
    Button detail_button;

    TransLogData clickData;

//    Message message = new Message();
//    private static final int MSG_ERROR_MSG = 0x01;
//    private static final int MSG_PRINT_OK = 0x02;

    public static final String TRACENO_KEY = "TRACENO_KEY" ;

    /**
     * método para iniciar la vista de impresion de factura
     */
    private void initView(){
        cardno = (TextView) findViewById(R.id.detail_cardno);
        date = (TextView) findViewById(R.id.detail_date);

        merchantno = (TextView) findViewById(R.id.detail_merchantno);
        terminalno = (TextView) findViewById(R.id.detail_terminalno);
        tranctype = (TextView) findViewById(R.id.detail_tranctype);
        voucherno = (TextView) findViewById(R.id.detail_voucherno);
        head = (TextView) findViewById(R.id.detail_head);
        amount = (TextView) findViewById(R.id.detail_amount);
        authno = (TextView) findViewById(R.id.detail_authno);
        detail_button = (Button) findViewById(R.id.detail_button);
        refno = (TextView) findViewById(R.id.detail_refno);

        setNaviTitle(UIUtils.getStringByInt(this,R.string.trans_detail),
                this.getResources().getColor(R.color.base_blue));
        setReturnVisible(View.VISIBLE);
        setRightVisiblity(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rongpay_history_detail);
        initView();
        TMConfig cfg = TMConfig.getInstance();
        clickData = (TransLogData)getIntent().getSerializableExtra("Selected");
        if(clickData != null){

            head.setText(PrintRes.CH.AMOUNT);

            if(clickData.getAmount() != 0 && clickData.getAmount() != null){
                amount.setText( PrintRes.CH.RMB+" "+ PAYUtils.getStrAmount(clickData.getAmount()));
            } else {
                amount.setVisibility(View.INVISIBLE);
            }

            if (!PAYUtils.isNullWithTrim(clickData.getPan())) {
                String temp ;
                if(clickData.getMode() == ServiceEntryMode.QRC){
                    temp = PrintRes.CH.SCANCODE + " " +clickData.getPan();
                }else {
                    temp = PrintRes.CH.CARD_NO + " " +clickData.getPan();
                }
                cardno.setText(temp);
            } else{
                cardno.setVisibility(View.INVISIBLE);
            }

            String timeStr = PAYUtils.StringPattern(clickData.getLocalDate() + clickData.getLocalTime(), "yyyyMMddHHmmss", "yyyy/MM/dd  HH:mm:ss");
            date.setText(PrintRes.CH.DATE_TIME+ " " + timeStr);

            if(!PAYUtils.isNullWithTrim(clickData.getTraceNo())){
                voucherno.setText(PrintRes.CH.VOUCHER_NO + " " + clickData.getTraceNo());
            }else{
                voucherno.setVisibility(View.INVISIBLE);
            }

            String type = PAYUtils.formatTranstype(clickData.getEName());
            if(!PAYUtils.isNullWithTrim(type)) {
                tranctype.setText(PrintRes.CH.TRANS_TYPE + " " +  type);
            }else{
                tranctype.setVisibility(View.INVISIBLE);
            }

            merchantno.setText(PrintRes.CH.TERNIMAL_ID + " " + cfg.getMerchID());
            terminalno.setText(PrintRes.CH.MERCHANT_ID + " " + cfg.getTermID());

            if(!PAYUtils.isNullWithTrim(clickData.getRRN())){
                refno.setText(PrintRes.CH.REF_NO + " " + clickData.getRRN());
            }else{
                refno.setVisibility(View.INVISIBLE);
            }

            if(!PAYUtils.isNullWithTrim(clickData.getAuthCode())){
                authno.setText(PrintRes.CH.AUTH_NO + " " + clickData.getAuthCode());
            }else{
                authno.setVisibility(View.INVISIBLE);
            }

        }

        /**
         * método para cambiar de la actividad HistoryDetails a la actividad PrintLastTrans
         */
        detail_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        print(clickData);
//                    }
//                }).start();

                Bundle bundle = new Bundle();
                Intent intent = new Intent(HistoryDetail.this , PrintLastTrans.class);
                bundle.putString(PrintLastTrans.EVENTS , PrintLastTrans.ANY);
                bundle.putString(TRACENO_KEY, clickData.getTraceNo());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }


//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//
//            switch (msg.what) {
//                case MSG_ERROR_MSG:
//                    showError(false , (int)msg.obj);
//                    HistoryDetail.this.finish();
//                    break;
//
//                case MSG_PRINT_OK:
//                    HistoryDetail.this.finish();
//                    break;
//
//            }
//        }
//    };
//
//    private void print(final TransLogData data) {
//        final PrintManager printManager = PrintManager.getmInstance(this);
//        handling(Tcode.PRINTING_RECEPT);
//        new Thread(){
//            @Override
//            public void run() {
//                boolean isPrinted = true ;
//                boolean isPrinterBroken = false ;
//                for (int i = 0 ; i < TMConfig.getInstance().getPrinterTickNumber() ; i ++){
//                    PrintTask task = printManager.buildTaskByTLD(data ,
//                            i , true);
//                    int ret = 0 ;
//                    do {
//                        handling(Tcode.PRINTING_RECEPT);
//                        ret = printManager.print(task);
//                        if(Printer.PRINTER_STATUS_PAPER_LACK == ret){
//                            int result = printerLackPaper();
//                            if(1 == result){
//                                isPrinted = false ;
//                                break;
//                            }
//                        }else if(Printer.PRINTER_OK != ret){
//                            isPrinterBroken = true ;
//                            break;
//                        }
//                    }while (ret == Printer.PRINTER_STATUS_PAPER_LACK) ;
//                }
//                if(!isPrinted){
//                    message = mHandler.obtainMessage();
//                    message.what = MSG_ERROR_MSG;
//                    message.obj = Tcode.USER_CANCEL;
//                    mHandler.sendMessage(message);
//                }else if(isPrinterBroken){
//                    message = mHandler.obtainMessage();
//                    message.what = MSG_ERROR_MSG;
//                    message.obj = Tcode.PRINT_FAIL;
//                    mHandler.sendMessage(message);
//                }else {
//                    message = mHandler.obtainMessage();
//                    message.what = MSG_PRINT_OK;
//                    message.obj = 0;
//                    mHandler.sendMessage(message);
//                }
//            }
//        }.start();
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//
//    private void showHanding(String msg){
//        TextView tv = (TextView) findViewById(R.id.handing_msginfo);
//        tv.setText(msg);
//        ImageView iv = (ImageView) findViewById(R.id.handing_img);
//        iv.setImageBitmap(PAYUtils.getLogoByBankId(this , TMConfig.getInstance().getBankid()));
//        WebView wv = (WebView) findViewById(R.id.handling_loading);
//        wv.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
//                "<img width=\"128\" height=\"128\" src='file:///android_asset/gif/loading.gif'/></div></body></html>", "text/html", "UTF-8",null);
//    }
//
//    @Override
//    public void handling(final int status) {
//
//        final String msg = PAYUtils.getStatusInfo(String.valueOf(status));
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                setContentView(R.layout.trans_handling);
//                showHanding(msg);
//            }
//        });
//    }
//
//    @Override
//    public int printerLackPaper() {
//        return 0;
//    }
//
//    @Override
//    public void trannSuccess(int code, String... args) {
//        final String info = PAYUtils.getStatusInfo(String.valueOf(code));
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                UIUtils.startResult(HistoryDetail.this , true , info);
//            }
//        });
//    }
//
//    @Override
//    public void showError(boolean isToast , int errcode) {
//
//        final String info = PAYUtils.getStatusInfo(String.valueOf(errcode));
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                UIUtils.startResult(HistoryDetail.this , false , info);
//            }
//        });
//    }
//
//    @Override
//    public QRCInfo getQRCInfo(InputManager.Style style) {
//        return null;
//    }
//
//    @Override
//    public InputInfo getInput(InputManager.Mode mode) {
//        return null;
//    }
//
//    @Override
//    public CardInfo getCard(int i1) {
//        return null;
//    }
//
//    @Override
//    public PinInfo getPinpadPin(PinType pinType) {
//        return null;
//    }
//
//    @Override
//    public int confirmCardNO(String s) {
//        return 0;
//    }
//
//    @Override
//    public int choseAppList(String[] strings) {
//        return 0;
//    }
//
//    @Override
//    public void beforeGPO() {
//
//    }
//
//    @Override
//    public int confirmTransInfo(TransLogData transLogData) {
//        return 0;
//    }
//
//    @Override
//    public int confirmCardVerifyCert(String s) {
//        return 0;
//    }
}