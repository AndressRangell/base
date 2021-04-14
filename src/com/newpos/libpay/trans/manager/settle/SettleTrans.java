package com.newpos.libpay.trans.manager.settle;

import android.content.Context;

import com.newpos.libpay.Logger;
import com.newpos.libpay.device.printer.PrintManager;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Type;
import com.newpos.libpay.trans.finace.ServiceEntryMode;
import com.newpos.libpay.trans.manager.ManageTrans;
import com.newpos.libpay.trans.manager.logout.LogoutTrans;
import com.newpos.libpay.trans.manager.reversal.RevesalTrans;
import com.newpos.libpay.trans.manager.script.ScriptTrans;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.printer.PrintTask;
import com.pos.device.printer.Printer;

import java.util.List;

/**
 * Created by zhouqiang on 2017/3/31.
 * @author zhouqiang
 * transacción de liquidación
 */
public class SettleTrans extends ManageTrans implements TransPresenter{

    private int sumCount = 0 ;

    public SettleTrans(Context ctx, String transEname , TransInterface tt) {
        super(ctx, transEname , tt);
        iso8583.setHasMac(false);
    }

    @Override
    public ISO8583 getISO8583() {
        return iso8583;
    }

    @Override
    public void start() {
        if(TransLog.getInstance().getSize()<=0){
            transInterface.showError(false , Tcode.BATCH_NO_TRANS);
            return;
        }
        if(!cfg.isOnline()){
            startLocalPresentations();
            return;
        }
        transInterface.handling(Tcode.SEND_SETTLE_NOTICE);
        setTraceNoInc(true);
        if(MsgID!=null) {
            iso8583.setField(0, MsgID);
        }
        iso8583.setField(11 , cfg.getTraceNo());
        iso8583.setField(41 , cfg.getTermID());
        iso8583.setField(42 , cfg.getMerchID());
        iso8583.setField(49 , cfg.getCurrencyCode());
        Logger.debug("SettleTrans>>settle>>Field60 = "+Field60);
        iso8583.setField(60 , Field60);
        iso8583.setField(63 , formatOPN(String.valueOf(cfg.getOprNo())));
        int retVal = getSettleSUM48();
        if(retVal!=0){
            transInterface.showError(false , retVal);
            return;
        }
        iso8583.setField(48 , Field48);
        retVal = OnLineTrans() ;
        if(retVal!=0){
            transInterface.showError(false , retVal);
            return;
        }
        String rsp = iso8583.getfield(39);
        Logger.debug("SettleTrans>>settle>>rsp="+rsp);
        if(!rsp.equals(RSP_00_SUCCESS)){
            transInterface.showError(false , formatRsp(rsp));
            return;
        }
        String f48 = iso8583.getfield(48);
        Logger.debug("SettleTrans>>settle>>f48="+f48);
        int flag = 1 ;
        shell2ReversalUpsend(flag);
    }

    /**
     * lidiar con el guión del emisor y la revocación antes de la liquidación
     * @param flag
     */
    private void shell2ReversalUpsend(int flag){
        Logger.debug("SettleTrans>>shell2ReversalUpsend");
        TransLogData data = TransLog.getScriptResult();
        int retVal = 0 ;
        if (data != null) {
            transInterface.handling(Tcode.SEND_SETTLE_SCRIPT);
            Logger.debug("SettleTrans>>shellUpsend>>issuer script");
            setTraceNoInc(true);
            ScriptTrans script = new ScriptTrans(context, Type.SENDSCRIPT);
            retVal = script.sendScriptResult(data);
            if(retVal == 0) {
                TransLog.clearScriptResult();
            }
        }
        if(retVal!=0){
            transInterface.showError(false , retVal);
            return;
        }
        TransLogData revesalData = TransLog.getReversal();
        if (revesalData == null) {
            //enviar los detalles de la transacción
            onlineUpsend(flag) ;
            return;
        }
        setTraceNoInc(true);
        Logger.debug("FinanceTrans>>OnlineTrans>>reversal");
        transInterface.handling(Tcode.SEND_REVERSAL);
        RevesalTrans revesal = new RevesalTrans(context, Type.REVERSAL);
        for (int i = 0; i < cfg.getReversalCount() ; i++) {
            retVal = revesal.sendRevesal();
            if(retVal == 0){
                //reversión exitosa, archivo de reversión claro
                TransLog.clearReveral();
                break;
            }
        }

        if(retVal == Tcode.SOCKET_FAIL || retVal == Tcode.SEND_DATA_FAIL){
            transInterface.showError(false , retVal);
        }else {
            if(retVal != 0){
                TransLog.clearReveral();
                transInterface.showError(false , Tcode.REVERSAL_FAIL);
            }else {
                //enviar los detalles de la transacción
                onlineUpsend(flag) ;
            }
        }
    }

