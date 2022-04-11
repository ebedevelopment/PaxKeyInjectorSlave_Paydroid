package cn.pax.odd.keyinjection.printer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;


import java.util.Locale;

import cn.pax.odd.keyinjection.R;
import cn.pax.odd.keyinjection.sdk.InjectApp;
import cn.pax.odd.keyinjection.sdk.utils.LogUtils;

public class PrinterUtil {
    private Context mContext;
    private ReceiptData mReceiptData;

    public PrinterUtil() {
    }

    private Bitmap toBitmap() {
        @SuppressLint("InflateParams")
        View receipt = LayoutInflater.from(mContext).inflate(R.layout.layout_printer_reciept,
                null, false);
        TextView sn = receipt.findViewById(R.id.txt_ct_sn);
        TextView tmkKCV = receipt.findViewById(R.id.txt_ct_tmk_kcv);
        TextView date = receipt.findViewById(R.id.txt_ct_date);
        TextView time = receipt.findViewById(R.id.txt_ct_time);
        TextView operator1 = receipt.findViewById(R.id.txt_ct_operator1);
        TextView operator2 = receipt.findViewById(R.id.txt_ct_operator2);

        sn.setText(mReceiptData.getSN());
        tmkKCV.setText(mReceiptData.getTmkKCV());
        date.setText(mReceiptData.getDate());
        time.setText(mReceiptData.getTime());
        operator1.setText(mReceiptData.getOperator1());
        operator2.setText(mReceiptData.getOperator2());

        receipt.measure(View.MeasureSpec.makeMeasureSpec(getReceiptWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED));
        receipt.layout(0, 0, receipt.getMeasuredWidth(), receipt.getMeasuredHeight());
        receipt.buildDrawingCache();
        Bitmap bitmap = receipt.getDrawingCache();
//        receipt.destroyDrawingCache();
        return bitmap;
    }

    private int getReceiptWidth() {
        switch (Build.MODEL) {
            case "A920":
            case "A910":
            case "A930":
            case "A620":
            case "A60":
                return 384;
            default:
                return 576;
        }
    }

    public void printReceipt(@NonNull Context context, @NonNull ReceiptData receiptData) {
        mContext = context;
        mReceiptData = receiptData;

        Bitmap bitmap = toBitmap();
        try {
            IDAL dal = InjectApp.getApp().getDal();
            if (dal == null) {
                LogUtils.d("Printer: dal is null");
                return;
            }
            IPrinter printer = dal.getPrinter();
            printer.init();
            printer.setGray(300);
            printer.printBitmap(bitmap);
            int start = printer.start();
            if (start != 0) {
                LogUtils.d(String.format(Locale.US,"Printer: result of printer.start() = %d", start));
            }
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }
}
