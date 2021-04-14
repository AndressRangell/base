package com.newpos.libpay.presenter;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputManager;
import com.newpos.libpay.device.card.CardInfo;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.device.pinpad.PinType;
import com.newpos.libpay.device.pinpad.PinpadListener;
import com.newpos.libpay.device.scanner.QRCInfo;
import com.newpos.libpay.trans.translog.TransLogData;

/**
 * Created by zhouqiang on 2017/3/15.
 * @author zhouqiang
 * definir interfaz MODELO
 */

public interface TransInterface {
    /**
     * Obtener datos de entrada de la usuario
     * como monto, cupón NO, contraseña, etc.
     * @param type @{@link com.android.desert.keyboard.InputManager.Mode}
     * @return InputInfo @{@link InputInfo}
     */
    InputInfo getInput(InputManager.Mode type);

    /**
     * detectar tarjeta, obtener información de la tarjeta
     * @param mode consulte @{@link com.newpos.libpay.device.card.CardType}
     * @return CardInfo @{@link CardInfo}
     * @attention tratar con la tarjeta, consulte @{@link com.newpos.libpay.device.card.CardManager}
     */
    CardInfo getCard(int mode);

    /**
     * obtener información del código QR
     * @param mode @{@link com.android.desert.keyboard.InputManager.Style}
     * @return QRCInfo @{@link QRCInfo}
     * @attention tratar con el scaner, consulte @{@link com.newpos.libpay.device.scanner.ScannerManager}
     */
    QRCInfo getQRCInfo(InputManager.Style mode);

    /**
     * obtener PIN de PIN PAD
     * @param type consule @{@link PinType}
     * @return PinInfo @{@link PinInfo}
     * @attention  tratar con el teclado de PIN, consulte @{@link com.newpos.libpay.device.pinpad.PinpadManager}
     * @attention  @{@link com.newpos.libpay.device.pinpad.PinpadManager#getPin(int, PinType, PinpadListener)}
     */
    PinInfo getPinpadPin(PinType type);

    /**
     * aviso usuario confirmar número de tarjeta
     * @param cn numero de tarjeta
     * @return 0:usuario confirmar otros: usuario cancelar
     */
    int confirmCardNO(String cn);

    /**
     * mostrar y seleccionar la aplicación de la tarjeta
     * @param list lista de aplicaciones de tarjetas
     * @return índice de la aplicación de tarjeta
     * @attention el índice comienza con 0
     */
    int choseAppList(String[] list);

    /**
     * antes de GPO del proceso EMV.
     * esta devolución de llamada al propósito del usuario es tener en cuenta una gran cantidad de requisitos especiales del proceso EMV
     */
    void beforeGPO();

    /**
     * mostrar el estado de la transacción
     * @param status Trans Status
     */
    void handling(int status);

    /**
     * Interfaz de pantalla interactiva hombre-computadora (Confirmar la información de la transacción original) aviso usuario confirmar información de transacción
     * @param logData refer to @{@link TransLogData}
     * @return 0:usuario confirmar otros: usuario cancelar
     */
    int confirmTransInfo(TransLogData logData);

    /**
     * comprobar la información de identidad de la tarjeta
     * @param info información de identidad de la tarjeta
     * @return 0:usuario confirmar otros: usuario cancelar
     */
    int confirmCardVerifyCert(String info);

    /**
     * mostrar transacción exitosa
     * @param code codigo de transaccion @{@link com.newpos.libpay.trans.Tcode}
     * @param args parámetros de adición
     */
    void trannSuccess(int code, String... args);

    /**
     * mostrar información de error
     * @param isToast
     * @param errcode consulte @{@link com.newpos.libpay.trans.Tcode}
     */
    void showError(boolean isToast , int errcode);

    /**
     * observe que la impresora del usuario no tiene papel
     * @return
     */
    int printerLackPaper();
}
