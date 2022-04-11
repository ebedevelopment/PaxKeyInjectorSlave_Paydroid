package cn.pax.odd.keyinjection.sdk.standardprotocol;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.support.annotation.NonNull;

import com.pax.dal.entity.RSAKeyInfo;
import com.pax.dal.exceptions.PedDevException;

import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.Map;

import cn.pax.odd.keyinjection.sdk.InjectApp;
import cn.pax.odd.keyinjection.sdk.InjectionProcess;
import cn.pax.odd.keyinjection.sdk.USBHelper;
import cn.pax.odd.keyinjection.sdk.crypto.DESAlgorithm;
import cn.pax.odd.keyinjection.sdk.crypto.RSAAlgorithm;
import cn.pax.odd.keyinjection.sdk.listener.TransportListener;
import cn.pax.odd.keyinjection.sdk.listener.TransportListenerImpl;
import cn.pax.odd.keyinjection.sdk.utils.ConvertUtils;
import cn.pax.odd.keyinjection.sdk.utils.DeviceUtils;
import cn.pax.odd.keyinjection.sdk.utils.LogUtils;
import cn.pax.odd.keyinjection.sdk.utils.PacketUtils;
import cn.pax.odd.keyinjection.sdk.utils.RsaUtils;

public class KeyDownload implements InjectionProcess {
    Map<String, Key> mRSAKeyMap;
    /**
     * the key type which user selects to download.
     */
    private String mInjectType;
    private byte mSrcIndex;
    private byte mDestIndex;
    private String injectTypeFromKeyMaserPOS;
    private byte[] recoverKeyData;
    private TransportListener mTransportListener;

    /**
     * to be written into PED
     */
    private RSAKeyInfo mPAXPublicKeyInfo;
    private RSAKeyInfo mPAXPrivateKeyInfo;

    public KeyDownload(@NonNull Activity context) {
        mTransportListener = new TransportListenerImpl(context);
    }

