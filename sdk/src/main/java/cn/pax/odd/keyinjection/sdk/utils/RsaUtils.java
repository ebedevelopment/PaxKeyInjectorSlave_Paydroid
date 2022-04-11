package cn.pax.odd.keyinjection.sdk.utils;

import android.support.annotation.NonNull;

import com.pax.dal.entity.RSAKeyInfo;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * @author ligq
 * @date 2018/1/22
 */

@SuppressWarnings("unused")
public class RsaUtils {
    private static final String KEY_ALGORITHM = "RSA";
    /**
     * 貌似默认是RSA/NONE/PKCS1Padding，未验证
     */
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
    public static final String PUBLIC_KEY = "publicKey";
    public static final String PRIVATE_KEY = "privateKey";
    /**
     * RSA密钥长度必须是64的倍数，在512~65536之间。默认是1024
     */
    private static final int KEY_SIZE = 1024;

    private RsaUtils() {

    }

    public static Map<String, Key> generateKeys() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator
                    .getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            Map<String, Key> keys = new HashMap<>(2);
            keys.put(PUBLIC_KEY, publicKey);
            keys.put(PRIVATE_KEY, privateKey);
            return keys;
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(e);
        }
        return null;
    }

    /**
     * 生成密钥对。注意这里是生成密钥对KeyPair，再由密钥对获取公私钥
     *
     * @return 生成密钥对
     */
    public static Map<String, byte[]> generateKeyBytes() {

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator
                    .getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            Map<String, byte[]> keyMap = new HashMap<>(2);
            keyMap.put(PUBLIC_KEY, publicKey.getEncoded());
            keyMap.put(PRIVATE_KEY, privateKey.getEncoded());
            return keyMap;
        } catch (NoSuchAlgorithmException e) {
            LogUtils.e(e);
        }
        return null;
    }

    /**
     * 还原公钥，X509EncodedKeySpec 用于构建公钥的规范
     */
    public static PublicKey restorePublicKey(byte[] keyBytes) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);

        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            return factory.generatePublic(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LogUtils.e(e);
        }
        return null;
    }

    /**
     * 还原私钥，PKCS8EncodedKeySpec 用于构建私钥的规范
     */
    public static PrivateKey restorePrivateKey(byte[] keyBytes) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                keyBytes);
        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            return factory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LogUtils.e(e);
        }
        return null;
    }

    /**
     * 加密，三步走。
     */
    public static byte[] rsaEncode(PublicKey key, byte[] plainText) {

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            LogUtils.e(e);
        }
        return new byte[0];

    }

    /**
     * 解密，三步走。
     */
    public static String rsaDecode(PrivateKey key, byte[] encodedText) {

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(encodedText));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            LogUtils.e(e);
        }
        return null;

    }

    public static RSAKeyInfo getPAXPublicKey(@NonNull Map<String, Key> keyMap) {
        RSAPublicKey publicKey = (RSAPublicKey) keyMap.get(RsaUtils.PUBLIC_KEY);
        assert publicKey != null;
        RSAKeyInfo publicKeyInfo = new RSAKeyInfo();
        byte[] publicModulus = publicKey.getModulus().toByteArray();
        byte[] publicExponent = publicKey.getPublicExponent().toByteArray();

        /**
         * NOTE android end must filter out the first byte when its value is 0x00. Otherwise
         * there will mismatch happen when android uses PED recover API to recover data which is
         * encrypted by calling Monitor PED recover API or Monitor RSA algorithm in Monitor end.
         * Monitor pads two bytes of 0x00 in front of data, then encrypts these data and transfers
         * the output cipher text to android, finally android will call ped RSA to recover. The
         * recovered result by android is wrong value if android use the modulus including 0x00.
         *
         * if android end filter out the first byte of cipher text sent by Monitor, it's strange
         * that the recover result is right when android calls PED recover API.
         *
         * To avoid these traps, we remove the first byte that's value of 0x00 in the Modulus and
         * Exponent generated by android
         */
        if (publicModulus[0] == (byte) 0x00) {
            publicModulus = Arrays.copyOfRange(publicModulus, 1, publicModulus.length);
        }
        if (publicExponent[0] == (byte) 0x00) {
            publicExponent = Arrays.copyOfRange(publicExponent, 1, publicExponent.length);
        }

        publicKeyInfo.setModulus(publicModulus);
        publicKeyInfo.setExponent(publicExponent);
        publicKeyInfo.setExponentLen(publicExponent.length * 8);
        publicKeyInfo.setModulusLen(publicModulus.length * 8);
        publicKeyInfo.setKeyInfo(publicKey.getEncoded());
//        RSAKeyInfo publicKeyInfo = new RSAKeyInfo();
//        byte[] publicModulus = ConvertUtils.hexString2Bytes("0098AE97D4CC22D818A62ED5F691173DA63881AD1CCD865A0D678676B0E8044ED216CDD74B5C464E424155B9ECA1375D005DE1A5BD7016125B36B6C9FC53197CB7788F99466FF5F410C98F814ACC5BF58149378DAC3BF41C0157A3BD8C48BD366A8ACB9FB00D656D1F4FC2B6D2C8C7304C857D7B5BCB765892B556CFB7B8E09DBB");
//        /* <p> byte[] publicModulus = Arrays.copyOfRange(publicModulusTemp, 1, publicModulusTemp.length);</p>*/
//        byte[] publicExponent = ConvertUtils.hexString2Bytes("010001");
//        publicKeyInfo.setModulus(publicModulus);
//        publicKeyInfo.setExponent(publicExponent);
//        publicKeyInfo.setExponentLen(publicExponent.length * 8);
//        publicKeyInfo.setModulusLen(publicModulus.length * 8);
//        publicKeyInfo.setKeyInfo(publicKey.getEncoded());

        return publicKeyInfo;
    }

    public static RSAKeyInfo getPAXPrivateKey(@NonNull Map<String, Key> keyMap) {
        RSAPrivateKey privateKey = (RSAPrivateKey) keyMap.get(RsaUtils.PRIVATE_KEY);
        assert privateKey != null;
        RSAKeyInfo privateKeyInfo = new RSAKeyInfo();
        byte[] privateModulus = privateKey.getModulus().toByteArray();
        byte[] privateExponent = privateKey.getPrivateExponent().toByteArray();
        if (privateModulus[0] == (byte) 0x00) {
            privateModulus = Arrays.copyOfRange(privateModulus, 1, privateModulus.length);
        }
        if (privateExponent[0] == (byte) 0x00) {
            privateExponent = Arrays.copyOfRange(privateExponent, 1, privateExponent.length);
        }
        privateKeyInfo.setKeyInfo(privateKey.getEncoded());
        privateKeyInfo.setModulus(privateModulus);
        privateKeyInfo.setExponent(privateExponent);
        privateKeyInfo.setExponentLen(privateExponent.length * 8);
        privateKeyInfo.setModulusLen(privateModulus.length * 8);
//        RSAKeyInfo privateKeyInfo = new RSAKeyInfo();
//        byte[] privateModulus = ConvertUtils.hexString2Bytes("0098AE97D4CC22D818A62ED5F691173DA63881AD1CCD865A0D678676B0E8044ED216CDD74B5C464E424155B9ECA1375D005DE1A5BD7016125B36B6C9FC53197CB7788F99466FF5F410C98F814ACC5BF58149378DAC3BF41C0157A3BD8C48BD366A8ACB9FB00D656D1F4FC2B6D2C8C7304C857D7B5BCB765892B556CFB7B8E09DBB");
//        /*<p> byte[] privateModulus = Arrays.copyOfRange(privateModulusTemp, 1, privateModulusTemp.length);</p>*/
//        byte[] privateExponent = ConvertUtils.hexString2Bytes("0E97E17B3D67B72FE3F26611031D2D8F4DA7575C81686E0309FA182E207F08E99BB460F79B023D48A7E601B754516E5A329EF6B07C9E1F73086FC02E95E2C1C0C1BEF34F6ED7985766372D0AE8C415907666C32CEB8E4496DBF9936D550292AAAC1FB54A6107C24CB7B2B9987D01B266157AF38F4CEE22CBF42B15FFCD58E8C1");
//        privateKeyInfo.setKeyInfo(ConvertUtils.hexString2Bytes("30820276020100300D06092A864886F70D0101010500048202603082025C0201000281810098AE97D4CC22D818A62ED5F691173DA63881AD1CCD865A0D678676B0E8044ED216CDD74B5C464E424155B9ECA1375D005DE1A5BD7016125B36B6C9FC53197CB7788F99466FF5F410C98F814ACC5BF58149378DAC3BF41C0157A3BD8C48BD366A8ACB9FB00D656D1F4FC2B6D2C8C7304C857D7B5BCB765892B556CFB7B8E09DBB02030100010281800E97E17B3D67B72FE3F26611031D2D8F4DA7575C81686E0309FA182E207F08E99BB460F79B023D48A7E601B754516E5A329EF6B07C9E1F73086FC02E95E2C1C0C1BEF34F6ED7985766372D0AE8C415907666C32CEB8E4496DBF9936D550292AAAC1FB54A6107C24CB7B2B9987D01B266157AF38F4CEE22CBF42B15FFCD58E8C1024100CAD2DF280D2EB14104CE9690C634CD133DA3753F4CA7501954F754C05B624334C8CF491FE7DD74C7ADADB9D9BC26D03C86351A58BAFE134E29586A679336FF77024100C0B64EF228F2C744388D2042C717DC10B3FCFB2D3B5936EF81D69C2C082B3A98DF504E4DD848BBAC8ED09A6E2758CC4A8698C3FC2DAC4433967ECC2EFD178CDD024100C33AA5591D0F43C94D023A05D2F3C22E721763E571385FAE728DBC4387F316B3B9536594D38FE437CC22BC112CBDC3956EADB0D595F0B8CE7497C99C37252A53024026A4E853DB2E4413441BD8BA4B5E577CE9FAF30B70F944F0E66278D2C39897998C3E785557E7CDEA9E80EA56E881000679D32C89E746C22E6E216324911025E902406A2EB28BA7586DD08F45009F17DDA07E7663648A8BDC3533F83DCB714E1B378290FF80082CF03BD73C6166ED283D5141AD14CA92A8E10DDFD2BE38D0FE393167"));
//        privateKeyInfo.setModulus(privateModulus);
//        privateKeyInfo.setExponent(privateExponent);
//        privateKeyInfo.setExponentLen(privateExponent.length * 8);
//        privateKeyInfo.setModulusLen(privateModulus.length * 8);
        return privateKeyInfo;
    }

}
