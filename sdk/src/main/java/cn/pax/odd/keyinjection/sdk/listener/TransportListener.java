package cn.pax.odd.keyinjection.sdk.listener;

/**
 * @author ligq
 * @date 2018/2/23
 */

public interface TransportListener {
    /**
     * start inject
     */
    void onStart(String msg);

    /**
     * wait for handshaking message
     */
    void onConnect(String msg);

    /**
     * send the public key
     */
    void onSend(String msg);

    /**
     * receive the key from mPos
     */
    void onReceive(String msg);

    /**
     * inject key to A920
     */
    void onWriting(String msg);

    /**
     * inject successfully
     */
    void onSuccess(String msg);

//    /**
//     * inject failed
//     */
//    void onError();

    /**
     * inject failed
     *
     * @param msg failed message
     */
    void onError(String msg);


    /**
     * 当用户点击了INJECT按钮后，为了在查找USB设备之前有一个友好提示信息，连接USB设备之前取消这个提示
     * 信息以免USB影响授权，特定提供了一个关闭对话框接口。
     */
    void hideDialog();

}