    /**
     * enviar los detalles de la transacción
     * @param flag
     */
    private void onlineUpsend(int flag){
        transInterface.handling(Tcode.SEND_SETTLE_TRANS_DETAILS);
        Logger.debug("SettleTrans>>onlineUpsend");
        TransEName = Type.UPSEND;
        setTraceNoInc(false);
        List<TransLogData> list = TransLog.getInstance().getData() ;
        if(list==null || list.size() <= 0){
            transInterface.showError(false , Tcode.BATCH_NO_TRANS);
            return;
        }
        int retVal = 0 ;
        //acuerdo ok
        if(flag == 1){
            for (int i = 0; i < list.size() ; i++) {
                TransLogData data = list.get(i);
                String type = data.getEName();
                boolean need = type.equals(Type.SALE) || type.equals(Type.VOID) || type.equals(Type.QUICKPASS);
                if (need) {
                    if (data.getMode() == ServiceEntryMode.ICC ||
                            data.getMode() == ServiceEntryMode.NFC) {

                        Field60 = null ;
                        setFixedDatas();
                        setFileds(data);
                        retVal = OnLineTrans();
                        if(retVal!=0){
                            retVal = Tcode.SETTLE_UPSEND_FAIL ;
                            break;
                        }
                        sumCount++;
                        String rsp = iso8583.getfield(39);
                        if (!rsp.equals(RSP_00_SUCCESS)) {
                            retVal = formatRsp(rsp) ;
                            break;
                        }
                    }
                }
            }
        }else {
            //Reconciliación desequilibrada
//                iso8583.clearData();
//                int mag_amount = 0 ;
//                int mag_count = 0 ;
//
//                mag_count ++ ;
//                mag_amount += data.getAmount() ;
//                if(mag_count / 8 == 0){
//                    //Lanzar
//                    String a = String.valueOf(mag_amount);
//                    a = ISOUtil.padleft(a + "", 12, '0') ;
//                    if(MsgID!=null)
//                        iso8583.setField(0 , MsgID);
//                    iso8583.setField(4, a);
//                    iso8583.setField(22 , "0210");
//                    iso8583.setField(41 , cfg.getTermID());
//                    iso8583.setField(42 , cfg.getMerchID());
//                    appendField60("60");
//                    iso8583.setField(60 , Field60);
//                    Field62 = "610000"+a+cfg.getCurrencyCode();
//                    Field62 = BCD2ASC(Field62.getBytes());
//                    Logger.debug("SettleTrans>>setFileds>>Field62="+Field62);
//                    iso8583.setField(62 , Field62);
//                }
        }
        if(retVal!=0){
            transInterface.showError(false , retVal);
            return;
        }
        settleOver();
    }

