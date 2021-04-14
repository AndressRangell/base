package com.newpos.libpay.device.pinpad;

import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.SDKException;
import com.pos.device.icc.SlotType;
import com.pos.device.ped.IccOfflinePinApdu;
import com.pos.device.ped.KeySystem;
import com.pos.device.ped.MACMode;
import com.pos.device.ped.Ped;
import com.pos.device.ped.PedRetCode;
import com.pos.device.ped.PinBlockCallback;
import com.pos.device.ped.PinBlockFormat;
import com.secure.api.PadView;

import java.util.Locale;

/**
 * Created by zhouqiang on 2017/3/14.
 * @author zhouqiang
 * Administrador de teclado de contraseña
 */

public class PinpadManager {
    private static PinpadManager instance ;

    private PinpadManager(){}

    public static PinpadManager getInstance(){
        if(instance == null){
            instance = new PinpadManager();
        }
        return instance ;
    }

    /**
     * Inyectar la llave maestra
     * @param info
     * @return
     */
    public static int loadMKey(MasterKeyinfo info){
        return Ped.getInstance().injectKey(
                PinpadKeytem.getKS(info.getKeySystem()),
                PinpadKeytype.getKT(info.getKeyType()),
                info.getMasterIndex(),
                info.getPlainKeyData());
    }

    /**
     * Inyectar clave de trabajo
     * @param info
     * @return
     */
    public static int loadWKey(WorkKeyinfo info){
        return Ped.getInstance().writeKey(
                PinpadKeytem.getKS(info.getKeySystem()),
                PinpadKeytype.getKT(info.getKeyType()),
                info.getMasterKeyIndex(),
                info.getWorkKeyIndex(),
                info.getMode(),
                info.getPrivacyKeyData());
    }

    /**
     * Monitoreo de operación de usuario de contraseña de entrada
     */
    private PinpadListener listener ;

    /**
     * número de tarjeta
     */
    private String pinCardNo ;

    /**
     * Longitud mínima del número de tarjeta
     */
    private int CARDNO_LESS_LEN = 13 ;

    /**
     * Obtener PIN de entrada
     * @param t tiempo extra
     * @param type Tipo de contraseña ingresada esta vez
     * @param l Devolución de llamada de entrada de contraseña
     */
    public void getPin(int t, PinType type ,PinpadListener l){
        this.listener = l ;
        this.pinCardNo = type.getCardNO() ;
        final PinInfo info = new PinInfo();
        final Ped ped = Ped.getInstance() ;
        final PadView padView = new PadView();
        if(t < 10 || t > 120){
            t = 60 ;
        }
        try {
            ped.setPinEntryTimeout(t);
        } catch (SDKException e) {
            e.printStackTrace();
        }
        if(null == l || type == null){
            throw new IllegalArgumentException("para is null");
        }
        if(type.isOnline()){
            if(pinCardNo.length() < CARDNO_LESS_LEN){
                throw new IllegalArgumentException("para is illegal");
            }
            pinCardNo = pinCardNo.substring(pinCardNo.length() - CARDNO_LESS_LEN, pinCardNo.length() - 1);
            pinCardNo = ISOUtil.padleft(pinCardNo, pinCardNo.length() + 4, '0');
            if(Locale.getDefault().getLanguage().equals("zh")){
                padView.setTitleMsg("Teclado de seguridad Huazhirong");
                padView.setAmountTitle("Monto:");
                padView.setAmount(PAYUtils.TwoWei(type.getAmount()));
                padView.setPinTips("Ingrese el PIN en línea:");
            }else {
                padView.setTitleMsg("Newpos Secure Keyboard");
                padView.setAmountTitle("Amount:");
                String a = PAYUtils.TwoWei(type.getAmount()) ;
                a = a.replace("," , ".") ;
                padView.setAmount(a);
                padView.setPinTips("Please enter PIN:");
            }
            ped.setPinPadView(padView);
            new Thread(){
                @Override
                public void run() {
                    ped.getPinBlock(KeySystem.MS_DES,
                            TMConfig.getInstance().getMasterKeyIndex(),
                            PinBlockFormat.PIN_BLOCK_FORMAT_0,
                            "0,4,5,6,7,8,9,10,11,12",
                            pinCardNo,
                            new PinBlockCallback() {
                                @Override
                                public void onPinBlock(int i, byte[] bytes) {
                                    info.setErrno(i);
                                    if(i == 0){
                                        info.setResult(PinResult.SUCCESS);
                                        info.setPinblock(bytes);
                                    }else if(i == PedRetCode.NO_PIN){
                                        info.setResult(PinResult.SUCCESS);
                                        info.setPinblock(null);
                                    }else {
                                        info.setResult(PinResult.FAIL);
                                    }
                                    listener.callback(info);
                                }
                            });
                }
            }.start();
        }else {
            if(Locale.getDefault().getLanguage().equals("zh")){
                padView.setTitleMsg("Teclado de seguridad Huazhirong");
                padView.setPinTips("Ingrese el PIN sin conexión\n" +"Restante "+ type.getCounts() +" Veces");
            }else {
                padView.setTitleMsg("Newpos Secure Keyboard");
                padView.setPinTips("Please enter offline PIN\n" + "Left "+ type.getCounts() +" times");
            }
            ped.setPinPadView(padView);
            IccOfflinePinApdu apdu = new IccOfflinePinApdu();
            if(type.getType() == 1){
                apdu.setRsakey(type.getPinKey());
            }
            apdu.setCla(0x00);
            apdu.setIns(0x20);
            apdu.setLe(0x00);
            apdu.setLeflg(0x00);
            apdu.setP1(0x00);
            apdu.setP2(type.getType() == 1 ? 0x88:0x80);
            ped.getOfflinePin(type.getType() == 1 ? KeySystem.ICC_CIPHER:KeySystem.ICC_PLAIN,
                    ped.getIccSlot(SlotType.USER_CARD),
                    "0,4,5,6,7,8,9,10,11,12",
                    apdu,
                    new PinBlockCallback() {
                        @Override
                        public void onPinBlock(int i, byte[] bytes) {
                            info.setErrno(i);
                            info.setPinblock(bytes);
                            if(i == 0){
                                info.setResult(PinResult.SUCCESS);
                            }else {
                                info.setResult(PinResult.FAIL);
                            }
                            listener.callback(info);
                        }
                    });
        }
    }

