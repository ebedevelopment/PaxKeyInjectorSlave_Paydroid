package cn.pax.odd.keyinjection;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.KeyboardUtils;
import com.pax.dal.entity.DUKPTResult;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.dal.entity.RSAKeyInfo;
import com.pax.dal.entity.RSARecoverInfo;
import com.pax.dal.exceptions.PedDevException;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import cn.pax.odd.keyinjection.printer.PrinterUtil;
import cn.pax.odd.keyinjection.printer.ReceiptData;
import cn.pax.odd.keyinjection.sdk.InjectApp;
import cn.pax.odd.keyinjection.sdk.InjectionProcess;
import cn.pax.odd.keyinjection.sdk.MiscData;
import cn.pax.odd.keyinjection.sdk.standardprotocol.KeyDownload;
import cn.pax.odd.keyinjection.sdk.utils.ConvertUtils;
import cn.pax.odd.keyinjection.sdk.utils.LogUtils;
import cn.pax.odd.keyinjection.sdk.utils.PacketUtils;
import cn.pax.odd.keyinjection.sdk.utils.ToastUtils;
import cn.pax.odd.keyinjection.utils.KeyUtils;
import cn.pax.odd.keyinjection.utils.Util;


/**
 * @author ligq
 */
public class MainActivity extends BaseActivity {
    //    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
//    private final byte PLAINTEXT_KEY_ID = 0;
//    private final byte CIPHERTEXT_KEY_ID = 1;
//    private final byte TMK_TWK_SYS_TYPE = 1;
//    private final byte DUKPT_SYS_TYPE = 2;
//    private final byte RSA_SYS_TYPE = 3;
    private static final byte MAX_TMK_TWK_INDEX = 100;
    private static final byte MIN_KEY_INDEX = 1;
    private static final byte MAX_DUKPT_GROUP_INDEX = 40;
    private static final byte MAX_RSA_INDEX = 10;
    @Inject
    ReceiptData mReceiptData;
    @Inject
    PrinterUtil mPrinterUtil;
    private Spinner mSpinner;
    private EditText sourceKid;
    private EditText descKid;
    private String mKeySystemType;
    private LinearLayout mEncCheckLayout;
    private CheckBox enc;
    private LinearLayout srcKeyLayout;
    private LinearLayout destKeyLayout;
    private Button injectBtn;
    private int[] funcIds = {R.id.layout_inject, R.id.layout_key_enc, R.id.layout_rsa};
    private EditText etPlainData;
    private TextView tvEncData;
    private EditText etCurrentKSNValue;
    private EditText etKSNValue;
    private CheckBox cbDukpt;
    private Button btTak;
    private Button btTdk;
    private Button btTpk;
    private Button btIncKSN;
    private EditText etKeyIndex;
    private EditText rsaPubKeyIndex;
    private EditText rsaPriKeyIndex;
    private EditText rsaInputData;
    private TextView rsaInputTagLabelVal;
    private TextView rsaOutputCipherData;
    private TextView rsaOutputData;
    private Button btRsaEncDec;
    private ActionBar actionBar;
    //private AtomicBoolean mIsInjecting;

    private String mInjectionType;
    private InjectionProcess mInjectionProcess;

