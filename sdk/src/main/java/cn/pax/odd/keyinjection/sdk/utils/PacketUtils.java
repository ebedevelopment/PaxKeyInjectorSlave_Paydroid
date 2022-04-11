package cn.pax.odd.keyinjection.sdk.utils;

import com.pax.dal.entity.RSAKeyInfo;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author ligq
 * @date 2018/1/24
 */

@SuppressWarnings("unused")
public class PacketUtils {
    private static final int MIN_LEN = 8;

    private PacketUtils() {

    }

    public static byte[] getSendData(byte[] send) {
        if (send.length < MIN_LEN) {
            return new byte[0];
        }

        byte temp = send[1];
        for (int i = 2; i < send.length - 1; i++) {
            temp ^= send[i];
        }
        send[send.length - 1] = temp;
        return send;
    }

    public static byte[] getPubSendData(RSAKeyInfo publicKeyInfo) {
        byte[] bytes = {0x02, 0x30, 0x30, 0x30, 0x33, 0x32};
        /**
         * ${@link RsaUtils#KEY_SIZE}
         * bit length: 1024
         */
        String modulusLen = formatData(Integer.toHexString(publicKeyInfo.getModulusLen()), 4);
        String modulusLenResult = ConvertUtils.str2strLe(modulusLen);
        String sb = ConvertUtils.bytes2HexString(bytes) + modulusLenResult
                + formatData(ConvertUtils.bytes2HexString(publicKeyInfo.getModulus()), 256 * 2)
                + formatData(ConvertUtils.bytes2HexString(publicKeyInfo.getExponent()), 256 * 2)
                + "03";//公钥指数
        //增加sn号,格式如下
        //[83, 78, 49, 48, 48, 56, 50, 48, 50, 57, 51, 53, 49, 55]
        //SN100820293517
        String sn = Utils.getSn();
//        String sn = "0820372381";
        byte[] keyBytes = ConvertUtils.hexString2Bytes(sb);
        if (keyBytes == null) {
            return new byte[0];
        }
        byte[] snTextBytes = "SN".getBytes();
        byte[] lenBytes = {(byte) sn.length()};
        byte[] snBytes = sn.getBytes();
        byte[] temp = new byte[keyBytes.length + snTextBytes.length + lenBytes.length + snBytes.length + 1];
        System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
        System.arraycopy(snTextBytes, 0, temp, keyBytes.length, snTextBytes.length);
        System.arraycopy(lenBytes, 0, temp, keyBytes.length + snTextBytes.length
                , lenBytes.length);
        System.arraycopy(snBytes, 0, temp, keyBytes.length + snTextBytes.length
                + lenBytes.length, snBytes.length);
        byte[] send = Arrays.copyOf(temp, temp.length + 1);


        /**
         * 协议定义的数据长度字段占用3个字节，但每一个字节存放的内容是这个长度的十进制数的百位，十位，个
         * 位的数的ascii码。比如长度等于15，那么这个字段的内容为[0x30, 0x31, 0x35]。所以协议最多只能
         * 传输999个字节的数据内容。
         */
        /**
         * 1个字节的[STX] , 3个字节的长度字段，1个字节的[ETX], 1个字节的LRC
         */
        int intSendLen = send.length - 6;
        String strSendLen = String.valueOf(intSendLen);
        byte[] byteSendLen = strSendLen.getBytes(StandardCharsets.UTF_8);
        if (byteSendLen.length > 3) {
            return new byte[0];
        }
        System.arraycopy(byteSendLen, 0, send, 4 - byteSendLen.length,
                byteSendLen.length);
        return getSendData(send);
    }

    public static boolean checkPubKeyRsp(byte[] resp) {
        byte[] temp = {resp[6], resp[7]};
        String ret = ConvertUtils.bytes2HexString(temp);
        return "3030".equals(ret);
    }

    private static String formatData(String data, int len) {
        if (data == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (data.length() < len) {
            for (int i = 0; i < (len - data.length()); i++) {
                sb.append("0");
            }
        }
        sb.append(data);
        return sb.toString();
    }

    public static byte[] getIpek(byte[] bytes) {
        if (bytes == null) {
            return new byte[0];
        }
        return Arrays.copyOfRange(bytes, 0, 16);
    }

    public static byte[] getKsn(byte[] bytes) {
        if (bytes == null) {
            return new byte[0];
        }
        byte[] result = new byte[10];
        byte[] temp = Arrays.copyOfRange(bytes, 16, 24);
        System.arraycopy(temp, 0, result, 0, temp.length);
        return result;
    }

    /**
     * @param modulusLenByte monitor填充的值是模的位数，并不是模数据占用的字节数
     * @return 返回模数据占用的字节数
     */
    public static int getModulusLen(byte[] modulusLenByte) {
        byte[] reversed = ConvertUtils.reverseArray(modulusLenByte);
        int bits = Integer.parseInt(ConvertUtils.bytes2HexString(reversed), 16);

        return (bits + 7) >>> 3;
    }


    /**
     * Key type：
     * 0x01 0x01: IPEK + KSN;
     * 0x02 0x02: TMK
     * 0x03 0x03: TPK
     * 0x04 0x04: TDK
     * 0x05 0x05: TAK
     */
    public static class InjectType {
        private InjectType() {

        }

        /**
         * when user selecting TWK or RSA in spinner, transfer ${@link InjectType#TWK} into
         * ${@link cn.pax.odd.keyinjection.sdk.InjectionProcess}
         */
        public static final String TWK = "FFF0";
        public static final String RSA = "FFF1";

        public static final String IPEK_KSN = "0101";
        public static final String TMK = "0202";
        public static final String TPK = "0303";
        public static final String TDK = "0404";
        public static final String TAK = "0505";

        /**
         * 由于代码对传输数据进行解析的时候，并没有严格的安装“长度+数据”的格式去解析。就是说当传输的
         * 数据内容含有长度和数据字段，或者传输协议中先后定义了长度和数据字段，但android和母POS两端
         * 的代码都没有严格按照协议来，在这个长度字段上填写正确的值，然后根据这个长度值去取后边的数据，
         * 而是直接以写死或者一个已知的固定长度值的方式去解析数据字段的内容。比如，传输RSA公钥时，有
         * 一个字段表示指数长度，根据代码推测在协议里就是占用2个字节，这两个字节应该填写正确的值，然后
         * 根据这个值去解析后边的数据字段。但是，在解析数据字段的时候，直接以已知的256的方式去解析的。
         *
         * 这种方式直接导致当想传RSA公钥和私钥的时候，不能用一个类型"0606"来传输了，因为公钥和私钥指数
         * 长度不一样，导致最终传输的公钥和私钥数据长度不同。所以不得不增加"0707"来传私钥。
         */
        public static final String RSA_PUB = "0606";
        public static final String RSA_PRI = "0707";
    }

}
