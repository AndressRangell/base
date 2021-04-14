package com.newpos.libpay.trans.translog;

import com.newpos.libpay.trans.finace.ServiceEntryMode;

import java.io.Serializable;

/**
 * datos de registro de transacciones
 * @author zhouqiang
 */
public class TransLogData implements Serializable {

	/**
	 * Nombre del emisor, del campo 63
	 */
	private String IssuerName;
	public String getIssuerName() {
		return IssuerName;
	}

	public void setIssuerName(String issuerName) {
		IssuerName = issuerName;
	}

	/**
	 * transacción en línea o transacción fuera de línea
	 */
	private boolean isOnline ;
	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean AAC) {
		this.isOnline = AAC;
	}

	/**
	 * modo de entrada de servicio
	 */
	private ServiceEntryMode mode ;
	public ServiceEntryMode getMode(){
		return mode ;
	}
	public void setMode(ServiceEntryMode mode){
		this.mode = mode ;
	}

	/*
	valor por defecto:   0
	enviar exitosamente: 1
	enviar fallido:  2
	 */
	private int RecState;

	/** monto de la propina **/
	private long TipAmout = 0;
	/** número de lote **/
	private String BatchNo;

	public String getBatchNo() {
		return BatchNo;
	}

	public void setBatchNo(String batchNo) {
		BatchNo = batchNo;
	}

	/**
	 * si la transacción fue anulada
	 */
	private boolean isVoided;
	public boolean getIsVoided(){
		return isVoided ;
	}

	public void setVoided(boolean isVoided){
		this.isVoided = isVoided ;
	}

	/**
	 * ya sea preautorización completa
	 */
	private boolean isPreComp;
	public boolean isPreComp() {
		return isPreComp;
	}

	public void setPreComp(boolean preComp) {
		isPreComp = preComp;
	}

	public int getRecState() {
		return RecState;
	}

	public void setRecState(int recState) {
		RecState = recState;
	}

	/**
	 * transacción nombre en inglés
	 * consulte @{@link com.newpos.libpay.trans.Type}
	 */
	private String TransEName;
	public String getEName() {
		return TransEName;
	}

	public void setEName(String eName) {
		TransEName = eName;
	}

	/**
	 * campo 60 de la transacción
	 */
	private String Field60;
	public String getField60() {
		return Field60;
	}

	public void setField60(String field60) {
		Field60 = field60;
	}

	/**
	 * número de tarjeta, del campo 2
	 */
	private String Pan;
	public String getPan() {
		return Pan;
	}

	public void setPan(String pan) {
		Pan = pan;
	}

	/**
	 * monto de la transacción, campo 4
	 */
	private Long Amount;
	public Long getAmount() {
		return Amount;
	}

	public void setAmount(Long amount) {
		Amount = amount;
	}

	/**
	 * Datos de la tarjeta IC de la transacción, del campo 55
	 */
	private byte[] ICCData;
	public byte[] getICCData() {
		return ICCData;
	}

	public void setICCData(byte[] iCCData) {
		ICCData = iCCData;
	}

	/**
	 * número de comprobante, campo 11
	 */
	private String TraceNo;
	public String getTraceNo() {
		return TraceNo;
	}

	public void setTraceNo(String traceNo) {
		TraceNo = traceNo;
	}

	/**
	 * tiempo de transacción, del campo 12
	 */
	private String LocalTime;
	public String getLocalTime() {
		return LocalTime;
	}

	public void setLocalTime(String localTime) {
		LocalTime = localTime;
	}

	/**
	 * fecha de la transacción, del campo 13
	 */
	private String LocalDate;
	public String getLocalDate() {
		return LocalDate;
	}

	public void setLocalDate(String localDate) {
		LocalDate = localDate;
	}

	/**
	 * fecha de caducidad de la tarjeta, del campo 14
	 */
	private String ExpDate;
	public String getExpDate() {
		return ExpDate;
	}

	public void setExpDate(String expDate) {
		ExpDate = expDate;
	}

	/**
	 * fecha de liquidación, del campo 15
	 */
	private String SettleDate;
	public String getSettleDate() {
		return SettleDate;
	}

	public void setSettleDate(String settleDate) {
		SettleDate = settleDate;
	}

	/**
	 * modo de entrada de servicio, del campo 22
	 */
	private String EntryMode;
	public String getEntryMode() {
		return EntryMode;
	}

	public void setEntryMode(String entryMode) {
		EntryMode = entryMode;
	}

	/**
	 * Número de serie de la tarjeta, del campo 23
	 */
	private String PanSeqNo;
	public String getPanSeqNo() {
		return PanSeqNo;
	}

	/**
	 * Adquirir el código de identificación de la institución, del campo 32
	 */
	private String AcquirerID;
	public void setPanSeqNo(String panSeqNo) {
		PanSeqNo = panSeqNo;
	}

	public String getAcquirerID() {
		return AcquirerID;
	}

	public void setAcquirerID(String acquirerID) {
		AcquirerID = acquirerID;
	}

	/**
	 * Número de referencia de recuperación, del campo 37
	 */
	private String RRN;
	public String getRRN() {
		return RRN;
	}

	public void setRRN(String rRN) {
		RRN = rRN;
	}

	/**
	 * Código de respuesta de identificación de autorización, del campo 38
	 */
	private String AuthCode;
	public String getAuthCode() {
		return AuthCode;
	}

	public void setAuthCode(String authCode) {
		AuthCode = authCode;
	}

	/**
	 * Código de respuesta, campo 39
	 */
	private String RspCode;
	public String getRspCode() {
		return RspCode;
	}

	public void setRspCode(String rspCode) {
		RspCode = rspCode;
	}

	/**
	 * Datos de respuesta adicionales, del campo 44
	 */
	private String Field44;
	public String getField44() {
		return Field44;
	}

	public void setField44(String field44) {
		Field44 = field44;
	}

	/**
	 * Código de procesamiento de transacciones, del campo 3
	 */
	private String ProcCode;
	public String getProcCode() {
		return ProcCode;
	}

	public void setProcCode(String procCode) {
		ProcCode = procCode;
	}

	/**
	 * Código de moneda de transacción, del campo 49
	 */
	private String CurrencyCode;
	public String getCurrencyCode() {
		return CurrencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		CurrencyCode = currencyCode;
	}

	/**
	 * los datos comienzan con el tercer byte del campo 63
	 */
	private String Refence;
	public String getRefence() {
		return Refence;
	}

	public void setRefence(String refence) {
		Refence = refence;
	}

	/**
	 * operador de terminal NO
	 */
	private int oprNo;
	public int getOprNo() {
		return oprNo;
	}

	public void setOprNo(int oprNo) {
		this.oprNo = oprNo;
	}

	/**
	 * Modo de condición de punto de servicio, del campo 25
	 */
	private String SvrCode;
	public String getSvrCode() {
		return SvrCode;
	}

	public void setSvrCode(String svrCode) {
		SvrCode = svrCode;
	}
}
