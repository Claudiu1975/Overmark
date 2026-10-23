package com.overmark.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {
    private static final int REQ_LOCATION = 10;
    private static final String PREFS = "overmark";
    private static final int COLOR_BG = 0xFF12110E;
    private static final int COLOR_SURFACE = 0xFF1C1A16;
    private static final int COLOR_FG = 0xFFF3EFE6;
    private static final int COLOR_MUTED = 0xFF9A9284;
    private static final int COLOR_SIGNAL = 0xFFE8A317;
    private static final int COLOR_ALARM = 0xFFE23B2F;
    private static final int COLOR_LINE = 0xFF2E2A24;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private LocationManager locations;
    private Vibrator vibrator;
    private SoundBoard sound;

    private TextView statusView;
    private TextView speedView;
    private TextView unitView;
    private TextView elapsedView;
    private TextView limitView;
    private TextView hintView;
    private PaceRail rail;
    private Button armButton;
    private Button gpsButton;
    private Button benchButton;
    private Button kmhButton;
    private Button mphButton;
    private SeekBar benchBar;
    private TextView benchLabel;
    private GradientDrawable armBg;
    private View mainPane;
    private View aboutPane;

    private float limitKmh = 90f;
    private float speedKmh;
    private float benchKmh;
    private boolean mph;
    private boolean bench;
    private boolean tracking;
    private boolean hasFix;
    private boolean alarming;
    private boolean flashOn = true;
    private long overSince = -1L;
    private int lastBeepSec;
    private Location previous;
    private Button aboutButton;
    private Button langButton;
    private Button minusButton;
    private Button plusButton;
    private Button exitButton;
    private Button minimizeButton;
    private Button backButton;
    private TextView limitLabel;
    private TextView aboutVersion;
    private TextView aboutAuthorKicker;
    private TextView aboutAuthor;
    private TextView aboutDisclaimerTitle;
    private TextView aboutDisclaimer;
    private TextView aboutPrivacyTitle;
    private TextView aboutPrivacy;
    private TextView aboutHow;
    private TextView langTitle;
    private TextView langHint;
    private EditText langSearch;
    private View langPane;
    private View rootView;
    private String versionName = "1.1.0";

    private final Runnable uiTick = new Runnable() {
        @Override
        public void run() {
            if (tracking) sample(speedKmh, hasFix || bench);
            else paint();
            handler.postDelayed(this, 100L);
        }
    };

    private final Runnable flashTick = new Runnable() {
        @Override
        public void run() {
            if (!alarming) return;
            flashOn = !flashOn;
            paint();
            handler.postDelayed(this, 160L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        I18n.load(this);
        if (prefs.getInt("limit_rev", 0) != 2) {
            limitKmh = 90f;
            prefs.edit().putFloat("limit_kmh", 90f).putInt("limit_rev", 2).apply();
        } else {
            limitKmh = prefs.getFloat("limit_kmh", 90f);
        }
        mph = prefs.getBoolean("mph", false);
        String savedLang = prefs.getString("lang", null);
        if (savedLang != null) I18n.lang = savedLang;
        locations = (LocationManager) getSystemService(LOCATION_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        sound = SoundBoard.create();
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (info.versionName != null) versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            versionName = "1.1.0";
        }

        getWindow().setStatusBarColor(COLOR_BG);
        getWindow().setNavigationBarColor(COLOR_BG);

        if (savedInstanceState != null) {
            tracking = savedInstanceState.getBoolean("tracking");
            bench = savedInstanceState.getBoolean("benchMode");
            benchKmh = savedInstanceState.getFloat("benchSpeed");
            speedKmh = savedInstanceState.getFloat("speed");
            hasFix = savedInstanceState.getBoolean("fix");
            overSince = savedInstanceState.getLong("overSince", -1L);
            lastBeepSec = savedInstanceState.getInt("lastBeep");
            alarming = savedInstanceState.getBoolean("alarm");
            long now = SystemClock.elapsedRealtime();
            if (overSince > now) {
                overSince = -1L;
                alarming = false;
                lastBeepSec = 0;
            } else if (overSince >= 0L) {
                lastBeepSec = (int) ((now - overSince) / 1000L);
            }
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(COLOR_BG);
        rootView = root;
        mainPane = buildMain();
        aboutPane = buildAbout();
        langPane = buildLanguage();
        aboutPane.setVisibility(View.GONE);
        root.addView(mainPane);
        root.addView(aboutPane);
        root.addView(langPane);
        applyDirection();
        setContentView(root);
        if (savedLang == null) showLang(true);

        if (tracking && !bench) beginGps();
        if (alarming) {
            if (sound != null) sound.startAlarm();
            handler.post(flashTick);
        }
        handler.post(uiTick);
        paint();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tracking && !bench && permissionGranted()) beginGps();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("tracking", tracking);
        out.putBoolean("benchMode", bench);
        out.putFloat("benchSpeed", benchKmh);
        out.putFloat("speed", speedKmh);
        out.putBoolean("fix", hasFix);
        out.putLong("overSince", overSince);
        out.putInt("lastBeep", lastBeepSec);
        out.putBoolean("alarm", alarming);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        stopGps();
        if (!isChangingConfigurations()) {
            stopService(new Intent(this, KeepAliveService.class));
        }
        if (sound != null) sound.release();
        if (vibrator != null) vibrator.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (langPane.getVisibility() == View.VISIBLE) {
            if (prefs.getString("lang", null) != null) {
                showLang(false);
                return;
            }
            exitApp();
            return;
        }
        if (aboutPane.getVisibility() == View.VISIBLE) {
            showAbout(false);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!tracking || bench || location == null) return;
        float kmh;
        if (location.hasSpeed()) {
            kmh = Math.max(0f, location.getSpeed()) * 3.6f;
        } else if (previous != null) {
            long dtNs = location.getElapsedRealtimeNanos() - previous.getElapsedRealtimeNanos();
            if (dtNs < 500_000_000L || location.getAccuracy() > 45f || previous.getAccuracy() > 45f) {
                previous = location;
                hasFix = true;
                paint();
                return;
            }
            float meters = previous.distanceTo(location);
            kmh = (meters / (dtNs / 1_000_000_000f)) * 3.6f;
        } else {
            previous = location;
            hasFix = true;
            paint();
            return;
        }
        previous = location;
        if (!Pace.acceptSpeed(kmh)) return;
        sample(kmh, true);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {
        if (tracking && !bench) {
            hasFix = false;
            paint();
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code != REQ_LOCATION) return;
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            beginGps();
        } else {
            tracking = false;
            resetPace();
            paint();
        }
    }

    private void sample(float kmh, boolean fix) {
        if (Pace.acceptSpeed(kmh)) speedKmh = kmh;
        hasFix = fix;
        long now = SystemClock.elapsedRealtime();
        boolean over = tracking && fix && Pace.overLimit(speedKmh, limitKmh);
        if (over) {
            if (overSince < 0L) {
                overSince = now;
                lastBeepSec = 0;
                stopAlarm();
            }
            long elapsed = now - overSince;
            int beep = Pace.beepSecond(elapsed, lastBeepSec);
            if (beep > 0) {
                lastBeepSec = beep;
                playBeep();
            }
            if (elapsed >= Pace.ALARM_AFTER_MS) startAlarm();
        } else if (overSince >= 0L || alarming) {
            resetPace();
        }
        paint();
    }

    private void resetPace() {
        overSince = -1L;
        lastBeepSec = 0;
        stopAlarm();
    }

    private void playBeep() {
        if (sound != null) sound.beep();
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(18, 40));
        }
    }

    private void startAlarm() {
        if (alarming) return;
        alarming = true;
        if (sound != null) sound.startAlarm();
        flashOn = true;
        handler.removeCallbacks(flashTick);
        handler.post(flashTick);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(160, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void stopAlarm() {
        if (!alarming && sound == null) return;
        alarming = false;
        handler.removeCallbacks(flashTick);
        if (sound != null) sound.stopAlarm();
    }

    private void onArm() {
        if (tracking) {
            tracking = false;
            stopGps();
            resetPace();
            clearKeepScreen();
            paint();
            return;
        }
        tracking = true;
        resetPace();
        keepScreen();
        if (bench) {
            sample(benchKmh, true);
            return;
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQ_LOCATION);
            paint();
            return;
        }
        if (!gpsReady()) {
            paint();
            openGpsSettings();
            return;
        }
        beginGps();
    }

    private void beginGps() {
        if (!tracking || bench) return;
        if (locations == null) return;
        boolean enabled;
        try {
            enabled = locations.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
            enabled = false;
        }
        if (!enabled) {
            hasFix = false;
            paint();
            return;
        }
        try {
            locations.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400L, 0f, this);
        } catch (SecurityException ignored) {
            tracking = false;
            resetPace();
        }
        paint();
    }

    private void stopGps() {
        if (locations != null) {
            try {
                locations.removeUpdates(this);
            } catch (Exception ignored) {
                /* already gone */
            }
        }
        previous = null;
    }

    private void keepScreen() {
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void clearKeepScreen() {
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void openGpsSettings() {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private void bumpLimit(float deltaKmh) {
        float next = Math.round((limitKmh + deltaKmh) * 2f) / 2f;
        if (next < 0.5f) next = 0.5f;
        if (next > 320f) next = 320f;
        limitKmh = next;
        prefs.edit().putFloat("limit_kmh", limitKmh).putInt("limit_rev", 2).apply();
        paint();
    }

    private void setUnitMph(boolean next) {
        if (mph == next) return;
        mph = next;
        prefs.edit().putBoolean("mph", mph).apply();
        if (benchBar != null) benchBar.setProgress(Math.round(Pace.toDisplay(benchKmh, mph)));
        paint();
    }

    private void setBenchMode(boolean next) {
        if (bench == next && !tracking) {
            bench = next;
            paint();
            return;
        }
        tracking = false;
        stopGps();
        resetPace();
        clearKeepScreen();
        bench = next;
        hasFix = false;
        paint();
    }

    private void showAbout(boolean show) {
        aboutPane.setVisibility(show ? View.VISIBLE : View.GONE);
        langPane.setVisibility(View.GONE);
        mainPane.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showLang(boolean show) {
        langPane.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            mainPane.setVisibility(View.GONE);
            aboutPane.setVisibility(View.GONE);
        } else {
            mainPane.setVisibility(View.VISIBLE);
        }
    }

    private void applyDirection() {
        if (rootView != null) {
            rootView.setLayoutDirection(I18n.rtl()
                    ? View.LAYOUT_DIRECTION_RTL
                    : View.LAYOUT_DIRECTION_LTR);
        }
    }

    private void chooseLang(String code) {
        I18n.lang = code;
        prefs.edit().putString("lang", code).apply();
        applyDirection();
        showLang(false);
        paint();
    }

    private void minimize() {
        if (tracking && !bench) {
            try {
                Intent intent = new Intent(this, KeepAliveService.class);
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent);
                else startService(intent);
            } catch (Exception ignored) {
                /* still leave the foreground */
            }
        }
        moveTaskToBack(true);
    }

    private void exitApp() {
        tracking = false;
        gpsWantedGone();
        stopGps();
        resetPace();
        clearKeepScreen();
        stopService(new Intent(this, KeepAliveService.class));
        wipeVolatile();
        finishAndRemoveTask();
    }

    private void gpsWantedGone() {
        hasFix = false;
    }

    private void wipeVolatile() {
        String lang = prefs.getString("lang", null);
        float limit = prefs.getFloat("limit_kmh", 90f);
        boolean useMph = prefs.getBoolean("mph", false);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putFloat("limit_kmh", limit);
        editor.putBoolean("mph", useMph);
        editor.putInt("limit_rev", 2);
        if (lang != null) editor.putString("lang", lang);
        editor.apply();
        emptyDir(getCacheDir());
        emptyDir(getCodeCacheDir());
        emptyDir(getExternalCacheDir());
        emptyDir(getFilesDir());
        emptyDir(getExternalFilesDir(null));
        try {
            Runtime.getRuntime().exec(new String[]{"logcat", "-c"});
        } catch (Exception ignored) {
            /* clearing the system log needs a privilege this app does not have */
        }
    }

    private void emptyDir(File dir) {
        if (dir == null || !dir.isDirectory()) return;
        File[] kids = dir.listFiles();
        if (kids == null) return;
        for (int i = 0; i < kids.length; i++) deleteTree(kids[i]);
    }

    private void deleteTree(File file) {
        if (file.isDirectory()) {
            File[] kids = file.listFiles();
            if (kids != null) {
                for (int i = 0; i < kids.length; i++) deleteTree(kids[i]);
            }
        }
        file.delete();
    }

    private void paint() {
        long elapsed = overSince >= 0L ? SystemClock.elapsedRealtime() - overSince : 0L;
        boolean over = tracking && (hasFix || bench) && Pace.overLimit(speedKmh, limitKmh) && overSince >= 0L;
        int shown = Math.round(Pace.toDisplay(speedKmh, mph));
        String unit = mph ? "mph" : "km/h";

        statusView.setText(statusText());
        statusView.setTextColor(alarming ? COLOR_ALARM : over ? COLOR_SIGNAL : COLOR_MUTED);
        speedView.setText(String.format(Locale.US, "%d", shown));
        speedView.setContentDescription("Speed " + shown + " " + unit);
        if (alarming) speedView.setTextColor(flashOn ? COLOR_ALARM : COLOR_FG);
        else if (!tracking) speedView.setTextColor(COLOR_MUTED);
        else speedView.setTextColor(COLOR_SIGNAL);
        unitView.setText(unit);
        elapsedView.setText(String.format(Locale.US, "%.1f s", elapsed / 1000.0));
        rail.setState(Pace.filledMarks(elapsed, over), alarming);
        limitView.setText(String.format(Locale.US, "%.1f", Pace.toDisplay(limitKmh, mph)));
        styleChoice(gpsButton, !bench);
        styleChoice(benchButton, bench);
        styleChoice(kmhButton, !mph);
        styleChoice(mphButton, mph);
        benchBar.setVisibility(bench ? View.VISIBLE : View.GONE);
        benchLabel.setVisibility(bench ? View.VISIBLE : View.GONE);
        benchLabel.setText(I18n.t("benchSpeed") + "  " + Math.round(Pace.toDisplay(benchKmh, mph)) + " " + unit);
        armBg.setColor(alarming ? COLOR_ALARM : COLOR_SIGNAL);
        if (!tracking) armButton.setText(bench ? I18n.t("armBench") : I18n.t("armGps"));
        else armButton.setText(I18n.t("disarm"));
        hintView.setText(hintText());
        relabel();
        if (tracking) keepScreen();
    }

    private void relabel() {
        if (aboutButton != null) aboutButton.setText(I18n.t("about"));
        if (langButton != null) langButton.setText(I18n.t("language"));
        if (gpsButton != null) gpsButton.setText("GPS");
        if (benchButton != null) benchButton.setText(I18n.t("bench"));
        if (exitButton != null) exitButton.setText(I18n.t("exit"));
        if (minimizeButton != null) minimizeButton.setText(I18n.t("minimize"));
        if (limitLabel != null) limitLabel.setText(I18n.t("limit"));
        if (minusButton != null) minusButton.setContentDescription(I18n.t("stepDown"));
        if (plusButton != null) plusButton.setContentDescription(I18n.t("stepUp"));
        if (backButton != null) backButton.setText(I18n.t("back"));
        if (aboutVersion != null) aboutVersion.setText(I18n.t("version") + " " + versionName);
        if (aboutAuthorKicker != null) aboutAuthorKicker.setText(I18n.t("author"));
        if (aboutAuthor != null) aboutAuthor.setText("Claudiu Ivan");
        if (aboutDisclaimerTitle != null) aboutDisclaimerTitle.setText(I18n.t("disclaimerTitle"));
        if (aboutDisclaimer != null) aboutDisclaimer.setText(I18n.t("disclaimer"));
        if (aboutPrivacyTitle != null) aboutPrivacyTitle.setText(I18n.t("privacyTitle"));
        if (aboutPrivacy != null) aboutPrivacy.setText(I18n.t("privacy"));
        if (aboutHow != null) aboutHow.setText(I18n.t("how") + "\n\n" + I18n.t("apkNote"));
        if (langTitle != null) langTitle.setText(I18n.t("chooseLanguage"));
        if (langHint != null) langHint.setText(I18n.t("chooseHint"));
        if (langSearch != null) langSearch.setHint(I18n.t("searchLang"));
    }

    private String statusText() {
        if (aboutPane != null && aboutPane.getVisibility() == View.VISIBLE) return "";
        if (!tracking) return I18n.t("standby");
        if (!bench && !hasFix) return I18n.t("searching");
        if (alarming) return I18n.t("alarm");
        if (hasFix && Pace.overLimit(speedKmh, limitKmh)) return I18n.t("marking");
        return I18n.t("under");
    }

    private String hintText() {
        if (!tracking && bench) return I18n.t("hintBenchIdle");
        if (!tracking) return I18n.t("hintGpsIdle");
        if (!bench && !permissionGranted()) return I18n.t("hintDenied");
        if (!bench && !gpsReady()) return I18n.t("hintNoFix");
        if (!bench && !hasFix) return I18n.t("hintSearch");
        if (alarming) return I18n.t("hintAlarm");
        return I18n.t("hintLive");
    }

    private boolean permissionGranted() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean gpsReady() {
        try {
            return locations != null && locations.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
            return false;
        }
    }

    private View buildMain() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(COLOR_BG);
        LinearLayout col = column();
        col.setPadding(dp(20), dp(18), dp(20), dp(24));

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = label("OVERMARK", 14, COLOR_FG, true);
        title.setLetterSpacing(0.18f);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        langButton = chip(I18n.t("language"), false);
        langButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLang(true);
            }
        });
        aboutButton = chip(I18n.t("about"), false);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbout(true);
            }
        });
        header.addView(title);
        header.addView(langButton);
        header.addView(aboutButton);
        col.addView(header);

        statusView = label("STANDBY", 12, COLOR_MUTED, true);
        statusView.setLetterSpacing(0.16f);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, dp(28), 0, dp(6));
        col.addView(statusView);

        speedView = label("0", 92, COLOR_MUTED, true);
        speedView.setGravity(Gravity.CENTER);
        speedView.setIncludeFontPadding(false);
        speedView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        col.addView(speedView);

        unitView = label("km/h", 14, COLOR_MUTED, false);
        unitView.setGravity(Gravity.CENTER);
        unitView.setLetterSpacing(0.14f);
        unitView.setPadding(0, dp(4), 0, dp(8));
        col.addView(unitView);

        elapsedView = label("0.0 s", 40, COLOR_FG, true);
        elapsedView.setGravity(Gravity.CENTER);
        elapsedView.setIncludeFontPadding(false);
        elapsedView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        col.addView(elapsedView);

        rail = new PaceRail(this);
        LinearLayout.LayoutParams railLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(8));
        railLp.topMargin = dp(18);
        rail.setLayoutParams(railLp);
        col.addView(rail);

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        col.addView(spacer);

        LinearLayout card = column();
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(rounded(COLOR_SURFACE, dp(20), COLOR_LINE));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.topMargin = dp(22);
        card.setLayoutParams(cardLp);

        LinearLayout cardHead = row();
        cardHead.setGravity(Gravity.CENTER_VERTICAL);
        limitLabel = label(I18n.t("limit"), 12, COLOR_MUTED, true);
        limitLabel.setLetterSpacing(0.16f);
        limitLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        kmhButton = chip("km/h", !mph);
        mphButton = chip("mph", mph);
        kmhButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnitMph(false);
            }
        });
        mphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnitMph(true);
            }
        });
        cardHead.addView(limitLabel);
        cardHead.addView(kmhButton);
        View gap = new View(this);
        gap.setLayoutParams(new LinearLayout.LayoutParams(dp(6), 1));
        cardHead.addView(gap);
        cardHead.addView(mphButton);
        card.addView(cardHead);

        LinearLayout stepper = row();
        stepper.setGravity(Gravity.CENTER_VERTICAL);
        stepper.setPadding(0, dp(8), 0, 0);
        minusButton = chip("−0.5", false);
        plusButton = chip("+0.5", false);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bumpLimit(-0.5f);
            }
        });
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bumpLimit(0.5f);
            }
        });
        limitView = label("90.0", 42, COLOR_FG, true);
        limitView.setGravity(Gravity.CENTER);
        limitView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        limitView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        stepper.addView(minusButton);
        stepper.addView(limitView);
        stepper.addView(plusButton);
        card.addView(stepper);
        col.addView(card);

        LinearLayout modes = row();
        modes.setPadding(0, dp(12), 0, 0);
        gpsButton = chip("GPS", true);
        benchButton = chip("Bench", false);
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, dp(48), 1f);
        gpsButton.setLayoutParams(half);
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, dp(48), 1f);
        half2.leftMargin = dp(8);
        benchButton.setLayoutParams(half2);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBenchMode(false);
            }
        });
        benchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBenchMode(true);
            }
        });
        modes.addView(gpsButton);
        modes.addView(benchButton);
        col.addView(modes);

        benchLabel = label("", 14, COLOR_MUTED, false);
        benchLabel.setPadding(0, dp(12), 0, 0);
        col.addView(benchLabel);
        benchBar = new SeekBar(this);
        benchBar.setMax(250);
        benchBar.setProgress(0);
        benchBar.setProgressTintList(android.content.res.ColorStateList.valueOf(COLOR_SIGNAL));
        benchBar.setThumbTintList(android.content.res.ColorStateList.valueOf(COLOR_SIGNAL));
        benchBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                benchKmh = Pace.fromDisplay(progress, mph);
                if (tracking && bench) sample(benchKmh, true);
                else paint();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        col.addView(benchBar);

        armButton = new Button(this);
        armButton.setAllCaps(false);
        armButton.setTransformationMethod(null);
        armButton.setTextColor(COLOR_BG);
        armButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        armButton.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        armButton.setStateListAnimator(null);
        armBg = new GradientDrawable();
        armBg.setCornerRadius(dp(28));
        armBg.setColor(COLOR_SIGNAL);
        armButton.setBackground(armBg);
        LinearLayout.LayoutParams armLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56));
        armLp.topMargin = dp(14);
        armButton.setLayoutParams(armLp);
        armButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onArm();
            }
        });
        col.addView(armButton);

        LinearLayout power = row();
        power.setPadding(0, dp(8), 0, 0);
        minimizeButton = chip(I18n.t("minimize"), false);
        exitButton = chip(I18n.t("exit"), false);
        LinearLayout.LayoutParams halfM = new LinearLayout.LayoutParams(0, dp(48), 1f);
        minimizeButton.setLayoutParams(halfM);
        LinearLayout.LayoutParams halfE = new LinearLayout.LayoutParams(0, dp(48), 1f);
        halfE.leftMargin = dp(8);
        exitButton.setLayoutParams(halfE);
        minimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minimize();
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitApp();
            }
        });
        power.addView(minimizeButton);
        power.addView(exitButton);
        col.addView(power);

        hintView = label("", 14, COLOR_MUTED, false);
        hintView.setPadding(dp(6), dp(12), dp(6), 0);
        hintView.setGravity(Gravity.CENTER);
        hintView.setLineSpacing(dp(3), 1f);
        col.addView(hintView);

        scroll.addView(col, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        return scroll;
    }

    private View buildAbout() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(COLOR_BG);
        LinearLayout col = column();
        col.setPadding(dp(20), dp(18), dp(20), dp(28));

        backButton = chip(I18n.t("back"), false);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbout(false);
            }
        });
        col.addView(backButton);

        TextView kicker = label("OVERMARK", 12, COLOR_SIGNAL, true);
        kicker.setLetterSpacing(0.18f);
        kicker.setPadding(0, dp(28), 0, dp(6));
        col.addView(kicker);

        aboutVersion = label(I18n.t("version") + " " + versionName, 42, COLOR_FG, true);
        aboutVersion.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        col.addView(aboutVersion);

        aboutAuthorKicker = label(I18n.t("author"), 12, COLOR_MUTED, true);
        aboutAuthorKicker.setLetterSpacing(0.16f);
        aboutAuthorKicker.setPadding(0, dp(28), 0, dp(6));
        col.addView(aboutAuthorKicker);

        aboutAuthor = label("Claudiu Ivan", 36, COLOR_FG, true);
        aboutAuthor.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        col.addView(aboutAuthor);

        aboutDisclaimerTitle = label(I18n.t("disclaimerTitle"), 12, COLOR_MUTED, true);
        aboutDisclaimerTitle.setLetterSpacing(0.16f);
        aboutDisclaimerTitle.setPadding(0, dp(28), 0, dp(6));
        col.addView(aboutDisclaimerTitle);

        aboutDisclaimer = label(I18n.t("disclaimer"), 16, COLOR_MUTED, false);
        aboutDisclaimer.setLineSpacing(dp(4), 1f);
        col.addView(aboutDisclaimer);

        aboutPrivacyTitle = label(I18n.t("privacyTitle"), 12, COLOR_MUTED, true);
        aboutPrivacyTitle.setLetterSpacing(0.16f);
        aboutPrivacyTitle.setPadding(0, dp(22), 0, dp(6));
        col.addView(aboutPrivacyTitle);

        aboutPrivacy = label(I18n.t("privacy"), 16, COLOR_MUTED, false);
        aboutPrivacy.setLineSpacing(dp(4), 1f);
        col.addView(aboutPrivacy);

        aboutHow = label(I18n.t("how"), 16, COLOR_MUTED, false);
        aboutHow.setPadding(0, dp(22), 0, 0);
        aboutHow.setLineSpacing(dp(4), 1f);
        col.addView(aboutHow);

        scroll.addView(col, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private View buildLanguage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(COLOR_BG);
        LinearLayout col = column();
        col.setPadding(dp(20), dp(18), dp(20), dp(24));

        langTitle = label(I18n.t("chooseLanguage"), 34, COLOR_FG, true);
        langTitle.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        col.addView(langTitle);

        langHint = label(I18n.t("chooseHint"), 15, COLOR_MUTED, false);
        langHint.setPadding(0, dp(8), 0, dp(14));
        langHint.setLineSpacing(dp(3), 1f);
        col.addView(langHint);

        langSearch = new EditText(this);
        langSearch.setSingleLine(true);
        langSearch.setHint(I18n.t("searchLang"));
        langSearch.setTextColor(COLOR_FG);
        langSearch.setHintTextColor(COLOR_MUTED);
        langSearch.setBackground(rounded(COLOR_SURFACE, dp(22), COLOR_LINE));
        langSearch.setPadding(dp(16), dp(12), dp(16), dp(12));
        col.addView(langSearch);

        final LinearLayout list = column();
        final ArrayList<Button> buttons = new ArrayList<Button>();
        JSONArray langs = I18n.languages();
        for (int i = 0; i < langs.length(); i++) {
            JSONObject item = langs.optJSONObject(i);
            if (item == null) continue;
            final String code = item.optString("code");
            String nativeName = item.optString("native");
            Button button = chip(nativeName, false);
            button.setTag((code + " " + nativeName).toLowerCase(Locale.US));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
            lp.topMargin = dp(8);
            button.setLayoutParams(lp);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseLang(code);
                }
            });
            list.addView(button);
            buttons.add(button);
        }
        langSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase(Locale.US);
                for (int i = 0; i < buttons.size(); i++) {
                    Button button = buttons.get(i);
                    String tag = String.valueOf(button.getTag());
                    boolean match = query.length() == 0 || tag.contains(query);
                    button.setVisibility(match ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        col.addView(list);
        scroll.addView(col, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(COLOR_BG);
        return layout;
    }

    private LinearLayout row() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private TextView label(String text, int sp, int color, boolean strong) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", strong ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    private Button chip(String text, boolean selected) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTransformationMethod(null);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        button.setStateListAnimator(null);
        button.setMinWidth(dp(72));
        button.setMinimumWidth(dp(72));
        button.setMinimumHeight(dp(44));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(44));
        button.setLayoutParams(lp);
        button.setPadding(dp(16), 0, dp(16), 0);
        styleChoice(button, selected);
        return button;
    }

    private void styleChoice(Button button, boolean selected) {
        button.setTextColor(selected ? COLOR_BG : COLOR_FG);
        button.setBackground(rounded(selected ? COLOR_SIGNAL : COLOR_SURFACE, dp(22), COLOR_LINE));
        button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
    }

    private GradientDrawable rounded(int fill, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        drawable.setStroke(Math.max(1, dp(1)), stroke);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