    /**
     * fin del asentamiento
     */
    private void settleOver(){
        transInterface.handling(Tcode.SEND_SETTLE_FINISH_NOTICE);
        Logger.debug("SettleTrans>>settleOver");
        TransEName = Type.UPSEND ;
        setFixedDatas();
        setTraceNoInc(true);
        iso8583.clearData();
        if(MsgID!=null) {
            iso8583.setField(0, MsgID);
        }
        iso8583.setField(11 , cfg.getTraceNo());
        iso8583.setField(41 , cfg.getTermID());
        iso8583.setField(42 , cfg.getMerchID());
        Logger.debug("SettleTrans>>settleOver>>sumCount"+sumCount);
        iso8583.setField(48 , ISOUtil.padleft(String.valueOf(sumCount) , 4 , '0'));
        iso8583.setField(60 , "00"+cfg.getBatchNo()+"207");
        int retVal = OnLineTrans();
        if(retVal!=0){
            transInterface.showError(false , retVal);
            return;
        }
        String rsp = iso8583.getfield(39);
        Logger.debug("SettleTrans>>settleOver>>rsp = "+rsp);
        if(!rsp.equals(RSP_00_SUCCESS)){
            transInterface.showError(false , formatRsp(rsp));
            return;
        }
        //imprimir detalles de la liquidación
        retVal = settlePrint();
        if(retVal!=0){
            //clear all transaction log
            TransLog.getInstance().clearAll();
            transInterface.showError(false , retVal);
            return;
        }
        //borrar todo el registro de transacciones
        TransLog.getInstance().clearAll();
        //aumentar el NO de lote
        cfg.setBatchNo(Integer.parseInt(cfg.getBatchNo()) + 1 )
                .incTraceNo()
                .save();
        transInterface.handling(Tcode.TERMINAL_LOGOUT);
        LogoutTrans logoutTrans = new LogoutTrans(context , Type.LOGOUT , null);
        retVal = logoutTrans.Logout();
        if(retVal!=0){
            transInterface.showError(false , retVal);
            return;
        }
        transInterface.trannSuccess(Tcode.LOGOUT_SUCCESS);
    }

    /**
     * imprimir detalles de la liquidación
     * @return
     */
    private int settlePrint(){
        saveSettleLog();
        Logger.debug("SettleTrans>>settlePrint>>start print settlement details");
        transInterface.handling(Tcode.PRINTING_DETAILS);
        PrintManager pm = PrintManager.getmInstance(context);
        int retVal = 0 ;
        boolean isPrinted = true ;
        boolean isPrinterBroken = false ;
        PrintTask task = pm.buildSettleTask(TransLog.getInstance().getLastTransLog());
        do{
            transInterface.handling(Tcode.PRINTING_RECEPT);
            retVal = pm.print(task);
            if(Printer.PRINTER_STATUS_PAPER_LACK == retVal){
                int result = transInterface.printerLackPaper();
                if(1 == result){
                    isPrinted = false ;
                    break;
                }
            }else if(Printer.PRINTER_OK != retVal){
                isPrinterBroken = true ;
                break;
            }
        }while (retVal == Printer.PRINTER_STATUS_PAPER_LACK);
        if(!isPrinted){
            retVal = Tcode.USER_CANCEL ;
        }
        if(isPrinterBroken){
            retVal = Tcode.PRINT_FAIL;
        }
        return retVal ;
    }

    /**
     * guardar registro de liquidación
     */
    private void saveSettleLog(){
        TransLogData LogData = new TransLogData();
        LogData.setOprNo(cfg.getOprNo());
        LogData.setEName(TransEName.replace("-" , " "));
        LogData.setTraceNo(cfg.getTraceNo());
        LogData.setBatchNo(cfg.getBatchNo());
        LogData.setLocalDate(PAYUtils.getYear() + PAYUtils.getLocalDate());
        LogData.setLocalTime(PAYUtils.getLocalTime());
        LogData.setOnline(true);
        TransLog.getInstance().saveLog(LogData);
        Logger.debug("save log logSize="+ TransLog.getInstance().getSize());
    }

    private void startLocalPresentations(){
        transInterface.handling(Tcode.SEND_SETTLE_NOTICE);
        int retVal = settlePrint();
        if(retVal!=0){
            //clear all transaction log
            TransLog.getInstance().clearAll();
            transInterface.showError(false , retVal);
            return;
        }
        //clear all transaction log
        TransLog.getInstance().clearAll();
        //increase batch NO
        cfg.setBatchNo(Integer.parseInt(cfg.getBatchNo()) + 1 )
                .incTraceNo()
                .save();
        transInterface.handling(Tcode.TERMINAL_LOGOUT);
        LogoutTrans logoutTrans = new LogoutTrans(context , Type.LOGOUT , null);
        retVal = logoutTrans.Logout();
        if(retVal!=0){
            transInterface.showError(false , retVal);
            return;
        }
        transInterface.trannSuccess(Tcode.LOGOUT_SUCCESS);
    }

