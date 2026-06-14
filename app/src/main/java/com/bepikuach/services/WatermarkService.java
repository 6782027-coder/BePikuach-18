package com.bepikuach.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bepikuach.R;
import com.bepikuach.utils.PrefManager;

public class WatermarkService extends Service {

    private WindowManager windowManager;
    private ImageView watermarkView;
    private PrefManager prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new PrefManager(this);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        removeWatermark();

        if (!prefs.isWatermarkEnabled()) return START_STICKY;

        watermarkView = new ImageView(this);
        watermarkView.setImageResource(R.drawable.ic_watermark);
        watermarkView.setAlpha(prefs.getWatermarkAlpha() / 100f);

        int sizeDp = prefs.getWatermarkSizeDp();
        int sizePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeDp,
                getResources().getDisplayMetrics());

        int gravity = gravityFromPref(prefs.getWatermarkGravity());

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                sizePx, sizePx,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = gravity;

        int pad = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        params.x = (gravity == (Gravity.CENTER_VERTICAL | Gravity.END)) ? pad :
                   (gravity == (Gravity.CENTER_VERTICAL | Gravity.START)) ? pad : 0;
        params.y = (gravity == (Gravity.TOP | Gravity.CENTER_HORIZONTAL)) ? pad :
                   (gravity == (Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL)) ? pad : 0;

        try {
            windowManager.addView(watermarkView, params);
        } catch (Exception ignored) {}

        return START_STICKY;
    }

    private int gravityFromPref(int g) {
        switch (g) {
            case 1: return Gravity.TOP    | Gravity.CENTER_HORIZONTAL;
            case 2: return Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            case 3: return Gravity.CENTER_VERTICAL | Gravity.START;
            case 4: return Gravity.CENTER_VERTICAL | Gravity.END;
            default: return Gravity.CENTER;
        }
    }

    private void removeWatermark() {
        if (watermarkView != null) {
            try { windowManager.removeView(watermarkView); } catch (Exception ignored) {}
            watermarkView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeWatermark();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
