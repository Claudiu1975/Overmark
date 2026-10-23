package com.overmark.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** 45 second marks. Filled ones are the seconds already beeped. */
public final class PaceRail extends View {
    private final Paint on = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint off = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint alarm = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private int filled;
    private boolean alarming;

    public PaceRail(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        on.setColor(0xFFE8A317);
        off.setColor(0xFF2E2A24);
        alarm.setColor(0xFFE23B2F);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public void setState(int filled, boolean alarming) {
        this.filled = filled;
        this.alarming = alarming;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int n = Pace.MARK_COUNT;
        float gap = 2f * density;
        float w = (getWidth() - gap * (n - 1)) / n;
        float h = getHeight();
        float r = 2f * density;
        if (w <= 0f) return;
        for (int i = 0; i < n; i++) {
            Paint paint = i < filled ? (alarming ? alarm : on) : off;
            float left = i * (w + gap);
            canvas.drawRoundRect(left, 0f, left + w, h, r, r, paint);
        }
    }
}