    private void setFileds(TransLogData data){
        iso8583.clearData();
        iso8583.set62AttrDataType(3);
        if(MsgID!=null) {
            iso8583.setField(0, MsgID);
        }
        //iso8583.setField(2 , data.getCardFullNo());
        iso8583.setField(2 , data.getPan());
        String a = String.valueOf(data.getAmount());
        a = ISOUtil.padleft(a + "", 12, '0');
        Logger.debug("SettleTrans>>setFileds>>Amount="+a);
        iso8583.setField(4, a);
        iso8583.setField(11 , data.getTraceNo());
        Logger.debug("SettleTrans>>setFileds>>EntryMode"+data.getEntryMode());
        if(data.getEntryMode() == null){
            iso8583.setField(22 , "0510");
        }else {
            iso8583.setField(22 , data.getEntryMode());
        }
        iso8583.setField(41 , cfg.getTermID());
        iso8583.setField(42 , cfg.getMerchID());
        if(data.getICCData() != null){
            iso8583.setField(55 , ISOUtil.byte2hex(data.getICCData()));
        }
        appendField60("60");
        iso8583.setField(60 , Field60);
        Field62 = "610000"+a+cfg.getCurrencyCode();
        Logger.debug("SettleTrans>>setFileds>>Field62="+Field62);
        iso8583.setField(62 , Field62);
    }

    private boolean isDebit(String type) {
        if( type.equals(Type.SALE) || type.equals(Type.QUICKPASS)
            || type.equals(Type.PREAUTHCOMPLETE)){
            return true;
        }

        return false;
    }

    private boolean isCredit(String type) {
        if( type.equals(Type.VOID) || type.equals(Type.REFUND)
                || type.equals(Type.PREAUTHCOMPLETEVOID)){
            return true;
        }

        return false;
    }

    private int getSettleSUM48(){
        List<TransLogData> list = TransLog.getInstance().getData();
        long debitAmount = 0 ;
        int debitCounts = 0 ;
        long creditAmount = 0 ;
        int creditCounts = 0 ;

        String f48_1;
        String f48_2;
        String f48_3;
        String f48_4;
        String f_waibi = "0000000000000000000000000000000";
        if(list!=null && list.size() > 0){
            Logger.debug("transaction log numbers:"+list.size());
            for (int i = 0  ; i < list.size() ; i++){
                String trans = list.get(i).getEName() ;
                Logger.debug("list["+i+"] type = "+trans);
                //Modify by Andy Yuan
                if(isDebit(trans)) {
                    debitAmount += list.get(i).getAmount();
                    debitCounts += 1;
                } else if(isCredit(trans)){
                    creditAmount += list.get(i).getAmount();
                    creditCounts += 1;
                }
                /*
                if(trans.equals(Type.SALE)){
                    debitAmount += list.get(i).getAmount();
                    debitCounts ++;
                }
                if(trans.equals(Type.QUICKPASS)){
                    debitAmount += list.get(i).getAmount();
                    debitCounts ++;
                }
                if(trans.equals(Type.VOID)){
                    creditAmount += list.get(i).getAmount();
                    creditCounts ++ ;
                }*/
            }
            f48_1 = ISOUtil.padleft(String.valueOf(debitAmount) , 12 , '0');
            f48_2 = ISOUtil.padleft(String.valueOf(debitCounts) , 3 , '0');
            f48_3 = ISOUtil.padleft(String.valueOf(creditAmount) , 12 , '0');
            f48_4 = ISOUtil.padleft(String.valueOf(creditCounts) , 3 , '0');
            Field48 = f48_1+f48_2+f48_3+f48_4+"0" + f_waibi;
            Logger.debug("field 48 of settlement="+Field48);
            return 0 ;
        }else {
            return Tcode.BATCH_NO_TRANS ;
        }
    }

    private String formatOPN(String opn){
        String two = ISOUtil.padleft(opn , 2 , '0');
        return two + " " ;
    }

    public final static char[] BToA = "0123456789abcdef".toCharArray() ;
    public static String BCD2ASC(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            int h = ((bytes[i] & 0xf0) >>> 4);
            int l = (bytes[i] & 0x0f);
            temp.append(BToA[h]).append( BToA[l]);
        }
        return temp.toString() ;
    }
}