    /**
     * @param keyType value from {@link PacketUtils.InjectType#IPEK_KSN}
     *                {@link PacketUtils.InjectType#TMK}
     *                {@link PacketUtils.InjectType#TPK}
     *                {@link PacketUtils.InjectType#TDK}
     *                {@link PacketUtils.InjectType#TAK}
     *                {@link PacketUtils.InjectType#RSA_PUB}
     *                {@link PacketUtils.InjectType#RSA_PRI}
     */
    @Override
    public void inject(@NonNull String keyType, byte srcIndex, byte destIndex) {
        /**
         * keyType本该是enum类型
         */
        mInjectType = keyType;
        mSrcIndex = srcIndex;
        mDestIndex = destIndex;

        /**
         * initialization
         */
        USBHelper.setCanceled(true);
        USBHelper.cancel();
        //USBHelper.disconnect();
        //find usb devices
        mTransportListener.onStart("Searching for devices...");
        USBHelper.setCanceled(false);
        UsbDevice usbDevice = USBHelper.findDevice();
        if (usbDevice == null) {
            //stopReceivedNoResponse();
            mTransportListener.onError("USB Devices Not Found");
            return;
        }

        /**
         * 隐藏对话框以免影响USB授权。连接过程是没有取消按键功能的。因为USB设备已经找到，从开始连接
         * 到得到连接的反馈结果时间会很短，所以没必要提供取消按键功能。
         * 需要调用dismiss来隐藏对话框，如果调用hide来隐藏，当授权对话框弹出时，之前被隐藏的对话框会再次
         * 出现。
         */
        mTransportListener.hideDialog();
        /**
         * for test
         * SystemClock.sleep(5000)
         */
        if (!USBHelper.connDevice(usbDevice)) {
            //stopReceivedNoResponse();
            mTransportListener.onError("Connect USB Device Failed");
            return;
        }

        /**
         * 重新显示对话框
         */
        mTransportListener.onConnect("Connecting...");
        if (!handshakeProcess()) {
            return;
        }


        /**
         * Listen to Key Master POS(S80) request.
         * POS(A920): send RSA public key to Key Master POS(S80).
         *
         * POS(A920) response data:
         * STX[0x02] len[xx xx xx] CMD[0x33 0x32]
         * data[monitorRSA + exponent len[0x03] + SN ID['S' 'N'] + SN len[0x0A] + SN[1234567890]]
         * ETX[0x30]
         * lrc[xx]
         *
         *
         */
        mTransportListener.onSend("Sending RSA key...");
        if (!exchangeSessionKey()) {
            return;
        }

        /**
         * Listen to Key Master POS(S80) sending Key data encrypted by RSA Public Key
         * STX[0x02] len[xx xx xx] CMD[0x33 0x31]
         * data[
         * key type[xx xx]
         * + rsa encrypted data 256 bytes[xx xx
         * keyB1 keyB2 keyB3 keyB4 keyB5 keyB6 keyB7 keyB8
         * keyB9 keyB10 keyB11 keyB12 keyB13 keyB14 keyB15 keyB16]
         * + remainder 238 bytes
         * ]
         * ETX[0x30]
         * lrc[xx]
         * */
        if (!fetchKeyDataFromMasterPOS()) {
            byte[] send = {0x02, 0x30, 0x30, 0x34, 0x32, 0x31, 0x31, 0x31, 0x03, 0x00};
            /**
             * 只有在最后一个阶段才发这个结束报文。
             */
            USBHelper.send(PacketUtils.getSendData(send));
            return;
        }

        /**
         * POS(A920) request: send successful or failed confirmation to server.
         * success: STX[0x02] len[0x30 0x30 0x34] CMD[0x32 0x31] data[0x30 0x30] ETX[0x30] lrc[xx]
         * failed: STX[0x02] len[0x30 0x30 0x34] CMD[0x32 0x31] data[0x31 0x31] ETX[0x30] lrc[xx]
         * */
        mTransportListener.onWriting("Writing key...");
        if (!writeKeys()) {
            byte[] send = {0x02, 0x30, 0x30, 0x34, 0x32, 0x31, 0x31, 0x31, 0x03, 0x00};
            USBHelper.send(PacketUtils.getSendData(send));
            return;
        } else {
            byte[] send = {0x02, 0x30, 0x30, 0x34, 0x32, 0x31, 0x30, 0x30, 0x03, 0x00};
            USBHelper.send(PacketUtils.getSendData(send));
        }

        mTransportListener.onSuccess("Inject successfully");
    }

    private boolean handshakeProcess() {

        /**
         * listen to Key Master POS(S80).
         * wait for STX[0x02] len[0x30 0x30 0x32] CMD[0x31 0x30] ETX[0x30] lrc[xx]
         */
        /**
         * 因为原App连接失败的情况出现太频繁了。增加等待时间和尝试次数来解决这个问题。
         * 等待时间值和尝试次数是随便取的。程序整体结构已定型，顾用了比较粗糙方法来解决这个问题。
         * 正常写法应该用非阻塞或者阻塞，监听取消事件，处理断开连接和回收连接资源.
         */
        while (true) {
            byte[] handshakeData = new byte[]{
                    0x02, 0x30, 0x30, 0x32, 0x31, 0x30, 0x03, 0x00
            };
            byte[] revData;
            try {
                revData = USBHelper.received(handshakeData.length);
            } catch (Exception e) {
                /**
                 * 用户主动取消注密钥或者接收异常
                 */
//                stopReceived("Injection Canceled");
                mTransportListener.onError("Receiving Failed");
                return false;
            }

            byte[] tmp1 = Arrays.copyOf(handshakeData, handshakeData.length - 1);
            byte[] tmp2 = Arrays.copyOf(revData, revData.length - 1);
            boolean isEqual = Arrays.equals(tmp1, tmp2);
            if (revData.length == handshakeData.length && isEqual) {
                /**
                 * POS (A920) response.
                 * STX[0x02] len[0x30 0x30 0x34] CMD[0x31 0x30] data[0x30 0x30] ETX[0x30] lrc[xx]
                 */
                byte[] temp = {0x02, 0x30, 0x30, 0x34, 0x31, 0x30, 0x30, 0x30, 0x03, 0x00};
                byte[] send = PacketUtils.getSendData(temp);
                if (!USBHelper.send(send)) {
                    mTransportListener.onError("handshaking failed, please try again");
                    return false;
                }

                break;
            }

            /**
             * 为了避免Android端本次received超时到下一次
             * 准备好received的这个期间，又收到Monitor发过来握手数据。
             * Android端才清除发送和接收缓冲区。
             */
            InjectApp.getApp().getUsbHost().reset();
        }

        return true;
    }

