package com.overmark.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

/** Keeps GPS alive after minimize. Holds no history and sends nothing. */
public class KeepAliveService extends Service {
    public static final int NOTE_ID = 7;
    private static final String CHANNEL = "overmark";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Create the ongoing-notice channel as early as possible. On Android 13+,
     * creating it only after POST_NOTIFICATIONS was denied can fail silently,
     * and the following startForeground then crashes the process.
     */
    static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26 || context == null) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (manager == null || manager.getNotificationChannel(CHANNEL) != null) return;
        I18n.load(context);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL, "Overmark", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(I18n.t("privacyTitle"));
        try {
            manager.createNotificationChannel(channel);
        } catch (Exception ignored) {
            /* noticeReady treats a missing channel as unsafe */
        }
    }

    /** False when a foreground notice cannot be posted without crashing. */
    static boolean noticeReady(Context context) {
        if (Build.VERSION.SDK_INT < 26) return true;
        if (context == null) return false;
        ensureChannel(context);
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        return manager != null && manager.getNotificationChannel(CHANNEL) != null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        I18n.load(this);
        if (!LocationGate.canStartLocationForeground(this) || !noticeReady(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        try {
            Notification note = buildNote();
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTE_ID, note, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(NOTE_ID, note);
            }
        } catch (Exception ignored) {
            /* Location permission missing or revoked, or the notice was rejected. */
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private Notification buildNote() {
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL)
                : new Notification.Builder(this);
        if (Build.VERSION.SDK_INT < 26) builder.setPriority(Notification.PRIORITY_LOW);
        return builder
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)))
                .setContentTitle("Overmark")
                .setContentText(I18n.t("minimized"))
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        try {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } catch (Exception ignored) {
            /* never reached the foreground */
        }
        super.onDestroy();
    }
}
