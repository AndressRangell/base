package cn.desert.newpos.payui.setting.ui.simple;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;

import com.android.newpos.pay.R;
import com.newpos.libpay.global.TMConfig;

import cn.desert.newpos.payui.IItem;
import cn.desert.newpos.payui.base.BaseActivity;
import cn.desert.newpos.payui.base.PayApplication;
import cn.desert.newpos.payui.setting.ui.SettingsFrags;
import cn.desert.newpos.payui.setting.view.ListViewAdapter;

/**
 * Creado por zhouqiang el 15/11/2017.
 * @author zhouqiang
 */
public class PrivateSettings extends BaseActivity {
    Button online_offline ;
    ImageView debug_nodebug ;
    RadioButton style_class ;
    RadioButton style_simple ;
    ListView bankList ;

    private TMConfig config ;
    private ListViewAdapter adapter ;
    private boolean isOnline ;
    private boolean isDebug ;
    private boolean isClassical ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_home_private);
        setNaviTitle(getIntent().getExtras().getString(SettingsFrags.JUMP_KEY));
        config = TMConfig.getInstance();
        initData();
        setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    private void initData(){
        online_offline = (Button) findViewById(R.id.pri_online_offlne);
        debug_nodebug = (ImageView) findViewById(R.id.pri_debug_nodebug);
        style_class = (RadioButton) findViewById(R.id.pri_style_class);
        style_simple = (RadioButton) findViewById(R.id.pri_style_simple);
        bankList = (ListView) findViewById(R.id.pri_listview);
        setOnlineSwitch(config.isOnline());
        setDebugSwitch(config.isDebug());
        setClassSwitch(PayApplication.getInstance().isClassical());
        bankList.setSelected(true);
        adapter = new ListViewAdapter(this, config.getBankid() , IItem.BankList.IMGS);
        bankList.setAdapter(adapter);
        View v = bankList.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        bankList.setSelectionFromTop(config.getBankid(), top);
        bankList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        online_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOnlineSwitch(!isOnline);
            }
        });
        debug_nodebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDebugSwitch(!isDebug);
            }
        });
        style_class.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    setClassSwitch(true);
                }
            }
        });
        style_simple.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    setClassSwitch(false);
                }
            }
        });
    }

    public void setOnlineSwitch(boolean is){
        isOnline = is ;
        if(is){
            online_offline.setText(getString(R.string.online_trans));
        }else {
            online_offline.setText(getString(R.string.local_present));
        }
    }

    public void setDebugSwitch(boolean is){
        isDebug = is ;
        if(is){
            debug_nodebug.setImageResource(R.drawable.home2_setting_commun_open);
        }else {
            debug_nodebug.setImageResource(R.drawable.home2_setting_commun_close);
        }
    }

    public void setClassSwitch(boolean is){
        isClassical = is ;
        if(is){
            style_class.setChecked(true);
            style_simple.setChecked(false);
        }else {
            style_class.setChecked(false);
            style_simple.setChecked(true);
        }
    }

    private void save(){
        config.setOnline(isOnline)
                .setDebug(isDebug)
                .setBankid(adapter.getSelectedItemPosition())
                .save();
        PayApplication.getInstance().setClassical(isClassical);
        finish();
    }
}