    private boolean exchangeSessionKey() {
        try {
            /**
             * KEY length 1024 bits
             */
            mRSAKeyMap = RsaUtils.generateKeys();
            assert mRSAKeyMap != null;
//            RSAKeyInfo privateKey = RsaUtils.getPAXPrivateKey(mRSAKeyMap);
            RSAKeyInfo publicKey = RsaUtils.getPAXPublicKey(mRSAKeyMap);

//            LogUtils.d("private key info:" + ConvertUtils.bytes2HexString(privateKey.getKeyInfo()));
//            LogUtils.d("privateKey Modulus:" + ConvertUtils.bytes2HexString(privateKey.getModulus()));
//            LogUtils.d("privateKey Modulus:" + privateKey.getModulus().length);
//            LogUtils.d("privateKey Exponent:" + ConvertUtils.bytes2HexString(privateKey.getExponent()));
            // save RSA private key for later decryption operation
            //InjectApp.getApp().getIped().writeRSAKey(RSA_PRIVATE_KEY_ID, privateKey);


            byte[] sendPubKey = PacketUtils.getPubSendData(publicKey);
            LogUtils.d("publicKey Modulus:" + ConvertUtils.bytes2HexString(publicKey.getModulus()));
            LogUtils.d("publicKey Modulus length:" + publicKey.getModulus().length);
            LogUtils.d("publicKey Exponent:" + ConvertUtils.bytes2HexString(publicKey.getExponent()));
            LogUtils.d("sending public key:" + ConvertUtils.bytes2HexString(sendPubKey));
            //SystemClock.sleep(2000);
            if (!USBHelper.send(sendPubKey)) {
                mTransportListener.onError("Sending RSA Public Key Failed");
                return false;
            }
        } catch (Exception e) {
            mTransportListener.onError(e.getMessage());
            //stopReceived("RSA Key Data Exception");
            return false;
        }

//        /**
//         * listen to Key Master POS(S80) sending RSA public request.
//         * wait for STX[0x02] len[0x30 0x30 0x32] CMD[0x33 0x33] ETX[0x30] lrc[xx]
//         */
//        byte[] rsaPubRequest = new byte[]{
//                0x02, 0x30, 0x30, 0x32, 0x33, 0x32, 0x03, 0x00
//        };
//        byte[] revData;
//        try {
//            revData = USBHelper.received(rsaPubRequest.length);
//        } catch (Exception e) {
//            /**
//             * 用户主动取消注密钥或者接收异常
//             */
////            stopReceived("Injection Canceled");
//            mTransportListener.onError("Receiving Failed");
//            return false;
//        }
//
//        byte[] tmp1 = Arrays.copyOf(rsaPubRequest, rsaPubRequest.length - 1);
//        byte[] tmp2 = Arrays.copyOf(revData, revData.length - 1);
//        boolean isEqual = Arrays.equals(tmp1, tmp2);
//        if (revData.length != rsaPubRequest.length || !isEqual) {
//            //stopReceived("RSA Public Key Request Data Error");
//            mTransportListener.onError("RSA Public Key Request Data Error");
//            return false;
//        }

        return true;
    }

