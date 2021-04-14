package com.newpos.libpay.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;

import com.android.newpos.libemv.PBOCManager;
import com.android.newpos.pay.R;
import com.newpos.libpay.Logger;
import com.newpos.libpay.PaySdk;
import com.newpos.libpay.PaySdkException;
import com.newpos.libpay.device.printer.PrintRes;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.global.TMConstants;
import com.newpos.libpay.paras.EmvAidInfo;
import com.newpos.libpay.paras.EmvCapkInfo;
import com.pos.device.SDKException;
import com.pos.device.beeper.Beeper;
import com.pos.device.emv.EMVHandler;
import com.pos.device.emv.IEMVHandler;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.SlotType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by zhouqiang on 2017/6/30.
 * @author zhouqiang
 * Pay Util
 */

public class PAYUtils {

    /**
     * campo 55 datos de la secuencia de comandos del emisor de la transacción de venta
     */
    public static final int wISR_tags[] = { 0x9F33, // Capacidades terminales
            0x95, // TVR
            0x9F37, // Número impredecible
            0x9F1E, // Número de serie IFD
            0x9F10, // Datos de solicitud de emisor
            0x9F26, // Criptograma de aplicación
            0x9F36, // Contador de transacciones de la aplicación
            0x82, // AIP
            0xDF31, // resultado del script del emisor
            0x9F1A, // Código de país del terminal
            0x9A, // Fecha de Transacción
            0 };

    /**
     * campo 55 datos de la transacción de venta
     */
    public static final int wOnlineTags[] = { 0x9F26, // AC (Criptograma de aplicación)
            0x9F27, // CID
            0x9F10, // IAD (Datos de solicitud de emisor)
            0x9F37, // Número impredecible
            0x9F36, // ATC (Contador de transacciones de la aplicación)
            0x95, // TVR
            0x9A, // Fecha de Transacción
            0x9C, // tipo de transacción
            0x9F02, // Monto autorizado
            0x5F2A, // Código de moneda de transacción
            0x82, // AIP
            0x9F1A, // Código de país del terminal
            0x9F03, // otra cantidad
            0x9F33, // Capacidades terminales
            // opt
            0x9F34, // CVM resultado
            0x9F35, // tipo de terminal
            0x9F1E, // IFD numero de serie
            0x84, // Nombre de archivo dedicado
            0x9F09, // Versión de la aplicación #
            0x9F41, // Contador de secuencia de transacciones
            0x4F,

            0x5F34, // PAN numero de secuencia
            0x50,//App label
            0 };
    // 0X8E, //CVM

    /** reversal emv tag **/
    public static final int reversal_tag[] = { 0x95, // TVR
            0x9F1E, // IFD numero de serie
            0x9F10, // Datos de solicitud de emisor
            0x9F36, // Contador de transacciones de la aplicación
            0xDF31, // resultado del script del emisor
            0 };

