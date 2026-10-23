package com.overmark.app;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;

/** Short tick plus a looping two-tone alarm. Falls back silently if audio fails. */
public final class SoundBoard {
    private static final int RATE = 16000;

    private final AudioTrack beepTrack;
    private final AudioTrack alarmTrack;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean alarmOn;
    private final Runnable alarmReplay;

    private SoundBoard(AudioTrack beepTrack, AudioTrack alarmTrack, int alarmMs) {
        this.beepTrack = beepTrack;
        this.alarmTrack = alarmTrack;
        alarmReplay = new Runnable() {
            @Override
            public void run() {
                if (!alarmOn) return;
                restart(alarmTrack);
                handler.postDelayed(this, Math.max(120, alarmMs - 40));
            }
        };
    }

    public static SoundBoard create() {
        try {
            short[] beep = beepPcm();
            short[] alarm = alarmPcm();
            AudioTrack beepTrack = track(beep, AudioAttributes.USAGE_MEDIA);
            AudioTrack alarmTrack = track(alarm, AudioAttributes.USAGE_ALARM);
            if (beepTrack.getState() != AudioTrack.STATE_INITIALIZED) return null;
            if (alarmTrack.getState() != AudioTrack.STATE_INITIALIZED) return null;
            return new SoundBoard(beepTrack, alarmTrack, alarm.length * 1000 / RATE);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public void beep() {
        restart(beepTrack);
    }

    public void startAlarm() {
        if (alarmOn) return;
        alarmOn = true;
        handler.removeCallbacks(alarmReplay);
        alarmReplay.run();
    }

    public void stopAlarm() {
        alarmOn = false;
        handler.removeCallbacks(alarmReplay);
        try {
            if (alarmTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                alarmTrack.pause();
            }
            alarmTrack.setPlaybackHeadPosition(0);
        } catch (IllegalStateException ignored) {
            /* already stopped */
        }
    }

    public void release() {
        stopAlarm();
        try {
            beepTrack.release();
        } catch (RuntimeException ignored) {
            /* gone */
        }
        try {
            alarmTrack.release();
        } catch (RuntimeException ignored) {
            /* gone */
        }
    }

    private static void restart(AudioTrack track) {
        try {
            if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                track.pause();
            }
            track.setPlaybackHeadPosition(0);
            track.play();
        } catch (IllegalStateException first) {
            try {
                track.stop();
                track.reloadStaticData();
                track.play();
            } catch (IllegalStateException ignored) {
                /* device refused playback */
            }
        }
    }

    private static AudioTrack track(short[] pcm, int usage) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(usage)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        AudioFormat format = new AudioFormat.Builder()
                .setSampleRate(RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build();
        AudioTrack track = new AudioTrack(
                attrs,
                format,
                pcm.length * 2,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE);
        track.write(pcm, 0, pcm.length);
        return track;
    }

    private static short[] beepPcm() {
        int n = (int) (RATE * 0.09);
        short[] pcm = new short[n];
        for (int i = 0; i < n; i++) {
            double env = Math.exp(-4.2 * i / n);
            double attack = Math.min(1.0, i / (RATE * 0.006));
            double sample = Math.sin(2.0 * Math.PI * 1046.0 * i / RATE);
            pcm[i] = (short) (sample * attack * env * 22000.0);
        }
        return pcm;
    }

    private static short[] alarmPcm() {
        int n = (int) (RATE * 0.64);
        short[] pcm = new short[n];
        double phase = 0.0;
        for (int i = 0; i < n; i++) {
            boolean high = (i / (RATE / 6)) % 2 == 0;
            double freq = high ? 880.0 : 622.0;
            phase += 2.0 * Math.PI * freq / RATE;
            double edge = 1.0;
            int fade = RATE / 80;
            if (i < fade) edge = i / (double) fade;
            if (i > n - fade) edge = (n - i) / (double) fade;
            double sample = Math.sin(phase) * 0.85 + Math.sin(2.0 * phase) * 0.15;
            pcm[i] = (short) (sample * edge * 20000.0);
        }
        return pcm;
    }
}
