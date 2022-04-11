package cn.pax.odd.keyinjection.sdk.crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import cn.pax.odd.keyinjection.sdk.utils.LogUtils;

public class RSAAlgorithm {
    public static RSAPublicKey genPublicKey(byte[] modulus, byte[] exponent) {
        try {
            BigInteger b1 = new BigInteger(1, modulus);
            BigInteger b2 = new BigInteger(1, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception var6) {
            LogUtils.e(var6);
            //var6.printStackTrace();
            return null;
        }
    }

    public static RSAPrivateKey genPrivateKey(byte[] modulus, byte[] exponent) {
        try {
            BigInteger b1 = new BigInteger(1, modulus);
            BigInteger b2 = new BigInteger(1, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception var6) {
            //var6.printStackTrace();
            LogUtils.e(var6);
            return null;
        }
    }

    /**
     * RSA/ECB/NoPadding
     *
     * @param key           密钥
     * @param data          待加密数据
     * @param paddingOption 填充方式
     * @return 错误返回new byte[0]，否则正常返回，返回长度是密钥长度整倍数。
     */
    public static byte[] encryptWithPublicKey(RSAPublicKey key, byte[] data,
                                              PaddingOption paddingOption) {
        try {
//            Cipher cipher;
//            if (paddingOption == Rsa.PaddingOption.NO_PADDING) {
//                cipher = Cipher.getInstance("RSA/ECB/NoPadding");
//            } else {
//                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            }

            Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
//            LogUtils.d("encryptWithPublicKey: Cipher Algorithm: " + cipher.getAlgorithm());
//            LogUtils.d("encryptWithPublicKey: Cipher Provider: " + cipher.getProvider());
//            LogUtils.d("encryptWithPublicKey: Cipher Block Size: " + cipher.getBlockSize());
//            LogUtils.d("encryptWithPublicKey: Cipher Parameters: ");
//            System.out.println("encryptWithPublicKey: Cipher Algorithm: " + cipher.getAlgorithm());
//            System.out.println("encryptWithPublicKey: Cipher Provider: " + cipher.getProvider());
//            System.out.println("encryptWithPublicKey: Cipher Block Size: " + cipher.getBlockSize());
//            System.out.println("encryptWithPublicKey: Cipher Parameters: ");
//            AlgorithmParameters algorithmParameters = cipher.getParameters();
//            if (algorithmParameters != null) {
//                //LogUtil.d("encryptWithPublicKey: AlgorithmParameters Algorithm: " + cipher
//                // .getParameters().getAlgorithm());
//                System.out.println("encryptWithPublicKey: AlgorithmParameters Algorithm: " + cipher.getParameters().getAlgorithm());
//            } else {
//                //LogUtil.d("encryptWithPublicKey: AlgorithmParameters null");
//                System.out.println("encryptWithPublicKey: AlgorithmParameters null");
//            }
            int keyLen = key.getModulus().bitLength() / 8;
            byte[][] blocks;
            if (paddingOption == RSAAlgorithm.PaddingOption.NO_PADDING) {
                blocks = a(data, keyLen, paddingOption);
            } else if (paddingOption == RSAAlgorithm.PaddingOption.PKCS1_PADDING) {
                int blockSize = keyLen - 11;
                blocks = a(data, blockSize, paddingOption);
            } else {
                return new byte[0];
            }

            ByteBuffer bb = ByteBuffer.allocate(blocks.length * keyLen);
            byte[][] var11 = blocks;
            int var10 = blocks.length;

            for (int var9 = 0; var9 < var10; ++var9) {
                byte[] block = var11[var9];
                byte[] encryptedBlock = cipher.doFinal(block);
                bb.put(encryptedBlock);
            }

            bb.flip();
            int totalLen = bb.limit();
            byte[] ret = new byte[totalLen];
            bb.position(0);
            bb.get(ret);
            return ret;
        } catch (Exception var13) {
            LogUtils.e(var13);
            return new byte[0];
        }
    }

    /**
     * @param key           密钥
     * @param data          待解密数据
     * @param paddingOption 填充方式
     * @return 错误返回new byte[0]，否则正常返回，返回长度是8的整倍数，且是密钥长度整倍数。
     */
    public static byte[] decryptWithPrivateKey(RSAPrivateKey key, byte[] data,
                                               PaddingOption paddingOption) {
        try {
//            Cipher cipher;
//            if (paddingOption == Rsa.PaddingOption.NO_PADDING) {
//                cipher = Cipher.getInstance("RSA/ECB/NoPadding");
//            } else {
//                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            }
            Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
//            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            paddingOption = Rsa.PaddingOption.PKCS1_PADDING;
            LogUtils.d("decryptWithPrivateKey: Cipher Algorithm: " + cipher.getAlgorithm());
//            System.out.println("decryptWithPrivateKey: Cipher Algorithm: " + cipher.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key);
            int keyLen = key.getModulus().bitLength() / 8;
            byte[][] blocks = a(data, keyLen, paddingOption);
            ByteBuffer bb = ByteBuffer.allocate(blocks.length * keyLen);
            for (byte[] block : blocks) {
                byte[] decryptedBlock = cipher.doFinal(block);
                bb.put(decryptedBlock);
            }

            bb.flip();
            int totalLen = bb.limit();
            byte[] ret = new byte[totalLen];
            bb.position(0);
            bb.get(ret);
            return ret;
        } catch (Exception var12) {
            LogUtils.e(var12);
            return new byte[0];
        }
    }

    /**
     * RSA/ECB/NoPadding
     *
     * @param key           密钥
     * @param data          待加密数据
     * @param paddingOption 填充方式
     * @return 错误返回new byte[0]，否则正常返回，返回长度是密钥长度整倍数。
     */
    public static byte[] encryptWithPrivateKey(RSAPrivateKey key, byte[] data, PaddingOption paddingOption) {
        try {
//            Cipher cipher;
//            if (paddingOption == Rsa.PaddingOption.NO_PADDING) {
//                cipher = Cipher.getInstance("RSA/ECB/NoPadding");
//            } else {
//
//            }
            Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
//            LogUtils.d("encryptWithPrivateKey: Cipher Algorithm: " + cipher.getAlgorithm());
//            LogUtils.d("encryptWithPrivateKey: Cipher Provider: " + cipher.getProvider());
//            LogUtils.d("encryptWithPrivateKey: Cipher Block Size: " + cipher.getBlockSize());
//            LogUtils.d("encryptWithPrivateKey: Cipher Parameters: ");
//            AlgorithmParameters algorithmParameters = cipher.getParameters();
//            if (algorithmParameters != null) {
//                LogUtils.d("encryptWithPrivateKey: AlgorithmParameters Algorithm: " + cipher.getParameters().getAlgorithm());
//            } else {
//                LogUtils.d("encryptWithPrivateKey: AlgorithmParameters null");
//            }

            int keyLen = key.getModulus().bitLength() / 8;
            byte[][] blocks;
            if (paddingOption == PaddingOption.NO_PADDING) {
                blocks = a(data, keyLen, paddingOption);
            } else if (paddingOption == PaddingOption.PKCS1_PADDING) {
                int blockSize = keyLen - 11;
                blocks = a(data, blockSize, paddingOption);
            } else {
                return new byte[0];
            }

            ByteBuffer bb = ByteBuffer.allocate(blocks.length * keyLen);
            byte[][] var11 = blocks;
            int var10 = blocks.length;

            for (int var9 = 0; var9 < var10; ++var9) {
                byte[] block = var11[var9];
                byte[] encryptedBlock = cipher.doFinal(block);
                bb.put(encryptedBlock);
            }

            bb.flip();
            int totalLen = bb.limit();
            byte[] ret = new byte[totalLen];
            bb.position(0);
            bb.get(ret);
            return ret;
        } catch (Exception var13) {
            LogUtils.e(var13);
            return new byte[0];
        }
    }

    /**
     * RSA/ECB/NoPadding
     *
     * @param key           密钥
     * @param data          待解密数据
     * @param paddingOption 填充方式
     * @return 错误返回new byte[0]，否则正常返回，返回长度是8的整倍数，且是密钥长度整倍数。
     */
    public static byte[] decryptWithPublicKey(RSAPublicKey key, byte[] data,
                                              RSAAlgorithm.PaddingOption paddingOption) {
        try {
//            Cipher cipher;
//            if (paddingOption == Rsa.PaddingOption.NO_PADDING) {
//                cipher = Cipher.getInstance("RSA/ECB/NoPadding");
//            } else {
//                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            }
            Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key);
//            LogUtils.d("decrypt With PublicKey: Cipher Algorithm: " + cipher.getAlgorithm());
//            LogUtils.d("decrypt With PublicKey: Cipher Provider: " + cipher.getProvider());
//            LogUtils.d("decrypt With PublicKey: Cipher Block Size: " + cipher.getBlockSize());
//            LogUtils.d("decrypt With PublicKey: Cipher Parameters: ");
//            AlgorithmParameters algorithmParameters = cipher.getParameters();
//            if (algorithmParameters != null) {
//                LogUtils.d("decrypt With PublicKey: AlgorithmParameters Algorithm: " + cipher.getParameters().getAlgorithm());
//            } else {
//                LogUtils.d("decrypt With PublicKey: AlgorithmParameters null");
//            }

            int keyLen = key.getModulus().bitLength() / 8;
            byte[][] blocks = a(data, keyLen, paddingOption);
            ByteBuffer bb = ByteBuffer.allocate(blocks.length * keyLen);
            for (byte[] block : blocks) {
                byte[] decryptedBlock = cipher.doFinal(block);
                bb.put(decryptedBlock);
            }

            bb.flip();
            int totalLen = bb.limit();
            byte[] ret = new byte[totalLen];
            bb.position(0);
            bb.get(ret);
            return ret;
        } catch (Exception var12) {
            LogUtils.e(var12);
            return new byte[0];
        }
    }

    private static byte[][] a(byte[] data, int blockSize, RSAAlgorithm.PaddingOption paddingOption) {
        int x = data.length / blockSize;
        int y = data.length % blockSize;
        int z = 0;
        if (y != 0) {
            z = 1;
        }

        byte[][] arrays = new byte[x + z][];

        for (int i = 0; i < x + z; ++i) {
            byte[] arr = new byte[blockSize];
            if (i == x + z - 1 && y != 0) {
                if (paddingOption == RSAAlgorithm.PaddingOption.PKCS1_PADDING) {
                    arr = new byte[y];
                }

                System.arraycopy(data, i * blockSize, arr, 0, y);
            } else {
                System.arraycopy(data, i * blockSize, arr, 0, blockSize);
            }

            arrays[i] = arr;
        }

        return arrays;
    }

    public enum PaddingOption {
        NO_PADDING,
        PKCS1_PADDING;

        PaddingOption() {
            //noting
        }
    }
}
