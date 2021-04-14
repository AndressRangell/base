package com.android.newpos.pay;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.desert.admanager.ConvenientBanner;
import com.android.desert.admanager.holder.CBViewHolderCreator;
import com.android.desert.baserecyle.BaseQuickAdapter;
import com.android.desert.baserecyle.adapter.HomeAdapter;
import com.android.desert.baserecyle.entity.HomeItem;
import com.android.newpos.mis.MisManager;
import com.android.newpos.mis.MisType;
import com.android.newpos.mis.listener.MisCallback;
import com.google.gson.Gson;
import com.newpos.libpay.Logger;
import com.newpos.libpay.PaySdk;
import com.newpos.libpay.device.printer.PrintRes;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.utils.PAYUtils;

import org.json.JSONObject;

import cn.desert.newpos.payui.mis.JsonTag;
import cn.desert.newpos.payui.mis.ResultPack;
import cn.desert.newpos.payui.mis.TransId;
import cn.desert.newpos.payui.simple.AdHolder;
import cn.desert.newpos.payui.IItem;
import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.base.PayApplication;
import cn.desert.newpos.payui.master.MasterControl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.desert.newpos.payui.setting.ui.SettingsFrags;
import cn.desert.newpos.payui.simple.SecondMenu;
import cn.desert.newpos.payui.transrecord.HistoryTrans;
import cn.desert.newpos.payui.transrecord.PrintLastTrans;

/**
 * @author zhouqiang
 * @email wy1376359644@163.com
 */
