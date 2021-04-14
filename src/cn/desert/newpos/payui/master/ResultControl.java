package cn.desert.newpos.payui.master;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.newpos.pay.R;
import com.newpos.libpay.utils.PAYUtils;

import java.util.Timer;
import java.util.TimerTask;

import cn.desert.newpos.payui.base.BaseActivity;
import cn.desert.newpos.payui.base.NavigationConfig;

/**
 * Creado por zhouqiang el 12/11/2016.
 */
public class ResultControl extends BaseActivity {
    Button confirm ;
    TextView details ;
    ImageView face ;

    private Timer timer = new Timer() ;

    private String info = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trans_result);
        setNaviTitle(R.string.trans_result);
        setRightVisiblity(View.GONE);
        confirm = (Button) findViewById(R.id.result_confirm);
        details = (TextView) findViewById(R.id.result_details);
        face = (ImageView) findViewById(R.id.result_img);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                over();
            }
        } , 5*1000);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                over();
            }
        });
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            displayDetails(bundle.getBoolean("flag") ,
                    bundle.getString("info"));
        }

        PAYUtils.detectICC(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer!=null){
            timer.cancel();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            over();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void over(){
        finish();
    }

    private void displayDetails(boolean flag , String info){
        this.info = info ;
        details.setText(info);
        if(flag){
            face.setImageResource(R.drawable.result_success);
            details.setTextColor(Color.parseColor("#333333"));
        }else {
            face.setImageResource(R.drawable.result_fail);
            details.setTextColor(Color.parseColor("#f54d4f"));
        }
    }
}
