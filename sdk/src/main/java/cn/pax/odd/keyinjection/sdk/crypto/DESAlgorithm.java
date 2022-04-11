package cn.pax.odd.keyinjection.sdk.crypto;

import android.annotation.SuppressLint;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import cn.pax.odd.keyinjection.sdk.utils.LogUtils;

public class DESAlgorithm {
    /**
     * ECB single des with NoPadding
     *
     * @param key des key, must be equal 8
     *            <p>
     *            ECB encryption mode should not be used (was "DES/ECB/NoPadding") less... (Ctrl+F1)
     *            Inspection info:Cipher#getInstance should not be called with ECB as the cipher
     *            mode or without setting the cipher mode because the default mode on android is ECB, which is insecure
     */
    public static byte[] ecbDes(byte[] key, byte[] data, boolean enc) {
        if (key.length != 8) {
            return new byte[0];
        }

        try {
            DESKeySpec desKey = new DESKeySpec(key);

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKey);
            final String alg = "DES/ECB/NoPadding";
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(alg);
            if (enc) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }

            return cipher.doFinal(data);
        } catch (Exception e) {
            LogUtils.d(e);
        }

        return new byte[0];
    }

    public static byte[] ecb3DesEncryption(byte[] key, byte[] data) {
        return ecb3Des(key, data, true);
    }

    public static byte[] ecb3DesDecryption(byte[] key, byte[] data) {
        return ecb3Des(key, data, false);
    }

    /**
     * ECB triple des with NoPadding
     *
     * @param key des key, must be equal 16 or 24
     * @return new byte[0] if errors happen
     * <p>
     * ECB encryption mode should not be used (was "DES/ECB/NoPadding") less... (Ctrl+F1)
     * Inspection info:Cipher#getInstance should not be called with ECB as the cipher
     * mode or without setting the cipher mode because the default mode on android is ECB, which is insecure
     */
    public static byte[] ecb3Des(byte[] key, byte[] data, boolean enc) {
        if (key.length != 16 && key.length != 24) {
            return new byte[0];
        }

        try {
            if (key.length == 16) {
                byte[] temp = new byte[24];
                System.arraycopy(key, 0, temp, 0, 16);
                System.arraycopy(key, 0, temp, 16, 8);
                key = temp;
            }
//            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            SecretKey secretKey = new SecretKeySpec(key, "DESede");
            final String alg = "DESede/ECB/NoPadding";
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(alg);
            if (enc) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }

            return cipher.doFinal(data);
        } catch (Exception e) {
            LogUtils.d(e);
        }

        return new byte[0];
    }
}
