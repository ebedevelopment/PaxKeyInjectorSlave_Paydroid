package cn.pax.odd.keyinjection.sdk.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import cn.pax.odd.keyinjection.sdk.InjectApp;


/**
 * @author ligq
 * @date 2018/1/26
 */

public class DialogUtils {
    private static ProgressDialog mDialog;
    private static DialogInterface.OnClickListener cancelListener;

    public static void showDialog(Context context, @NonNull String message) {
        InjectApp.getApp().runOnUiThread(() -> {
            if (mDialog == null) {
                mDialog = new ProgressDialog(context);
                mDialog.setTitle("Mode:" + InjectApp.getApp().getPedMode().toString());
                mDialog.setMessage(message);
                if (cancelListener != null) {
                    mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", cancelListener);
                }
                mDialog.setCancelable(false);
            }
            if (!mDialog.isShowing()) {
                mDialog.setMessage(message);
                mDialog.show();
            }
        });

    }

    public static void setCancelListener(DialogInterface.OnClickListener listener) {
        cancelListener = listener;
    }

    public static void setDialogMsg(String msg) {
        InjectApp.getApp().runOnUiThread(() -> {
            mDialog.setMessage(msg);
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
        });
    }

    /**
     * dismiss and clear dialog object
     */
    public static void releaseDialog() {
        InjectApp.getApp().runOnUiThread(() -> {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        });
    }

    /**
     * call {@link ProgressDialog#dismiss()} but do not recycle dialog object
     */
    public static void hideDialog() {
        InjectApp.getApp().runOnUiThread(() -> {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        });
    }
}
