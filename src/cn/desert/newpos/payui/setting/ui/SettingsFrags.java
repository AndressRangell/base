package cn.desert.newpos.payui.setting.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.newpos.pay.R;
import com.newpos.libpay.global.TMConfig;

import java.util.ArrayList;
import java.util.HashMap;

import cn.desert.newpos.payui.IItem;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.setting.ui.classical.TransparaFrags;
import cn.desert.newpos.payui.setting.ui.simple.CommunSettings;
import cn.desert.newpos.payui.setting.ui.simple.ErrlogSettings;
import cn.desert.newpos.payui.setting.ui.simple.FeedbackSettings;
import cn.desert.newpos.payui.setting.ui.simple.KeysSettings;
import cn.desert.newpos.payui.setting.ui.simple.PrivateSettings;
import cn.desert.newpos.payui.setting.ui.simple.TransSetting;

/**
 * @author zhouqiang
 * @email wy1376359644@163.com
 */
public class SettingsFrags extends Activity implements AdapterView.OnItemClickListener{

    private GridView mGrid ;
    private LinearLayout rootLayout ;
    private Dialog mDialog ;
    private ListView listView = null ;
    public static String JUMP_KEY = "JUMP_KEY" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void setting_return(View v){
        init();
    }

    /**
     * método para inicializar los datos del fragmento de configuraciones
     */
    private void init(){
        setContentView(R.layout.setting);
        mGrid = (GridView) findViewById(R.id.setting_gridview);
        listView = (ListView) findViewById(R.id.setting_listview);
        listView.setAdapter(formatAdapter2());
        listView.setOnItemClickListener(this);
        rootLayout = (LinearLayout) findViewById(R.id.setting_root);
        mGrid.setAdapter(formatAdapter());
        mGrid.setOnItemClickListener(this);
        findViewById(R.id.setting_maintain_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMaintainPwd();
            }
        });
        findViewById(R.id.setting_errlog_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(JUMP_KEY , getString(R.string.errlog_list));
                intent.setClass(SettingsFrags.this , ErrlogSettings.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.setting_advice_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(JUMP_KEY , getString(R.string.feedback_advice));
                intent.setClass(SettingsFrags.this , FeedbackSettings.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.setting_android_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent();
        String text = ((TextView)view.findViewById(R.id.setting_listitem_tv)).getText().toString();
        intent.putExtra(JUMP_KEY , text);
        switch (i){
            case 0:
                intent.setClass(this , CommunSettings.class);
                break;
            case 1:
                intent.setClass(this , TransSetting.class);
                break;
            case 2:
                intent.setClass(this , KeysSettings.class);
                break;
            case 3:
                intent.setClass(this , PrivateSettings.class);
                break;
        }
        startActivity(intent);
    }

/**
    private long mkeyTime = 0 ;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mkeyTime) > 2000) {
                mkeyTime = System.currentTimeMillis();
                UIUtils.toast(SettingsFrags.this , false , R.string.app_exit);
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String title = getResources().getStringArray(R.array.setting_items)[i];
        switch (i){
            case 0 :new CommunFrags(this , rootLayout , title);break;
            case launcher_classical_en :startTranspara();break;
            case launcher_simple_en :new KeysparaFrags(this , rootLayout , title);break;
            case 3 :changeMaintainPwd();break;
            case 4 :new ErrlogFrags(this , rootLayout , title);break;
            case 5 :new PrivacyFrags(this , rootLayout , title);break;
            case 6 :new AboutDesert(this , rootLayout , title);break;
            case 5 :new DeviceFrags(this , rootLayout , title);break;
        }
    }

    private void startTranspara(){
        Intent intent = new Intent();
        intent.setClass(this , TransparaFrags.class);
        startActivity(intent);
    }

    private void changeMaintainPwd(){
        mDialog = UIUtils.centerDialog(this , R.layout.setting_changepwd_dialog, R.id.setting_changepwd_root_layout);
        final EditText newEdit = (EditText) mDialog.findViewById(R.id.setting_changepwd_edit_new);
        final EditText oldEdit = (EditText) mDialog.findViewById(R.id.setting_changepwd_edit_old);
        TextView confirm = (TextView) mDialog.findViewById(R.id.setting_changepwd_confirm);
        TextView cancel = (TextView) mDialog.findViewById(R.id.setting_changepwd_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        mDialog.dismiss();
        }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        String np = newEdit.getText().toString();
        String op = oldEdit.getText().toString();
        if(op.equals(TMConfig.getInstance().getMaintainPass())){
        if(np.equals("")||np == null){
        UIUtils.toast(SettingsFrags.this , false , R.string.data_null);
        }else {
        mDialog.dismiss();
        TMConfig.getInstance().setMaintainPass(np).save();
        UIUtils.toast(SettingsFrags.this , true , R.string.save_success);
        }
        }else {
        newEdit.setText("");
        oldEdit.setText("");
        UIUtils.toast(SettingsFrags.this , false , R.string.original_pass_err);
        }
        }
        });
    }
*/

    private static final String MAP_TV = "MAP_TV" ;
    private static final String MAP_IV = "MAP_IV" ;
    private ArrayList<HashMap<String,Object>> list ;

    private ListAdapter formatAdapter(){
        list = new ArrayList<>();
        String[] names = getResources().getStringArray(R.array.setting_items);
        for (int i = 0 ; i < names.length ; i++){
            HashMap<String,Object> map = new HashMap<>();
            map.put(MAP_TV , names[i]);
            map.put(MAP_IV , IItem.Settings.IMGS[i]);
            list.add(map);
        }
        return new SimpleAdapter(this , list , R.layout.setting_item_view,
                new String[]{MAP_IV , MAP_TV},new int[]{R.id.setting_item_iv,R.id.setting_item_tv});
    }

    private ListAdapter formatAdapter2(){
        list = new ArrayList<>();
        String[] names = getResources().getStringArray(R.array.setting_items2);
        for (int i = 0 ; i < names.length ; i++){
            HashMap<String,Object> map = new HashMap<>();
            map.put(MAP_TV , names[i]);
            map.put(MAP_IV , IItem.Settings.IMGS2[i]);
            list.add(map);
        }
        return new SimpleAdapter(this , list , R.layout.setting_list_item,
                new String[]{MAP_IV , MAP_TV},new int[]{R.id.setting_listitem_iv,R.id.setting_listitem_tv});
    }

    /**
     * método para cambiar el la contraseña actual del administrador, requiere introducir la contraseña actual
     */
    private void changeMaintainPwd(){
        mDialog = UIUtils.centerDialog(this , R.layout.setting_home_pass, R.id.setting_pass_layout);
        final EditText newEdit = (EditText) mDialog.findViewById(R.id.setting_pass_new);
        final EditText oldEdit = (EditText) mDialog.findViewById(R.id.setting_pass_old);
        Button confirm = (Button) mDialog.findViewById(R.id.setting_pass_confirm);
        mDialog.findViewById(R.id.setting_pass_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String np = newEdit.getText().toString();
                String op = oldEdit.getText().toString();
                if(op.equals(TMConfig.getInstance().getMaintainPass())){
                    if(np.equals("")||np == null){
                        Toast.makeText(SettingsFrags.this , getString(R.string.data_null) , Toast.LENGTH_SHORT).show();
                    }else {
                        mDialog.dismiss();
                        TMConfig.getInstance().setMaintainPass(np).save();
                        Toast.makeText(SettingsFrags.this , getString(R.string.save_success) , Toast.LENGTH_SHORT).show();
                    }
                }else {
                    newEdit.setText("");
                    oldEdit.setText("");
                    Toast.makeText(SettingsFrags.this , getString(R.string.original_pass_err) , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
