package com.newpos.libpay.device.card;

import android.util.Log;

import com.newpos.libpay.Logger;
import com.newpos.libpay.trans.Tcode;
import com.pos.device.SDKException;
import com.pos.device.beeper.Beeper;
import com.pos.device.icc.ContactCard;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.IccReaderCallback;
import com.pos.device.icc.OperatorMode;
import com.pos.device.icc.SlotType;
import com.pos.device.icc.VCC;
import com.pos.device.magcard.MagCardCallback;
import com.pos.device.magcard.MagCardReader;
import com.pos.device.magcard.MagneticCard;
import com.pos.device.magcard.TrackInfo;
import com.pos.device.picc.PiccReader;
import com.pos.device.picc.PiccReaderCallback;

/**
 * administrar tarjeta
 */

public class CardManager {
    private static CardManager instance ;

    private static int mode ;

    private CardManager(){}

    /**
     *
     * @param m = Los codigos del tipo de tarjeta a leer.
     *          Pueden ser - - -
     * @return
     */
    public static CardManager getInstance(int m){
        mode = m ;
        if(null == instance){
            instance = new CardManager();
        }
        return instance ;
    }

    private MagCardReader magCardReader ;
    private IccReader iccReader ;
    private PiccReader piccReader ;

    private void init(){
        //Log.d("yuan", "1-->"+(mode & CardType.INMODE_MAG.getVal()));
        if( (mode & CardType.INMODE_MAG.getVal() ) != 0 ){
            //Log.d("yuan", "4-->");
            magCardReader = MagCardReader.getInstance();
            //Log.d("yuan", magCardReader.getClass().getName());
        }
        //Log.d("yuan", "2-->"+(mode & CardType.INMODE_IC.getVal()));
        if( (mode & CardType.INMODE_IC.getVal()) != 0 ){
            iccReader = IccReader.getInstance(SlotType.USER_CARD);
        }
        //Log.d("yuan", "3-->"+(mode & CardType.INMODE_NFC.getVal()));
        if( (mode & CardType.INMODE_NFC.getVal()) != 0 ){
            piccReader = PiccReader.getInstance();
        }
        isEnd = false ;
    }

