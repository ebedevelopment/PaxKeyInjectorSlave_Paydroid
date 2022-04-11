package cn.pax.odd.keyinjection.sdk.listener;

import android.content.Context;
import android.support.annotation.NonNull;

import cn.pax.odd.keyinjection.sdk.USBHelper;
import cn.pax.odd.keyinjection.sdk.utils.DeviceUtils;
import cn.pax.odd.keyinjection.sdk.utils.DialogUtils;
import cn.pax.odd.keyinjection.sdk.utils.ToastUtils;

/**
 * @author ligq
 * @date 2018/2/23
 */

public class TransportListenerImpl implements TransportListener {
    private Context context;

    public TransportListenerImpl(@NonNull Context context) {
        this.context = context;
    }

    @Override
    public void onStart(String msg) {
        DialogUtils.setCancelListener((dialog, which) -> {
            /**
             * 一定先设置停止标志位，在取消接收过程。因为接收的封装函数里会根据这个标志位判断是否要
             * 抛出异常。只有接收超时或者调用了取消函数，接收的封装函数里判断标志位这段逻辑才会得到执行。
             */
            USBHelper.setCanceled(true);
            USBHelper.cancel();
            //USBHelper.disconnect();
        });

        DialogUtils.showDialog(context, msg);
    }

    @Override
    public void onConnect(String msg) {
        DialogUtils.setDialogMsg(msg);
    }

    @Override
    public void onSend(String msg) {
        DialogUtils.setDialogMsg(msg);
    }

    @Override
    public void onReceive(String msg) {
        DialogUtils.setDialogMsg(msg);
    }

    @Override
    public void onWriting(String msg) {
        DialogUtils.setDialogMsg(msg);
    }

    @Override
    public void onSuccess(String msg) {
        DialogUtils.releaseDialog();
        DeviceUtils.beepOk();
        ToastUtils.showShort(msg);
    }

//    @Override
//    public void onError(String msg) {
//        DialogUtils.dismissDialog();
//        DeviceUtils.beepErr();
//        ToastUtils.showShort("Inject failed, please try again");
//    }

    @Override
    public void onError(String msg) {
        DialogUtils.releaseDialog();
        DeviceUtils.beepErr();
        ToastUtils.showShort("inject failed:" + msg);
    }

    @Override
    public void hideDialog() {
        DialogUtils.hideDialog();
    }

}
