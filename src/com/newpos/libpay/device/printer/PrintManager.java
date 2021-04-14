package com.newpos.libpay.device.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.android.newpos.libemv.PBOCUtil;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.presenter.TransInterface;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Type;
import com.newpos.libpay.trans.finace.ServiceEntryMode;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.printer.PrintCanvas;
import com.pos.device.printer.PrintTask;
import com.pos.device.printer.Printer;
import com.pos.device.printer.PrinterCallback;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhouqiang on 2017/3/14.
 * @author zhouqiang
 * Administradora de impresoras
 */
public class PrintManager {

	private static PrintManager mInstance ;
	private static TMConfig cfg ;

	private PrintManager(){}

	private static Context mContext ;

	public static PrintManager getmInstance(Context c){
		mContext = c ;
		if(null == mInstance){
			mInstance = new PrintManager();
		}
		cfg = TMConfig.getInstance();
		return mInstance ;
	}

	private Printer printer = null ;

	private int retval = 0 ;

	private int formatErrorCode(int ret){
		switch (ret){
			case Printer.PRINTER_STATUS_BUSY:
				ret = Tcode.PRINTER_BUSY;
				break;
			case Printer.PRINTER_STATUS_HIGHT_TEMP:
				ret = Tcode.PRINTER_HIGH_TEMP;
				break;
			case Printer.PRINTER_STATUS_NO_BATTERY:
				ret = Tcode.PRINTER_NO_BATTERY;
				break;
			case Printer.PRINTER_STATUS_PAPER_LACK:
				ret = Tcode.PRINTER_LACK_PAPER;
				break;
			case Printer.PRINTER_STATUS_PRINT:
				ret = Tcode.PRINTER_STATUS_PRINT;
				break;
			default:
				break;
		}

		return ret;
	}

