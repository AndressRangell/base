package cn.desert.newpos.payui.transrecord;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.newpos.pay.R;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;

import java.util.ArrayList;
import java.util.List;

import cn.desert.newpos.payui.base.BaseActivity;
import cn.desert.newpos.payui.base.PayApplication;

/**
 * Creado por Andy Yuan
 * Requerir todos los detalles de la transacción
 */
public class HistoryTrans extends BaseActivity implements
        AdapterView.OnItemClickListener, PaginationListView.OnLoadListener{

    public static final String EVENTS = "EVENTS" ;

    PaginationListView lv_trans;
    View view_nodata;
    ImageButton search_bt;
    EditText search_edit;

    private HistorylogAdapter adapter;

    private List<TransLogData> all = new ArrayList<TransLogData>();
    private List<TransLogData> temp = new ArrayList<TransLogData>();
    private int pageItem = 10;
    private int totalItem = 0;
    private TransLog transLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.trans_history);

        PayApplication.getInstance().addActivity(this);
        //Log.d("YUAN", "Entry HistoryTrans >>>>>>>>>>>>>");

        String title = getIntent().getExtras().getString(HistoryTrans.EVENTS);
        if(title!=null){
            setNaviTitle(title,this.getResources().getColor(R.color.base_blue));
        }
        setReturnVisible(View.VISIBLE);
        setRightVisiblity(View.GONE);

        lv_trans = (PaginationListView) findViewById(R.id.history_lv);
        view_nodata = findViewById(R.id.history_nodata);
        search_edit = (EditText) findViewById(R.id.history_search_edit);
        search_bt = (ImageButton) findViewById(R.id.history_search_bt);

        transLog = TransLog.getInstance();

        adapter = new HistorylogAdapter(this) ;
        lv_trans.setAdapter(adapter);

        lv_trans.setOnItemClickListener(this);
        lv_trans.setOnLoadListener(this);

        search_bt.setOnClickListener(new SearchListener());
    }

    @Override
    protected void onResume() {
        super.onResume();

        all = transLog.getData();
        totalItem = all.size();
        lv_trans.initParameters();
        lv_trans.setTotalItem(totalItem);
        temp.clear();
        loadfirstData();
    }

    /**
     * método para validar si hay items en la lista, si existen items se carga la vista
     */
    private void loadfirstData() {
        if (totalItem == 0) {
            showView(true);
        } else {
            loadData();
            showView(false);
        }
    }

    /**
     * método para cargar los datos al adaptador
     */
    private void loadData() {
        int flag = 0;
        //Log.d("YUAN", "loadData >>>>>>>>>totalItem=" + totalItem);
        if (totalItem < 10) {
            flag = totalItem;
        } else {
            flag = pageItem;

        }
        for (int i = flag; i > 0; i--) {
            temp.add(all.get(totalItem - 1));
            totalItem--;
        }
        if (totalItem == 0) {
            lv_trans.setFinishFlag(true);
        }
        adapter.setList(temp);
        adapter.notifyDataSetChanged();
    }

    /**
     * método para mostrar la vista de historial
     * @param isShow
     */
    private void showView(boolean isShow) {
        if (isShow) {
            lv_trans.setVisibility(View.GONE);
            view_nodata.setVisibility(View.VISIBLE);
        } else {
            lv_trans.setVisibility(View.VISIBLE);
            view_nodata.setVisibility(View.GONE);
            lv_trans.setOnItemClickListener(this);
        }
    }

    /**
     * método para iniciar el historial de detalles
     * @param transLogData
     */
    private void start2HistoryDetail(TransLogData transLogData){

        Bundle bundle = new Bundle();

        bundle.putSerializable("Selected", (TransLogData)transLogData);
        Intent i = new Intent();
        i.putExtras(bundle);
        i.setClass(this, HistoryDetail.class);
        startActivity(i);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(position >=all.size()){
            return;
        }

        TransLogData clickData = null;
        clickData = all.get(all.size()-position-1);
        if(clickData != null){
            start2HistoryDetail(clickData);
        }
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                loadData();
                // TODO Auto-generated method stub
                lv_trans.loadComplete();
            }
        }, 100);
    }

    /**
     * método para crear un oyente de busqueda
     */
    private final class SearchListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            String traceNo = search_edit.getText().toString();
            hintKb();
            if (!PAYUtils.isNullWithTrim(traceNo)) {
                if (traceNo.length() < 6) {
                    traceNo = ISOUtil.padleft(traceNo, 6, '0');
                }
                TransLogData data = transLog.searchTransLogByTraceNo(traceNo);
                if (data != null) {
                    start2HistoryDetail(data);
                } else {
                    Toast.makeText(HistoryTrans.this ,
                            HistoryTrans.this.getResources().getString(R.string.not_any_record) ,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * método para validar si el input de busqueda está activo
     */
    private void hintKb() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(search_edit.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            this.setResult(RESULT_CANCELED,null);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
