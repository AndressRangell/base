package com.newpos.libpay.device.card;

/**
 * Creado por zhouqiang el 14/03/2017.
 * @author zhouqiang
 * información detallada de la tarjeta
 */

public class CardInfo {

    private boolean resultFalg ;

    /** información de la tarjeta */
    private CardType cardType ;
    private byte[] cardAtr ;
    private String[] trackNo ;
    private int nfcType ;

    /**
     * información fallida
     */
    private int errno ;

    public CardInfo(){}

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public byte[] getCardAtr() {
        return cardAtr;
    }

    public void setCardAtr(byte[] cardAtr) {
        this.cardAtr = cardAtr;
    }

    public String[] getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(String[] trackNo) {
        this.trackNo = trackNo;
    }

    public int getNfcType() {
        return nfcType;
    }

    public void setNfcType(int nfcType) {
        this.nfcType = nfcType;
    }

    public boolean isResultFalg() {
        return resultFalg;
    }

    public void setResultFalg(boolean resultFalg) {
        this.resultFalg = resultFalg;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }
}
