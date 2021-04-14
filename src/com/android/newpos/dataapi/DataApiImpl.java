package com.android.newpos.dataapi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;

import com.google.gson.Gson;
import com.newpos.libpay.device.printer.PrintRes;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.trans.finace.FinanceTrans;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;

import java.util.List;

/**
 * Creado por zhouqiang el 4/5/2018.
 */

public class DataApiImpl {
    private IDataServiceAidl aidl = null ;

    private static DataApiImpl I = null ;

    public static DataApiImpl get(Context context){
        if(null == I){
            I = new DataApiImpl(context) ;
        }
        return I ;
    }

    private DataApiImpl(Context context){
        Intent intent = new Intent("android.intent.action.NEWPOS_DATA_SERVER_API");
        context.bindService(createExplicitFromImplicitIntent(context, intent),
                conn , Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            aidl = IDataServiceAidl.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public void release(Context context){
        if(conn!=null){
            context.unbindService(conn);
        }
    }

    public void add(TransRecord record){
        try {
            if(aidl != null && record != null){
                Gson gson = new Gson();
                int add = aidl.addTransRecord(gson.toJson(record));
                System.out.println("DATAAPI->add:"+add);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * Android L (lollipop, API 21) introdujo un nuevo problema al intentar invocar una intención implícita,
     * "java.lang.IllegalArgumentException: la intención del servicio debe ser explícita"
     *
     * Si utiliza una intención implícita y sabe que solo 1 objetivo respondería a esta intención,
     * Este método le ayudará a convertir la intención implícita en la forma explícita.
     *
     * Inspirado en la respuesta SO: http://stackoverflow.com/a/26318757/1446466
     * @param context
     * @param implicitIntent - La intención implícita original
     * @return Intención explícita creada a partir de la intención original implícita
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Recuperar todos los servicios que pueden coincidir con la intención dada
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Asegúrese de que solo se haya encontrado una coincidencia
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Obtenga información del componente y cree ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Crea una nueva intención. Use el anterior para extras y tal reutilización.
        Intent explicitIntent = new Intent(implicitIntent);

        // Establecer el componente para que sea explícito
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    /**
     * transferir WftOrder a TransRecord
     * @param wftOrder
     * @return
     */
//    public static TransRecord transfer(WftOrder wftOrder){
//        TransRecord record = new TransRecord();
//        record.setAcquirer(" ");
//        record.setAmount(StringUtil.TwoWei(wftOrder.getAmount()));
//        record.setBatchNumber(" ");
//        record.setCardNumber(wftOrder.getTradeOrder());
//        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        try{
//            Date date = format.parse(wftOrder.getTransTime());
//            record.setDate(new SimpleDateFormat("yyyyMMddHHmmss").format(date));
//        }catch (Exception e){
//            record.setDate(wftOrder.getTransTime());
//        }
//        record.setIsSend(0);
//        record.setMerchantName(wftOrder.getGoodsName());
//        record.setMerchantNumber(wftOrder.getMerchantID());
//        String paytype = wftOrder.getPayType() ;
//        if("微信支付".equals(paytype)){
//            record.setModeOfPayment("Wechat");
//        }else {
//            record.setModeOfPayment("Alipay");
//        }
//        record.setOperatorName(wftOrder.getOperatorID());
//        record.setRefNumber(wftOrder.getReference());
//        String transtype = wftOrder.getTransType();
//        if(Wft.TransType.REFUND.equals(transtype)){
//            record.setRefundMoney(true);
//            record.setTransType(TransType.REFUND.getVaule());
//            record.setTransTypeCn("退款");
//        }else {
//            record.setRefundMoney(false);
//            record.setTransType(TransType.SALE.getVaule());
//            record.setTransTypeCn("付款");
//        }
//        record.setTerminalId(wftOrder.getTerminalID());
//        record.setTraceNumber(wftOrder.getTradeOrder());
//        return record ;
//    }

    /**
     * transferir TransLogData a TransRecord
     * @param logData
     * @return
     */
    public static TransRecord transfer(TransLogData logData){
        TransRecord record = new TransRecord();
        record.setAcquirer(logData.getAcquirerID());
        record.setAmount(PAYUtils.TwoWei(String.valueOf(logData.getAmount())));
        record.setBatchNumber(logData.getBatchNo());
        record.setCardNumber(logData.getPan());
        record.setDate(logData.getLocalDate()+logData.getLocalTime());
        record.setIsSend(0);
        record.setMerchantName(TMConfig.getInstance().getMerchName());
        record.setMerchantNumber(TMConfig.getInstance().getMerchID());
        record.setModeOfPayment("Bankcard");
        record.setOperatorName(TMConfig.getInstance().getOprNo()+"");
        record.setRefNumber(logData.getRRN());
        String ename = logData.getEName() ;
        record.setRefundMoney(false);
        if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[1])){
            record.setTransType(TransType.SALE.getVaule());
            record.setTransTypeCn("consumo");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[2])){
            record.setRefundMoney(true);
            record.setTransType(TransType.VOID.getVaule());
            record.setTransTypeCn("Revocar");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[3])){
            record.setTransType(TransType.EC_BALANCE.getVaule());
            record.setTransTypeCn("Consulta electrónica de saldo de caja");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[4])){
            record.setTransType(TransType.SALE.getVaule());
            record.setTransTypeCn("consumo");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[10])){
            record.setRefundMoney(true);
            record.setTransType(TransType.REFUND.getVaule());
            record.setTransTypeCn("Regreso");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[6])){
            record.setTransType(TransType.AUTH.getVaule());
            record.setTransTypeCn("Preautorización");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[7])){
            record.setTransType(TransType.AUTH_COMP.getVaule());
            record.setTransTypeCn("Autorización previa completada");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[9])){
            record.setRefundMoney(true);
            record.setTransType(TransType.AUTH_VOID.getVaule());
            record.setTransTypeCn("Revocación de preautorización");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[8])){
            record.setRefundMoney(true);
            record.setTransType(TransType.COMP_VOID.getVaule());
            record.setTransTypeCn("Revocación de preautorización completada");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[17])){
            record.setTransType(TransType.SALE.getVaule());
            record.setTransTypeCn("Consumo de código de escaneo");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[19])){
            record.setRefundMoney(true);
            record.setTransType(TransType.REFUND.getVaule());
            record.setTransTypeCn("Escanear código para regresar");
        }else if(ename.equals(PrintRes.STANDRAD_TRANS_TYPE[18])){
            record.setRefundMoney(true);
            record.setTransType(TransType.VOID.getVaule());
            record.setTransTypeCn("Escanear código para cancelar");
        }
        record.setTerminalId(TMConfig.getInstance().getTermID());
        record.setTraceNumber(logData.getTraceNo());
        return record ;
    }
}
