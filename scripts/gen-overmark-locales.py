#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate Overmark locale tables for the web app and the Android APK."""
import json
from pathlib import Path

KEYS = [
    "back", "version", "author", "disclaimerTitle", "disclaimer", "privacyTitle", "privacy",
    "how", "apkNote", "download", "chooseLanguage", "chooseHint", "searchLang", "noMatch",
    "language", "about", "standby", "searching", "under", "marking", "alarm",
    "hintBenchIdle", "hintBenchOn", "hintGpsIdle", "hintDenied", "hintInsecure", "hintNoFix",
    "hintSearch", "hintAlarm", "hintLive", "limit", "stepDown", "stepUp", "bench", "benchSpeed",
    "disarm", "armGps", "armBench", "minimize", "exit", "closedTitle", "closedBody", "reopen",
    "minimized", "restore",
]

ROWS = []


def add(code, native, direction, texts):
    missing = [key for key in KEYS if key not in texts]
    extra = [key for key in texts if key not in KEYS]
    if missing or extra:
        raise SystemExit("%s missing=%s extra=%s" % (code, missing, extra))
    ROWS.append({
        "code": code,
        "native": native,
        "dir": direction,
        "messages": {key: texts[key] for key in KEYS},
    })


def T(**kwargs):
    return kwargs

add("en", "English", "ltr", T(
    back="Back", version="Version", author="Author",
    disclaimerTitle="Disclaimer",
    disclaimer="Overmark is only a driving aid, not a certified speedometer. GPS speed can lag or jump. You alone are responsible for watching the road and obeying the law. Do not let the sound take your eyes off the road.",
    privacyTitle="No data collected",
    privacy="This app collects no data. Speed is read on the device and is not saved as a trip, not sent to a server, and not shared. There is no account. When you exit, temporary files and any logs this app can delete are wiped. Only your language and speed limit stay on the device.",
    how="Set the limit before you move. It starts at 90 km/h and changes in steps of 0.5 km/h. While you stay faster than the limit, a short beep marks each second. After 45 seconds an alarm sounds and the speed flashes red. Drop under the limit and the count starts over.",
    apkNote="Minimize sends the app to the background. Exit stops tracking and clears everything except the language and the speed limit. Beeps follow media volume. The alarm uses alarm volume.",
    download="Download Android APK",
    chooseLanguage="Choose a language",
    chooseHint="This choice stays on the device. Nothing is uploaded. You can change the language later.",
    searchLang="Search languages", noMatch="No matching language.",
    language="Language", about="About",
    standby="STANDBY", searching="SEARCHING", under="UNDER LIMIT", marking="MARKING", alarm="ALARM",
    hintBenchIdle="Slide a speed, then arm. You get the same beeps and the 45 second alarm without moving.",
    hintBenchOn="Each second over the limit is a short beep. At 45 seconds the alarm sounds and the speed flashes red.",
    hintGpsIdle="Set the limit, then arm GPS. Each second over the limit is marked with a beep.",
    hintDenied="Location permission was denied. Allow it, or use Bench.",
    hintInsecure="This page cannot read GPS. Use Bench, or install the Android app.",
    hintNoFix="No GPS fix yet. Go outdoors with location on, or use Bench.",
    hintSearch="Waiting for GPS. The timer starts when speed is live.",
    hintAlarm="Still over the limit. The speed keeps flashing until you slow down.",
    hintLive="Live GPS. Drop under the limit and the count resets.",
    limit="LIMIT",
    stepDown="Decrease the limit by 0.5 km/h",
    stepUp="Increase the limit by 0.5 km/h",
    bench="Bench", benchSpeed="Bench speed",
    disarm="Disarm", armGps="Arm GPS", armBench="Arm bench",
    minimize="Minimize", exit="Exit",
    closedTitle="Closed",
    closedBody="Tracking is off. Temporary files were cleared. No data was collected. Your language and speed limit were kept.",
    reopen="Open again", minimized="In the background", restore="Restore",
))

# LANGS

