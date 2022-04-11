package cn.pax.odd.keyinjection.sdk;

import android.app.Application;
import android.os.Handler;

import com.pax.dal.IDAL;
import com.pax.dal.IPed;
import com.pax.dal.entity.EPedType;
import com.pax.gl.commhelper.ICommUsbHost;
import com.pax.gl.commhelper.impl.PaxGLComm;
import com.pax.neptunelite.api.NeptuneLiteUser;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.pax.odd.keyinjection.sdk.utils.LogUtils;
import cn.pax.odd.keyinjection.sdk.utils.Utils;

/**
 * @author ligq
 * @date 2018/1/26
 */

@SuppressWarnings("unused")
public class InjectApp extends Application {
    private ThreadPoolExecutor poolExecutor;
    private Handler mainHandler;
    /**
     * 获取IPPI常用接口
     */
    private IDAL dal;
    private IPed iped;
    private PedMode pedMode;
    private ICommUsbHost usbHost;

    protected static InjectApp app;

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        app = this;
        Utils.init(this);
    }

    protected void saveLogs() {
        /**
         * 不保留终端输出和日志功能。一是影响性能，而是注密钥APK最好不打印内部内容。
         */
//        LogUtils.getConfig().setLog2FileSwitch(BuildConfig.DEBUG);
        LogUtils.getConfig().setLogHeadSwitch(BuildConfig.DEBUG);
        LogUtils.getConfig().setLogSwitch(BuildConfig.DEBUG);
        LogUtils.getConfig().setConsoleSwitch(BuildConfig.DEBUG);

        /**
         * 需要写外部SD卡权限，不能放在这里
         */
        //CrashUtils.init();
    }

    public static InjectApp getApp() {
        return app;
    }

    private void initData() {
        try {
            usbHost = PaxGLComm.getInstance(this).createUsbHost();
            dal = NeptuneLiteUser.getInstance().getDal(this);
            if (dal != null) {
                iped = dal.getPed(EPedType.INTERNAL);
                if (iped != null) {
                    pedMode = PedMode.SHARE;
                } else {
                    iped = dal.getPedKeyIsolation(EPedType.INTERNAL);
                    pedMode = PedMode.ISOLATION;
                }
            }
        } catch (Exception e) {
            LogUtils.e(e);
            return;
        }

        poolExecutor = new ThreadPoolExecutor(3, 10,
                5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(128),
                runnable -> {
                    Thread thread = new Thread(runnable, "Background executor service");
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.setDaemon(true);
                    return thread;
                });

        mainHandler = new Handler(getMainLooper());
    }

    public IDAL getDal() {
        return dal;
    }

    public void setDal(IDAL dal) {
        this.dal = dal;
    }

    public IPed getIped() {
        return iped;
    }

    public void setIped(IPed iped) {
        this.iped = iped;
    }

    public PedMode getPedMode() {
        return pedMode;
    }

    public void setPedMode(PedMode pedMode) {
        this.pedMode = pedMode;
    }

    public ICommUsbHost getUsbHost() {
        return usbHost;
    }

    public void setUsbHost(ICommUsbHost usbHost) {
        this.usbHost = usbHost;
    }

    public void runOnUiThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    public void runInBackground(Runnable runnable) {
        poolExecutor.execute(runnable);
    }
}