    @Override
    protected void initView() {
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(Util.getString(R.string.function_inject_key));

        mSpinner = findViewById(R.id.spinner_key_sys_selector);

        descKid = findViewById(R.id.et_desc_kid);
        sourceKid = findViewById(R.id.et_source_kid);
        mEncCheckLayout = findViewById(R.id.ll_encrypted_injection_check);

        srcKeyLayout = findViewById(R.id.ll_source);
        destKeyLayout = findViewById(R.id.ll_desc);
        enc = findViewById(R.id.cb_enc);
        injectBtn = findViewById(R.id.bt_inject);

        etPlainData = findViewById(R.id.plain_data);
        etKeyIndex = findViewById(R.id.et_key_index);
        tvEncData = findViewById(R.id.enc_data);
        etCurrentKSNValue = findViewById(R.id.et_current_ksn_value);
        etKSNValue = findViewById(R.id.et_ksn_value);
        cbDukpt = findViewById(R.id.cb_dukpt);
        btTak = findViewById(R.id.tak);
        btTdk = findViewById(R.id.tdk);
        btTpk = findViewById(R.id.tpk);
        btIncKSN = findViewById(R.id.btn_inc_ksn);

        rsaPubKeyIndex = findViewById(R.id.et_rsa_public_index);
        rsaPriKeyIndex = findViewById(R.id.et_rsa_private_index);
        rsaInputTagLabelVal = findViewById(R.id.plain_label_val);
        rsaInputData = findViewById(R.id.rsa_input_data);
        rsaOutputData = findViewById(R.id.rsa_output_data);
        rsaOutputCipherData = findViewById(R.id.rsa_output_cipher_data);
        btRsaEncDec = findViewById(R.id.bt_rsa_enc_dec);

        /**
         * not use file log in APK
         */
//       if (BuildConfig.DEBUG) {
//            if (ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED) {
//                //CrashUtils.init();
//            } else {
//                checkStoragePermission();
//            }
//        }

    }

//    private void checkStoragePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            final String[] permission = {
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//            };
//            if (ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, permission,
//                        REQUEST_WRITE_EXTERNAL_STORAGE);
//            }
//        }
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
//            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                //finish();
//            } else {
//                //CrashUtils.init();
//            }
//        }
//
//    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fun_inject_key:
                actionBar.setTitle(Util.getString(R.string.function_inject_key));
                showLayout(R.id.layout_inject);
                mSpinner.setSelection(0, true);
//                if (mEncCheckLayout.getVisibility() != View.VISIBLE) {
//                    enc.setChecked(false);
//                    mEncCheckLayout.setVisibility(View.VISIBLE);
//                }

                break;
            case R.id.fun_key_enc:
                actionBar.setTitle(Util.getString(R.string.function_key_enc));
                showLayout(R.id.layout_key_enc);
                if (mEncCheckLayout.getVisibility() != View.GONE) {
//                    enc.setChecked(false);
                    mEncCheckLayout.setVisibility(View.GONE);
                }

                if (srcKeyLayout.getVisibility() != View.GONE) {
                    srcKeyLayout.setVisibility(View.GONE);
                }

                break;
            case R.id.fun_RSA_key:
                actionBar.setTitle(Util.getString(R.string.function_rsa));
                showLayout(R.id.layout_rsa);
                if (mEncCheckLayout.getVisibility() != View.GONE) {
//                    enc.setChecked(false);
                    mEncCheckLayout.setVisibility(View.GONE);
                }

                if (srcKeyLayout.getVisibility() != View.GONE) {
                    srcKeyLayout.setVisibility(View.GONE);
                }

