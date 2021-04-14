package com.newpos.libpay.presenter;

import com.android.desert.keyboard.InputManager;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.device.pinpad.PinType;
import com.newpos.libpay.device.user.OnUserResultListener;
import com.newpos.libpay.trans.translog.TransLogData;

/**
 * Created by zhouqiang on 2017/4/25.
 * User UI
 * @author zhouqiang
 */

public interface TransView {
    /**
     * show search card UI
     * @param timeout : s
     * @param mode modo de tarjeta de búsqueda, consulte:@{@link com.newpos.libpay.device.card.CardManager}
     */
    void showCardView(int timeout, int mode);

    /**
     * show scan QR UI
     * @param timeout : s
     * @param mode scan QR mode, refer to:@{@link com.android.desert.keyboard.InputManager.Style}
     *             if use bank card, please refer to:@{@link TransView}{@link #showCardView(int, int)}
     */
    void showQRCView(int timeout, InputManager.Style mode);

    /**
     * show card number UI
     * @param timeout : s
     * @param pan : card number
     * @param l User confirm or cancel callback function, refer to:@{@link OnUserResultListener}
     */
    void showCardNo(int timeout, String pan, OnUserResultListener l);

    /**
     * show enter parameters UI
     * @param timeout :s
     * @param mode :enter mode @{@link com.android.desert.keyboard.InputManager.Mode}
     * @param l User confirm or cancel callback function @{@link OnUserResultListener}
     */
    void showInputView(int timeout, InputManager.Mode mode, OnUserResultListener l);

    /**
     *  get enter information
     * @param type enter mode @{@link com.android.desert.keyboard.InputManager.Mode}
     * @return enter information
     */
    String getInput(InputManager.Mode type);

    /**
     * show the detail information of transaction
     * @param timeout timeout
     * @param data the detail information of transaction @{@link TransLogData}
     * @param l User confirm or cancel callback function @{@link OnUserResultListener}
     */
    void showTransInfoView(int timeout, TransLogData data, OnUserResultListener l);

    /**
     * show the identity information of card
     * @param timeout  timeout
     * @param info the identity information of card
     * @param l User confirm or cancel callback function @{@link OnUserResultListener}
     */
    void showCardVerifyCertView(int timeout, String info, OnUserResultListener l);

    /**
     * show and select card applications
     * @param timeout timeout
     * @param apps application list
     * @param l User confirm or cancel callback function @{@link OnUserResultListener}
     * @return select index
     */
    int showCardAppListView(int timeout, String[] apps, OnUserResultListener l);

    /**
     * show enter PIN
     * @param timeout timeout
     * @param type the type of PIN
     * @return @{@link PinInfo}
     */
    PinInfo showEnterPinView(int timeout, PinType type, OnUserResultListener listener);

    /**
     *  La interfaz de usuario de notificación muestra la selección de varios idiomas de la tarjeta
     * @param timeout tiempo extra
     */
    void handleBeforceGPO(int timeout);

    /**
     *  mostrar información sobre el éxito de la transacción
     * @param timeout
     * @param info la información detallada del resultado de la transacción
     */
    void showSuccess(int timeout, String info);

    /**
     *  mostrar información de transacción fallida
     * @param timeout timeout
     * @param isToast si es información tostada o no
     * @param err la información detallada del resultado de la transacción
     */
    void showError(int timeout, boolean isToast , String err);

    /**
     * mostrar la información de la transacción
     * @param timeout timeout
     * @param status la información detallada del resultado de la transacción
     */
    void showMsgInfo(int timeout, String status);

    /**
     * observe que la impresora del usuario no tiene papel
     * @param timeout timeout
     * @param listener  El usuario confirma o cancela la función de devolución de llamada @{@link OnUserResultListener}
     */
    void printerLackPaper(int timeout , OnUserResultListener listener);
}
