package cn.pax.odd.keyinjection.sdk;

import org.junit.Test;

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
    public void testBit() {

        byte b1 = (byte) 0xF1;
        byte b2 = (byte) 0x82;
        byte b3 = (byte) 0x7F;
        byte b4 = (byte) 0x01;

        int length = 128;
        int exponentBits = (length - 1) << 3;

        System.out.println("exponentBits=" + exponentBits);
        System.out.println("b1=" + bitSize(b1));
        System.out.println("b2=" + bitSize(b2));
        System.out.println("b3=" + bitSize(b3));
        System.out.println("b4=" + bitSize(b4));
    }

    private int bitSize(byte b) {
        int j;
        for (j = 1; j <= 8; j++) {
            b = (byte) ((b & 0xFF) >>> 1);
            if (b == (byte) 0x00) {
                break;
            }
        }

        return j;
    }

    @Test
    public void test2Byte2Int() {
        byte b1 = (byte) 0xF1;
        byte b2 = (byte) 0x82;
        byte b3 = (byte) 0x7F;

//        (int)b1=-15
//        b1=241
//        b2=130
//        b3=127
        System.out.println("(int)b1=" + (int) b1);
        System.out.println("b1=" + byte2Int(b1));
        System.out.println("b2=" + byte2Int(b2));
        System.out.println("b3=" + byte2Int(b3));
    }

    private int byte2Int(byte b) {
        return (b & 0xFF);
    }

    @Test
    public void test1Byte2Int() {
        byte b1 = (byte) 0xF1;
        byte b2 = (byte) 0x82;
        byte b3 = (byte) 0x7F;

        // byte最高位符号位
        // b1=-15
        int v = b1;
        System.out.println("b1=" + v);
        // b2=-126
        v = b2;
        System.out.println("b2=" + v);
        // b3=127
        v = b3;
        System.out.println("b3=" + v);

        byte[] bs1 = new byte[]{b1};
        String s1 = bytes2HexString(bs1);
        System.out.println("s1=" + Integer.parseInt(s1, 16));

        byte[] bs2 = new byte[]{b2};
        String s2 = bytes2HexString(bs2);
        System.out.println("s2=" + Integer.parseInt(s2, 16));

        byte[] bs3 = new byte[]{b3};
        String s3 = bytes2HexString(bs3);
        System.out.println("s3=" + Integer.parseInt(s3, 16));
    }

    private String bytes2HexString(final byte[] bytes) {
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