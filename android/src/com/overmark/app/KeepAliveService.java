package com.overmark.app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        I18n.load(this);
        if (!hasLocationPermission()) {
            stopSelf();
            return START_NOT_STICKY;
        }
        try {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26 && manager.getNotificationChannel(CHANNEL) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL, "Overmark", NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(I18n.t("privacyTitle"));
                manager.createNotificationChannel(channel);
            }
            Notification note = new Notification.Builder(this, CHANNEL)
                    .setContentTitle("Overmark")
                    .setContentText(I18n.t("minimized"))
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .setOngoing(true)
                    .build();
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTE_ID, note, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(NOTE_ID, note);
            }
        } catch (RuntimeException ignored) {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onDestroy() {
        try {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } catch (RuntimeException ignored) {
            /* never reached the foreground */
        }
        super.onDestroy();
    }
}
