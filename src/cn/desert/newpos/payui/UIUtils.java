package cn.desert.newpos.payui;

import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.newpos.pay.R;
import com.newpos.libpay.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.desert.newpos.payui.master.ResultControl;
import cn.desert.newpos.payui.simple.ADComparator;

/**
 * @author zhouqiang
 * @email wy1376359644@163.com
 */
public class UIUtils {

    /**
     * método para iniciar el activity ResultControl con los datos de respuesta
     * @param activity
     * @param flag
     * @param info
     */
    public static void startResult(Activity activity , boolean flag , String info){
        Intent intent = new Intent();
        intent.setClass(activity , ResultControl.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putBoolean("flag" , flag);
        bundle.putString("info" , info);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.finish();
    }


    /**
     * método para lanzar un toast a una actividad especifica con un mensaje de respuesta
     * @param activity
     * @param content
     */
    public static void toast(Activity activity , boolean flag , int content){
        LayoutInflater inflater_3 = activity.getLayoutInflater();
        View view_3 = inflater_3.inflate(R.layout.app_toast,
                (ViewGroup) activity.findViewById(R.id.toast_layout));
        ImageView face = (ImageView) view_3.findViewById(R.id.app_t_iv);
        if(flag){
            face.setBackgroundResource(R.drawable.icon_face_laugh);
        }else {
            face.setBackgroundResource(R.drawable.icon_face_cry);
        }
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view_3);
        ((TextView)view_3.findViewById(R.id.toast_tv)).
                setText(activity.getResources().getString(content));
        toast.show();
    }

