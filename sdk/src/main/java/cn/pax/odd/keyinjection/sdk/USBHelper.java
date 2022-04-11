package cn.pax.odd.keyinjection.sdk;

import android.hardware.usb.UsbDevice;

import com.pax.gl.commhelper.IComm;
import com.pax.gl.commhelper.ICommUsbHost;
import com.pax.gl.commhelper.exception.CommException;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.pax.odd.keyinjection.sdk.utils.ConvertUtils;
import cn.pax.odd.keyinjection.sdk.utils.LogUtils;


/**
 * @author ligq
 * @date 2018/1/23
 */

@SuppressWarnings("unused")
public class USBHelper {
    /**
     * 点击了对话框的取消按键后会更新isCanceled的值为true
     */
    private static AtomicBoolean isCanceled = new AtomicBoolean(true);
    private final static Object cancelFind = new Object();

    private static USBHelper instance;

    private USBHelper() {

    }

    public static USBHelper getInstance() {
        synchronized (USBHelper.class) {
            if (instance == null) {
                instance = new USBHelper();
            }
            return instance;
        }
    }

    public static void setCanceled(boolean stop) {
        synchronized (cancelFind) {
            isCanceled.set(stop);
        }
    }


    public static UsbDevice findDevice() {
        while (true) {
            if (isCanceled.get()) {
                return null;
            }
            UsbDevice peerDevice = getPeerDevice();
            /**
             * 可能出现getPeerDevice()返回了设备之后，isCanceled被设置为true的情形。
             */
            synchronized (cancelFind) {
                if (isCanceled.get()) {
                    return null;
                }

                if (peerDevice != null) {
                    return peerDevice;
                }
            }

        }
    }

    /**
     * connecting after find device
     *
     * @param peerDevice connected device
     */
    public static boolean connDevice(UsbDevice peerDevice) {
        if (peerDevice == null) {
            return false;
        }
        //GLCommDebug.setDebugLevel(GLCommDebug.EDebugLevel.DEBUG_LEVEL_ALL);
//        InjectApp.getApp().getUsbHost().setPaxSpecialUsbDevice(peerDevice);
        ICommUsbHost usbHost = InjectApp.getApp().getUsbHost();
        usbHost.setUsbDevice(peerDevice, null, 0);
//        usbHost.setPaxSpecialDevice(false);

        /**
         * 因为原App连接失败的情况出现太频繁了。增加等待时间和尝试次数来解决这个问题。
         * 等待时间值和尝试次数是随便取的。程序整体结构已定型，顾用了比较粗糙方法来解决这个问题。
         * 正常写法应该用非阻塞或者阻塞，监听取消事件，处理断开连接和回收连接资源.
         */
        usbHost.setConnectTimeout(10000);
        usbHost.setRecvTimeout(30000);
        usbHost.setSendTimeout(300);
        int trials = 3;
        while (trials-- != 0) {
            try {
                usbHost.connect();
                if (usbHost.getConnectStatus().ordinal() != IComm.EConnectStatus.CONNECTED.ordinal()) {
                    continue;
                }
                usbHost.reset();
                return true;
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }

        return false;
    }

    public static boolean send(byte[] send) {
        LogUtils.d("send & receive[write > ]: " + ConvertUtils.bytes2HexString(send));
        try {
            InjectApp.getApp().getUsbHost().send(send);
        } catch (Exception e) {
            LogUtils.e(e);
            return false;
        }
        return true;
    }

    /**
     * 返回条件：要么接收成功，返回期待的长度；要么等待用户取消，抛出异常。
     */
    public static byte[] received(int len) throws Exception {
        /**
         * 用户取消阻塞着的接收过程。取消操作里会执行USB.cancelRecv()操作,之后阻塞着的
         * UsbUtils.received()会返回。
         */
        byte[] received;
        try {
            /**
             * 超时返回new byte[0]，用户取消也是返回new byte[0]
             */
            received = InjectApp.getApp().getUsbHost().recv(len);
            LogUtils.d("send & receive[read < ]: "
                    + ConvertUtils.bytes2HexString(received));
                /*if (received.length == len) {
                    return received;
                }*/
            if (received.length == 0) {
                throw new Exception("Receiving Failed");
            }
        } catch (Exception e) {
            LogUtils.e(e);
            /**
             * 因为取消操作时,recv抛出的是{@link CommException#ERR_RECV},并非取消异常。
             * 所以在这里不方便用{@link CommException}自带的错误码来判别是用户点了取消按键，
             * 还是接收函数真的返回了{@link CommException#ERR_RECV}异常。
             * 这时，如果用{@link USBHelper#isCanceled}的值来判断是否是用户自己取消的方法看似可以，
             * 用户点击取消按键后设置{@link USBHelper#isCanceled}为true，然后再执行取消操作，
             * {@link ICommUsbHost#recv}立即返回。看似可以但不一定行。因为当设
             * 置{@link USBHelper#isCanceled}为true执行后到{@link ICommUsbHost#recv}立即返回之前，
             * {@link USBHelper#isCanceled}不一定的true值不一定反应到了内存之中。如果
             * {@link ICommUsbHost#recv}返回了，但{@link USBHelper#isCanceled}更新，那么这时会出现
             * 对话框消失，而这里的循环还在执行的现象。
             * 所以为了避免这种情况的出现，直接在catch到异常后抛出失败异常，将之前的用户取消提示
             * 改成接收失败。
             */
            throw new Exception("Receiving Failed");
        }

        return received;
//            if (isCanceled.get()) {
//
//            }
    }

    private static UsbDevice getPeerDevice() {
        List<ICommUsbHost.IUsbDeviceInfo> usbDevList = InjectApp.getApp().getUsbHost().getPeerDevice();
        if (usbDevList.isEmpty()) {
            return null;
        }

        for (int i = 0; i < usbDevList.size(); i++) {
            ICommUsbHost.IUsbDeviceInfo temp = usbDevList.get(i);
            if (temp.getDevice() != null && temp.isPaxDevice()) {
                return temp.getDevice();
            }

        }
        return null;
    }

    public static void disconnect() {
        /*try {
            InjectApp.getApp().getUsbHost().disconnect();
        } catch (Exception e) {
            LogUtils.e(e);
        }*/
    }

    public static void reset() {
        InjectApp.getApp().getUsbHost().reset();
    }

    public static void cancel() {
        InjectApp.getApp().getUsbHost().cancelRecv();
    }
}
