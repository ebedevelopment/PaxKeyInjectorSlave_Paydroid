package cn.pax.odd.keyinjection.utils;

import android.text.TextUtils;

import cn.pax.odd.keyinjection.sdk.InjectApp;
import cn.pax.odd.keyinjection.sdk.utils.ConvertUtils;
import cn.pax.odd.keyinjection.sdk.utils.LogUtils;


/**
 * @author ligq
 * @date 2018/2/26
 */

public class Util {
//    private static final char[] BToA = "0123456789abcdef".toCharArray();

    public static boolean checkEmpty(String data) {
        return TextUtils.isEmpty(data);
    }

    public static byte[] formatEncData(String encData) {
        byte[] data = asciiToBcd(encData.getBytes());
        byte[] result = new byte[1032 / 8];
        System.arraycopy(data, 0, result, 2, data.length);
        return result;
    }

    public static String getString(int id) {
        return InjectApp.getApp().getString(id);
    }

    public static String bcdToStr(byte[] b) throws IllegalArgumentException {
        if (b == null) {
            LogUtils.e("bcdToStr input arg is null");
            throw new IllegalArgumentException("bcdToStr input arg is null");
        }

        char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }

        return sb.toString();
    }

    public static byte[] strToBcd(String str, ConvertUtils.EPaddingPosition paddingPosition) throws IllegalArgumentException {
        if (str == null || paddingPosition == null) {
            LogUtils.e("strToBcd input arg is null");
            throw new IllegalArgumentException("strToBcd input arg is null");
        }

        int len = str.length();
        int mod = len % 2;
        if (mod != 0) {
            if (paddingPosition == ConvertUtils.EPaddingPosition.PADDING_RIGHT) {
                str = str + "0";
            } else {
                str = "0" + str;
            }
            len = str.length();
        }

        byte[] abt;
        if (len >= 2) {
            len = len / 2;
        }
        byte[] bbt = new byte[len];
        abt = str.getBytes();
        int j, k;
        for (int p = 0; p < str.length() / 2; p++) {
            if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else if ((abt[2 * p] >= 'A') && (abt[2 * p] <= 'Z')) {
                j = abt[2 * p] - 'A' + 0x0a;
            } else {
                j = abt[2 * p] - '0';
            }

            if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else if ((abt[2 * p + 1] >= 'A') && (abt[2 * p + 1] <= 'Z')) {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            } else {
                k = abt[2 * p + 1] - '0';
            }

            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    public static byte ascToBcd(byte asc) {
        return (byte) (asc - 48);
    }

    public static byte[] asciiToBcd(byte[] ascii) {
        byte[] bcd = new byte[ascii.length];
        for (int i = 0; i < ascii.length; i++) {
            bcd[i] = ascToBcd(ascii[i]);
        }
        return bcd;
    }

    public static byte bcdToAsc(byte bcd) {
        return (byte) (bcd + 48);
    }


    public static byte[] bcd2asc(byte[] bcd) {
        byte[] ascii = new byte[bcd.length];
        for (int i = 0; i < ascii.length; i++) {
            ascii[i] = bcdToAsc(bcd[i]);
        }
        return ascii;
    }

    public static class FastClick {
        private static final int INTERVAL_TIME = 500;
        private static long lastTime = 0;

        private FastClick() {
        }

        public static synchronized boolean isFastClick() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime >= INTERVAL_TIME) {
                return false;
            }

            lastTime = currentTime;
            return true;
        }
    }

}
