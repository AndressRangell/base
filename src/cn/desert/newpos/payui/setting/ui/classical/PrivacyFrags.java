package cn.desert.newpos.payui.setting.ui.classical;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.newpos.pay.R;
import com.newpos.libpay.PaySdk;
import com.newpos.libpay.global.TMConfig;

import cn.desert.newpos.payui.IItem;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.base.PayApplication;
import cn.desert.newpos.payui.setting.view.ListViewAdapter;

/**
 * Creado por zhouqiang el 5/7/2017.
 * @author zhouqiang
 */
public class PrivacyFrags implements View.OnClickListener{

    private Activity mActivity = null ;
    private RelativeLayout rLayout = null ;
    private TMConfig cfg = null ;

    private Spinner online = null ;
    private Spinner debug = null ;
    private ListView listView = null ;
    private ListViewAdapter lva = null ;

    private RadioButton classical = null ;
    private RadioButton simple = null ;

    private TMConfig tmConfig = null ;

    public PrivacyFrags(Activity a , RelativeLayout l , String title){
        this.mActivity = a ;
        this.rLayout = l ;
        this.tmConfig = TMConfig.getInstance() ;
        rLayout.removeAllViews();
        rLayout.inflate(mActivity , R.layout.setting_frag_privacy , rLayout);
        ((TextView)rLayout.findViewById(R.id.setting_title_tv)).setText(title);
        rLayout.findViewById(R.id.setting_save).setOnClickListener(this);
        classical = (RadioButton) rLayout.findViewById(R.id.setting_pri_lun_class);
        simple = (RadioButton) rLayout.findViewById(R.id.setting_pri_lun_simple);
        classical.setChecked(PayApplication.getInstance().isClassical());
        simple.setChecked(!PayApplication.getInstance().isClassical());
        classical.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    simple.setChecked(false);
                }
            }
        });
        simple.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    classical.setChecked(false);
                }
            }
        });
        online = (Spinner) rLayout.findViewById(R.id.setting_pri_online);
        String[] array = mActivity.getResources().getStringArray(R.array.privacy_online);
        ArrayAdapter aa = new ArrayAdapter(mActivity , android.R.layout.simple_spinner_dropdown_item , array);
        online.setAdapter(aa);
        online.setSelection(tmConfig.isOnline()?0:1);
        debug = (Spinner) rLayout.findViewById(R.id.setting_pri_debug);
        array = mActivity.getResources().getStringArray(R.array.privacy_debug);
        aa = new ArrayAdapter(mActivity , android.R.layout.simple_spinner_dropdown_item , array);
        debug.setAdapter(aa);
        debug.setSelection(tmConfig.isDebug()?0:1);
        listView = (ListView) rLayout.findViewById(R.id.setting_pri_bank_list);
        listView.setSelected(true);
        lva = new ListViewAdapter(mActivity, tmConfig.getBankid() , IItem.BankList.IMGS);
        listView.setAdapter(lva);

        //Deslizar a la posición seleccionada
        View v = listView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        listView.setSelectionFromTop(tmConfig.getBankid(), top);
//        if(Build.VERSION.SDK_INT >= 8){
//            listView.smoothScrollToPosition(TMConfig.getBankId());
//            lva.notifyDataSetChanged();
//        }else {
//            listView.setSelection(TMConfig.getBankId());
//            lva.notifyDataSetChanged();
//        }
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        cfg = TMConfig.getInstance() ;
    }

    @Override
    public void onClick(View view) {
        if(R.id.setting_save == view.getId()){
            tmConfig.setOnline(online.getSelectedItemPosition() == 0 ? true : false);
            tmConfig.setDebug(debug.getSelectedItemPosition() == 0 ? true:false);
            tmConfig.setBankid(lva.getSelectedItemPosition());
            tmConfig.save();
            PayApplication.getInstance().setClassical(classical.isChecked());
            UIUtils.toast(mActivity , true , R.string.save_success);
        }
    }
}
