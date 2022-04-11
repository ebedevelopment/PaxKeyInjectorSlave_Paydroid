package cn.pax.odd.keyinjection.sdk.utils;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.pax.odd.keyinjection.sdk.InjectApp;


/**
 * @author ligq
 * @date 2018/1/30
 */

public class ToastUtils {
    private static final int COLOR_DEFAULT = 0xFEFFFFFF;
    private static Toast sToast;
    private static int gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
    private static int xOffset = 0;
    private static int yOffset = (int) (64 * InjectApp.getApp().getResources().getDisplayMetrics().density + 0.5);
    private static int msgColor = COLOR_DEFAULT;
    private static int bgColor = COLOR_DEFAULT;
    private static int bgResource = -1;

    /**
     * 取消吐司显示
     */
    public static void cancel() {
        if (sToast != null) {
            sToast.cancel();
            sToast = null;
        }
    }

    private static void show(final CharSequence text, final int duration) {
        InjectApp.getApp().runOnUiThread(() -> {
            cancel();
            sToast = Toast.makeText(InjectApp.getApp(), text, duration);
            // solve the font of toast
            TextView tvMessage = sToast.getView().findViewById(android.R.id.message);
            TextViewCompat.setTextAppearance(tvMessage, android.R.style.TextAppearance);
            tvMessage.setTextColor(msgColor);
            sToast.setGravity(gravity, xOffset, yOffset);
            setBg(tvMessage);
            sToast.show();
        });
    }

    private static void setBg(final TextView tvMsg) {
        View toastView = sToast.getView();
        if (bgResource != -1) {
            toastView.setBackgroundResource(bgResource);
            tvMsg.setBackgroundColor(Color.TRANSPARENT);
        } else if (bgColor != COLOR_DEFAULT) {
            Drawable tvBg = toastView.getBackground();
            Drawable msgBg = tvMsg.getBackground();
            if (tvBg != null && msgBg != null) {
                tvBg.setColorFilter(new PorterDuffColorFilter(bgColor, PorterDuff.Mode.SRC_IN));
                tvMsg.setBackgroundColor(Color.TRANSPARENT);
            } else if (tvBg != null) {
                tvBg.setColorFilter(new PorterDuffColorFilter(bgColor, PorterDuff.Mode.SRC_IN));
            } else if (msgBg != null) {
                msgBg.setColorFilter(new PorterDuffColorFilter(bgColor, PorterDuff.Mode.SRC_IN));
            } else {
                toastView.setBackgroundColor(bgColor);
            }
        }
    }

    /**
     * 安全地显示短时吐司
     *
     * @param text 文本
     */
    public static void showShort(@NonNull final CharSequence text) {
        show(text, Toast.LENGTH_SHORT);
    }
}