public class HomeActivity extends AppCompatActivity implements
        BaseQuickAdapter.OnItemClickListener , View.OnClickListener{

    /**
     * Escritorio clásico
     */
    private ArrayList<HomeItem> mDataList;
    private RecyclerView mRecyclerView;
    private BaseQuickAdapter homeAdapter ;
    private boolean refreash = false ;
    private long mkeyTime = 0 ;

    private static String[] HOMES = null ;
    private static String[] SCANS = null ;
    private static String[] AUTHS = null ;
    private static String[] PRINTS = null ;
    private static String[] MANAGES = null ;
    private static String[] OTHERS = null ;

    /**
     * Escritorio simple
      */
    private ConvenientBanner<String> adColumn ;
    private Dialog secondMenuDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PaySdk.getInstance().setActivity(this);
        //PayApplication.getInstance().addActivity(this);
        PayApplication.getInstance().setRunned();
        initMis();
    }

    /**
     * Inicializar el escritorio clásico
     */
    private void initClassical(){
        setContentView(R.layout.home);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        HOMES = IItem.HOMEMENUS ;
        SCANS = IItem.ALLMENUS[1] ;
        AUTHS = IItem.ALLMENUS[2] ;
        PRINTS = IItem.ALLMENUS[4] ;
        MANAGES = IItem.ALLMENUS[5] ;
        OTHERS = IItem.ALLMENUS[6] ;
        initHomeView(HOMES , IItem.HOMEIMSG);
    }

    /**
     * Inicializar escritorio simple
     */
    private void initSimple(){
        setContentView(R.layout.home_simple);
        File file = new File(getFilesDir()+"/"+"ad");
        if(!file.exists()){
            file.mkdirs();
        }
        UIUtils.copyToAssets(this,"ad",getFilesDir()+"/"+"ad/");
        adColumn = (ConvenientBanner<String>)findViewById(R.id.adcolumn);
        adColumn.setPages(new CBViewHolderCreator<AdHolder>() {

            @Override
            public AdHolder createHolder() {
                return new AdHolder();
            }

        }, UIUtils.getAds(getFilesDir()+"/"+"ad/")).setPageIndicator(new int[] { R.drawable.dot_normal,R.drawable.dot_focused });
        findViewById(R.id.scan).setOnClickListener(Home2ClickListener);
        findViewById(R.id.preauth).setOnClickListener(Home2ClickListener);
        findViewById(R.id.print).setOnClickListener(Home2ClickListener);
        LinearLayout manager = (LinearLayout) findViewById(R.id.manager) ;
        manager.setOnClickListener(Home2ClickListener);
        ImageView cash = (ImageView) findViewById(R.id.cash_iv);
        findViewById(R.id.cash).setOnClickListener(Home2ClickListener);
        LinearLayout revocation = (LinearLayout) findViewById(R.id.revocation) ;
        revocation.setOnClickListener(Home2ClickListener);
        findViewById(R.id.others).setOnClickListener(Home2ClickListener);
        if(Locale.getDefault().getLanguage().equals("zh")){
            cash.setImageResource(R.drawable.home2_cash);
            revocation.setBackground(getResources().getDrawable(R.drawable.home2_void));
            manager.setBackground(getResources().getDrawable(R.drawable.home2_manager));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(PayApplication.getInstance().isClassical()){
            initClassical();
        }else {
            initSimple();
        }
        if(adColumn != null){
            adColumn.startTurning(5000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(adColumn != null){
            adColumn.stopTurning();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(refreash){
                refreash = false ;
                initHomeView(HOMES , IItem.HOMEIMSG);
            }else {
                if ((System.currentTimeMillis() - mkeyTime) > 2000) {
                    mkeyTime = System.currentTimeMillis();
                    Toast.makeText(this , getString(R.string.app_exit) , Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
            return true ;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        String text = ((TextView)view.findViewById(R.id.text)).getText().toString().trim();
        if(text.equals(IItem.HOMEMENUS[0])){
            refreash = true ;
            initHomeView(SCANS,SecondMenu.SCAN_);
        }else if(text.equals(IItem.HOMEMENUS[1])){
            refreash = true ;
            initHomeView(AUTHS,SecondMenu.PREAUTH_);
        }else if(text.equals(IItem.HOMEMENUS[3])){
            refreash = true ;
            initHomeView(PRINTS,SecondMenu.PRINT_);
        }else if(text.equals(IItem.HOMEMENUS[4])){
            refreash = true ;
            initHomeView(MANAGES,SecondMenu.MANA_);
        }else if(text.equals(IItem.HOMEMENUS[5])){
            refreash = true ;
            initHomeView(OTHERS,SecondMenu.OTHERS_);
        }else {
            secondMenuClick(text);
        }
    }

    /**
     * Eventos de clic en el segundo menú
     * @param text
     */
    private void secondMenuClick(String text){
        if(!Locale.getDefault().getLanguage().equals("zh")) {
            text = text.toUpperCase() ;
        }
        if(text.equals(IItem.ALLMENUS[4][0])){
            Intent intent = new Intent(this , HistoryTrans.class);
            intent.putExtra(HistoryTrans.EVENTS , text);
            startActivity(intent);
        }else if(text.equals(IItem.ALLMENUS[4][1])){
            if(TransLog.getInstance().getSize()>0){
                Intent intent = new Intent(this , PrintLastTrans.class);
                intent.putExtra(PrintLastTrans.EVENTS , PrintLastTrans.LAST);
                startActivity(intent);
            }else {
                Toast.makeText(this , getResources().getString(R.string.not_any_record) ,
                        Toast.LENGTH_SHORT).show();
            }
        }else if(text.equals(IItem.ALLMENUS[4][2])){
            if(TransLog.getInstance().getSize()>0){
                Intent intent = new Intent(this , PrintLastTrans.class);
                intent.putExtra(PrintLastTrans.EVENTS , PrintLastTrans.ALL);
                startActivity(intent);
            }else {
                Toast.makeText(this , getResources().getString(R.string.not_any_record) ,
                        Toast.LENGTH_SHORT).show();
            }
        }else if(text.equals(IItem.ALLMENUS[5][3])){
            startActivity(new Intent(HomeActivity.this , SettingsFrags.class));
        }else{
            startTrans(text);
        }
    }

    @Override
    public void onClick(View view) {
        startTrans(PrintRes.TRANS[1]);
    }

    /**
     * Inicializar datos y vistas de escritorio simples
     * @param TITLES
     * @param IMGS
     */
    private void initHomeView(String[] TITLES , int[] IMGS) {
        mDataList = new ArrayList<>();
        for (int i = 0; i < TITLES.length; i++) {
            HomeItem item = new HomeItem();
            item.setTitle(TITLES[i]);
            item.setImageResource(IMGS[i]);
            mDataList.add(item);
        }
        homeAdapter = new HomeAdapter(R.layout.home_item_view, mDataList);
        homeAdapter.openLoadAnimation();
        View top = getLayoutInflater().inflate(R.layout.home_top_view, (ViewGroup) mRecyclerView.getParent(), false);
        top.findViewById(R.id.topview).setOnClickListener(this);
        homeAdapter.addHeaderView(top);
        homeAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(homeAdapter);
    }

    /**
     * iniciar una transacción de hechos
     * @param name
     */
    private void startTrans(String name){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(this , MasterControl.class);
        intent.putExtra(MasterControl.TRANS_KEY , name);
        startActivity(intent);
    }

    /**
     * Oyente de clics de escritorio simple
     */
    private final View.OnClickListener Home2ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.cash){
                startTrans(PrintRes.TRANS[1]);
            } else if(view.getId() == R.id.revocation){
                startTrans(PrintRes.TRANS[2]);
            } else {
                String[] items = null ;
                List<HashMap<String , Object>> list = new ArrayList<>();
                int[] ids = null ;
                switch (view.getId()){
                    case R.id.scan:
                        ids = SecondMenu.SCAN_;
                        items = HomeActivity.this.getResources().getStringArray(R.array.scan_);
                        break;
                    case R.id.preauth:
                        ids = SecondMenu.PREAUTH_ ;
                        items = HomeActivity.this.getResources().getStringArray(R.array.preauth_);
                        break;
                    case R.id.print:
                        ids = SecondMenu.PRINT_ ;
                        items = HomeActivity.this.getResources().getStringArray(R.array.print_);
                        break;
                    case R.id.manager:
                        ids = SecondMenu.MANA_;
                        items = HomeActivity.this.getResources().getStringArray(R.array.mana_);
                        break;
                    case R.id.others:
                        ids = SecondMenu.OTHERS_ ;
                        items = HomeActivity.this.getResources().getStringArray(R.array.others_);
                        break;
                }
                for (int i = 0 ; i < items.length ; i++){
                    HashMap<String , Object> map = new HashMap<>();
                    map.put(SecondMenu.IVKEY , ids[i]);
                    map.put(SecondMenu.TVKEY , items[i]);
                    list.add(map);
                }
                displaySecondMenu(list);
            }
        }
    };

    /**
     * El oyente del icono de cierre de Simple desktop
     * @param v
     */
    public void close_second_menu(View v){
        secondMenuDialog.dismiss();
    }

    /**
     * Mostrar el segundo menú del escritorio simple
     * @param list
     */
    private void displaySecondMenu(List<HashMap<String , Object>> list){
        secondMenuDialog = new Dialog(this, R.style.Translucent_Dialog);
        secondMenuDialog.setContentView(R.layout.home_simple_second_menu);
        LinearLayout layout = (LinearLayout) secondMenuDialog.findViewById(R.id.r_dialog);
        //layout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.down_up));
        GridView gridView = (GridView) layout.findViewById(R.id.second_menu_gv);
        gridView.setNumColumns(list.size());
        gridView.setAdapter(new SimpleAdapter(this , list , R.layout.home_simple_second_menu_item ,
                 new String[]{SecondMenu.IVKEY , SecondMenu.TVKEY} ,
                 new int[]{R.id.second_menu_item_iv , R.id.second_menu_item_tv}));
        gridView.setOnItemClickListener(new SecondMenuListener());
        secondMenuDialog.show();
    }

    /**
     * Oyente de clic del segundo menú de escritorio simple
     */
    private final class SecondMenuListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            secondMenuDialog.dismiss();
            String text = ((TextView)view.findViewById(R.id.second_menu_item_tv)).getText().toString();
            secondMenuClick(text);
        }
    }

    private void initMis(){
        boolean result = MisManager.I()
                .setDebug(true)
                .setServerPort("8989")
                .setMisType(MisType.NETWORK)
                .registerService(this);
        Toast.makeText(this , result?
                getResources().getString(R.string.mis_success)+
                        "\nIP:"+MisManager.I().getServerAddress(this)[0]
                        +"\nPort:"+MisManager.I().getServerAddress(this)[1]
                :
                getResources().getString(R.string.mis_fail)  ,
                Toast.LENGTH_SHORT).show();
        if(result){
            MisManager.I().addListener(new MisCallback() {
                @Override
                public void callback(final byte[] bytes) {
                    startMisTrans(bytes);
                }

                @Override
                public void restart() { }

                @Override
                public void stop() { }
            });
        }
    }

    private void startMisTrans(byte[] data){
        try {
            Logger.debug("startTrans>>" + data);
            if(data == null || data.length <= 5){
                sendResult(null);
                return ;
            }
            String pack = new String(data , "utf-8");
            Logger.debug("startTrans>>pack:" + pack);
            if(!verifyLen(pack)){
                sendResult(null);
                return ;
            }
            pack = pack.substring(5 , pack.length());
            JSONObject jsonObject = new JSONObject(pack);
            String appname = jsonObject.getString(JsonTag.sAppName);
            Logger.debug("startTrans>>appname:"+appname);
            String trandID = jsonObject.getString(JsonTag.sTransId);
            Logger.debug("startTrans>>trandID:"+trandID);
            if(TransId.SALE.equals(trandID)){
                String amount = jsonObject.getString(JsonTag.sAmt);
                startTrans(PrintRes.TRANS[1]);
            }else if(TransId.CANCEL.equals(trandID)){
                startTrans(PrintRes.TRANS[2]);
            }else if(TransId.REFUND.equals(trandID)){
                startTrans(PrintRes.TRANS[10]);
            }else if(TransId.QUERY.equals(trandID)){
                startTrans(PrintRes.TRANS[0]);
            }else {
                sendResult(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug("startTrans>>"+e.getMessage());
            try {
                sendResult(null);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void sendResult(Intent intent){
        try {
            Logger.debug("sendResult>>"+intent);
            Gson gson = new Gson();
            String json = gson.toJson(transferIntent(intent));
            Logger.debug("buildResultPack>>json:"+json);
            String len = PAYUtils.paddingLeft(json.length()+"" , 5 , "0") ;
            Logger.debug("buildResultPack>>len:"+len);
            MisManager.I().sendToClient((len + json).getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResultPack transferIntent(Intent intent) {
        ResultPack resultPack = new ResultPack();
        if(intent == null){
            resultPack.setResultCode("01");
            return resultPack ;
        }

//        try {
//            Map<String , String> map = AppHelper.filterTransResult(intent);
//            resultPack.setResultCode(map.get(AppHelper.RESULT_CODE));
//            JSONObject transData = new JSONObject(map.get(AppHelper.TRANS_DATA));
//            if(transData.has("resCode")){
//                String resCode = transData.getString("resCode") ;
//                if(!"00".equals(resCode)){
//                    resultPack.setResultCode(resCode+"("+transData.getString("resDesc")+")");
//                    return resultPack ;
//                }
//                resultPack.setResultCode(resCode);
//            }
//            if(transData.has("merchantNo")) {
//                resultPack.setMerchantId(transData.getString("merchantNo"));
//            }
//            if(transData.has("terminalNo")) {
//                resultPack.setTerminalId(transData.getString("terminalNo"));
//            }
//            if(transData.has("amt")) {
//                resultPack.setTransAmount(transData.getString("amt"));
//            }
//            if(transData.has("traceNo")) {
//                resultPack.setVoucherNo(transData.getString("traceNo"));
//            }
//            if(transData.has("refNo")) {
//                resultPack.setReferenceNo(transData.getString("refNo"));
//            }
//            if(transData.has("extBillNo")) {
//                resultPack.setPlatformBillNo(transData.getString("extBillNo"));
//            }
//            if(transData.has("extOrderNo")) {
//                resultPack.setMerchantBillNo(transData.getString("extOrderNo"));
//            }
//            if(transData.has("date")) {
//                resultPack.setTransDate(transData.getString("date"));
//            }
//            if(transData.has("time")) {
//                resultPack.setTransTime(transData.getString("time"));
//            }
//            if(transData.has("cardNo")) {
//                resultPack.setPaymentCode(transData.getString("cardNo"));
//            }
//            resultPack.setTransId(map.get(AppHelper.TRANS_BIZ_ID));
//            return resultPack ;
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        resultPack.setResultCode("01");
        return resultPack ;
    }

    private boolean verifyLen(String string){
        long len = Long.parseLong(string.substring(0 , 5));
        return len == string.length() - 5 ;
    }
}
