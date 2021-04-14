package cn.desert.newpos.payui.setting.ui.simple;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.newpos.pay.R;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.utils.PAYUtils;

import cn.desert.newpos.payui.base.BaseActivity;
import cn.desert.newpos.payui.setting.ui.SettingsFrags;
import cn.desert.newpos.payui.setting.view.IPEditText;

/**
 * Creado por zhouqiang el 15/11/2017.
 */
public class CommunSettings extends BaseActivity {
    EditText commun_timeout ;
    ImageView commun_public ;
    IPEditText commun_pub_ip ;
    EditText commun_pub_port ;
    IPEditText commun_inner_ip ;
    EditText commun_inner_port ;

    private TMConfig config ;
    private boolean isOpen ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_home_commun);
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
        commun_timeout = (EditText) findViewById(R.id.setting_com_timeout);
        commun_public = (ImageView) findViewById(R.id.setting_com_public);
        commun_pub_ip = (IPEditText) findViewById(R.id.setting_com_public_ip);
        commun_pub_port = (EditText) findViewById(R.id.setting_com_public_port);
        commun_inner_ip = (IPEditText) findViewById(R.id.setting_com_inner_ip);
        commun_inner_port = (EditText) findViewById(R.id.setting_com_inner_port);
        commun_timeout.setText(String.valueOf(config.getTimeout()/1000));
        setPubSwitch(config.getPubCommun());
        commun_pub_ip.setIPText(config.getIp().split("\\."));
        commun_pub_port.setText(config.getPort());
        commun_inner_ip.setIPText(config.getIP2().split("\\."));
        commun_inner_port.setText(config.getPort2());
        commun_public.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPubSwitch(!isOpen);
            }
        });
    }

    private void setPubSwitch(boolean is){
        isOpen = is ;
        if(is){
            commun_public.setImageResource(R.drawable.home2_setting_commun_open);
        }else {
            commun_public.setImageResource(R.drawable.home2_setting_commun_close);
        }
    }

    private void save(){
        String ip = commun_pub_ip.getIPText() ;
        String port = commun_pub_port.getText().toString() ;
        String ip2 = commun_inner_ip.getIPText() ;
        String port2 = commun_inner_port.getText().toString() ;
        String timeout = commun_timeout.getText().toString();
        if(PAYUtils.isNullWithTrim(ip)||
                PAYUtils.isNullWithTrim(port)||
                PAYUtils.isNullWithTrim(ip2)||
                PAYUtils.isNullWithTrim(port2)||
                PAYUtils.isNullWithTrim(timeout)){
            Toast.makeText(this , getString(R.string.data_null) ,Toast.LENGTH_SHORT).show();
            return;
        }
        config.setIp(ip)
              .setIp2(ip2)
              .setPort(port)
              .setPort2(port2)
              .setTimeout(Integer.parseInt(timeout)*1000)
              .setPubCommun(isOpen)
              .save();
        finish();
    }
}