                initRSATestUIData();
                break;
            default:
                break;
        }

        if (srcKeyLayout.getVisibility() != View.GONE) {
            srcKeyLayout.setVisibility(View.GONE);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initData() {
//        DemoApp.getApp().runInBackground(connHelper::startConn);
    }

    @Override
    protected void initListeners() {
        // key system
        initKeyTypeItemsClickListener();

        //inject
        initInjectListeners();

        //key test enc
        initKeyTestListeners();

        //rsa test
        initRsaTestListeners();
    }

    private void initRsaTestListeners() {
        initRSATestUIData();
        btRsaEncDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidRSAKeyIndex(rsaPubKeyIndex)
                        || !isValidRSAKeyIndex(rsaPriKeyIndex)) {
                    return;
                }

                String str1 = rsaPubKeyIndex.getText().toString();
                int n1;
                try {
                    n1 = Integer.parseInt(str1);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Key Index Must Be in Number Format",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                str1 = rsaPriKeyIndex.getText().toString();
                int n2;
                try {
                    n2 = Integer.parseInt(str1);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Key Index Must Be in Number Format",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                final String s = "Waiting ...";
                rsaOutputData.setText(s);
                rsaOutputCipherData.setText(s);

                DemoApp.getApp().runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (DemoApp.class) {
                            rsaEncDec((byte) n1, (byte) n2);
                        }
                    }
                });
            }
        });
    }

    private void initRSATestUIData() {
//        final String plainText =
//                "0000000000000000000000000000000000000000000000000000000000000000"
//                        + "0000000000000000000000000000000000000000000000000000000000000000"
//                        + "0000000000000000000000000000000000000000000000000000000000000000"
//                        + "0000000000000000000000000000000000000000000000000000000000000000"
//                        + "0000000000000000000000000000000000000000000000000000000000000000"
//                        + "0000000000000000000000000000000000000000000000000000000000000000"
//                        + "0000000000000000000000000000000000000000000000000000000000000000"
//                        + "0000000000000000000000000000000000000000000000000000000011111111";
//        rsaInputData.setText(plainText);
        rsaInputTagLabelVal.setText(getResources().getString(R.string.input_text_label_val));
    }

    private boolean isValidRSAKeyIndex(@NonNull EditText text) {
        String str1 = text.getText().toString();
        int n1;
        try {
            n1 = Integer.parseInt(str1);
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Key Index Must Be in Number Format",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (n1 < MIN_KEY_INDEX || n1 > MAX_RSA_INDEX) {
            Toast.makeText(MainActivity.this, "Key Index Must Be in Range of " +
                    MIN_KEY_INDEX + "~" + MAX_RSA_INDEX, Toast.LENGTH_SHORT).show();
            text.setText("");
            text.requestFocus();
            return false;
        }

        return true;
    }

    private void rsaEncDec(byte pubIndex, byte priIndex) {
        /*
         * used for developer RSA test
         *
         * PED RSA API require that the length of the data to be encrypted or decrypted must be equal
         * to the one of corresponding Key. E.g if the data length is 2048 bits, then its RSA keys
         * lengths must be 2048 bits too. This value will be changed during Android receives RSA public
         * or private key.
         */

        RSARecoverInfo cipherTextInfo;
        try {
            StringBuffer buffer = new StringBuffer();
            RSAKeyInfo info = KeyUtils.rsaReadTest(pubIndex);
            int modulusLen = info.getModulusLen() >>> 3;
            /**
             * one byte to 2 ascii
             */
            modulusLen = modulusLen << 1;
            String s = rsaInputData.getText().toString();
            int sLen = s.length();
            if (sLen > modulusLen) {
                s = s.substring(sLen - modulusLen);
            } else if (sLen < modulusLen) {
                sLen = modulusLen - sLen;
                for (int i = 0; i < sLen; i++) {
                    buffer.append("0");
                }
                buffer.append(s);
                s = buffer.toString();
            }

            cipherTextInfo = KeyUtils.rsaRecoverTest(pubIndex,
                    ConvertUtils.hexString2Bytes(s));
            updateText(rsaOutputCipherData, ConvertUtils.bytes2HexString(cipherTextInfo.getData()));
        } catch (PedDevException e) {
            updateText(rsaOutputCipherData, e.getMessage());
            LogUtils.d(e);
            return;
        }

        try {
            String cipherText = ConvertUtils.bytes2HexString(cipherTextInfo.getData());
            RSARecoverInfo plainTextInfo = KeyUtils.rsaRecoverTest(priIndex,
                    ConvertUtils.hexString2Bytes(cipherText));
            updateText(rsaOutputData, ConvertUtils.bytes2HexString(plainTextInfo.getData()));
            LogUtils.d("RSA Test Output Data:" + ConvertUtils.bytes2HexString(
                    plainTextInfo.getData()));
        } catch (PedDevException e) {
            updateText(rsaOutputData, e.getMessage());
            LogUtils.d(e);
        }
    }

    /**
     * 切换到主线程
     */
    private void updateText(TextView tv, String msg) {
        DemoApp.getApp().runOnUiThread(() -> tv.setText(msg));
    }

    private void initKeyTypeItemsClickListener() {
        String[] strings = getResources().getStringArray(R.array.key_sys_array);
        List<String> spinnerItemDataList = Arrays.asList(strings);

        // android.R.layout.simple_spinner_item
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item,
                spinnerItemDataList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Resources res = getResources();
                mKeySystemType = parent.getItemAtPosition(position).toString();
                mInjectionType = PacketUtils.InjectType.TMK;
                if (mKeySystemType.equals(res.getString(R.string.TMK_SYS))) {
                    mInjectionType = PacketUtils.InjectType.TMK;
                    descKid.setHint(R.string.tmk_index_hint);
                } else if (mKeySystemType.equals(res.getString(R.string.TWK_SYS))) {
                    mInjectionType = PacketUtils.InjectType.TWK;
                    descKid.setHint(R.string.twk_index_hint);
                } else if (mKeySystemType.equals(res.getString(R.string.DUKPT_SYS))) {
                    mInjectionType = PacketUtils.InjectType.IPEK_KSN;
                    descKid.setHint(R.string.dukpt_index_hint);
                } else if (mKeySystemType.equals(res.getString(R.string.RSA_SYS))) {
                    mInjectionType = PacketUtils.InjectType.RSA;
                    descKid.setHint(R.string.rsa_index_hint);
                }

                if (mKeySystemType.equals(res.getString(R.string.TMK_SYS))
                        || mKeySystemType.equals(res.getString(R.string.DUKPT_SYS))
                        || mKeySystemType.equals(res.getString(R.string.RSA_SYS))) {
                    if (mEncCheckLayout.getVisibility() != View.GONE) {
                        mEncCheckLayout.setVisibility(View.GONE);
                    }

                    if (srcKeyLayout.getVisibility() != View.GONE) {
                        srcKeyLayout.setVisibility(View.GONE);
                    }
                    enc.setChecked(false);
                } else if (mKeySystemType.equals(res.getString(R.string.TWK_SYS))) {
                    if (mEncCheckLayout.getVisibility() != View.VISIBLE) {
                        mEncCheckLayout.setVisibility(View.VISIBLE);
                    }

                    if (!enc.isChecked()) {
                        srcKeyLayout.setVisibility(View.GONE);
                    } else {
                        srcKeyLayout.setVisibility(View.VISIBLE);
                    }
                }

                if (destKeyLayout.getVisibility() != View.VISIBLE) {
                    destKeyLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //NOTHING
            }
        });
        mSpinner.setSelection(0, true);
    }

    private void initKeyTestListeners() {
        cbDukpt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            View v1 = findViewById(R.id.ksn_layout);
            View v2 = findViewById(R.id.current_ksn_layout);
            View v3 = findViewById(R.id.ll_inc_ksn_layout);
            if (isChecked) {
                etPlainData.setText("");
                btTak.setText(Util.getString(R.string.dukpt_mac));
                btTpk.setText(Util.getString(R.string.dukpt_pin));
                btTdk.setText(Util.getString(R.string.dukpt_des));
                etKSNValue.setText("");
                etCurrentKSNValue.setText("");
                v1.setVisibility(View.VISIBLE);
                v2.setVisibility(View.VISIBLE);
                v3.setVisibility(View.VISIBLE);
            } else {
                etPlainData.setText("");
                v1.setVisibility(View.GONE);
                v2.setVisibility(View.GONE);
                v3.setVisibility(View.GONE);
                btTak.setVisibility(View.VISIBLE);
                btTpk.setVisibility(View.VISIBLE);
                btTak.setText(Util.getString(R.string.tak));
                btTpk.setText(Util.getString(R.string.tpk));
                btTdk.setText(Util.getString(R.string.tdk));

            }
        });
        btTak.setOnClickListener(v -> showEncData(btTak.getId()));
        btTdk.setOnClickListener(v -> showEncData(btTdk.getId()));
        btTpk.setOnClickListener(v -> showEncData(btTpk.getId()));
        btIncKSN.setOnClickListener(v -> updateNextKSN());
    }

    private void initInjectListeners() {
        mInjectionProcess = new KeyDownload(MainActivity.this);
        enc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                srcKeyLayout.setVisibility(View.VISIBLE);
                sourceKid.setFocusable(true);
                sourceKid.setFocusableInTouchMode(true);
                sourceKid.requestFocus();
                sourceKid.findFocus();
            } else {
                sourceKid.setText("");
                sourceKid.clearFocus();
                srcKeyLayout.setVisibility(View.GONE);
                descKid.requestFocus();
            }
        });

        //mIsInjecting = new AtomicBoolean(false);
        injectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 因为在点击事件后，如果没有进行注密钥，界面也有可能出现一些Toast类的提示，避免连续点击
                 * 出现许多类似这种事件的发生，所以添加了点击保护两次点击之间有500ms的间隔。这个时间足
                 * 够${@link MainActivity#injectKey执行到disable这个button这里}
                 */
                if (Util.FastClick.isFastClick())
                    return;

                injectKey();
            }
        });
    }

    /**
     * check whether Key Index is valid
     */
    private boolean isValidInjectionKeyIndex() {
        if (mKeySystemType.equals(getResources().getString(R.string.TWK_SYS)) && enc.isChecked()) {
            String str1 = sourceKid.getText().toString();
            int n1;
            try {
                n1 = Integer.parseInt(str1);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "TMK Index Must Be in Number Format",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (n1 < MIN_KEY_INDEX || n1 > MAX_TMK_TWK_INDEX) {
                Toast.makeText(this, "TMK Index Must Be in Range of " +
                        "1~100", Toast.LENGTH_SHORT).show();
                sourceKid.setText("");
                sourceKid.requestFocus();
                return false;
            }
        } else {
            sourceKid.setText("");
        }

        String str2 = descKid.getText().toString();
        int n2;
        try {
            n2 = Integer.parseInt(str2);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Key Index Must Be in Number Format",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        byte low = MIN_KEY_INDEX;
        byte high = MAX_TMK_TWK_INDEX;
        if (mKeySystemType.equals(getResources().getString(R.string.DUKPT_SYS))) {
            high = MAX_DUKPT_GROUP_INDEX;
        } else if (mKeySystemType.equals(getResources().getString(R.string.RSA_SYS))) {
            high = MAX_RSA_INDEX;
        }

        if (n2 < low || n2 > high) {
            Toast.makeText(this, "Injection Index Must Be in Range Of "
                    + low + "~" + high, Toast.LENGTH_SHORT).show();
            descKid.setText("");
            descKid.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * 注意这个函数里面的加密操作运行在主线程之中
     */
    private void showEncData(int id) {
        String plainData = etPlainData.getText().toString().trim();
        String keyIndex = etKeyIndex.getText().toString().trim();
        if (Util.checkEmpty(plainData) || Util.checkEmpty(keyIndex)) {
            ToastUtils.showShort("input empty!");
            return;
        }

        if (!isValidEncDecIndex()) {
            return;
        }

        DUKPTResult dukptResult = null;
        try {
            byte[] encData = null;
            switch (id) {
                case R.id.tak:
                    if (cbDukpt.isChecked()) {
                        dukptResult = KeyUtils.calcDuktpMac(plainData,
                                (byte) Integer.parseInt(keyIndex));
                        encData = dukptResult.getResult();
                    } else {
                        encData = KeyUtils.calcMac(plainData, (byte) Integer.parseInt(keyIndex));
                    }
                    break;

                case R.id.tdk:
                    if (cbDukpt.isChecked()) {
                        dukptResult = KeyUtils.calcDukptDes(plainData,
                                (byte) Integer.parseInt(keyIndex));
                        encData = dukptResult.getResult();
                    } else {
                        encData = KeyUtils.calcDes(plainData, (byte) Integer.parseInt(keyIndex));
                    }
                    break;

                case R.id.tpk:
                    if (cbDukpt.isChecked()) {
                        dukptResult = KeyUtils.getDukptPin(keyIndex, plainData, true, false);
                        encData = dukptResult.getResult();
                    } else {
                        encData = KeyUtils.getPinBlock(keyIndex, plainData, true, false);
                    }
                    break;

                default:
                    break;
            }
            LogUtils.d("encData:" + ConvertUtils.bytes2HexString(encData));
            tvEncData.setText(ConvertUtils.bytes2HexString(encData));

            if (cbDukpt.isChecked()) {
                if (dukptResult != null) {
                    byte[] currentKSN = dukptResult.getKsn();
                    etCurrentKSNValue.setText(ConvertUtils.bytes2HexString(currentKSN));
                }
                byte[] nextKSN = KeyUtils.getKsn(keyIndex);
                etKSNValue.setText(ConvertUtils.bytes2HexString(nextKSN));
            }
        } catch (PedDevException e) {
            LogUtils.e(e);
            tvEncData.setText(e.getMessage());
        } catch (Exception e) {
            tvEncData.setText(R.string.input_hex_string);
        }
    }

    private void updateNextKSN() {
        String keyIndex = etKeyIndex.getText().toString().trim();
        if (Util.checkEmpty(keyIndex)) {
            ToastUtils.showShort("input empty!");
            return;
        }

        try {
            DemoApp.getApp().getIped().incDUKPTKsn((byte) Integer.parseInt(keyIndex));
            byte[] nextKSN = KeyUtils.getKsn(keyIndex);
            etKSNValue.setText(ConvertUtils.bytes2HexString(nextKSN));
        } catch (PedDevException e) {
            LogUtils.e(e);
        }
    }

    private boolean isValidEncDecIndex() {
        String str1 = etKeyIndex.getText().toString().trim();
        int n1;
        try {
            n1 = Integer.parseInt(str1);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Key Index Must Be in Number Format",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int min = MIN_KEY_INDEX;
        int max = MAX_TMK_TWK_INDEX;
        if (cbDukpt.isChecked()) {
            max = MAX_DUKPT_GROUP_INDEX;
        }

        if (n1 < min || n1 > max) {
            Toast.makeText(this, "Key Index Must Be in Range of " +
                    min + "~" + max, Toast.LENGTH_SHORT).show();
            etKeyIndex.setText("");
            etKeyIndex.requestFocus();
            return false;
        }

        return true;
    }

    private void injectKey() {
        /**
         * remember to recover the injecting state to its initial value(false) if you return from
         * the middle of executing ${@link MainActivity#injectKey}
         *
         * 确保在Util.FastClick.isFastClick()返回正常点击事件之后，injectBtn.setEnabled(false)生效
         * 之前，在这段期间内产生的数据的一致性。
         */
        /*if (!mIsInjecting.compareAndSet(false, true)) {
            return;
        }*/

        if (!isValidInjectionKeyIndex()) {
            //mIsInjecting.set(false);
            return;
        }

        KeyboardUtils.hideSoftInput(MainActivity.this);
        DemoApp.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                byte srcIndex = 0;
                byte destIndex = 0;
                String sourceKeyId = sourceKid.getText().toString().trim();
                if (!sourceKeyId.isEmpty()) {
                    srcIndex = (byte) Integer.parseInt(sourceKeyId);
                }
                String descKeyId = descKid.getText().toString().trim();
                if (!descKeyId.isEmpty()) {
                    destIndex = (byte) Integer.parseInt(descKeyId);
                }


                //DemoApp.getApp().runOnUiThread(() -> injectBtn.setEnabled(false));
                synchronized (MainActivity.class) {
                    mInjectionProcess.inject(mInjectionType, srcIndex, destIndex);
                }

                //DemoApp.getApp().runOnUiThread(() -> injectBtn.setEnabled(true));
                //mIsInjecting.compareAndSet(true, false);
            }
        });
    }

//    private void testSn() {
//        String sn = Utils.getSn();
//        byte[] snTextBytes = "SN".getBytes();
//        byte[] lenBytes = {(byte) sn.length()};
//        byte[] snBytes = sn.getBytes();
//        byte[] temp = new byte[snTextBytes.length + lenBytes.length + snBytes.length];
//        LogUtils.d("snTextBytes:" + Arrays.toString(snTextBytes) + "\n" +
//                "sn:" + sn + "\n" +
//                "lenBytes:" + Arrays.toString(lenBytes) + "\n" +
//                "snBytes:" + Arrays.toString(snBytes));
//        System.arraycopy(snTextBytes, 0, temp, 0, snTextBytes.length);
//        System.arraycopy(lenBytes, 0, temp, snTextBytes.length, lenBytes.length);
//        System.arraycopy(snBytes, 0, temp, snTextBytes.length + lenBytes.length
//                , snBytes.length);
//        LogUtils.d("temp:" + Arrays.toString(temp));
//        LogUtils.d("temp:" + ConvertUtils.bytes2HexString(temp));
//    }

    private void showLayout(int id) {
        for (int funcId : funcIds) {
            findViewById(funcId).setVisibility(funcId == id ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * return time in format of "MM/dd/yyyy HH:mm"
     */
    private String getTime() {
        Calendar calendar = Calendar.getInstance(Locale.US);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        calendar.clear();
        calendar.set(year, month, day, hour, minutes, seconds);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
        return simpleDateFormat.format(calendar.getTime());
    }

    private void printReceipt(MiscData miscData) {
        String sn = InjectApp.getApp().getDal().getSys().getTermInfo()
                .get(ETermInfoKey.SN);
        mReceiptData.setSN(sn);
        mReceiptData.setTmkKCV(miscData.getTmkKCV());

        String time = getTime();
        int i = time.indexOf(' ');
        mReceiptData.setDate(time.substring(0, i));
        mReceiptData.setTime(time.substring(i + 1));

        mReceiptData.setOperator1(miscData.getOperator1());
        mReceiptData.setOperator2(miscData.getOperator2());

        mPrinterUtil.printReceipt(this, mReceiptData);
    }
}
