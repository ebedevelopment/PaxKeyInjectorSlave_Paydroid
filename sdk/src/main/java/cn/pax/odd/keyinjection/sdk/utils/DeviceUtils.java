package cn.pax.odd.keyinjection.sdk.utils;

import com.pax.dal.entity.EBeepMode;
import com.pax.dal.entity.ECheckMode;
import com.pax.dal.entity.EPedKeyType;
import com.pax.dal.exceptions.PedDevException;

import cn.pax.odd.keyinjection.sdk.InjectApp;

/**
 * @author ligq
 * @date 2018/1/17
 */

@SuppressWarnings("unused")
public class DeviceUtils {
    private DeviceUtils() {

    }


    /**
     * beep 成功
     */
    public static void beepOk() {
        try {
            InjectApp.getApp().getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_3, 100);
            InjectApp.getApp().getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_4, 100);
            InjectApp.getApp().getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_5, 100);
        } catch (Exception e) {
            LogUtils.e("", e);
        }
    }

    /**
     * beep 失败
     */
    public static void beepErr() {
        try {
            InjectApp.getApp().getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_6, 200);
        } catch (Exception e) {
            LogUtils.e("", e);
        }
    }

//    public static void writeTMK(int tmkIndex, byte[] tmkValue) throws PedDevException {
//        InjectApp.getApp().getIped().writeKey(EPedKeyType.TLK, (byte) 0, EPedKeyType.TMK, (byte) tmkIndex,
//                tmkValue, ECheckMode.KCV_NONE, null);
//    }

    public static void writeTMK(int tlkIndex, int tmkIndex, byte[] tmkValue) throws PedDevException {
        InjectApp.getApp().getIped().writeKey(EPedKeyType.TLK, (byte) tlkIndex, EPedKeyType.TMK, (byte) tmkIndex,
                tmkValue, ECheckMode.KCV_NONE, null);
    }


    public static void writeTPK(byte[] tpkValue, byte[] tpkKcv, int tmkIndex, int tpkIndex) throws PedDevException {
        ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
        if (tpkKcv == null || tpkKcv.length == 0) {
            checkMode = ECheckMode.KCV_NONE;
        }
        InjectApp.getApp().getIped().writeKey(EPedKeyType.TMK, (byte) tmkIndex,
                EPedKeyType.TPK, (byte) tpkIndex, tpkValue, checkMode, tpkKcv);

    }


    public static void writeTAK(byte[] takValue, byte[] takKcv, int tmkIndex, int takIndex) throws PedDevException {
        ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
        if (takKcv == null || takKcv.length == 0) {
            checkMode = ECheckMode.KCV_NONE;
        }
        InjectApp.getApp().getIped().writeKey(EPedKeyType.TMK, (byte) tmkIndex,
                EPedKeyType.TAK, (byte) takIndex, takValue, checkMode, takKcv);
    }


    public static void writeTDK(byte[] tdkValue, byte[] tdkKcv, int tmkIndex, int tdkIndex) throws PedDevException {
        ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
        if (tdkKcv == null || tdkKcv.length == 0) {
            checkMode = ECheckMode.KCV_NONE;
        }
        InjectApp.getApp().getIped().writeKey(EPedKeyType.TMK, (byte) tmkIndex,
                EPedKeyType.TDK, (byte) tdkIndex, tdkValue, checkMode, tdkKcv);
    }


    /**
     * writeTIK(byte groupIndex, byte srcKeyIndex,
     * byte[] keyValue, byte[] ksn, ECheckMode checkMode, byte[] checkBuf)
     */
    public static void writeTIK(int groupIndex, int srcKeyIndex, byte[] tikValue, byte[] ksn) throws PedDevException {
        InjectApp.getApp().getIped().writeTIK((byte) groupIndex, (byte) srcKeyIndex, tikValue,
                ksn, ECheckMode.KCV_NONE, null);
    }
}