    private boolean fetchKeyDataFromMasterPOS() {
        mTransportListener.onReceive("Receiving key...");
        try {
            byte[] revData = USBHelper.received(4);
            LogUtils.d("key data(receive data after public key sent):" + ConvertUtils.bytes2HexString(revData));
            if (revData.length != 4) {
                mTransportListener.onError("Key Data Received Error");
                return false;
            }

            int len = (revData[1] & 0x0F) * 100 + (revData[2] & 0x0F) * 10 + (revData[3] & 0x0F);
            /**
             * [data] + [ETX] + [LRC]
             */
            revData = USBHelper.received(len + 2);
            if (revData.length != len + 2) {
                mTransportListener.onError("Key Data Received Error");
                return false;
            }

            byte[] keyType = new byte[]{revData[2], revData[3]};
            injectTypeFromKeyMaserPOS = ConvertUtils.bytes2HexString(keyType);
            LogUtils.d("key data(receive data after public key sent):" + ConvertUtils.bytes2HexString(revData));
            LogUtils.d("key data(receive key type):" + injectTypeFromKeyMaserPOS);
            LogUtils.d("key data(injecting key type):" + mInjectType);
            if (!matchKeyType()) {
                return false;
            }

            return handleKeyData(revData);
        } catch (Exception e) {
            mTransportListener.onError("Key Data Received Error");
            return false;
        }
    }