	/**
	 * imprimir el recibo
	 * @param data registro de transacciones
	 * @param isRePrint
     * @return
     */
	/**
	 * crear una tarea de impresión por translogdata
	 * @param data datos
	 * @param print_num imprimir num
	 * @param isRePrint si es reimpresión o no
	 * @return
	 */
	public PrintTask buildTaskByTLD(TransLogData data,
									int print_num ,
									boolean isRePrint){
		PrintTask printTask = new PrintTask();
		printTask.setGray(130);
		boolean isICC = data.getMode() == ServiceEntryMode.ICC;
		boolean isNFC = data.getMode() == ServiceEntryMode.NFC;
		boolean isScan = data.getMode() == ServiceEntryMode.QRC ;
		if (TransLog.getInstance().getSize() == 0) {
			return null ;
		}
		printer = Printer.getInstance() ;
		if (printer == null) {
			return null ;
		}
		Bitmap image = PAYUtils.getLogoByBankId(mContext, cfg.getBankid());
		PrintCanvas canvas = new PrintCanvas() ;
		Paint paint = new Paint() ;
		setFontStyle(paint , 2 , false);
		canvas.drawText(PrintRes.CH.WANNING , paint);
		printLine(paint , canvas);
		setFontStyle(paint , 1 , true);
		canvas.drawBitmap(image , paint);
		printLine(paint , canvas);
		setFontStyle(paint , 2 , false);
		if (print_num == 0) {
			canvas.drawText(PrintRes.CH.MERCHANT_COPY, paint);
		}else if (print_num == 1){
			canvas.drawText(PrintRes.CH.CARDHOLDER_COPY , paint);
		}else{
			canvas.drawText(PrintRes.CH.BANK_COPY , paint);
		}
		printLine(paint , canvas);
		setFontStyle(paint , 2 , false);
		canvas.drawText(PrintRes.CH.MERCHANT_NAME+"\n"+cfg.getMerchName() , paint);
		canvas.drawText(PrintRes.CH.MERCHANT_ID+"\n"+cfg.getMerchID() , paint);
		canvas.drawText(PrintRes.CH.TERNIMAL_ID+"\n"+cfg.getTermID() , paint);
		String operNo = data.getOprNo() < 10 ? "0" + data.getOprNo() : data.getOprNo()+"";
		canvas.drawText(PrintRes.CH.OPERATOR_NO+"    "+operNo, paint);
		printLine(paint , canvas);
		setFontStyle(paint , 2 , false);
		canvas.drawText(PrintRes.CH.ISSUER, paint);
		canvas.drawText(PrintRes.CH.ACQUIRER, paint);
		if(isScan){
			canvas.drawText(PrintRes.CH.SCANCODE, paint);
		}else {
			canvas.drawText(PrintRes.CH.CARD_NO, paint);
		}
		setFontStyle(paint , 3 , true);
		if (isICC){
			canvas.drawText("     "+ PAYUtils.getSecurityNum(data.getPan(), 6, 4) + " I" , paint);
		}else if(isNFC){
			canvas.drawText("     "+ PAYUtils.getSecurityNum(data.getPan(), 6, 4) + " C" , paint);
		}else if(isScan){
			canvas.drawText("     "+ data.getPan() , paint);
		}else{
			canvas.drawText("     "+ PAYUtils.getSecurityNum(data.getPan(), 6, 4) + " S" , paint);
		}
		setFontStyle(paint , 2 , false);
		canvas.drawText(PrintRes.CH.TRANS_TYPE , paint);
		setFontStyle(paint , 3 , true);
		canvas.drawText(formatTranstype(data.getEName()) , paint);
		setFontStyle(paint , 2 , false);
		if (!PAYUtils.isNullWithTrim(data.getExpDate())){
			canvas.drawText(PrintRes.CH.CARD_EXPDATE+"       " + data.getExpDate() , paint);
		}
		printLine(paint , canvas);
		setFontStyle(paint , 2 , false);
		if(!PAYUtils.isNullWithTrim(data.getBatchNo())){
			canvas.drawText(PrintRes.CH.BATCH_NO + data.getBatchNo(), paint);
		}
		if(!PAYUtils.isNullWithTrim(data.getTraceNo())){
			canvas.drawText(PrintRes.CH.VOUCHER_NO+data.getTraceNo(), paint);
		}
		if(!PAYUtils.isNullWithTrim(data.getAuthCode())){
			canvas.drawText(PrintRes.CH.AUTH_NO+data.getAuthCode() , paint);
		}
		setFontStyle(paint , 2 , false);
		if(!PAYUtils.isNullWithTrim(data.getLocalDate()) && !PAYUtils.isNullWithTrim(data.getLocalTime())){
			String timeStr = PAYUtils.StringPattern(data.getLocalDate() + data.getLocalTime(), "yyyyMMddHHmmss", "yyyy/MM/dd  HH:mm:ss");
			canvas.drawText(PrintRes.CH.DATE_TIME+"\n          " + timeStr, paint);
		}
		if(!PAYUtils.isNullWithTrim(data.getRRN())){
			canvas.drawText(PrintRes.CH.REF_NO+ data.getRRN(), paint);
		}
		canvas.drawText(PrintRes.CH.AMOUNT, paint);
		setFontStyle(paint , 3 , true);
		canvas.drawText("           "+PrintRes.CH.RMB+"     "+ PAYUtils.getStrAmount(data.getAmount()) + "" , paint);
		printLine(paint , canvas);
		setFontStyle(paint , 1 , false);
		if(!PAYUtils.isNullWithTrim(data.getRefence())){
			canvas.drawText(PrintRes.CH.REFERENCE +"\n" + data.getRefence() , paint);
		}
		if(data.getICCData() != null){
			printAppendICCData(data.getICCData() , canvas , paint);
		}
		if (isRePrint) {
			setFontStyle(paint , 3 , true);
			canvas.drawText(PrintRes.CH.REPRINT , paint);
		}
		if (print_num != 1 && !isScan) {
			setFontStyle(paint , 3 , true);
			canvas.drawText("         "+PrintRes.CH.CARDHOLDER_SIGN+"\n\n\n" , paint);
			printLine(paint , canvas);
			setFontStyle(paint , 1 , false);
			canvas.drawText(PrintRes.CH.AGREE_TRANS+"\n" , paint);
		}
		printTask.setPrintCanvas(canvas);
		if (image != null){
			image.recycle();
		}
		return printTask ;
		//retorna formatErrorCode(ret);
	}

