package cn.desert.newpos.payui.setting.ui.simple.transson;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.newpos.pay.R;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.utils.PAYUtils;

import cn.desert.newpos.payui.base.BaseActivity;

/**
 * Creado por zhouqiang el 15/11/2017.
 * @author zhouqiang
 */
public class TransSysSetting extends BaseActivity {

    EditText traceNO ;
    EditText batchNo ;
    EditText tpdu ;
    EditText firmNo ;
    Spinner printNo ;
    Spinner reversal ;
    Spinner waitTime ;
    ImageView trackEncrypt ;
    ImageView playVoice ;

    private TMConfig config ;
    private boolean isEncrypted ;
    private boolean isPlay ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_home_trans_sys);
        config = TMConfig.getInstance() ;
        initData();
        setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    /**
     * método para inicializar los datos de configuración del sistema
     */
    private void initData(){
        traceNO = (EditText) findViewById(R.id.sys_trace_no);
        batchNo = (EditText) findViewById(R.id.sys_batch_no);
        tpdu = (EditText) findViewById(R.id.sys_tpdu);
        firmNo = (EditText) findViewById(R.id.sys_firm_no);
        printNo = (Spinner) findViewById(R.id.sys_print_no);
        reversal = (Spinner) findViewById(R.id.sys_reversal);
        waitTime = (Spinner) findViewById(R.id.sys_wait_time);
        trackEncrypt = (ImageView) findViewById(R.id.sys_encrypted);
        playVoice = (ImageView) findViewById(R.id.sys_play_voice);
        setEncrypedSwitch(config.isTrackEncrypt());
        setPlayVoiceSwitch(config.isVocie());
        traceNO.setText(config.getTraceNo());
        batchNo.setText(config.getBatchNo());
        tpdu.setText(config.getTpdu());
        firmNo.setText(config.getFirmCode());
        setAdaper(printNo , R.array.print_num);
        printNo.setSelection(config.getPrinterTickNumber()-1 , true);
        setAdaper(reversal , R.array.reversal_counts);
        reversal.setSelection(config.getReversalCount()-3 , true);
        setAdaper(waitTime , R.array.wait_time);
        waitTime.setSelection(config.getWaitUserTime()/30 -1 , true);
        trackEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEncrypedSwitch(!isEncrypted);
            }
        });
        playVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPlayVoiceSwitch(!isPlay);
            }
        });
    }

    /**
     * método para enviar un adaptador con sus respectivos datos para posteriormente mostrarlos
     * @param spinner
     * @param arrayID
     */
    private void setAdaper(Spinner spinner , int arrayID){
        String[] array = getResources().getStringArray(arrayID);
        ArrayAdapter adapter = new ArrayAdapter(this , android.R.layout.simple_spinner_dropdown_item , array);
        spinner.setAdapter(adapter);
    }

    /**
     * método para mostrar interruptor activado o desactivado
     * @param is boolean -> true: drawable interruptor activado; false: drawable interruptor desactivado
     */
    public void setEncrypedSwitch(boolean is){
        isEncrypted = is ;
        if(is){
            trackEncrypt.setImageResource(R.drawable.home2_setting_commun_open);
        }else {
            trackEncrypt.setImageResource(R.drawable.home2_setting_commun_close);
        }
    }

    /**
     * método para mostrar interruptor activado o desactivado
     * @param is boolean -> true: drawable interruptor activado; false: drawable interruptor desactivado
     */
    public void setPlayVoiceSwitch(boolean is){
        isPlay = is ;
        if(is){
            playVoice.setImageResource(R.drawable.home2_setting_commun_open);
        }else {
            playVoice.setImageResource(R.drawable.home2_setting_commun_close);
        }
    }

    /**
     * método para guardar los datos de configuración que hemos seleccionado
     */
    private void save(){
        String tn = traceNO.getText().toString();
        String bn = batchNo.getText().toString();
        String tp = tpdu.getText().toString();
        String fn = firmNo.getText().toString();
        if(PAYUtils.isNullWithTrim(tn)||
                PAYUtils.isNullWithTrim(bn)||
                PAYUtils.isNullWithTrim(tp)||
                PAYUtils.isNullWithTrim(fn)){
            Toast.makeText(this , getString(R.string.data_null) , Toast.LENGTH_SHORT).show();
            return;
        }
        if(tp.length()!=10){
            Toast.makeText(this , getString(R.string.len_err) , Toast.LENGTH_SHORT).show();
            return;
        }
        config.setTraceNo(Integer.parseInt(tn))
                .setBatchNo(Integer.parseInt(bn))
                .setTpdu(tp)
                .setFirmCode(fn)
                .setPrinterTickNumber(printNo.getSelectedItemPosition()+1)
                .setWaitUserTime((waitTime.getSelectedItemPosition()+1)*30)
                .setTrackEncrypt(isEncrypted)
                .setVocie(isPlay)
                .save();
        finish();
    }
}
