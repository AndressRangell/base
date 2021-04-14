package cn.desert.newpos.payui.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.android.newpos.dataapi.DataApiImpl;
import com.android.newpos.pay.R;
import com.newpos.libpay.PaySdk;
import com.newpos.libpay.PaySdkException;
import com.newpos.libpay.PaySdkListener;
import com.newpos.libpay.device.pinpad.MasterKeyinfo;
import com.newpos.libpay.device.pinpad.PinpadKeytem;
import com.newpos.libpay.device.pinpad.PinpadKeytype;
import com.newpos.libpay.device.pinpad.PinpadManager;
import com.newpos.libpay.device.pinpad.WorkKeyinfo;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.utils.ISOUtil;
import com.pos.device.ped.Ped;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Creado por zhouqiang el 3/7/2017.
 */

public class PayApplication extends Application {

    private static PayApplication app ;
    private List<Activity> mList = new LinkedList<>();
    public static volatile boolean isInit = false ;
    private static final String APP_RUN = "app_run" ;
    private static final String APP_DEK = "app_des" ;
    private SharedPreferences runPreferences ;
    private SharedPreferences.Editor runEditor ;
    private SharedPreferences dekPreferences ;
    private SharedPreferences.Editor dekEditor ;

    private DataApiImpl dataApi ;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this ;
        initPaysdk();
    }

    private void initPaysdk(){
        //ApplicationCrash.getInstance().init(app);
        try {
            dataApi = DataApiImpl.get(getApplicationContext());
        }catch (Exception e){
            Toast.makeText(this , getString(R.string.data_service) , Toast.LENGTH_SHORT).show();
        }
        runPreferences = getSharedPreferences(APP_RUN , MODE_PRIVATE);
        runEditor = runPreferences.edit() ;
        dekPreferences = getSharedPreferences(APP_DEK , MODE_PRIVATE);
        dekEditor = dekPreferences.edit() ;
        try {
            PaySdk.getInstance().init(app, new PaySdkListener() {
                @Override
                public void success() {
                    isInit = true ;
                    initKeys();
                }
            });
        }catch (PaySdkException e){
            e.printStackTrace();
        }
    }

    public DataApiImpl getDataApi(){
        return dataApi ;
    }

    public static PayApplication getInstance(){
        return app ;
    }

    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    public void exit() {
        isInit = false ;
        PaySdk.getInstance().exit();
        //Pila final
        try {
            for (Activity activity : mList) {
                if (activity != null) {
                    activity.finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
            System.gc();
        }
    }

    public void setRunned(){
        runEditor.clear().commit();
        runEditor.putBoolean(APP_RUN , true).commit();
    }

    public boolean isRunned(){
        return runPreferences.getBoolean(APP_RUN , false) ;
    }

    /**
     * escritorio clásico o escritorio simple
     * @return
     */
    public boolean isClassical(){
        return dekPreferences.getBoolean(APP_DEK , false);
    }

    public void setClassical(boolean classical){
        dekEditor.clear().commit();
        dekEditor.putBoolean(APP_DEK , classical).commit();
    }

    public boolean isAppInstalled(Context context, String uri , boolean b) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }

    /*
        Inicializar claves de terminal. Como Master Key, DUKPT, Work Key, etc.
     */
    private void initKeys() {
        TMConfig cfg = TMConfig.getInstance();

        MasterKeyinfo masterKeyinfo = new MasterKeyinfo();
        masterKeyinfo.setKeySystem(PinpadKeytem.MS_DES);
        masterKeyinfo.setKeyType(PinpadKeytype.KEY_TYPE_MASTK);
        masterKeyinfo.setMasterIndex(cfg.getMasterKeyIndex());
        masterKeyinfo.setPlainKeyData(ISOUtil.str2bcd("11111111111111112222222222222222" , false));
        PinpadManager.loadMKey(masterKeyinfo);

        byte[] keyData = ISOUtil.str2bcd("1CF08008FD62A1E217153829C3A6E51C2A7B0CB84A187EE99C9D002BE1010250792913C4325EA56471657F39F8B3D6562CC515E0403BEB676CCCB22E" , false);
        WorkKeyinfo workKeyinfo = new WorkKeyinfo() ;
        workKeyinfo.setMasterKeyIndex(cfg.getMasterKeyIndex());
        workKeyinfo.setWorkKeyIndex(cfg.getMasterKeyIndex());
        workKeyinfo.setMode(16777216);
        workKeyinfo.setKeySystem(PinpadKeytem.MS_DES);

        byte[] temp;
        int keyLen;
        keyLen = 20 ;

        temp = new byte[keyLen];
        System.arraycopy(keyData, 0, temp, 0, keyLen);
        workKeyinfo.setKeyType(PinpadKeytype.KEY_TYPE_PINK);
        workKeyinfo.setPrivacyKeyData(temp);
        PinpadManager.loadWKey(workKeyinfo);

        System.arraycopy(keyData, keyLen, temp, 0, keyLen);
        workKeyinfo.setKeyType(PinpadKeytype.KEY_TYPE_MACK);
        if(cfg.getStandard() == 1){
            System.arraycopy(temp , 0 , temp , 8 , 8 );
        }
        workKeyinfo.setPrivacyKeyData(temp);
        PinpadManager.loadWKey(workKeyinfo);

        System.arraycopy(keyData, keyLen*2, temp, 0, keyLen);
        workKeyinfo.setKeyType(PinpadKeytype.KEY_TYPE_EAK);
        workKeyinfo.setPrivacyKeyData(temp);
        PinpadManager.loadWKey(workKeyinfo);
        
    }
}