	/**
	 * construir tarea de asentamiento
	 * @param data
	 * @return
	 */
	public PrintTask buildSettleTask(final TransLogData data){
		PrintTask printTask = new PrintTask();
		printTask.setGray(130);
		printer = Printer.getInstance() ;
		if(printer == null){
			return null ;
		}
		PrintCanvas canvas = new PrintCanvas() ;
		Paint paint = new Paint() ;
		setFontStyle(paint , 2 , false);
		canvas.drawText(PrintRes.CH.WANNING , paint);
		printLine(paint , canvas);
		setFontStyle(paint , 1 , true);
		Bitmap image = PAYUtils.getLogoByBankId(mContext,cfg.getBankid());
		canvas.drawBitmap(image , paint);
		printLine(paint , canvas);
		setFontStyle(paint , 2 , false);
		canvas.drawText(PrintRes.CH.SETTLE_SUMMARY, paint);
		printLine(paint , canvas);
		setFontStyle(paint , 2 , false);
		canvas.drawText(PrintRes.CH.MERCHANT_NAME+"\n"+cfg.getMerchName() , paint);
		canvas.drawText(PrintRes.CH.MERCHANT_ID+"\n"+cfg.getMerchID() , paint);
		canvas.drawText(PrintRes.CH.TERNIMAL_ID+"\n"+cfg.getTermID() , paint);
		String operNo = data.getOprNo() < 10 ? "0" + data.getOprNo() : data.getOprNo()+"";
		canvas.drawText(PrintRes.CH.OPERATOR_NO+"    "+operNo, paint);
		if(!PAYUtils.isNullWithTrim(data.getBatchNo())){
			canvas.drawText(PrintRes.CH.BATCH_NO + data.getBatchNo(), paint);
		}
		if(!PAYUtils.isNullWithTrim(data.getLocalDate()) && !PAYUtils.isNullWithTrim(data.getLocalTime())){
			String timeStr = PAYUtils.StringPattern(data.getLocalDate() + data.getLocalTime(), "yyyyMMddHHmmss", "yyyy/MM/dd  HH:mm:ss");
			canvas.drawText(PrintRes.CH.DATE_TIME+"\n          " + timeStr, paint);
		}
		printLine(paint , canvas);
		canvas.drawText(PrintRes.CH.SETTLE_LIST , paint);
		printLine(paint , canvas);
		canvas.drawText(PrintRes.CH.SETTLE_INNER_CARD , paint);
		List<TransLogData> list = TransLog.getInstance().getData();
		int saleAmount = 0 ;
		int saleSum = 0 ;
		int quickAmount = 0 ;
		int quickSum = 0 ;
		int voidAmount = 0 ;
		int voidSum = 0 ;
		for (int i = 0 ; i < list.size() ; i++ ){
			TransLogData tld = list.get(i) ;
			if(tld.getEName().equals(Type.SALE)){
				saleAmount += tld.getAmount() ;
				saleSum ++ ;
			}if(tld.getEName().equals(Type.QUICKPASS)){
				if(tld.isOnline()){
					saleAmount += tld.getAmount() ;
					saleSum ++ ;
				}else{
					quickAmount += tld.getAmount() ;
					quickSum ++ ;
				}
			}if(tld.getEName().equals(Type.VOID)){
				voidAmount += tld.getAmount() ;
				voidSum ++ ;
			}
		}

		if(saleSum != 0){
			canvas.drawText(formatTranstype(Type.SALE)+"           "+saleSum+"               "+PAYUtils.getStrAmount(saleAmount) , paint);
		}if(quickSum != 0){
			canvas.drawText("EC SALE/SALE"+"           "+quickSum+"               "+PAYUtils.getStrAmount(quickAmount) , paint);
		}if(voidSum != 0){
			canvas.drawText(formatTranstype(Type.VOID)+"           "+voidSum+"               "+PAYUtils.getStrAmount(voidAmount) , paint);
		}

		printLine(paint , canvas);
		canvas.drawText(PrintRes.CH.SETTLE_OUTER_CARD , paint);

		canvas.drawText("\n\n\n\n\n" , paint);

		//detail
		canvas.drawText(PrintRes.CH.WANNING , paint);
		printLine(paint , canvas);
		setFontStyle(paint , 1 , true);
		canvas.drawBitmap(image , paint);
		setFontStyle(paint , 2 , false);
		printLine(paint , canvas);
		canvas.drawText(PrintRes.CH.SETTLE_DETAILS, paint);
		printLine(paint , canvas);
		setFontStyle(paint , 1 , false);
		canvas.drawText(PrintRes.CH.SETTLE_DETAILS_LIST , paint);
		setFontStyle(paint , 2 , false);
		printLine(paint , canvas);

		//detalle de la transacción
		List<TransLogData> list1 = TransLog.getInstance().getData();
		for (int i = 0 ; i < list1.size() ; i++ ){
			TransLogData tld = list1.get(i) ;
			if(tld.getEName().equals(Type.SALE) || tld.getEName().equals(Type.QUICKPASS) || tld.getEName().equals(Type.VOID)){
				canvas.drawText(tld.getTraceNo()+"     "+
						formatDetailsType(tld)+"    "+
						formatDetailsAuth(tld)+"    "+
						PAYUtils.getStrAmount(tld.getAmount())+"   "+
						tld.getPan() , paint);
			}
		}
		printTask.setPrintCanvas(canvas);
		return printTask ;
	}

