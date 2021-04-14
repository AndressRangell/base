package cn.desert.newpos.payui.base;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;

import com.newpos.libpay.Logger;
import com.newpos.libpay.PaySdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Creado por zhouqiang el 3/7/2017.
 * Supervisión de fallos inesperados de la aplicación
 */

public class ApplicationCrash implements Thread.UncaughtExceptionHandler {

    /**
     * Objeto ApplicationCrash
     */
    private static ApplicationCrash INSTANCE = new ApplicationCrash();

    /**
     * Contexto de aplicación
     */
    private Context mContext;

    /**
     * el objeto UncaughtExceptionHandler predeterminado del sistema
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    /**
     * Se usa para almacenar información del dispositivo e información anormal
     */
    private Map<String, String> infos = new HashMap<>();

    /**
     * Se utiliza para la fecha de formato como parte del nombre del archivo de registro.
     */
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /** Patrón de diseño singleton */
    private ApplicationCrash() {
    }

    /** Patrón de diseño singleton */
    public static ApplicationCrash getInstance() {
        return INSTANCE;
    }

    /**
     * inicializar
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        // obtener el objeto UncaughtExceptionHandler predeterminado del sistema
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // establecer el controlador de fallos predeterminado de la aplicación
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * atrapar UncaughtException
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // salir de la aplicación
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            System.gc();
        }
    }

    /**
     * El manejo de errores personalizado, la recopilación de errores, el informe de errores y otras operaciones se realizan aquí
     * @param ex
     * @return verdadero: si está hecho; de lo contrario: falso
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "Application terminated", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PaySdk.getInstance().releaseCard();

        // recopilar información del dispositivo
        collectDeviceInfo(mContext);
        // Guardar registro
        saveCrashInfo2File(ex);
        //salir de la aplicación
        ((PayApplication) mContext).exit();
        return true;
    }

    /**
     * recopilar información del dispositivo
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);

            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.error("an error happened when collect package info:"+ e.toString());
        }

        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Logger.debug("Build.class.getDeclaredFields = "+field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Logger.error("an error happened when collect crash info:"+e.toString());
            }
        }
    }

    /**
     * guardar registro de errores
     * @param ex
     * @return nombre de archivo
     */
    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();

        String result = writer.toString();
        sb.append(result);
        try {
            String time = formatter.format(new Date());
            String fileName = "err-" + time + ".newpos";
            String path = PaySdk.getInstance().getCacheFilePath() + "errlog/";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(path + fileName);
            fos.write(sb.toString().getBytes());
            fos.close();
            return fileName;
        } catch (Exception e) {
            Logger.error("an error happened while writing file:"+ e.toString());
        }
        return null;
    }
}