    /**
     * Obtenga información MAC cifrada
     * @param data Datos de origen cifrados
     * @param offset Compensar
     * @param len Longitud real
     * @return Información MAC cifrada
     */
    public byte[] getMac(byte[] data, int offset, int len) {
        byte[] macIn ;
        macIn = new byte[((len + 7) >> 3) << 3];
        System.arraycopy(data, offset, macIn, 0, len);
        byte[] macBlock = Ped.getInstance().getMac(KeySystem.MS_DES, TMConfig.getInstance().getMasterKeyIndex(), MACMode.MAC_MODE_CUP_8, macIn);
        return macBlock;
    }

    /**
     * CITIC Bank calcula MAC utilizando el método CBC
     * @param data Datos de origen cifrados
     * @param offset Compensar
     * @param len Longitud real
     * @return Información MAC cifrada
     */
    public byte[] getCITICMac(byte[] data, int offset, int len) {
        byte[] macIn ;
        macIn = new byte[((len + 7) >> 3) << 3];
        System.arraycopy(data, offset, macIn, 0, len);
        byte[] macBlock = Ped.getInstance().getMac(KeySystem.MS_DES, TMConfig.getInstance().getMasterKeyIndex(), MACMode.MAC_MODE_CUP, macIn);
        return macBlock;
    }

    /**
     * Obtenga información de seguimiento encriptada
     * @param index indice de Clave
     * @param track Datos de la pista original
     * @return Información encriptada
     */
    public String getEac(int index , String track) {
        int ofs, org_len;
        StringBuffer trackEnc = new StringBuffer(120);
        byte[] bufSrc ;
        byte[] bufDest ;
        if (track == null || track.equals("")) {
            return null;
        }
        org_len = track.length();//37
        if (((org_len % 2) != 0)) {
            if (track.length() < 17) {
                return null;
            }
            ofs = org_len - 17;
        } else {
            if (track.length() < 18) {
                return null;
            }
            ofs = org_len - 18;
        }
        trackEnc.append(track.substring(0, ofs));
        bufSrc = ISOUtil.str2bcd(track.substring(ofs, ofs + 16), false);
        bufDest = Ped.getInstance().encryptAccount(KeySystem.MS_DES, index, Ped.TDEA_MODE_ECB, bufSrc);
        if ( bufDest == null ) {
            return null;
        }
        trackEnc.append(ISOUtil.byte2hex(bufDest));
        trackEnc.append(track.substring(ofs + 16, org_len));
        return trackEnc.toString();
    }
}
