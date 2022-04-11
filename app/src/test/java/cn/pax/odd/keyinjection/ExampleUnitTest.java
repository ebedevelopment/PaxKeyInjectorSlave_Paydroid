package cn.pax.odd.keyinjection;

import android.annotation.SuppressLint;

import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

import cn.pax.odd.keyinjection.sdk.utils.LogUtils;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void testECB3Des() {
        byte[] data = new byte[256];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) 0x51;
        }

        byte[] key1 = new byte[16];
        for (int i = 0; i < key1.length; i++) {
            key1[i] = (byte) 0x11;
        }

        System.out.println("key:");
        System.out.println(bytes2HexString(key1));
        System.out.println("data:");
        System.out.println(bytes2HexString(data));
        byte[] plaintext = ecb3Des(key1, data, true);
        System.out.println("encryption output:");
        System.out.println(bytes2HexString(plaintext));

        byte[] key2 = new byte[24];
        for (int i = 0; i < key2.length; i++) {
            key2[i] = (byte) 0x22;
        }

        System.out.println("key:");
        System.out.println(bytes2HexString(key2));
        System.out.println("data:");
        System.out.println(bytes2HexString(data));
        plaintext = ecb3Des(key2, data, false);
        System.out.println("decryption output:");
        System.out.println(bytes2HexString(plaintext));
    }

    public byte[] ecb3Des(byte[] key, byte[] data, boolean enc) {
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
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            SecretKey secretKey = new SecretKeySpec(key, "DESede");
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
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

    public String bytes2HexString(final byte[] bytes) {
        final char[] HEXDIGITS =
                {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        if (bytes == null) {
            return null;
        }
        int len = bytes.length;
        if (len <= 0) {
            return null;
        }
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = HEXDIGITS[bytes[i] >>> 4 & 0x0f];
            ret[j++] = HEXDIGITS[bytes[i] & 0x0f];
        }
        return new String(ret);
    }
}