	/**
	 * tarea de detalles de construcción
	 * @return
	 */
	public PrintTask buildDetailsTask(){
		PrintTask printTask = new PrintTask();
		printTask.setGray(130);
		if (TransLog.getInstance().getSize() == 0) {
			return null ;
		}
		printer = Printer.getInstance() ;
		if (printer == null) {
			return null ;
		}
		PrintCanvas canvas = new PrintCanvas() ;
		Paint paint = new Paint() ;
		setFontStyle(paint , 2 , true);
		canvas.drawText(PrintRes.CH.WANNING , paint);
		printLine(paint , canvas);
		setFontStyle(paint , 3 , true);
		canvas.drawText("                     "+PrintRes.CH.DETAILS , paint);
		setFontStyle(paint , 2 , true);
		canvas.drawText(PrintRes.CH.MERCHANT_NAME+"\n"+cfg.getMerchName() , paint);
		canvas.drawText(PrintRes.CH.MERCHANT_ID+"\n"+cfg.getMerchID() , paint);
		canvas.drawText(PrintRes.CH.TERNIMAL_ID+"\n"+cfg.getTermID() , paint);
		canvas.drawText(PrintRes.CH.BATCH_NO+"\n"+cfg.getBatchNo() , paint);
		canvas.drawText(PrintRes.CH.DATE_TIME+"\n"+PAYUtils.getSysTime(), paint);
		printLine(paint , canvas);
		int num = TransLog.getInstance().getSize() ;
		for (int i = 0 ; i < num ; i++){
			setFontStyle(paint , 1 , true);
			TransLogData data = TransLog.getInstance().get(i);
			if(data.getMode() == ServiceEntryMode.QRC){
				canvas.drawText(PrintRes.CH.SCANCODE+PAYUtils.getSecurityNum(data.getPan(), 6, 4), paint);
			}else {
				if(data.getMode() == ServiceEntryMode.ICC){
					canvas.drawText(PrintRes.CH.CARD_NO+PAYUtils.getSecurityNum(data.getPan(), 6, 4) + " I", paint);
				}else if(data.getMode() == ServiceEntryMode.NFC){
					canvas.drawText(PrintRes.CH.CARD_NO+PAYUtils.getSecurityNum(data.getPan(), 6, 4) + " C", paint);
				}else {
					canvas.drawText(PrintRes.CH.CARD_NO+PAYUtils.getSecurityNum(data.getPan(), 6, 4) + " S", paint);
				}
			}
			canvas.drawText(PrintRes.CH.TRANS_TYPE+formatTranstype(data.getEName()) , paint);
			canvas.drawText(PrintRes.CH.AMOUNT+PrintRes.CH.RMB+PAYUtils.getStrAmount(data.getAmount()), paint);
			canvas.drawText(PrintRes.CH.VOUCHER_NO+data.getTraceNo(), paint);
			printLine(paint , canvas);
		}
		printTask.setPrintCanvas(canvas);
		return printTask ;
	}

