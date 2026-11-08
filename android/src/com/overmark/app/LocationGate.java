package com.overmark.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Android 14 (API 34) and newer throw SecurityException from
 * {@code startForeground(..., FOREGROUND_SERVICE_TYPE_LOCATION)} unless the app
 * already holds fine or coarse location. The check is synchronous and crashes
 * the process when it is uncaught inside a service.
 */
final class LocationGate {
    private LocationGate() {}

    static boolean hasLocationPermission(Context context) {
        return granted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                || granted(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    static boolean canStartLocationForeground(Context context) {
        return canStartLocationForeground(Build.VERSION.SDK_INT, hasLocationPermission(context));
    }

    static boolean canStartLocationForeground(int sdkInt, boolean locationGranted) {
        if (sdkInt < 34) return true;
        return locationGranted;
    }

    private static boolean granted(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
}
