# XposedFakeLocation

![GitHub License](https://img.shields.io/github/license/noobexon1/XposedFakeLocation?color=blue)
![GitHub Release Date](https://img.shields.io/github/release-date/noobexon1/XposedFakeLocation?color=violet)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/noobexon1/XposedFakeLocation/total)
![GitHub repo size](https://img.shields.io/github/repo-size/noobexon1/XposedFakeLocation)
![GitHub Repo stars](https://img.shields.io/github/stars/noobexon1/XposedFakeLocation)
![GitHub Release](https://img.shields.io/github/v/release/noobexon1/XposedFakeLocation?color=red)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)]()

**XposedFakeLocation** is an Android app and Xposed module for location spoofing without enabling Android Developer Options mock location. Current versions focus on system-service-level hooks, MIUI 13 / Android 12 compatibility, and in-app target app selection.

<div align="center">
    <img src="images/xposedfakelocation.webp" alt="App Logo" width="256" />
</div>

---

## Table of Contents

- [Current Version](#current-version)
- [Features](#features)
- [Supported Environment](#supported-environment)
- [Installation](#installation)
- [Detailed Usage](#detailed-usage)
- [LSPosed Scope Setup](#lsposed-scope-setup)
- [Target Apps](#target-apps)
- [Map and Spoofing Controls](#map-and-spoofing-controls)
- [Settings](#settings)
- [Troubleshooting](#troubleshooting)
- [Build from Source](#build-from-source)
- [License](#license)
- [Disclaimer](#disclaimer)
- [Acknowledgements](#acknowledgements)

---

## Current Version

- Version name: `0.0.7`
- Version code: `7`
- Primary tested device: Redmi K40
- Primary tested ROM: MIUI 13.0.7.0
- Primary tested Android version: Android 12
- Recommended LSPosed scope: `android` system framework only

See `RELEASEINFO.md` for the release summary.

---

## Features

- **System-service-level spoofing**: installs the main hooks in the Android system framework process.
- **In-app target app selection**: choose spoofed apps inside XposedFakeLocation instead of selecting every target app in LSPosed.
- **MIUI 13 / Android 12 handling**: includes MIUI location service hooks such as `MiuiBlurLocationManagerImpl`.
- **Per-app filtering**: only apps selected in `Target Apps` receive spoofed location data.
- **Custom coordinates**: select precise latitude and longitude from the map.
- **Accuracy, altitude, speed, and randomization**: optional controls for more realistic location data.
- **GNSS / geofence / Wi-Fi side-channel handling**: reduces obvious inconsistencies for selected target apps.
- **Favorites**: save frequently used locations.

---

## Supported Environment

Required:

- Rooted Android device.
- LSPosed or another compatible Xposed framework.
- Android 11+ is expected by the app project. Android 12 / MIUI 13 is the primary optimized target.

Recommended test environment:

- Redmi K40
- MIUI 13.0.7.0
- Android 12
- LSPosed enabled in Zygisk or Riru environment

Important:

- This version is designed so that LSPosed only needs the `android` system framework scope.
- Target apps are selected inside XposedFakeLocation, not in LSPosed.
- After changing LSPosed scope, reboot the phone.

---

## Installation

1. Build or download the APK.
2. Install the APK on the phone.
3. Open LSPosed Manager.
4. Enable the XposedFakeLocation module.
5. Open the module scope page.
6. Select only:

   ```text
   Android System Framework (android)
   ```

7. Reboot the phone.
8. Open XposedFakeLocation and complete the in-app setup.

Do not select target apps in LSPosed for the normal `0.0.7` flow. Use the app's `Target Apps` page instead.

---

## Detailed Usage

### 1. Enable the Module

In LSPosed:

1. Enable `XposedFakeLocation`.
2. Enter module scope settings.
3. Select `Android System Framework (android)` only.
4. Reboot the phone.

If you previously selected many target apps in LSPosed, remove them from the LSPosed scope. The new flow is controlled by the app itself.

### 2. Select Target Apps

Open XposedFakeLocation:

1. Open the navigation drawer.
2. Tap `Target Apps`.
3. Search for the app you want to spoof.
4. Check the app.
5. Return to the map.

Only checked apps receive spoofed location data. Unchecked apps continue to receive normal system location data.

### 3. Select a Fake Location

1. Open `Map`.
2. Tap on the map to place the marker.
3. Use the selected point as the spoofed location.
4. Optional: save it to `Favorites` for reuse.

### 4. Start Spoofing

1. Make sure at least one target app is selected in `Target Apps`.
2. Make sure a map location is selected.
3. Tap the start/play control on the map screen.
4. Force stop and reopen the target app if it was already running.

### 5. Stop Spoofing

1. Return to XposedFakeLocation.
2. Tap the stop control.
3. Target apps should return to normal location behavior.

---

## LSPosed Scope Setup

Correct scope for version `0.0.7`:

```text
android
```

Do not normally select:

```text
com.android.phone
target app package names
```

Why:

- The module hooks the Android system framework process.
- System hooks inspect the caller package name.
- The app's `Target Apps` list decides which apps receive spoofed data.
- This avoids the old workflow where every target app had to be manually selected in LSPosed.

When to reboot:

- After enabling the module.
- After changing LSPosed scope.
- After installing a new build.

When reboot is not normally required:

- Changing the selected target app list inside XposedFakeLocation.
- Changing the fake coordinate.
- Changing accuracy, altitude, speed, or randomization settings.

---

## Target Apps

The `Target Apps` page manages the per-app spoofing list.

What it does:

- Lists installed launcher apps.
- Supports searching by app name or package name.
- Saves selected packages to `target_apps`.
- Writes data to both app storage and Xposed-readable SharedPreferences.
- Allows system hooks to decide whether to spoof each caller.

Recommended process:

1. Select only the apps that need spoofed location.
2. Keep system apps unchecked unless you know why they need spoofed location.
3. After selecting an app, force stop and reopen that app.

If an app is not selected in `Target Apps`, XposedFakeLocation should not spoof it.

---

## Map and Spoofing Controls

Use the `Map` page to control the active spoofed coordinate.

Basic flow:

1. Tap the map.
2. Confirm the marker is placed at the desired location.
3. Start spoofing.
4. Open the target app.

Favorites:

- Save common locations from the map.
- Reuse saved locations from `Favorites`.
- If a marker is already selected, the favorite dialog can reuse that coordinate.

---

## Settings

Open `Settings` to tune the generated location data.

Available options:

- **Randomize Nearby Location**: randomizes coordinates within a radius.
- **Custom Horizontal Accuracy**: sets horizontal accuracy in meters.
- **Custom Vertical Accuracy**: sets vertical accuracy in meters.
- **Custom Altitude**: sets altitude.
- **Custom MSL**: sets mean sea level value where supported.
- **Custom MSL Accuracy**: sets mean sea level accuracy.
- **Custom Speed**: sets speed.
- **Custom Speed Accuracy**: sets speed accuracy.

Recommended starting point:

- Keep advanced settings disabled at first.
- Confirm basic coordinate spoofing works.
- Enable randomization or accuracy tuning only when needed.

---

## Redmi K40 / MIUI 13 Notes

For Redmi K40 on MIUI 13.0.7.0 / Android 12:

- Use LSPosed scope `android` only.
- Reboot after enabling the module.
- Select target apps inside `Target Apps`.
- Force stop target apps after changing target selection.
- MIUI-specific hooks include `MiuiBlurLocationManagerImpl` handling.

Expected hook areas:

- `LocationManagerService.getLastLocation`
- `LocationProviderManager.onReportLocation`
- `MiuiBlurLocationManagerImpl.getBlurryLocation`
- `MiuiBlurLocationManagerImpl.getBlurryCellLocation`
- `MiuiBlurLocationManagerImpl.getBlurryCellInfos`
- GNSS registration blocking for selected target apps
- Wi-Fi location side-channel replacement for selected target apps

---

## Troubleshooting

### Build succeeds but app does not spoof location

Check:

1. LSPosed module is enabled.
2. Scope contains `android`.
3. Phone has been rebooted after enabling/changing scope.
4. Target app is selected in `Target Apps`.
5. A fake location is selected on the map.
6. Spoofing is started.
7. Target app has been force stopped and reopened.

### Target app still receives real location

Try:

1. Reopen XposedFakeLocation and confirm the target app is still checked.
2. Stop spoofing, start spoofing again.
3. Force stop the target app.
4. Reboot the phone.
5. Check LSPosed logs for XposedFakeLocation hook messages.

### All apps appear affected

Check that only intended apps are selected in `Target Apps`.

If you selected target apps in LSPosed directly, remove them from LSPosed scope and keep only `android`.

### Target app detects abnormal location

Try enabling more realistic settings:

- Set horizontal accuracy.
- Set vertical accuracy.
- Avoid zero speed if the app expects movement.
- Use small randomization radius.

Some apps use server-side checks, account history, network region, IP address, Bluetooth, sensors, or proprietary SDKs. XposedFakeLocation cannot guarantee bypassing every detection strategy.

### LSPosed logs to look for

Useful keywords:

```text
MainHook
SystemServicesHooks
MiuiBlurLocationManagerImpl
LocationProviderManager
Target Apps
```

---

## Build from Source

Clone the repository:

```bash
git clone https://github.com/noobexon1/XposedFakeLocation.git
```

Build debug APK:

```bash
ANDROID_HOME="/path/to/android/sdk" ANDROID_SDK_ROOT="/path/to/android/sdk" sh ./gradlew :app:assembleDebug
```

Build debug APK, unit test artifact, and Android test artifact:

```bash
ANDROID_HOME="/path/to/android/sdk" ANDROID_SDK_ROOT="/path/to/android/sdk" sh ./gradlew :app:assembleDebug :app:assembleDebugUnitTest :app:assembleDebugAndroidTest
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The project keeps Java 8 bytecode compatibility and suppresses JDK 21 obsolete source/target option warnings.

---

## Development

Built with:

- Kotlin
- Jetpack Compose
- Material 3
- Xposed API
- OSMDroid
- DataStore
- Gson
- HiddenApiBypass

Key module areas:

- `xposed/MainHook.kt`: Xposed entry point.
- `xposed/hooks/SystemServicesHooks.kt`: system framework location hooks.
- `xposed/hooks/LocationApiHooks.kt`: optional app-process fallback hooks.
- `xposed/utils/PreferencesUtil.kt`: Xposed-side preference reader.
- `manager/ui/targetapps`: in-app target app selection UI.
- `data/repository/PrefrencesRepository.kt`: app-side preference storage.

---

## License

Distributed under the MIT License. See `LICENSE` for more information.

---

## Disclaimer

This application is intended for development, testing, and privacy research. Misuse of location spoofing can violate app terms of service, local laws, or platform policies. Use it at your own risk. The authors are not responsible for damage, account restrictions, data loss, or other consequences caused by use of this module.

---

## Acknowledgements

- [GpsSetter](https://github.com/Android1500/GpsSetter)
- [Xposed Framework](https://repo.xposed.info/)
- [LSPosed](https://github.com/LSPosed/LSPosed)
- [OSMDroid](https://github.com/osmdroid/osmdroid)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Line Awesome Icons](https://icons8.com/line-awesome)
- [Fucklocation](https://github.com/Mikotwa/FuckLocation)
