package cn.desert.newpos.payui.master;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputListener;
import com.android.desert.keyboard.InputManager;
import com.android.desert.keyboard.KeyViewEx;
import com.android.desert.keyboard.KeyViewExHelper;
import com.android.newpos.pay.HomeActivity;
import com.android.newpos.pay.R;
import com.newpos.libpay.PaySdk;
import com.newpos.libpay.PaySdkException;
import com.newpos.libpay.device.card.CardType;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.device.pinpad.PinResult;
import com.newpos.libpay.device.pinpad.PinType;
import com.newpos.libpay.device.printer.PrintRes;
import com.newpos.libpay.device.user.OnUserResultListener;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.presenter.TransView;
import com.newpos.libpay.trans.Type;
import com.newpos.libpay.trans.finace.ServiceEntryMode;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;

import java.util.Locale;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.base.BaseActivity;
import cn.desert.newpos.payui.base.PayApplication;

public class MasterControl extends BaseActivity implements TransView, View.OnClickListener{

    WebView wvInsert ;
    WebView wvSwipe ;
    WebView wvPat ;
    Button btnConfirm ;
    Button btnCancel ;
    EditText editCardNO ;
    EditText transInfo ;

    OnUserResultListener listener ;

    String inputContent ;

    public static String TRANS_KEY = "TRANS_KEY" ;

    private String type ;

