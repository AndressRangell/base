package com.newpos.libpay.global;

import android.content.Context;

import com.newpos.libpay.PaySdk;
import com.newpos.libpay.PaySdkException;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by zhouqiang on 2017/4/29.
 * @author zhouqiang
 * configurar terminal
 */

public class TMConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	private static String ConfigPath = "config.dat";
	private static TMConfig mInstance = null ;

	/**
	 * Guardar ruta para todos los archivos en este programa
	 */
	private static String ROOT_FILE_PATH ;
	public static String getRootFilePath() {
		return ROOT_FILE_PATH;
	}
	public static void setRootFilePath(String rootFilePath) {
		ROOT_FILE_PATH = rootFilePath;
	}

	private static boolean lockReturn = false ;
	public static boolean isLockReturn() {
		return lockReturn;
	}

	public static void setLockReturn(boolean lockReturn) {
		TMConfig.lockReturn = lockReturn;
	}

	/**
	 * Ya sea para abrir la información de depuración del SDK
	 */
	private boolean isDebug ;
	public boolean isDebug() {
		return isDebug;
	}

	public TMConfig setDebug(boolean debug) {
		isDebug = debug;
		return mInstance ;
	}

	/**
	 * Ya sea para abrir la función en línea
	 */
	private boolean isOnline ;
	public boolean isOnline() {
		return isOnline;
	}

	public TMConfig setOnline(boolean online) {
		isOnline = online;
		return mInstance ;
	}

	/**
	 * Ya sea para abrir el juego de voz en el proceso de transacción
	 */
	private boolean isVocie ;
	public boolean isVocie() {
		return isVocie;
	}

	public TMConfig setVocie(boolean vocie) {
		isVocie = vocie;
		return mInstance ;
	}

	/**
	 * indice de lista de logotipos bancarios
	 */
	private int bankid ;
	public int getBankid() {
		return bankid;
	}

	public TMConfig setBankid(int bankid) {
		if(bankid < TMConstants.BANKID.ASSETS.length && bankid >= 0){
			this.bankid = bankid ;
		}else {
			this.bankid = 0 ;
		}
		return mInstance ;
	}

	/**
	 * La especificación de pago que marca el soporte del entorno SDK actual es diferente en los estándares relacionados y los algoritmos de transacción.
	 * 1 ==== CUP
	 * 2 ==== CITIC Bank
	 */
	private int standard ;
	public int getStandard() {
		return standard;
	}

	public TMConfig setStandard(int standard) {
		if(standard == 1 || standard == 2){
			this.standard = standard ;
		}else {
			this.standard = 1;
		}
		return mInstance ;
	}

	/**
	 * ip
	 */
	private String ip ;
	public String getIp() {
		return ip;
	}

	public TMConfig setIp(String ip) {
		this.ip = ip;
		return mInstance ;
	}

	/**
	 * ip2
	 */
	private String ip2 ;
	public String getIP2(){
		return ip2 ;
	}

	public TMConfig setIp2(String s){
		this.ip2 = s ;
		return mInstance ;
	}

	/**
	 * si forzar el proceso de PBOC
	 */
	private boolean forcePboc ;
	public boolean isForcePboc() {
		return forcePboc;
	}

	public TMConfig setForcePboc(boolean forcePboc) {
		this.forcePboc = forcePboc;
		return mInstance ;
	}

	/**
	 * puerto
	 */
	private String port ;
	public String getPort() {
		return port;
	}

	public TMConfig setPort(String port) {
		this.port = port;
		return mInstance ;
	}

	/**
	 * puerto2
	 */
	private String port2 ;
	public String getPort2(){
		return port2 ;
	}

	public TMConfig setPort2(String s){
		this.port2 = s ;
		return mInstance ;
	}

	/**
	 * tiempo de espera en línea
	 */
	private int timeout ;
	public int getTimeout() {
		return timeout;
	}

	public TMConfig setTimeout(int timeout) {
		this.timeout = timeout;
		return mInstance ;
	}

	/**
	 * red pública
	 */
	private boolean isPubCommun ;
	public TMConfig setPubCommun(boolean is){
		this.isPubCommun = is ;
		return mInstance ;
	}

	public boolean getPubCommun(){
		return isPubCommun ;
	}

	/**
	 * Tiempo de espera de operación del usuario (s)
	 */
	private int waitUserTime ;
	public int getWaitUserTime() {
		return waitUserTime;
	}

	public TMConfig setWaitUserTime(int waitUserTime) {
		this.waitUserTime = waitUserTime;
		return mInstance ;
	}

	/**
	 * Abra el flash cuando se borre el código
	 */
	private boolean scanTorchOn ;
	public boolean isScanTorchOn() {
		return scanTorchOn;
	}

	public TMConfig setScanTorchOn(boolean scanTorchOn) {
		this.scanTorchOn = scanTorchOn;
		return mInstance ;
	}

	/**
	 * Abra el pitido cuando se borre el código
	 */
	private boolean scanBeeper ;
	public boolean isScanBeeper() {
		return scanBeeper;
	}

	public TMConfig setScanBeeper(boolean scanBeeper) {
		this.scanBeeper = scanBeeper;
		return mInstance ;
	}

	/**
	 * Carroñero trasero
	 */
	private boolean scanBack ;
	public boolean isScanBack() {
		return scanBack;
	}

	public TMConfig setScanBack(boolean scanFront) {
		this.scanBack = scanFront;
		return mInstance ;
	}

	/**
	 * si necesita contraseña para la transacción nula
	 */
	private boolean revocationPassWSwitch ;
	public boolean getRevocationPassSwitch(){
		return revocationPassWSwitch ;
	}

	public TMConfig setRevocationPassWSwitch(boolean is){
		this.revocationPassWSwitch = is ;
		return mInstance ;
	}

	/**
	 * si necesita tarjeta para transacción nula
	 */
	private boolean revocationCardSwitch ;
	public TMConfig setRevocationCardSwitch(boolean is){
		this.revocationCardSwitch = is ;
		return mInstance ;
	}

	public boolean getRevocationCardSwitch(){
		return revocationCardSwitch ;
	}

	/**
	 * si necesita contraseña para anular la transacción previa a la autorización
	 */
	private boolean preauthVoidPassSwitch ;
	public boolean isPreauthVoidPassSwitch() {
		return preauthVoidPassSwitch;
	}

	public TMConfig setPreauthVoidPassSwitch(boolean preauthVoidPassSwitch) {
		this.preauthVoidPassSwitch = preauthVoidPassSwitch;
		return mInstance ;
	}

	/**
	 * si necesita contraseña para completar la transacción previa a la autorización
	 */
	private boolean preauthCompletePassSwitch ;
	public boolean isPreauthCompletePassSwitch() {
		return preauthCompletePassSwitch;
	}

	public TMConfig setPreauthCompletePassSwitch(boolean preauthCompletePassSwitch) {
		this.preauthCompletePassSwitch = preauthCompletePassSwitch;
		return mInstance ;
	}

	/**
	 * si necesita contraseña para anular la transacción completa previa a la autorización
	 */
	private boolean preauthCompleteVoidCardSwitch ;
	public boolean isPreauthCompleteVoidCardSwitch() {
		return preauthCompleteVoidCardSwitch;
	}

	public TMConfig setPreauthCompleteVoidCardSwitch(boolean preauthCompleteVoidCardSwitch) {
		this.preauthCompleteVoidCardSwitch = preauthCompleteVoidCardSwitch;
		return mInstance ;
	}

	/**
	 * contraseñas maestras
	 */
	private String masterPass ;
	public String getMasterPass(){
		return masterPass ;
	}

	public TMConfig setMasterPass(String pass){
		this.masterPass = pass ;
		return mInstance ;
	}

	/**
	 * mantener contraseña
	 */
	private String maintainPass ;
	public String getMaintainPass(){
		return maintainPass ;
	}

	public TMConfig setMaintainPass(String pass){
		this.maintainPass = pass ;
		return mInstance ;
	}

	/**
	 * índice de clave maestra
	 */
	private int masterKeyIndex ;
	public int getMasterKeyIndex() {
		return masterKeyIndex;
	}

	public TMConfig setMasterKeyIndex(int masterKeyIndex) {
		this.masterKeyIndex = masterKeyIndex;
		return mInstance ;
	}

	/**
	 * si verifique ICC cuando pase la tarjeta
	 */
	private boolean isCheckICC ;
	public boolean isCheckICC(){
		return isCheckICC ;
	}

	public TMConfig setCheckICC(boolean is){
		this.isCheckICC = is ;
		return mInstance ;
	}

	/**
	 * TPDU
	 */
	private String tpdu ;
	public String getTpdu() {
		return tpdu;
	}

	public TMConfig setTpdu(String tpdu) {
		this.tpdu = tpdu;
		return mInstance ;
	}

	/**
	 * cabecera
	 */
	private String header ;
	public String getHeader() {
		return header;
	}

	public TMConfig setHeader(String header) {
		this.header = header;
		return mInstance ;
	}

	/**
	 * ID del terminal
	 */
	private String TermID ;
	public String getTermID() {
		return TermID;
	}

	public TMConfig setTermID(String termID) {
		TermID = termID;
		return mInstance ;
	}

	/**
	 * Identificación del comerciante
	 */
	private String MerchID ;
	public String getMerchID() {
		return MerchID;
	}

	public TMConfig setMerchID(String merchID) {
		MerchID = merchID;
		return mInstance ;
	}

	/**
	 * lote No
	 */
	private int BatchNo ;
	public String getBatchNo() {
		return ISOUtil.padleft(BatchNo + "", 6, '0');
	}

	public TMConfig setBatchNo(int batchNo) {
		BatchNo = batchNo;
		return mInstance ;
	}

	/**
	 * vale NO.
	 */
	private int TraceNo ;
	public String getTraceNo() {
		return ISOUtil.padleft(TraceNo + "", 6, '0');
	}

	public TMConfig setTraceNo(int traceNo) {
		TraceNo = traceNo;
		return mInstance ;
	}

	/**
	 * operador NO.
	 */
	private int oprNo ;
	public int getOprNo() {
		return oprNo;
	}

	public TMConfig setOprNo(int oprNo) {
		this.oprNo = oprNo;
		return mInstance ;
	}

	/**
	 * print receipt numbers.
	 * the value is 1-3.
	 * 1 -> merchant receipt
	 * 2 -> cardholder receipt
	 * 3 -> bank receipt
	 */
	private int PrinterTickNumber ;
	public int getPrinterTickNumber() {
		return PrinterTickNumber;
	}

	public TMConfig setPrinterTickNumber(int n) {
		this.PrinterTickNumber = n ;
		return mInstance ;
	}

	/**
	 * nombre comerciante
	 */
	private String MerchName ;
	public String getMerchName() {
		return MerchName;
	}

	public TMConfig setMerchName(String merchName) {
		MerchName = merchName;
		return mInstance ;
	}

	/**
	 * whether print english on receipt
	 * 1 -> Chinese
	 * 2 -> English
	 * 3 -> Chinese and English
	 */
	private int printEn ;
	public int getPrintEn() {
		return printEn;
	}

	public TMConfig setPrintEn(int lang) {
		this.printEn = lang;
		return mInstance ;
	}

	/**
	 * si cifrar los datos de la pista
	 */
	private boolean isTrackEncrypt ;
	public boolean isTrackEncrypt() {
		return isTrackEncrypt;
	}

	public TMConfig setTrackEncrypt(boolean is){
		this.isTrackEncrypt = is ;
		return mInstance ;
	}

	/**
	 * si es una sola tecla larga
	 */
	private boolean isSingleKey ;
	public boolean isSingleKey() {
		return isSingleKey;
	}

	public TMConfig setSingleKey(boolean is){
		this.isSingleKey = is ;
		return mInstance ;
	}

	/**
	 * código de moneda
	 */
	private String CurrencyCode ;
	public String getCurrencyCode() {
		return CurrencyCode;
	}

	public TMConfig setCurrencyCode(String cur){
		this.CurrencyCode = cur ;
		return mInstance ;
	}

	/**
	 * código firme
	 */
	private String firmCode ;
	public String getFirmCode() {
		return firmCode;
	}

	public TMConfig setFirmCode(String firmCode) {
		this.firmCode = firmCode;
		return mInstance ;
	}

	/**
	 * tiempos de reenvío de reversión
	 */
	private int reversalCount ;
	public int getReversalCount() {
		return reversalCount;
	}

	public TMConfig setReversalCount(int reversalCount) {
		this.reversalCount = reversalCount;
		return mInstance ;
	}

	private TMConfig() {
		try {
			loadFile(PaySdk.getInstance().getContext() ,
					PaySdk.getInstance().getParaFilepath());
		}catch (PaySdkException pse){
			System.err.println("TMConfig->"+pse.toString());
		}
	}

	public static TMConfig getInstance() {
		String fullPath = getRootFilePath() + ConfigPath;
		if (mInstance == null) {
			try {
				mInstance = (TMConfig) PAYUtils.file2Object(fullPath);
			} catch (FileNotFoundException e) {
				System.err.println("getInstance->"+e.toString());
			} catch (IOException e) {
				System.err.println("getInstance->"+e.toString());
			} catch (ClassNotFoundException e) {
				System.err.println("getInstance->"+e.toString());
			}
			if (mInstance == null) {
				mInstance = new TMConfig();
			}
		}
		return mInstance ;
	}

	private void loadFile(Context context , String path) {
		System.out.println("loadFile->path:"+path);
		String T = "1" ;
		Properties properties = PAYUtils.lodeConfig(context , TMConstants.DEFAULTCONFIG);
		if(properties!=null){
			Enumeration<?> enumeration = properties.propertyNames() ;
			while (enumeration.hasMoreElements()){
				String name = (String) enumeration.nextElement() ;
				System.out.println("loadFile->name:"+name);
				if(!PAYUtils.isNullWithTrim(name)){
					int index = Integer.parseInt(name.substring(name.length()-2 , name.length()));
					String prop = properties.getProperty(name);
					try {
						switch (index-1){
							case 0 :setIp(prop);break;
							case 1 :setPort(prop);break;
							case 2 :setIp2(prop);break;
							case 3 :setPort2(prop);break;
							case 4 :setTimeout(Integer.parseInt(prop) * 1000);break;
							case 5 :setPubCommun(prop.equals(T)?true:false);break;
							case 6 :setWaitUserTime(Integer.parseInt(prop));break;
							case 7 :setRevocationPassWSwitch(prop.equals(T)?true:false);break;
							case 8 :setRevocationCardSwitch(prop.equals(T)?true:false);break;
							case 9 :setPreauthVoidPassSwitch(prop.equals(T)?true:false);break;
							case 10 :setPreauthCompletePassSwitch(prop.equals(T)?true:false);break;
							case 11 :setPreauthCompleteVoidCardSwitch(prop.equals(T)?true:false);break;
							case 12 :setMasterPass(prop);break;
							case 13 :setMasterKeyIndex(Integer.parseInt(prop));break;
							case 14 :setCheckICC(prop.equals(T)?true:false);break;
							case 15 :setTpdu(prop);break;
							case 16 :setHeader(prop);break;
							case 17 :setTermID(prop);break;
							case 18 :setMerchID(prop);break;
							case 19 :setBatchNo(Integer.parseInt(prop));break;
							case 20 :setTraceNo(Integer.parseInt(prop));break;
							case 21 :setOprNo(Integer.parseInt(prop));break;
							case 22 :setPrinterTickNumber(Integer.parseInt(prop));break;
							case 23 :setMerchName(prop);break;
							case 24 :setPrintEn(Integer.parseInt(prop));break;
							case 25 :setTrackEncrypt(prop.equals(T)?true:false);break;
							case 26 :setSingleKey(prop.equals(T)?true:false);break;
							case 27 :setCurrencyCode(prop);break;
							case 28 :setFirmCode(prop);break;
							case 29 :setReversalCount(Integer.parseInt(prop));break;
							case 30 :setMaintainPass(prop);break;
							case 31 :setScanTorchOn(prop.equals(T)?true:false);break;
							case 32 :setScanBeeper(prop.equals(T)?true:false);break;
							case 33 :setScanBack(prop.equals(T)?true:false);break;
							case 34 :setDebug(prop.equals(T)?true:false);break;
							case 35 :setOnline(prop.equals(T)?true:false);break;
							case 36 :setBankid(Integer.parseInt(prop));break;
							case 37 :setStandard(Integer.parseInt(prop));break;
							case 38 :setForcePboc(prop.equals(T)?true:false);break;
							case 39 :setVocie(prop.endsWith(T)?true:false);break;
						}
					}catch (Exception e){
						System.err.println("loadFile->"+e.toString());
					}
				}
			}
		}
		save();
	}

	/**
	 * aumentar el vale NO.
	 * @return
     */
	public TMConfig incTraceNo() {
		if (this.TraceNo == 999999) {
			this.TraceNo = 0;
		}
		this.TraceNo += 1;
		this.save();
		return mInstance ;
	}

	/**
	 * guardar configuración de terminal
	 */
	public void save(){
		String FullName = getRootFilePath() + ConfigPath;
		try {
			File file = new File(FullName);
			if (file.exists()) {
				file.delete();
			}
			PAYUtils.object2File(mInstance, FullName);
		} catch (FileNotFoundException e) {
			System.err.println("save->"+e.toString());
		} catch (IOException e) {
			System.err.println("save->"+e.toString());
		}
	}
}