    private boolean handleKeyData(byte[] keyResp) {
        try {
            /* TMK, TPK, TDK, TAK RSA content */
            byte[] content = Arrays.copyOfRange(keyResp, 4, keyResp.length - 2);
            LogUtils.d("key data receive : " + ConvertUtils.bytes2HexString(keyResp));
            LogUtils.d("key data content(ciphertext):" + ConvertUtils.bytes2HexString(content));
            if (PacketUtils.InjectType.IPEK_KSN.equals(injectTypeFromKeyMaserPOS)
                    || PacketUtils.InjectType.TMK.equals(injectTypeFromKeyMaserPOS)
                    || PacketUtils.InjectType.TPK.equals(injectTypeFromKeyMaserPOS)
                    || PacketUtils.InjectType.TDK.equals(injectTypeFromKeyMaserPOS)
                    || PacketUtils.InjectType.TAK.equals(injectTypeFromKeyMaserPOS)
            ) {
                if (content.length != 256) {
                    mTransportListener.onError("Key Data Received Error");
                    return false;
                }
                byte[] keyEncryptedByRSA = Arrays.copyOfRange(content, 0, 256);
                //RSARecoverInfo rsaRecoverInfo =
                //        InjectApp.getApp().getIped().RSARecover(RSA_PRIVATE_KEY_ID,
                //        keyEncryptedByRSA);
                //byte[] data = rsaRecoverInfo.getData();
                byte[] data = RSAAlgorithm.decryptWithPrivateKey((RSAPrivateKey) mRSAKeyMap
                                .get(RsaUtils.PRIVATE_KEY), keyEncryptedByRSA,
                        RSAAlgorithm.PaddingOption.NO_PADDING);
                LogUtils.d("key data blocks(plaintext):" + ConvertUtils.bytes2HexString(data));

                /**
                 * byte[] data recovered by RSA private key  has two bytes of [\x00] in the header
                 *
                 * skip [00, 00]
                 */
                recoverKeyData = Arrays.copyOfRange(data, 2, 256);
                LogUtils.d("key data (plaintext):" + ConvertUtils.bytes2HexString(recoverKeyData));

//                byte[] miscDataLength = Arrays.copyOfRange(content, 256, 256+2);
//                int miscLength = Integer.parseInt(ConvertUtils.bytes2HexString(miscDataLength));
//                if (content.length < 258 + miscLength)
//                    return false;
//                mMiscDataBytes = Arrays.copyOfRange(content, 258, 258 + miscLength);
            } else if (PacketUtils.InjectType.RSA_PUB.equals(injectTypeFromKeyMaserPOS)) {
                /* RSA */
                /**
                 * 此次收到的是Monitor端公钥结构体。这段代码没有严格根据总长度去解析数据，所以遇到数据
                 * 长度不对的情况时，容易运行错误。
                 * Monitor端公钥结构体：2字节短整型模长(位个数) + 256字节模，左补零右对齐 + 256字节指
                 * 数，左补零右对齐
                 */
                final int FIXED_LEN = 2 + 256 + 256 + 2;
                //结构体4字节对齐，最终导致最终结构体是4的整倍数。模长是short类型，后边紧跟char类型，
                //导致char数据紧跟填充到了结构体的第3个地址（结构体首地址从第1个地址开始），最终指数
                //后边补充了两个字节的0x00。
                if (content.length != FIXED_LEN) {
                    mTransportListener.onError("Key Data Received Error[Public Key]");
                    return false;
                }

                /**
                 * 注意模数据占用的字节数和模的位数的区别。
                 *
                 */
                byte[] modulusLenByte = Arrays.copyOfRange(content, 0, 2);
                /**
                 * byte length: 256
                 */
                int modulusBits = PacketUtils.getModulusLen(modulusLenByte) << 3;
                byte[] modulusByte = Arrays.copyOfRange(content, 2, (modulusBits >> 3) + 2);
                /**
                 * 此次收到的是Monitor端公钥结构体。这段代码没有严格根据总长度去解析数据，所以遇到数据
                 * 长度不对的情况时，容易运行错误。原先，Monitor传送结构体这种方式会存在字节对齐，
                 * 字节序与空洞的问题。
                 * Monitor端公钥结构体：2字节短整型模长(位数) + 256字节模，左补零右对齐 + 256字节指数，左补
                 * 零右对齐
                 */
                byte[] exponentByte = Arrays.copyOfRange(content, 2 + 256, FIXED_LEN - 2);
                int i;
                for (i = exponentByte.length - 1; i >= 0; i--) {
                    if (exponentByte[i] != (byte) 0x00) {
                        break;
                    }
                }

                if (i < 0) {
                    mTransportListener.onError("Key Data Received Error[Pub Exponent length]");
                    return false;
                }

                /**
                 * get bits length. 从Neptune demo来看, 模和指数的位长是按照字节乘以8来计算的。
                 * 而JDK中的位长是按照模和指数占用的实际位数来计算的。比如，模和指数都是03xxxxxxxxxxx，
                 * 如果按照JDK算法，03中只有最低的2个bit会算在长度内，而按照Neptune的算法03这个字节的
                 * 8个bit都应该算作长度。
                 */
                int exponentBits = (i + 1) << 3;
//                int exponentBits = (exponentByte.length) << 3;
                /**
                 * 此次收到的是Monitor端公钥结构体。这段代码没有严格根据总长度去解析数据，所以遇到数据
                 * 长度不对的情况时，容易运行错误。原先，Monitor传送结构体这种方式会存在字节对齐，
                 * 节序与空洞的问题。
                 * Monitor端公钥结构体：2字节短整型模长 + 256字节模，左补零右对齐 + 256字节指数，左补
                 * 零右对齐
                 */
                exponentByte = Arrays.copyOfRange(exponentByte, 0, i + 1);
                LogUtils.d("key data(RSA key Public modulus):" + ConvertUtils.bytes2HexString(modulusByte));
                LogUtils.d("key data(RSA key Public modulus length[bits]):"
                        + modulusBits);
                LogUtils.d("key data(RSA key Public exponent):" + ConvertUtils.bytes2HexString(exponentByte));
                LogUtils.d("key data(RSA key Public exponent length[bits]):"
                        + exponentBits);
                mPAXPublicKeyInfo = new RSAKeyInfo();
                mPAXPublicKeyInfo.setModulusLen(modulusBits);
                mPAXPublicKeyInfo.setModulus(modulusByte);
                mPAXPublicKeyInfo.setExponent(exponentByte);
                mPAXPublicKeyInfo.setExponentLen(exponentBits);
            } else if (PacketUtils.InjectType.RSA_PRI.equals(injectTypeFromKeyMaserPOS)) {
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
                 *
                 * RSA私钥传输格式:
                 * RSA private key transmission protocol:
                 * 3des cipher key[256 bytes] + cipher modulus[256 bytes] + cipher exponent[256
                 * bytes]
                 *
                 * 3des plaintext can be gotten through decrypting 3des cipher key[256 byte] with
                 * using RSA transmission key.
                 * 3des plaintext = [00 00] + [16-byte key] + [any other unuseful 238 bytes].
                 * Plaintext of modulus and exponent can be acquired through decrypting
                 * cipher modulus[256 byte] and cipher exponent[256 byte] with using 3des plaintext
                 * key.
                 */
                //byte[] data = Arrays.copyOfRange(content, 0, 128);
                /**
                 * API要求数据长度必须跟模长相等。而会话密钥的模长是1024bits
                 */
                if (content.length < 256 * 3) {
                    mTransportListener.onError("Key Data Received Error[Private Key length]");
                    return false;
                }

                byte[] data = Arrays.copyOfRange(content, 0, 128);
                byte[] plaintext = RSAAlgorithm.decryptWithPrivateKey((RSAPrivateKey) mRSAKeyMap
                                .get(RsaUtils.PRIVATE_KEY), data,
                        RSAAlgorithm.PaddingOption.NO_PADDING);
                LogUtils.d("key data(RSA key plaintext):" + ConvertUtils.bytes2HexString(plaintext));
                /**
                 * skip [00 00]
                 */
                byte[] desKey = Arrays.copyOfRange(plaintext, 2, 18);
                LogUtils.d("key data(3des key plaintext):" + ConvertUtils.bytes2HexString(desKey));
                /**
                 * 私钥模长度固定传输256个字节，固定占用2048bits
                 */
                data = Arrays.copyOfRange(content, 256, 256 + 512);
                plaintext = DESAlgorithm.ecb3DesDecryption(desKey, data);
                mPAXPrivateKeyInfo = new RSAKeyInfo();
                mPAXPrivateKeyInfo.setModulusLen(2048);
                byte[] modulusByte = Arrays.copyOfRange(plaintext, 0, 256);
                mPAXPrivateKeyInfo.setModulus(modulusByte);
                /**
                 * 私钥指数长度固定传输256个字节，但是含有填充的0x00
                 */
                byte[] exponentByte = Arrays.copyOfRange(plaintext, 256, 512);
                /**
                 * 获取指数位数，设置指数长度. 去掉指数中填充的0x00
                 * 从Neptune demo来看, 模和指数的位长是按照字节乘以8来计算的。
                 * 而JDK中的位长是按照模和指数占用的实际位数来计算的。比如，模和指数都是03xxxxxxxxxxx，
                 * 如果按照JDK算法，03中只有最低的2个bit会算在长度内，而按照Neptune的算法03这个字节的
                 * 8个bit都应该算作长度。
                 */
                int i;
                for (i = 0; i < exponentByte.length; i++) {
                    if (exponentByte[i] != (byte) 0x00) {
                        break;
                    }
                }
                if (i >= exponentByte.length) {
                    mTransportListener.onError("Key Data Received Error[Pub Exponent length]");
                    return false;
                }
                int exponentBits = (exponentByte.length - i) << 3;
                /**
                 * 去掉指数中填充的0x00
                 */
                exponentByte = Arrays.copyOfRange(exponentByte, i, exponentByte.length);
                mPAXPrivateKeyInfo.setExponentLen(exponentBits);
                mPAXPrivateKeyInfo.setExponent(exponentByte);
                LogUtils.d("key data(RSA key Private modulus):" + ConvertUtils.bytes2HexString(modulusByte));
                LogUtils.d("key data(RSA key Public modulus length[bits]): 2048");
                LogUtils.d("key data(RSA key Private exponent):" + ConvertUtils.bytes2HexString(exponentByte));
                LogUtils.d("key data(RSA key Private exponent length[bits]):" + exponentBits);
            }
            return true;
        } catch (Exception e) {
            //pde = e;
            //ConnHelper.sendWriteKeyFailed();
            return false;
        }
    }