	/**
	 * imprimir datos
	 * @param task
	 * @return
	 */
	public int print(PrintTask task) {
		printer = Printer.getInstance();
		if (printer == null) {
			return -1 ;
		}
		retval = printer.getStatus() ;
		if(retval != Printer.PRINTER_OK){
			return retval ;
		}
		final CountDownLatch latch = new CountDownLatch(1);
		printer.startPrint(task, new PrinterCallback() {
			@Override
			public void onResult(int i, PrintTask printTask) {
				retval = i ;
				latch.countDown();
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return retval ;
	}

	/** =======================funcion privada=====================**/

	private String formatTranstype(String type){
		type = type.replace("-" , " ") ;
		int index = 0 ;
		for (int i = 0 ; i < PrintRes.STANDRAD_TRANS_TYPE.length ; i++){
			if(PrintRes.STANDRAD_TRANS_TYPE[i].equals(type)){
				index = i ;
			}
		}

		if(Locale.getDefault().getLanguage().equals("zh")){
			return PrintRes.TRANS[index]+"("+type+")";
		}else {
			return PrintRes.TRANS[index] ;
		}
	}

	private String formatDetailsType(TransLogData data){
		if(data.getMode() == ServiceEntryMode.ICC){
			return "I" ;
		}else if(data.getMode() == ServiceEntryMode.NFC){
			return "C" ;
		}else {
			return "S" ;
		}
	}

	private String formatDetailsAuth(TransLogData data){
		if(data.getAuthCode() == null){
			return "000000" ;
		}else {
			return data.getAuthCode() ;
		}
	}

	/**
	 * establecer la fuente y el estilo de la impresora
	 * @param paint
	 * @param size font size 1---small , 2---middle , 3---large
	 * @param isBold bold
	 * @author zq
	 */
	private void setFontStyle(Paint paint , int size , boolean isBold){
		if(isBold) {
			paint.setTypeface(Typeface.DEFAULT_BOLD);
		}else {
			paint.setTypeface(Typeface.DEFAULT);
		}
		switch (size) {
			case 0 : break;
			case 1 : paint.setTextSize(16F) ;break;
			case 2 : paint.setTextSize(22F) ;break;
			case 3 : paint.setTextSize(30F) ;break;
			default:break;
		}
	}

	/**
	 * imprimir una línea en el recibo
	 * @param paint
	 * @param canvas
	 */
	private void printLine(Paint paint , PrintCanvas canvas){
		String line = "----------------------------------------------------------------";
		setFontStyle(paint , 1 , true);
		canvas.drawText(line , paint);
	}

	private void printAppendICCData(byte[] ICCData , PrintCanvas canvas , Paint paint){
		byte[] temp = new byte[256];
		int len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x4F, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("AID: "+ ISOUtil.byte2hex(temp, 0, len) , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x50, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("LABLE: "+ ISOUtil.hex2AsciiStr(ISOUtil.byte2hex(temp, 0, len)) , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x9F26, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("TC: "+ ISOUtil.byte2hex(temp, 0, len) , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x5F34,temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("PanSN: "+ ISOUtil.byte2hex(temp, 0, len) , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x95, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("TVR: "+ ISOUtil.byte2hex(temp, 0, len) , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x9B, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("TSI: "+ ISOUtil.byte2hex(temp, 0, len) , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x9F36, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("ATC: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x9F33, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("TermCap: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x9F09, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("AppVer: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x9F34, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("CVM: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x9F10, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("IAD: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x82, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("AIP: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = PBOCUtil.get_tlv_data(ICCData, ICCData.length, 0x9F1E, temp, false);
		if (!PAYUtils.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("IFD: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
	}
}
