package com.overmark.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
    private static Ring ring;
    private static String size = SMALL;
    private static int shown;
    private static String unit = "km/h";
    private static boolean over;
    private static boolean alarming;
    private static boolean flashOn = true;
    private static int marks;
    private static int thin;
    private static int thick;

    private FloatGauge() {}

    public static boolean canDraw(Context context) {
        if (Build.VERSION.SDK_INT < 23) return true;
        return Settings.canDrawOverlays(context);
    }

    public static boolean showing() {
        return root != null;
    }

    public static void update(int speed, String unitLabel, boolean overLimit, boolean alarm, boolean flash, int filled) {
        shown = speed;
        if (unitLabel != null) unit = unitLabel;
        over = overLimit;
        alarming = alarm;
        flashOn = flash;
        marks = filled;
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
        ring = null;
    }

    private static void attach(Context app) {
        windowManager = (WindowManager) app.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) return;
        thin = dp(app, 3);
        thick = dp(app, 7);
        int diameter = diameter(app);

        ring = new Ring(app);
        LinearLayout column = new LinearLayout(app);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER);
        speedView = text(app, speedSp(false), COLOR_SIGNAL);
        unitView = text(app, LARGE.equals(size) ? 13 : 10, COLOR_MUTED);
        column.addView(speedView);
        column.addView(unitView);

        FrameLayout frame = new FrameLayout(app);
        frame.addView(ring, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams center = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        center.gravity = Gravity.CENTER;
        frame.addView(column, center);
        root = frame;
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
        thin = dp(app, 3);
        thick = dp(app, 7);
        params.width = diameter(app);
        params.height = params.width;
        try {
            windowManager.updateViewLayout(root, params);
        } catch (Exception ignored) {
            /* window already gone */
        }
    }

    private static void paint() {
        if (speedView == null || unitView == null) return;
        speedView.setText(String.valueOf(shown));
        unitView.setText(unit);
        unitView.setTextSize(TypedValue.COMPLEX_UNIT_SP, LARGE.equals(size) ? 13 : 10);
        boolean heavy = over || alarming;
        speedView.setTextSize(TypedValue.COMPLEX_UNIT_SP, speedSp(heavy));
        speedView.getPaint().setFakeBoldText(heavy);
        speedView.invalidate();
        if (alarming) speedView.setTextColor(flashOn ? COLOR_ALARM : COLOR_FG);
        else if (over) speedView.setTextColor(COLOR_ALARM);
        else speedView.setTextColor(COLOR_SIGNAL);
        if (ring != null) ring.setMarks(marks, over || alarming, thin, thick);
    }

    private static int speedSp(boolean heavy) {
        int base = LARGE.equals(size) ? 56 : 38;
        return heavy ? base + 4 : base;
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
        return dp(context, LARGE.equals(size) ? 168 : 108);
    }

    private static TextView text(Context context, int sp, int color) {
        TextView view = new TextView(context);
        view.setTextColor(color);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setGravity(Gravity.CENTER);
        view.setIncludeFontPadding(false);
        view.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        return view;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    /** Sixty-second ring. The first 45 seconds are amber, the last 15 are red. */
    private static final class Ring extends View {
        private static final float AMBER_SWEEP = 270f;
        private static final float RED_SWEEP = 90f;

        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint dimAmber = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint dimRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint amber = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint red = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF oval = new RectF();
        private int seconds;
        private boolean hotRing;

        Ring(Context context) {
            super(context);
            fill.setStyle(Paint.Style.FILL);
            fill.setColor(COLOR_BG);
            stroke(dimAmber, 0xFF8A6414);
            stroke(dimRed, 0xFF7A2A24);
            stroke(amber, COLOR_SIGNAL);
            stroke(red, COLOR_ALARM);
            setWillNotDraw(false);
        }

        void setMarks(int next, boolean overLimit, int thin, int thick) {
            seconds = Math.max(0, Math.min(60, next));
            hotRing = overLimit;
            float width = overLimit ? thick : thin;
            dimAmber.setStrokeWidth(width);
            dimRed.setStrokeWidth(width);
            amber.setStrokeWidth(width);
            red.setStrokeWidth(width);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = Math.min(cx, cy) - dimAmber.getStrokeWidth() / 2f - 1f;
            if (radius <= 0f) return;
            canvas.drawCircle(cx, cy, radius, fill);
            oval.set(cx - radius, cy - radius, cx + radius, cy + radius);
            canvas.drawArc(oval, -90f, AMBER_SWEEP, false, dimAmber);
            canvas.drawArc(oval, -90f + AMBER_SWEEP, RED_SWEEP, false, dimRed);
            if (!hotRing || seconds <= 0) return;
            float amberDone = 360f * Math.min(seconds, 45) / 60f;
            canvas.drawArc(oval, -90f, amberDone, false, amber);
            if (seconds > 45) {
                canvas.drawArc(oval, -90f + AMBER_SWEEP, 360f * (seconds - 45) / 60f, false, red);
            }
        }

        private static void stroke(Paint paint, int color) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setColor(color);
        }
    }
}
