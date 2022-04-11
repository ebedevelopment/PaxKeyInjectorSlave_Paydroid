package cn.pax.odd.keyinjection.utils;

import android.support.annotation.NonNull;

import com.pax.dal.IPed;
import com.pax.dal.entity.DUKPTResult;
import com.pax.dal.entity.EDUKPTDesMode;
import com.pax.dal.entity.EDUKPTMacMode;
import com.pax.dal.entity.EDUKPTPinMode;
import com.pax.dal.entity.EPedDesMode;
import com.pax.dal.entity.EPedMacMode;
import com.pax.dal.entity.EPinBlockMode;
import com.pax.dal.entity.RSAKeyInfo;
import com.pax.dal.entity.RSARecoverInfo;
import com.pax.dal.exceptions.PedDevException;

import cn.pax.odd.keyinjection.DemoApp;
import cn.pax.odd.keyinjection.sdk.InjectApp;
import cn.pax.odd.keyinjection.sdk.utils.ConvertUtils;
import cn.pax.odd.keyinjection.sdk.utils.LogUtils;

/**
 * @author ligq
 * @date 2018/2/26
 */

@SuppressWarnings("unused")
public class KeyUtils {
    private KeyUtils() {

    }

    /**
     * calculate MAC with TAK
     *
     * @param data input data
     * @return MAC value
     */
    @NonNull
    public static byte[] calcMac(String data, byte takIndex) throws PedDevException {
        IPed ped = DemoApp.getApp().getIped();
        return ped.getMac(takIndex,  ConvertUtils.hexString2Bytes(data), EPedMacMode.MODE_00);
    }

    /**
     * calculate DES with TDK
     *
     * @param data input data
     * @return DES value
     * @throws PedDevException exception
     */
    public static byte[] calcDes(String data, byte tdkIndex) throws PedDevException {
        IPed ped = DemoApp.getApp().getIped();
        return ped.calcDes(tdkIndex,  ConvertUtils.hexString2Bytes(data), EPedDesMode.ENCRYPT);
    }

    /**
     * calculate PIN block
     *
     * @param descKeyId descKeyId
     * @param panBlock  shifted pan block
     * @return PIN block
     * @throws PedDevException exception
     */
    public static byte[] getPinBlock(String descKeyId, String panBlock, boolean supportBypass, boolean landscape) throws PedDevException {
        String formatPan = formatPan(panBlock);
        LogUtils.d("formatPan:" + formatPan);
        IPed ped = DemoApp.getApp().getIped();
        String pinLen = "4,5,6,7,8,9,10,11,12";
        if (supportBypass) {
            pinLen = "0," + pinLen;
        }
        ped.setKeyboardLayoutLandscape(landscape);
        return ped.getPinBlock((byte) Integer.parseInt(descKeyId), pinLen, formatPan.getBytes(), EPinBlockMode.ISO9564_0, 60 * 1000);
    }

    private static String formatPan(String panBlock) {
        return "0000" + panBlock.substring(panBlock.length() - 13, panBlock.length() - 1);
    }


    public static DUKPTResult getDukptPin(String descKeyId, String panBlock, boolean supportBypass, boolean landscape) throws PedDevException {
        IPed ped = DemoApp.getApp().getIped();
        String pinLen = "4,5,6,7,8,9,10,11,12";
        if (supportBypass) {
            pinLen = "0," + pinLen;
        }
        ped.setKeyboardLayoutLandscape(landscape);
        return ped.getDUKPTPin((byte) Integer.parseInt(descKeyId), pinLen, formatPan(panBlock).getBytes(), EDUKPTPinMode.ISO9564_0, 60 * 1000);
    }

    /**
     * @param data input data
     * @return MAC value
     */
    public static DUKPTResult calcDuktpMac(String data, byte takIndex) throws PedDevException {
        IPed ped = DemoApp.getApp().getIped();
        return ped.getDUPKTMac(takIndex, ConvertUtils.hexString2Bytes(data), EDUKPTMacMode.MODE_00);
    }

    public static DUKPTResult calcDukptDes(String data, byte tdkIndex) throws PedDevException {
        IPed ped = DemoApp.getApp().getIped();
        return ped.calcDUKPTDes(tdkIndex, (byte) 0x01, null, ConvertUtils.hexString2Bytes(data), EDUKPTDesMode.CBC_ENCRYPTION);
    }

    public static RSARecoverInfo rsaRecoverPublic(byte[] bytes, int privateKeyIndex, int publicKeyIndex) throws PedDevException {
        IPed ped = InjectApp.getApp().getIped();
        return ped.RSARecover((byte) publicKeyIndex, ped.RSARecover((byte) privateKeyIndex, bytes).getData());

    }

    public static RSARecoverInfo rsaRecoverPrivate(byte[] bytes, int privateKeyIndex, int publicKeyIndex) throws PedDevException {
        IPed ped = InjectApp.getApp().getIped();
        RSARecoverInfo rsaRecoverInfo = ped.RSARecover((byte) publicKeyIndex, bytes);
        return ped.RSARecover((byte) privateKeyIndex, rsaRecoverInfo.getData());
    }

    public static RSARecoverInfo rsaRecoverEncPublic(byte[] bytes, int publicKeyIndex) throws PedDevException {
        IPed ped = InjectApp.getApp().getIped();
        return ped.RSARecover((byte) publicKeyIndex, bytes);
    }

    public static RSAKeyInfo rsaReadTest(byte index) throws PedDevException {
        IPed ped = InjectApp.getApp().getIped();
        return ped.readRSAKey(index);
    }

    public static RSARecoverInfo rsaRecoverTest(byte index, byte[] bytes) throws PedDevException {
        IPed ped = InjectApp.getApp().getIped();
        return ped.RSARecover(index, bytes);
    }

    public static void writeRsaKey(RSAKeyInfo info) throws PedDevException {
        InjectApp.getApp().getIped().writeRSAKey((byte) 10, info);
    }

    public static RSAKeyInfo genPrivateKey(String module, String exponent) {
        byte[] moduleByte = ConvertUtils.hexString2Bytes(module);
        byte[] exponentByte = ConvertUtils.hexString2Bytes(exponent);
        RSAKeyInfo info = new RSAKeyInfo();
        info.setExponent(exponentByte);
        info.setExponentLen(exponentByte.length * 8);
        info.setModulus(moduleByte);
        info.setModulusLen(moduleByte.length * 8);
        return info;
    }

    public static byte[] getKsn(String index) throws PedDevException {
        return DemoApp.getApp().getIped().getDUKPTKsn((byte) Integer.parseInt(index));
    }
}