    /**
     * método para lanzar un toast a una actividad especifica con un mensaje de respuesta
     * @param activity
     * @param str
     */
    public static void toast(Activity activity , boolean flag , String str){
        LayoutInflater inflater_3 = activity.getLayoutInflater();
        View view_3 = inflater_3.inflate(R.layout.app_toast,
                (ViewGroup) activity.findViewById(R.id.toast_layout));
        ImageView face = (ImageView) view_3.findViewById(R.id.app_t_iv);
        if(flag){
            face.setBackgroundResource(R.drawable.icon_face_laugh);
        }else {
            face.setBackgroundResource(R.drawable.icon_face_cry);
        }
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view_3);
        ((TextView)view_3.findViewById(R.id.toast_tv)).setText(str);
        toast.show();
    }

    /**
     * método para ejecutar un cuadro de dialogo con un mensaje
     * @param mContext
     * @param resID
     * @param root
     * @return
     */
    public static Dialog centerDialog(Context mContext , int resID , int root){
        final Dialog pd = new Dialog(mContext, R.style.Translucent_Dialog);
        pd.setContentView(resID);
        LinearLayout layout = (LinearLayout) pd.findViewById(root);
        layout.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.up_down));
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(true);
        pd.show();
        return pd ;
    }

    public static String getInputTitle(Context c , int type){
        String str = "" ;
        for (int i = 0 ; i < 5 ; i++){
            if(type == i){
                str = c.getResources().getString(IItem.InputTitle.TITLEs[i]);
                break;
            }
        }
        return str ;
    }

    public static void sendKeyCode(final int keyCode){
        new Thread () {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                }catch(Exception e){
                    Logger.error("Exception when sendPointerSync");
                }
            }
        }.start();
    }

    /**
     * Copie la carpeta de activos a un directorio
     * @param context
     * @param assetDir
     * @param dir
     */
    public static void copyToAssets(Context context, String assetDir, String dir) {

        String[] files;
        try {
            files = context.getResources().getAssets().list(assetDir);
        } catch (IOException e1) {
            return;
        }
        File mWorkingPath = new File(dir);

        if (!mWorkingPath.exists()) {

            if (!mWorkingPath.mkdirs()) {

            }
        }

        for (int i = 0; i < files.length; i++) {
            try {

                String fileName = files[i];

                if (!fileName.contains(".")) {
                    if (0 == assetDir.length()) {
                        copyToAssets(context, fileName, dir + fileName + "/");
                    } else {
                        copyToAssets(context, assetDir + "/" + fileName, dir + "/"
                                + fileName + "/");
                    }
                    continue;
                }

                InputStream in = null;
                try {
                    if (0 != assetDir.length()) {
                        in = context.getAssets().open(assetDir + "/" + fileName);
                    }else {
                        in = context.getAssets().open(fileName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //dijo que este es un directorio
                    if (0 == assetDir.length()) {
                        copyToAssets(context, fileName, dir + fileName + "/");
                    } else {
                        copyToAssets(context, assetDir + "/" + fileName, dir + "/"
                                + fileName + "/");
                    }
                    continue;
                }

                File outFile = new File(mWorkingPath, fileName);
                if (outFile.exists()) {
                    outFile.delete();
                }
                FileOutputStream out = new FileOutputStream(outFile);

                // Transferir bytes de adentro hacia afuera
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                out.flush();
                out.getFD().sync();
                out.close();
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * método para decodificar mapa de bits obteniendo la información desde un archivo
     * @param pathName
     * @param reqWidth px
     * @param reqHeight px
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName,
                                                     int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight, options.inSampleSize);
    }

    /**
     * método para calcular el tamaño de muestra de un mapa de bits
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * método para crear mapa de bits de escala
     * @param src
     * @param dstWidth
     * @param dstHeight
     * @param inSampleSize
     * @return
     */
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth,
                                            int dstHeight, int inSampleSize) {

        if (inSampleSize == 1) {
            return src;
        }

        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) {
            src.recycle();
        }
        return dst;
    }

    /**
     * Acceso a publicidad
     * @param dir
     * @return
     */
    public static List<String> getAds(String dir) {
        List<String> adList = new ArrayList<>();
        File adRoot = new File(dir);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String now = sdf.format(new Date());

        File[] adDirs = adRoot.listFiles();
        if (adDirs == null) {
            return adList;
        }
        for (File adDir : adDirs) {
            Log.d("ad" , "ad list files = "+adDir.getAbsolutePath());
            if (adDir.isDirectory()) {
                String dirName = adDir.getName();
                String[] infos = dirName.split("-");
                if (infos.length != 3) {
                    continue;
                } else if (now.compareTo(infos[2]) > 0) {
                    delete(adDir);
                } else if (now.compareTo(infos[1]) > 0) {
                    for (File ad : adDir.listFiles()) {
                        adList.add(ad.getAbsolutePath());
                    }
                }
            }
        }
        // Si no hay anuncios enviados por el servidor, muestre el anuncio predeterminado
        if(adList.size() == 0) {
            for (File adFile : adDirs) {
                if (adFile.getName().contains(".json")) {
                    continue;
                }
                adList.add(adFile.getAbsolutePath());
            }
        }
        Collections.sort(adList, new ADComparator());
        Log.v("ad","adList:" + adList);
        return adList;
    }

    /**
     * eliminar archivo o directorio
     * @param file
     */
    public static void delete(File file) {
        if (!file.exists()){
            return;
        }

        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    /**
     * obtener cadena por ID de recurso
     * @param context contexto
     * @param resid ID del recurso
     * @return
     */
    public static String getStringByInt(Context context, int resid) {
        String sAgeFormat1 = context.getResources().getString(resid);
        return sAgeFormat1;
    }

    /**
     * obtener cadena por ID de recurso
     * @param context
     * @param resid
     * @param parm
     * @return
     */
    public static String getStringByInt(Context context, int resid, String parm) {
        String sAgeFormat1 = context.getResources().getString(resid);
        String sFinal1 = String.format(sAgeFormat1, parm);
        return sFinal1;
    }

    /**
     * obtener cadena por ID de recurso
     * @param context
     * @param resid
     * @param parm1
     * @param parm2
     * @return
     */
    public static String getStringByInt(Context context, int resid,
                                        String parm1, String parm2) {
        String sAgeFormat1 = context.getResources().getString(resid);
        String sFinal1 = String.format(sAgeFormat1, parm1, parm2);
        return sFinal1;
    }
}
