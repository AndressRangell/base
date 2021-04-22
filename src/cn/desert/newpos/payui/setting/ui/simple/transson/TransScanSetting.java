package cn.desert.newpos.payui.setting.ui.simple.transson;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.newpos.pay.R;
import com.newpos.libpay.global.TMConfig;


import cn.desert.newpos.payui.base.BaseActivity;

/**
 * Creado por zhouqiang el 15/11/2017.
 * @author zhouqiang
 */
public class TransScanSetting extends BaseActivity {
    ImageView beep ;
    ImageView light ;
    Button location ;

    private TMConfig config ;
    private boolean isBeep ;
    private boolean isLight ;
    private boolean isBack ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_home_trans_scan);
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
     * método para modificar configuración en la vista merchant
     */
    private void initData(){
        beep = (ImageView) findViewById(R.id.scan_beep);
        light = (ImageView) findViewById(R.id.scan_light);
        location = (Button) findViewById(R.id.scan_location);
        setCameraSwitch(config.isScanBack());
        setBeepSwitch(config.isScanBeeper());
        setLightSwitch(config.isScanTorchOn());
        beep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBeepSwitch(!isBeep);
            }
        });
        light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLightSwitch(!isLight);
            }
        });
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCameraSwitch(!isBack);
            }
        });
    }

    /**
     * método para cambiar el estado del interruptor de pitido (activo:inactivo)
     * @param is
     */
    public void setBeepSwitch(boolean is){
        isBeep = is ;
        if(is){
            beep.setImageResource(R.drawable.home2_setting_commun_open);
        }else {
            beep.setImageResource(R.drawable.home2_setting_commun_close);
        }
    }

    /**
     * método para cambiar el estado del interruptor de luz (activo:inactivo)
     * @param is
     */
    public void setLightSwitch(boolean is){
        isLight = is ;
        if(is){
            light.setImageResource(R.drawable.home2_setting_commun_open);
        }else {
            light.setImageResource(R.drawable.home2_setting_commun_close);
        }
    }

    /**
     * método para cambiar el estado del interruptor de luz (activo:inactivo)
     * @param isback
     */
    public void setCameraSwitch(boolean isback){
        isBack = isback ;
        if(isback){
            location.setText(R.string.back_camera);
        }else {
            location.setText(R.string.front_camera);
        }
    }

    private void save(){
        config.setScanBack(isBack)
                .setScanBeeper(isBeep)
                .setScanTorchOn(isLight)
                .save();
        finish();
    }
}