    private void stopMAG(){
        try {
            if(magCardReader!=null){
                magCardReader.stopSearchCard();
            }
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }

    private void stopICC(){
        if(iccReader!=null){
            try {
                iccReader.stopSearchCard();
            } catch (SDKException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopPICC(){
        if(piccReader!=null){
            piccReader.stopSearchCard();
            try {
                piccReader.release();
            } catch (SDKException e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseAll(){
        isEnd = true ;
        try {
            if(magCardReader!=null){
                magCardReader.stopSearchCard();
                Logger.debug("mag stop");
            }
            if(iccReader!=null){
                iccReader.stopSearchCard();
                iccReader.release();
                Logger.debug("icc stop");
            }
            if(piccReader!=null){
                piccReader.stopSearchCard();
                piccReader.release();
                Logger.debug("picc stop");
            }
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }

    private CardListener listener ;

    private boolean isEnd = false ;

    public void getCard(final int timeout , CardListener l){
        Logger.debug("CardManager>>getCard>>timeout="+timeout);
        init();
        final CardInfo info = new CardInfo() ;
        if(null == l){
            info.setResultFalg(false);
            info.setErrno(Tcode.SEARCH_CARD_FAIL);
            listener.callback(info);
        }else {
            this.listener = l ;
            new Thread(){
                @Override
                public void run(){
                    try{
                        if( (mode & CardType.INMODE_MAG.getVal()) != 0 ){
                            Logger.debug("CardManager>>getCard>>MAG");

                            magCardReader.startSearchCard(timeout, new MagCardCallback() {
                                @Override
                                public void onSearchResult(int i, MagneticCard magneticCard) {
                                    if(!isEnd){
                                        Logger.debug("CardManager>>getCard>>MAG>>i="+i);
                                        isEnd = true ;
                                        stopICC();
                                        stopPICC();
                                        if( 0 == i ){
                                            listener.callback(handleMAG(magneticCard));
                                        }else {
                                            info.setResultFalg(false);
                                            info.setErrno(Tcode.SEARCH_CARD_FAIL);
                                            listener.callback(info);
                                        }
                                    }
                                }
                            });
                        }if( (mode & CardType.INMODE_IC.getVal()) != 0 ){
                            Logger.debug("CardManager>>getCard>>ICC");
                            iccReader.startSearchCard(timeout, new IccReaderCallback() {
                                @Override
                                public void onSearchResult(int i) {
                                    if(!isEnd){
                                        Logger.debug("CardManager>>getCard>>ICC>>i="+i);
                                        isEnd = true ;
                                        stopMAG();
                                        stopPICC();
                                        if( 0 == i ){
                                            try {
                                                listener.callback(handleICC());
                                            } catch (SDKException e) {
                                                info.setResultFalg(false);
                                                info.setErrno(Tcode.SEARCH_CARD_FAIL);
                                                listener.callback(info);
                                            }
                                        }else {
                                            info.setResultFalg(false);
                                            info.setErrno(Tcode.SEARCH_CARD_FAIL);
                                            listener.callback(info);
                                        }
                                    }
                                }
                            });
                        }if( (mode & CardType.INMODE_NFC.getVal()) != 0 ){
                            Logger.debug("CardManager>>getCard>>NFC");
                            piccReader.startSearchCard(timeout, new PiccReaderCallback() {
                                @Override
                                public void onSearchResult(int i, int i1) {
                                    try {
                                        Thread.sleep(400);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if(!isEnd){
                                        Logger.debug("CardManager>>getCard>>NFC>>i="+i);
                                        isEnd = true ;
                                        stopICC();
                                        stopMAG();
                                        if( 0 == i ){
                                            try {
                                                Beeper.getInstance().beep(3000 , 500);
                                            } catch (SDKException e) {
                                                e.printStackTrace();
                                            }
                                            listener.callback(handlePICC(i1));
                                        }else {
                                            info.setResultFalg(false);
                                            info.setErrno(Tcode.SEARCH_CARD_FAIL);
                                            listener.callback(info);
                                        }
                                    }
                                }
                            });
                        }
                    }catch (SDKException sdk){
                        Logger.debug("SDKException="+sdk.getMessage().toString());
                        releaseAll();
                        info.setResultFalg(false);
                        info.setErrno(Tcode.SEARCH_CARD_FAIL);
                        listener.callback(info);
                    }
                }
            }.start();
        }
    }

    private CardInfo handleMAG(MagneticCard card){
        CardInfo info = new CardInfo() ;
        info.setResultFalg(true);
        info.setCardType(CardType.INMODE_MAG);
        TrackInfo ti_1 = card.getTrackInfos(MagneticCard.TRACK_1);
        TrackInfo ti_2 = card.getTrackInfos(MagneticCard.TRACK_2);
        TrackInfo ti_3 = card.getTrackInfos(MagneticCard.TRACK_3);
        info.setTrackNo(new String[]{ti_1.getData() , ti_2.getData() , ti_3.getData()});
        return info ;
    }

    private CardInfo handleICC() throws SDKException {
        CardInfo info = new CardInfo();
        info.setCardType(CardType.INMODE_IC);
        if (iccReader.isCardPresent()) {
            ContactCard contactCard = iccReader.connectCard(VCC.VOLT_5 , OperatorMode.EMV_MODE);
            byte[] atr = contactCard.getATR() ;
            if (atr.length != 0) {
                info.setResultFalg(true);
                info.setCardAtr(atr);
            } else {
                info.setResultFalg(false);
                info.setErrno(Tcode.SEARCH_CARD_FAIL);
            }
        } else {
            info.setResultFalg(false);
            info.setErrno(Tcode.SEARCH_CARD_FAIL);
        }
        return info;
    }

    private CardInfo handlePICC(int nfcType){
        CardInfo info = new CardInfo();
        info.setResultFalg(true);
        info.setCardType(CardType.INMODE_NFC);
        info.setNfcType(nfcType);
        return info ;
    }
}