    private int appIndex = 0 ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PayApplication.getInstance().addActivity(this);
        type = getIntent().getStringExtra(TRANS_KEY);
        setNaviTitle(type);
        setSaveVisible(View.INVISIBLE);
        setReturnVisible(View.INVISIBLE);
        startTrans(getTransType(type));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PaySdk.getInstance().releaseCard();
    }

    @Override
    public void onClick(View view) {
        if(view.equals(btnCancel)){
            listener.cancel();
        }if(view.equals(btnConfirm)){
            listener.confirm(InputManager.Style.COMMONINPUT);
        }
    }

    @Override
    public void showCardView(int timeout, final int mode) {
        TMConfig.setLockReturn(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_show_card);
                loadWebGif(mode);
            }
        });
    }

    @Override
    public void showQRCView(int timeout, InputManager.Style mode) {
        TMConfig.setLockReturn(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_show_qrc);
            }
        });
    }

    @Override
    public void showCardNo(int timeout, final String pan, OnUserResultListener l) {
        TMConfig.setLockReturn(false);
        this.listener = l ;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_show_cardno);
                showConfirmCardNO(PAYUtils.getSecurityNum(pan , 6 , 4));
            }
        });
    }

    @Override
    public void showInputView(int timeout, final InputManager.Mode mode, OnUserResultListener l) {
        TMConfig.setLockReturn(false);
        this.listener = l ;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_input);
                showInput(mode);
            }
        });
    }

    @Override
    public String getInput(InputManager.Mode type) {
        return inputContent ;
    }

    @Override
    public void showTransInfoView(int timeout, final TransLogData data, OnUserResultListener l) {
        TMConfig.setLockReturn(false);
        this.listener = l ;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_show_transinfo);
                showOrignalTransInfo(data);
            }
        });
    }

    @Override
    public int showCardAppListView(int timeout, final String[] apps, OnUserResultListener l) {
        TMConfig.setLockReturn(false);
        this.listener = l ;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_chose_applist);
                ListView listView = (ListView) findViewById(R.id.chose_applist_lv);
                ArrayAdapter adapter = new ArrayAdapter(MasterControl.this ,
                        android.R.layout.simple_list_item_1 , apps);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        appIndex = i ;
                        listener.confirm(InputManager.Style.COMMONINPUT);
                    }
                });
            }
        });
        return appIndex ;
    }

    @Override
    public void showCardVerifyCertView(int i, String s, OnUserResultListener l) {
        TMConfig.setLockReturn(false);
        this.listener = l ;
    }

    @Override
    public PinInfo showEnterPinView(int i, PinType pinType , OnUserResultListener l) {
        this.listener = l ;
        PinInfo info = new PinInfo();
        info.setResult(PinResult.NO_OPERATION);
        return info;
    }

    @Override
    public void handleBeforceGPO(int i) {
        TMConfig.setLockReturn(false);
    }

    @Override
    public void showSuccess(int timeout, final String info) {
        TMConfig.setLockReturn(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HomeActivity.sendResult(null);
                UIUtils.startResult(MasterControl.this , true , info);
            }
        });
    }

    @Override
    public void showError(int timeout, final boolean isToast , final String err) {
        TMConfig.setLockReturn(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isToast){
                    Toast.makeText(MasterControl.this , err , Toast.LENGTH_SHORT).show();
                }else {
                    UIUtils.startResult(MasterControl.this , false , err);
                }
            }
        });
    }

    @Override
    public void printerLackPaper(int timeout, OnUserResultListener listener) {
        this.listener = listener ;
        TMConfig.setLockReturn(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_printer_paper);
                showPrinterPaper();
            }
        });
    }

    @Override
    public void showMsgInfo(int timeout, final String status) {
        TMConfig.setLockReturn(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_handling);
                showHanding(status);
            }
        });
    }

    /**
     * método para iniciar una vista dependiendo del tipo de transaccion
     * @param type se envía el tipo de transaccion
     */
    private void startTrans(String type){
        try {
            PaySdk.getInstance().startTrans(type , this);
        } catch (PaySdkException e) {
            e.printStackTrace();
        }
    }

    /**
     * obtener el código del tipo de transaccion que estamos realizando
     * @param name se envía el nombre de la transaccion como parametro
     * @return
     */
    public static String getTransType(String name){
        String[] list = PrintRes.TRANS ;
        int index = 0 ;
        for (int i = 0 ; i < list.length ; i++){
            if(list[i].equals(name)){
                index = i ;
            }
        }
        return PrintRes.STANDRAD_TRANS_TYPE[index] ;
    }

    /**
     * método para cargar una gif de internet por medio de un en
     * @param mode
     */
    private void loadWebGif(int mode){
        wvInsert = (WebView) findViewById(R.id.webview_insert);
        wvPat = (WebView) findViewById(R.id.webview_pat);
        wvSwipe = (WebView) findViewById(R.id.webview_swipe);
        if(!Locale.getDefault().getLanguage().equals("zh")){
            wvInsert.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
                    "<img width=\"128\" height=\"128\" src='file:///android_asset/card/Insert.png'/></div></body></html>", "text/html", "UTF-8",null);
            wvSwipe.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
                    "<img width=\"128\" height=\"128\" src='file:///android_asset/card/Swipe.png'/></div></body></html>", "text/html", "UTF-8",null);
            wvPat.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
                    "<img width=\"128\" height=\"128\" src='file:///android_asset/card/Tap.png'/></div></body></html>", "text/html", "UTF-8",null);
        }else {
            wvInsert.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
                    "<img width=\"128\" height=\"128\" src='file:///android_asset/card/chaka.png'/></div></body></html>", "text/html", "UTF-8",null);
            wvSwipe.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
                    "<img width=\"128\" height=\"128\" src='file:///android_asset/card/shuaka.png'/></div></body></html>", "text/html", "UTF-8",null);
            wvPat.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
                    "<img width=\"128\" height=\"128\" src='file:///android_asset/card/huika.png'/></div></body></html>", "text/html", "UTF-8",null);
        }

        if((mode & CardType.INMODE_MAG.getVal() ) == 0){
            findViewById(R.id.ll_swipe).setVisibility(View.GONE);
        }

        if((mode & CardType.INMODE_IC.getVal() ) == 0){
            findViewById(R.id.ll_insert).setVisibility(View.GONE);
        }

        if((mode & CardType.INMODE_NFC.getVal() ) == 0){
            findViewById(R.id.ll_pat).setVisibility(View.GONE);
        }
    }

    /**
     * método para confirmar el numero de la tarjeta
     * @param pan
     */
    private void showConfirmCardNO(String pan){
        btnConfirm = (Button) findViewById(R.id.cardno_confirm);
        btnCancel = (Button) findViewById(R.id.cardno_cancel);
        editCardNO = (EditText) findViewById(R.id.cardno_display_area);
        ImageView iv = (ImageView)findViewById(R.id.trans_cardno_iv) ;
        iv.setImageBitmap(PAYUtils.getLogoByBankId(this , TMConfig.getInstance().getBankid()));
        btnCancel.setOnClickListener(MasterControl.this);
        btnConfirm.setOnClickListener(MasterControl.this);
        editCardNO.setText(pan);
    }

    /**
     * método para mostrar un input dependiendo del tipo que se requiera
     * @param mode tipo de input
     */
    private void showInput(final InputManager.Mode mode){
        final TextView inputTitle = (TextView) findViewById(R.id.input_title);
        EditText editText = (EditText) findViewById(R.id.input_edit);
        KeyViewEx keyView = (KeyViewEx) findViewById(R.id.input_key);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 52);
        editText.setCursorVisible(false);
        KeyViewExHelper mKeyboardHelper = new KeyViewExHelper(this, keyView);
        if(mode == InputManager.Mode.AMOUNT){
            inputTitle.setText(R.string.please_input_amount);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            mKeyboardHelper.onAttach(editText, KeyViewExHelper.INPUT_TYPE_NUMBER_DECIMAL);
        }if(mode == InputManager.Mode.PASSWORD){
            inputTitle.setText(R.string.please_input_master_pass);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            mKeyboardHelper.onAttach(editText, KeyViewExHelper.INPUT_TYPE_PASSWORD);
        }if(mode == InputManager.Mode.VOUCHER){
            inputTitle.setText(R.string.please_input_trace_no);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            mKeyboardHelper.onAttach(editText, KeyViewExHelper.INPUT_TYPE_NUMBER);
        }if(mode == InputManager.Mode.AUTHCODE){
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            mKeyboardHelper.onAttach(editText, KeyViewExHelper.INPUT_TYPE_NUMBER);
            inputTitle.setText(R.string.please_input_auth_code);
        }if(mode == InputManager.Mode.DATETIME){
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            mKeyboardHelper.onAttach(editText, KeyViewExHelper.INPUT_TYPE_NUMBER);
            inputTitle.setText(R.string.please_input_data_time);
        }if(mode == InputManager.Mode.REFERENCE){
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
            mKeyboardHelper.onAttach(editText, KeyViewExHelper.INPUT_TYPE_NUMBER);
            inputTitle.setText(R.string.please_input_reference);
        }
        mKeyboardHelper.setOnOkClickListener(new KeyViewExHelper.OnOkClickListener() {
            @Override
            public void onClick(String data) {
                inputContent = data ;
                if(mode == InputManager.Mode.AMOUNT){
                    inputContent = data.replaceAll("," , "");
                    listener.confirm(InputManager.Style.UNIONPAY);
                }else {
                    listener.confirm(InputManager.Style.COMMONINPUT);
                }
            }
        });
        mKeyboardHelper.setOnCancelClickListener(new KeyViewExHelper.OnCancelClickListener() {
            @Override
            public void onClick() {
                listener.cancel();
            }
        });
    }

    /**
     * segundo método para mostrar un input dependiendo del tipo que se requiera
     * @param mode
     */
    private void showInput2(InputManager.Mode mode){
        InputManager inputManager = new InputManager(MasterControl.this) ;
        inputManager.setListener(new InputListener() {
            @Override
            public void callback(InputInfo inputInfo) {
                if(inputInfo.isResultFlag()){
                    inputContent = inputInfo.getResult() ;
                    listener.confirm(inputInfo.getNextStyle());
                }else {
                    listener.cancel();
                }
            }
        });
        if(Locale.getDefault().getLanguage().equals("zh")){
            inputManager.setLang(InputManager.Lang.CH);
        }else {
            inputManager.setLang(InputManager.Lang.EN);
        }

        if(mode == InputManager.Mode.AMOUNT){
            inputManager.setTitle(R.string.please_input_amount);
        }if(mode == InputManager.Mode.PASSWORD){
            inputManager.setTitle(R.string.please_input_master_pass);
        }if(mode == InputManager.Mode.VOUCHER){
            inputManager.setTitle(R.string.please_input_trace_no);
        }if(mode == InputManager.Mode.AUTHCODE){
            inputManager.setTitle(R.string.please_input_auth_code);
        }if(mode == InputManager.Mode.DATETIME){
            inputManager.setTitle(R.string.please_input_data_time);
        }if(mode == InputManager.Mode.REFERENCE){
            inputManager.setTitle(R.string.please_input_reference);
        }

        inputManager.addEdit(mode);

        inputManager.addKeyboard(false);

        if(mode == InputManager.Mode.AMOUNT && getTransType(type).equals(Type.SALE)){
            inputManager.addStyles();
        }

        setContentView(inputManager.getView());
    }

    /**
     * método para mostrar informacion sobre una transaccion
     * @param data
     */
    private void showOrignalTransInfo(TransLogData data){
        btnConfirm = (Button) findViewById(R.id.transinfo_confirm);
        btnCancel = (Button) findViewById(R.id.transinfo_cancel);
        btnCancel.setOnClickListener(MasterControl.this);
        btnConfirm.setOnClickListener(MasterControl.this);
        transInfo = (EditText) findViewById(R.id.transinfo_display_area);
        String info = getString(R.string.void_original_trans)+data.getEName()+"\n" ;
        if(data.getMode() == ServiceEntryMode.QRC){
            info += getString(R.string.void_pay_code)+data.getPan()+"\n" ;
        }else {
            info += getString(R.string.void_card_no)+data.getPan()+"\n" ;
        }
        info += getString(R.string.void_trace_no)+data.getTraceNo()+"\n" ;
        if(!PAYUtils.isNullWithTrim(data.getAuthCode())){
            info += getString(R.string.void_auth_code)+data.getAuthCode()+"\n";
        }
        info += getString(R.string.void_batch_no)+data.getBatchNo()+"\n";
        info += getString(R.string.void_amount)+PAYUtils.getStrAmount(data.getAmount())+"\n";
        info += getString(R.string.void_time)+PAYUtils.printStr(data.getLocalDate(), data.getLocalTime());
        transInfo.setText(info);
    }

    /**
     * método para mostrar un mensaje de información sobre la entrega
     * @param msg
     */
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
     * método para mostrar el papel de la impresora
     */
    private void showPrinterPaper(){
        btnConfirm = (Button) findViewById(R.id.printer_confirm);
        btnCancel = (Button) findViewById(R.id.printer_cancel);
        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }
}
