package cn.desert.newpos.payui.mis;

public class ResultPack {
    private String resultCode ;
    private String merchantId ;
    private String terminalId ;
    private String transAmount ;
    private String voucherNo ;
    private String referenceNo ;
    private String platformBillNo ;
    private String merchantBillNo ;
    private String transDate ;
    private String transTime ;
    private String paymentCode ;
    private String transId ;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getTransAmount() {
        return transAmount;
    }

    public void setTransAmount(String transAmount) {
        this.transAmount = transAmount;
    }

    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getPlatformBillNo() {
        return platformBillNo;
    }

    public void setPlatformBillNo(String platformBillNo) {
        this.platformBillNo = platformBillNo;
    }

    public String getMerchantBillNo() {
        return merchantBillNo;
    }

    public void setMerchantBillNo(String merchantBillNo) {
        this.merchantBillNo = merchantBillNo;
    }

    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }
}