    private boolean writeKeys() {
//        LogUtils.d("desc kid:" + mKidEntity.getDescKid());
//        LogUtils.d("source kid:" + mKidEntity.getSourceKid());
        switch (mInjectType) {
            case PacketUtils.InjectType.IPEK_KSN:
                return writeDukpt();
            case PacketUtils.InjectType.TMK:
                return writeTmk();
            case PacketUtils.InjectType.TWK:
                if (PacketUtils.InjectType.TPK.equals(injectTypeFromKeyMaserPOS)) {
                    return writeTpk();
                } else if (PacketUtils.InjectType.TDK.equals(injectTypeFromKeyMaserPOS)) {
                    return writeTdk();
                } else if (PacketUtils.InjectType.TAK.equals(injectTypeFromKeyMaserPOS)) {
                    return writeTak();
                } else {
                    return false;
                }
            case PacketUtils.InjectType.RSA:
                if (PacketUtils.InjectType.RSA_PUB.equals(injectTypeFromKeyMaserPOS)) {
                    return writeRsa(PacketUtils.InjectType.RSA_PUB);
                } else if (PacketUtils.InjectType.RSA_PRI.equals(injectTypeFromKeyMaserPOS)) {
                    return writeRsa(PacketUtils.InjectType.RSA_PRI);
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    private boolean writeRsa(@NonNull String type) {
        try {
            if (type.equals(PacketUtils.InjectType.RSA_PRI)) {
                InjectApp.getApp().getIped().writeRSAKey(mDestIndex, mPAXPrivateKeyInfo);
            } else if (type.equals(PacketUtils.InjectType.RSA_PUB)) {
                InjectApp.getApp().getIped().writeRSAKey(mDestIndex, mPAXPublicKeyInfo);
            }
//            ConnHelper.sendWriteKeySuccess();
            return true;
        } catch (PedDevException e) {
            mTransportListener.onError(e.getMessage());
//            ConnHelper.sendWriteKeyFailed();
        }
        return false;
    }

    private boolean writeTpk() {
        try {
            byte[] key = Arrays.copyOfRange(recoverKeyData, 0, 16);
            LogUtils.d("key data tpk:" + ConvertUtils.bytes2HexString(key));

            DeviceUtils.writeTPK(key, null, mSrcIndex, mDestIndex);
//            ConnHelper.sendWriteKeySuccess();
            return true;
        } catch (Exception e) {
            mTransportListener.onError(e.getMessage());
//            ConnHelper.sendWriteKeyFailed();
        }
        return false;
    }

    private boolean writeTdk() {
        try {
            byte[] key = Arrays.copyOfRange(recoverKeyData, 0, 16);
            LogUtils.d("key data tdk:" + ConvertUtils.bytes2HexString(key));

            DeviceUtils.writeTDK(key, null, mSrcIndex, mDestIndex);
//            ConnHelper.sendWriteKeySuccess();
            return true;
        } catch (Exception e) {
            mTransportListener.onError(e.getMessage());
//            ConnHelper.sendWriteKeyFailed();
        }
        return false;
    }

    private boolean writeTak() {
        try {
            byte[] key = Arrays.copyOfRange(recoverKeyData, 0, 16);
            LogUtils.d("key data tak:" + ConvertUtils.bytes2HexString(key));
            DeviceUtils.writeTAK(key, null, mSrcIndex, mDestIndex);
            //ConnHelper.sendWriteKeySuccess();
            return true;
        } catch (Exception e) {
            mTransportListener.onError(e.getMessage());
            //ConnHelper.sendWriteKeyFailed();
        }
        return false;
    }

    /**
     * 注TMK只支持明文。Android端注密钥程序不支持注TLK。
     */
    private boolean writeTmk() {
        try {
//            if (mKidEntity.isEnc()) {
//                DeviceUtils.writeTMK(mKidEntity.getSourceKid(), mKidEntity.getDescKid(), recoverKeyData);
//            } else {
//                DeviceUtils.writeTMK(mKidEntity.getDescKid(), recoverKeyData);
//            }
            byte[] key = Arrays.copyOfRange(recoverKeyData, 0, 16);
            LogUtils.d("key data tmk:" + ConvertUtils.bytes2HexString(key));
            DeviceUtils.writeTMK(mSrcIndex, mDestIndex, key);

//            if (miscDataBytesToMiscData() == null) {
//                ConnHelper.sendWriteKeyFailed();
//                return false;
//            }

            //ConnHelper.sendWriteKeySuccess();
            return true;
        } catch (Exception e) {
            mTransportListener.onError(e.getMessage());
            //ConnHelper.sendWriteKeyFailed();
        }
        return false;
    }

    /**
     * 注密钥程序不支持TLK的注入，所以当注入TIK时只支持以明文方式注入
     */
    private boolean writeDukpt() {
        try {
            byte[] ipek = Arrays.copyOfRange(recoverKeyData, 0, 16);
            byte[] ksn = new byte[10];
            /**
             * KEY POS server only transfers previous 8 bytes of KSN
             */
            System.arraycopy(recoverKeyData, 16, ksn, 0, 8);
            LogUtils.d("key data ipek:" + ConvertUtils.bytes2HexString(ipek));
            LogUtils.d("key data ksn:" + ConvertUtils.bytes2HexString(ksn));
            DeviceUtils.writeTIK(mDestIndex, mSrcIndex, ipek, ksn);
            //ConnHelper.sendWriteKeySuccess();
            return true;
        } catch (Exception e) {
            mTransportListener.onError(e.getMessage());
            LogUtils.e(e);
            //ConnHelper.sendWriteKeyFailed();
        }
        return false;
    }

    /**
     * 设置错误提示信息
     */
    private boolean matchKeyType() {
        if (mInjectType.equals(PacketUtils.InjectType.TMK)
                && !PacketUtils.InjectType.TMK.equals(
                injectTypeFromKeyMaserPOS)) {
            mTransportListener.onError("Please Inject TMK on Key Master POS");
            return false;
        } else if (mInjectType.equals(PacketUtils.InjectType.TWK)
                && !(PacketUtils.InjectType.TPK.equals(injectTypeFromKeyMaserPOS)
                || PacketUtils.InjectType.TDK.equals(injectTypeFromKeyMaserPOS)
                || PacketUtils.InjectType.TAK.equals(injectTypeFromKeyMaserPOS))
        ) {
            mTransportListener.onError("Please Inject TPK/TDK/TAK on Key Master POS");
            return false;
        } else if (mInjectType.equals(PacketUtils.InjectType.IPEK_KSN)
                && !PacketUtils.InjectType.IPEK_KSN.equals(injectTypeFromKeyMaserPOS)) {
            mTransportListener.onError("Please Inject DUKPT Key on Key Master POS");
            return false;
        } else if (mInjectType.equals(PacketUtils.InjectType.RSA)
                && !(PacketUtils.InjectType.RSA_PUB.equals(injectTypeFromKeyMaserPOS)
                || PacketUtils.InjectType.RSA_PRI.equals(injectTypeFromKeyMaserPOS))
        ) {
            mTransportListener.onError("Please Inject RSA Key on Key Master POS");
            return false;
        }

        return true;
    }

}
