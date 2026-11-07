package com.overmark.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Speed bubble drawn over other apps. A tap opens Overmark. Nothing is stored. */
public final class FloatGauge {
    public static final String SMALL = "small";
    public static final String LARGE = "large";

    private static final int COLOR_BG = 0xF012110E;
    private static final int COLOR_FG = 0xFFF3EFE6;
    private static final int COLOR_MUTED = 0xFF9A9284;
    private static final int COLOR_SIGNAL = 0xFFE8A317;
    private static final int COLOR_ALARM = 0xFFE23B2F;

    private static View root;
    private static WindowManager windowManager;
    private static WindowManager.LayoutParams params;
    private static TextView speedView;
    private static TextView unitView;
    private static GradientDrawable plate;
    private static String size = SMALL;
    private static int shown;
    private static String unit = "km/h";
    private static boolean alarming;
    private static boolean flashOn = true;
    private static int stroke;

    private FloatGauge() {}

    public static boolean canDraw(Context context) {
        if (Build.VERSION.SDK_INT < 23) return true;
        return Settings.canDrawOverlays(context);
    }

    public static boolean showing() {
        return root != null;
    }

    public static void update(int speed, String unitLabel, boolean alarm, boolean flash) {
        shown = speed;
        if (unitLabel != null) unit = unitLabel;
        alarming = alarm;
        flashOn = flash;
        paint();
    }

    public static void show(Context context, String nextSize) {
        Context app = context.getApplicationContext();
        size = LARGE.equals(nextSize) ? LARGE : SMALL;
        if (root == null) attach(app);
        else resize(app);
        paint();
    }

    public static void hide() {
        if (root != null && windowManager != null) {
            try {
                windowManager.removeView(root);
            } catch (Exception ignored) {
                /* already detached */
            }
        }
        root = null;
        windowManager = null;
        params = null;
        speedView = null;
        unitView = null;
        plate = null;
    }

    private static void attach(Context app) {
        windowManager = (WindowManager) app.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) return;
        stroke = dp(app, 2);
        int diameter = diameter(app);

        plate = new GradientDrawable();
        plate.setShape(GradientDrawable.OVAL);
        plate.setColor(COLOR_BG);
        plate.setStroke(stroke, COLOR_SIGNAL);

        LinearLayout column = new LinearLayout(app);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER);
        speedView = text(app, LARGE.equals(size) ? 48 : 30, COLOR_SIGNAL, true);
        unitView = text(app, LARGE.equals(size) ? 14 : 11, COLOR_MUTED, false);
        column.addView(speedView);
        column.addView(unitView);

        root = column;
        root.setBackground(plate);
        root.setContentDescription("Overmark");

        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(
                diameter,
                diameter,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = dp(app, 16);
        params.y = dp(app, 72);
        root.setOnTouchListener(new View.OnTouchListener() {
            private float downX;
            private float downY;
            private int startX;
            private int startY;
            private boolean moved;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (params == null || windowManager == null) return false;
                int slop = dp(view.getContext(), 10);
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        startX = params.x;
                        startY = params.y;
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downX;
                        float dy = event.getRawY() - downY;
                        if (Math.abs(dx) > slop || Math.abs(dy) > slop) moved = true;
                        params.x = startX - Math.round(dx);
                        params.y = startY + Math.round(dy);
                        try {
                            windowManager.updateViewLayout(root, params);
                        } catch (Exception ignored) {
                            /* window already gone */
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) openApp(view.getContext());
                        return true;
                    default:
                        return false;
                }
            }
        });
        try {
            windowManager.addView(root, params);
        } catch (Exception ignored) {
            root = null;
            windowManager = null;
        }
    }

    private static void resize(Context app) {
        if (params == null || windowManager == null || root == null) return;
        int diameter = diameter(app);
        params.width = diameter;
        params.height = diameter;
        speedView.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE.equals(size) ? 48 : 30);
        unitView.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE.equals(size) ? 14 : 11);
        try {
            windowManager.updateViewLayout(root, params);
        } catch (Exception ignored) {
            /* window already gone */
        }
    }

    private static void paint() {
        if (speedView == null || unitView == null || plate == null) return;
        speedView.setText(String.valueOf(shown));
        unitView.setText(unit);
        int color = alarming ? (flashOn ? COLOR_ALARM : COLOR_FG) : COLOR_SIGNAL;
        speedView.setTextColor(color);
        plate.setStroke(stroke, alarming ? COLOR_ALARM : COLOR_SIGNAL);
    }

    private static void openApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        try {
            context.startActivity(intent);
        } catch (Exception ignored) {
            /* no task to bring forward */
        }
        hide();
    }

    private static int diameter(Context context) {
        return dp(context, LARGE.equals(size) ? 156 : 96);
    }

    private static TextView text(Context context, int sp, int color, boolean strong) {
        TextView view = new TextView(context);
        view.setTextColor(color);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setGravity(Gravity.CENTER);
        view.setIncludeFontPadding(false);
        view.setTypeface(Typeface.create("sans-serif-condensed", strong ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