    /**
     * file to object
     * @param fileName file path
     * @return Object
     */
    public static Object file2Object(String fileName) throws IOException,ClassNotFoundException {
        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        }
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis) ;
        Object object = ois.readObject();
        if (fis != null) {
            fis.close();
        }
        if (ois != null) {
            ois.close();
        }
        return object;
    }

    /**
     * object to file
     * @param obj object
     * @param outputFile file path
     */
    public static void object2File(Object obj, String outputFile) throws IOException {
        File dir = new File(outputFile);
        if (!dir.exists()) {
            // crear archivo
            dir.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(dir) ;
        ObjectOutputStream oos = new ObjectOutputStream(fos) ;
		//fos = context.openFileOutput(dir.getName() , Context.MODE_WORLD_READABLE);
        oos.writeObject(obj);
        oos.flush();
        fos.getFD().sync();
        if (oos != null) {
            oos.close();
        }
        if (fos != null) {
            fos.close();
        }
    }

    /**
     * obtener el archivo de propiedades de los activos
     * @param context contexto
     * @param fildName nombre de archivo de propiedades
     * @return propiedades
     */
    public static Properties lodeConfig(Context context, String fildName) {
        Properties prop = new Properties();
        try {
            prop.load(context.getResources().getAssets().open(fildName));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return prop;
    }

    /**
     * obtener el archivo de propiedades de los activos
     * @param context contexto
     * @param fildName nombre del archivo
     * @param name nombre de la propiedad
     * @return valor del nombre de las propiedades
     */
    public static String lodeConfig(Context context, String fildName, String name) {
        Properties pro = new Properties();
        try {
            pro.load(context.getResources().getAssets().open(fildName));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return null;
        }
        return (String) pro.get(name);
    }

    /**
     * obtener el nombre del banco por código
     * @param mContext contexto
     * @param code codigo del banco
     * @return nombre del banco
     */
    public static String getBankName(Context mContext , String code) {
        Properties pro = lodeConfig(mContext, "bankcodelist.properties");
        if (pro == null) {
            System.out.println("bankcodelist.properties error");
            return null;
        }
        String bname ;
        try {
            if (!isNullWithTrim(pro.getProperty(code))) {
                bname = new String(pro.getProperty(code).getBytes("ISO-8859-1"), "utf-8");
            }else {
                return code;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return code;
        }
        return bname;
    }

    /**
     * obtener información de error por código de respuesta
     * @param mcontext contexto
     * @param code código de respuesta
     * @return informacion de error
     */
    public static String getRspCode(Context mcontext, String code) {
        String tiptitle ;
        String tipcontent ;
        Properties pro = lodeConfig(mcontext, "props/rspcode.properties");
        if (pro == null) {
            System.out.println("rspcode.properties error");
            return null;
        }
        try {
            String prop = pro.getProperty(code);
            String[] propGroup = prop.split(",");
            if (!isNullWithTrim(propGroup[0])) {
                tiptitle = new String(propGroup[0].trim().getBytes("ISO-8859-1"), "utf-8");
            }else {
                tiptitle = code;
            }
            if (!isNullWithTrim(propGroup[1])) {
                tipcontent = new String(propGroup[1].trim().getBytes("ISO-8859-1"), "utf-8");
            }else {
                tipcontent = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return code;
        }
        return tiptitle + "\n" + tipcontent;
    }


    public static boolean copyAssetsToData(Context context , String fileName) {
        try {
            AssetManager as = context.getAssets();
            InputStream ins = as.open(fileName);
            String dstFilePath = context.getFilesDir().getAbsolutePath() + "/" + fileName;
            OutputStream outs = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
            byte[] data = new byte[1 << 20];
            int length = ins.read(data);
            outs.write(data, 0, length);
            ins.close();
            outs.flush();
            outs.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static String[] getProps(Context c , String name, String proName) {
        Properties pro = new Properties();
        try {
            pro.load(c.getResources().getAssets().open(name));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return null;
        }
        String prop = pro.getProperty(proName);
        if (prop == null) {
            return null;
        }
        String[] results = prop.split(",") ;
        for (int i = 0 ; i < results.length ; i++){
            try {
                results[i] = new String(results[i].trim().getBytes("ISO-8859-1"), "utf-8");
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }
        return results;
    }


    public static Bitmap getImageFromAssetsFile(Context context, String path) {
        //Obtenga recursos bajo activos
        Bitmap image = null ;
        try {
            //La imagen se coloca en la carpeta img.
            InputStream is = context.getAssets().open(path);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    public static Bitmap getLogoByBankId(Context context , int bankId){
        return getImageFromAssetsFile(context , TMConstants.BANKID.ASSETS[bankId]);
    }

    /**
     * obtener la red en tiempo real
     * @return tiempo
     * @throws Exception
     */
    public static String getNetworkTime() throws Exception {
        URL url = new URL("http://www.bjtime.cn");
        URLConnection uc = url.openConnection();
        uc.connect();
        long ld = uc.getDate();
        Date date = new Date(ld);

        return date.getYear()+"-"+date.getMonth()+"-"+date.getDay()+"  "+
                date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
    }

    /**
     * obtener el tipo de acceso a la red actual
     * @param context contexto
     * @return
     * -1：sin red ;
     * 1：WIFI;
     * 2：wap ;
     * 3：net
     */
    public static int getNetype(Context context){
        int netType = -1;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if( nType == ConnectivityManager.TYPE_MOBILE) {
            if(networkInfo.getExtraInfo().toLowerCase().equals("cmnet")){
                netType = 3;
            }else{
                netType = 2;
            }
        }else if(nType== ConnectivityManager.TYPE_WIFI){
            netType = 1;
        }
        return netType ;
    }

    /**
     * obtener la hora actual del sistema
     * @return YYYYMMDDHHmmss
     */
    public static String getSysTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    /**
     * obtener HHmmss del sistema
     * @return HHmmss
     */
    public static String getHMS(){
        Calendar calendar = Calendar.getInstance() ;
        return str2int(calendar.get(Calendar.HOUR_OF_DAY))+
                str2int(calendar.get(Calendar.MINUTE))+
                str2int(calendar.get(Calendar.SECOND)) ;
    }

    /**
     * obtener AAAAMMDD del sistema
     * @return YYYYMMDD
     */
    public static String getYMD(){
        Calendar calendar = Calendar.getInstance() ;
        return str2int(calendar.get(Calendar.YEAR))+
                str2int(calendar.get(Calendar.MONTH))+
                str2int(calendar.get(Calendar.SECOND)) ;
    }


    public static String str2int(int date){
        String temp = String.valueOf(date) ;
        if(temp.length() == 1){
            return "0"+temp ;
        }
        return temp;
    }

    /**
     *
     * @param str
     * @param format
     * @return
     */
    public static String strToDateFormat(String str, String format) {
        return DateToStr(StrToDate(str), format);
    }

    /**
     * Cadena hasta la fecha
     * @param str //yyyy-MM-dd HH:mm:ss
     * @return date
     */
    public static Date StrToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * Cadena hasta la fecha
     * @param str //yyyy-MM-dd HH:mm:ss
     * @return date
     */
    public static Date StrToDate(String str, String formatString) {
        SimpleDateFormat format = new SimpleDateFormat(formatString);// "yyyy-MM-dd HH:mm:ss"
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public static String DateToStr(Date date, String formatString) {
        String str = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatString);// formatString
            str = format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }


    public static int getYear(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        return year;
    }


    public static String printStr(String date, String time){
        String newdate = "";
        if(!isNullWithTrim(date)&&!isNullWithTrim(time)){
            if(time.length() == 5){
                newdate = date.substring(0,4)+"/"+date.substring(4,6)+"/"+date.substring(6,8)+"  "
                        +"0"+time.substring(0,1)+":"+time.substring(1,3) ;
            }else {
                newdate = date.substring(0,4)+"/"+date.substring(4,6)+"/"+date.substring(6,8)+"  "
                        +time.substring(0,2)+":"+time.substring(2,4) ;
            }
            return newdate ;
        }return "    " ;

//			if(date.length()==8&&time.length()==6){
//				newdate=date.substring(0,4)+"/"
//						+date.substring(4,6)+"/"
//						+date.substring(6,8)+" "
//						+time.substring(0,2)+":"
//						+time.substring(2,4)+":"
//						+time.substring(4,6);
//			}

    }


    public static boolean isNullWithTrim(String str) {
        return str == null || str.trim().equals("")||str.trim().equals("null");
    }

    public static String getStatusInfo(String status){
        try {
            String[] infos = Locale.getDefault().getLanguage().equals("zh")?
                    PAYUtils.getProps(PaySdk.getInstance().getContext(), TMConstants.CODE, status):
                    PAYUtils.getProps(PaySdk.getInstance().getContext(), TMConstants.CODE_EN, status);
            if(infos!=null){
                return infos[0];
            }
        }catch (PaySdkException pse){
            pse.printStackTrace();
        }
        if(Locale.getDefault().getLanguage().equals("zh")){
            return "Información desconocida" ;
        }else {
            return "Unknown error" ;
        }
    }

    public static String getSecurityNum(String cardNo, int prefix, int suffix) {
        StringBuffer cardNoBuffer = new StringBuffer();
        int len = prefix + suffix;
        if ( cardNo.length() > len) {
            cardNoBuffer.append(cardNo.substring(0, prefix));
            for (int i = 0; i < cardNo.length() - len; i++) {
                cardNoBuffer.append("*");
            }
            cardNoBuffer.append(cardNo.substring(cardNo.length() - suffix, cardNo.length()));
        }
        return cardNoBuffer.toString();
    }


    public static String TwoWei(double s){
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(s);
    }


    public static String TwoWei(String amount){
        DecimalFormat df = new DecimalFormat("0.00");
        double d = 0;
        if(!isNullWithTrim(amount)) {
            d = Double.parseDouble(amount) / 100;
        }
        return df.format(d);
    }

    /**
     * 20160607152954 --> 2016-06-07 15:29:54
     * @param date   20160607152954
     * @param oldPattern  yyyyMMddHHmmss
     * @param newPattern yyyy-MM-dd HH:mm:ss
     * @return 2016-06-07 15:29:54
     */
    public static String StringPattern(String date, String oldPattern,
                                       String newPattern) {
        if (date == null || oldPattern == null || newPattern == null) {
            return "";
        }
        SimpleDateFormat sdf1 = new SimpleDateFormat(oldPattern);
        SimpleDateFormat sdf2 = new SimpleDateFormat(newPattern);
        Date d = null;
        try {
            d = sdf1.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sdf2.format(d);
    }


    public static int Object2Int(Object obj) {
        return Integer.parseInt((String) obj);
    }


    public static String getBankInfo(Context c , String bankcode) {
        Properties pro = lodeConfig(c, TMConstants.BANKNAME);
        try {
            return new String(pro.getProperty(
                    ISOUtil.padright(bankcode, 8, '0')).getBytes("ISO-8859-1"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * comprimir imagen
     * @param c contexto
     * @param rid ID del recurso
     * @return Objeto de mapa de bits
     */
    public static Bitmap compress(Context c , int rid){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.decodeResource(c.getResources(), rid).compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return BitmapFactory.decodeByteArray(bytes , 0 , bytes.length);
    }

    /**
     *  cambio de objeto dibujable a mapa de bits
     * @param drawable
     * @return
     */
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }


    public static String getStrAmount(long Amount) {
        double f1 = Double.valueOf(Amount + "");
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(f1 / 100);
    }

    /**
     * Convierte la dp a px
     */
    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }


    /**
     * Convierte la sp a px
     */
    public static int sp2px(Context context, int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                context.getResources().getDisplayMetrics());
    }


    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * obtener TLV
     * @param src
     * @param totalLen
     * @param tag
     * @param value
     * @param withTL
     * @return
     */
    public static int get_tlv_data(byte[] src, int totalLen, int tag,
                                   byte[] value, boolean withTL) {
        int i, Tag, Len;
        int T;

        if (totalLen == 0) {
            return 0;
        }

        i = 0;
        while (i < totalLen) {
            T = i;

            if ((src[i] & 0x1f) == 0x1f) {
                Tag = ISOUtil.byte2int(src, i, 2);
                i += 2;
            } else {
                Tag = ISOUtil.byte2int(new byte[] { src[i++] });
            }

            Len = ISOUtil.byte2int(new byte[] { src[i++] });
            if ((Len & (byte) 0x80) != 0) {
                int lenL = Len & 3;
                Len = ISOUtil.byte2int(src, i, lenL);
                i += lenL;
            }

            if (tag == Tag) {
                //contain Tag and Len
                if (withTL) {
                    Len = Len + (i - T);
                    System.arraycopy(src, T, value, 0, Len);
                    return Len;
                //not contain tag and len
                } else {
                    System.arraycopy(src, i, value, 0, Len);
                    return Len;
                }
            } else {
                i += Len;
            }
        }
        return 0;
    }

    /**
     * obtener datos de TLV por etiqueta
     * @param iTag etiqueta de TLV
     * @param data valor de TLV
     * @return len of TLV
     */
    public static int get_tlv_data_kernal(int iTag, byte[] data) {
        IEMVHandler handler = EMVHandler.getInstance();
        int len = 0;
        byte[] Tag ;
        if (iTag < 0x100) {
            Tag = new byte[1];
            Tag[0] = (byte) iTag;
        } else {
            Tag = new byte[2];
            Tag[0] = (byte) (iTag >> 8);
            Tag[1] = (byte) iTag;
        }
        Logger.debug("Tag = "+ ISOUtil.hexString(Tag));
        if (handler.checkDataElement(Tag) == 0) {
            try {
                byte[] result = handler.getDataElement(Tag);
                Logger.debug("get_tlv_data_kernal result = "+ ISOUtil.hexString(result));
                System.arraycopy(result , 0 , data , 0 , result.length);
                len = result.length ;
            } catch (SDKException e) {
                e.printStackTrace();
            }
        //issuer script result
        } else if (iTag == 0xDF31) {
            byte[] result = handler.getScriptResult() ;
            if(result!=null){
                System.arraycopy(result , 0 , data , 0 , result.length);
                len = result.length ;
            }
        }
        return len;
    }

    /**
     * paquete TLV cadena por algunas etiquetas
     * @param iTags
     * @param dest
     * @return
     */
    public static int pack_tags(int[] iTags, byte[] dest) {
        int i, iTag_len, len;
        byte[] Tag = new byte[2];
        int offset = 0;
        byte[] ptr = new byte[256];

        i = 0;
        while (iTags[i] != 0) {

            if (iTags[i] < 0x100) {
                iTag_len = 1;
                Tag[0] = (byte) iTags[i];
            } else {
                iTag_len = 2;
                Tag[0] = (byte) (iTags[i] >> 8);
                Tag[1] = (byte) iTags[i];
            }

            len = get_tlv_data_kernal(iTags[i], ptr);
            if (len > 0) {
                System.arraycopy(Tag, 0, dest, offset, iTag_len);
                offset += iTag_len;

                if (len < 128) {
                    dest[offset++] = (byte) len;
                } else if (len < 256) {
                    dest[offset++] = (byte) 0x81;
                    dest[offset++] = (byte) len;
                }

                System.arraycopy(ptr, 0, dest, offset, len);
                offset += len;
            }

            i++;
        }
        return offset;
    }

    /**
     * pack a _tlv data
     *
     * @param result
     *            out
     * @param tag
     * @param len
     * @param value
     *            in
     * @return
     */
    public static int pack_tlv_data(byte[] result, int tag, int len,
                                    byte[] value, int valueOffset) {
        byte[] temp = null;
        int offset = 0;

        if (len == 0 || value == null || result == null) {
            return 0;
        }

        temp = result;
        if (tag > 0xff) {
            temp[offset++] = (byte) (tag >> 8);
            temp[offset++] = (byte) tag;
        } else {
            temp[offset++] = (byte) tag;
        }

        if (len < 128) {
            temp[offset++] = (byte) len;
        } else if (len < 256) {
            temp[offset++] = (byte) 0x81;
            temp[offset++] = (byte) len;
        } else {
            temp[offset++] = (byte) 0x82;
            temp[offset++] = (byte) (len >> 8);
            temp[offset++] = (byte) len;
        }
        System.arraycopy(value, valueOffset, temp, offset, len);

        return offset + len;
    }

    /**
     * obtener una tarjeta emitida por AID
     * @param rid
     * @return
     */
    public static String getIssureByRid(String rid) {
        String cardCode = null;
        if (rid.length() < 10) {
            return "CUP";
        }
        if (rid.length() > 10) {
            cardCode = rid.substring(0, 10);
        } else {
            cardCode = rid;
        }

        if (cardCode.equals("A000000003")) {
            return "VIS";
        }
        if (cardCode.equals("A000000004")) {
            return "MCC";
        }
        if (cardCode.equals("A000000065")) {
            return "JCB";
        }
        if (cardCode.equals("A000000025")) {
            return "AEX";
        }
        return "CUP";
    }

    /**
     * HHmmss
     * @return
     */
    public static String getLocalTime() {
        return DateToStr(new Date(), "HHmmss");
    }

    /**
     * MMdd
     * @return
     */
    public static String getLocalDate() {
        return DateToStr(new Date(), "MMdd");
    }

    /**
     * YYMM
     * @return
     */
    public static String getExpDate(){
        return DateToStr(new Date(),"YYMM");
    }

    /**
     * Play voice
     * @param rid
     */
    public static void playVoice(Context context , int rid){
        MediaPlayer player = MediaPlayer.create(context , rid);
        player.start();
    }

    /**
     * activar voz
     * @param voice file path
     */
    public static void platVoice(String voice){
        try {
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(voice);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Add by Andy Yuan. detect IC card after finished transaction
    public static void detectICC(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean playVoice = true;
                while (IccReader.getInstance(SlotType.USER_CARD).isCardPresent()){
                    if( playVoice && TMConfig.getInstance().isVocie() ) {
                        PAYUtils.playVoice(context, R.raw.retrieve_card);
                        playVoice = false;
                    }
                    try {
                        Beeper.getInstance().beep(1000, 500);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static String formatTranstype(String type){

        if(!Locale.getDefault().getLanguage().equals("zh")){
            return type;
        }

        for (int i = 0; i < PrintRes.STANDRAD_TRANS_TYPE.length ; i++){
            if(PrintRes.TRANS[i].equals(type)){
                return PrintRes.TRANS[i]+"("+type+")";
            }
        }

        return null;
    }

    /**
     * Método de relleno a la izquierda
     * @param strSrc
     * @param len
     * @param pad
     * @return
     */
    public static String paddingLeft(String strSrc, int len, String pad){
        while(strSrc.length()<len){
            strSrc=pad+strSrc;
        }
        return strSrc;
    }

    /**
     * Método de llenado a la derecha
     * @param strSrc
     * @param len
     * @param pad
     * @return
     */
    public static String paddingRight(String strSrc, int len, String pad){
        while(strSrc.length()<len){
            strSrc=strSrc+pad;
        }
        return strSrc;
    }
}
