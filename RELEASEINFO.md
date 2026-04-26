# XposedFakeLocation 0.0.7 Release Info

## Version

- Version name: `0.0.7`
- Version code: `7`
- Target device focus: Redmi K40, MIUI 13.0.7.0, Android 12
- LSPosed scope model: select `android` system framework only

## Highlights

- Reworked spoofing from app-process hooks toward system-service hooks.
- Added in-app target app selection, so target apps no longer need to be selected in LSPosed.
- Added MIUI 13 / Android 12 location service handling for Redmi K40.
- Added system-side filtering by caller package name to avoid spoofing non-target apps.
- Improved build warning handling for JDK 21 Java 8 compatibility warnings.

## New User Flow

1. Enable the module in LSPosed.
2. Select only `Android System Framework (android)` as the LSPosed scope.
3. Reboot the phone.
4. Open XposedFakeLocation.
5. Open `Target Apps` from the drawer and select apps that should receive spoofed locations.
6. Select a location on the map and start spoofing.
7. Force stop and reopen the target app if it was already running.

## Xposed / System Hook Changes

- `MainHook` now installs system hooks in the `android` process.
- LSPosed default scope is reduced to `android`.
- App-layer hooks now check the in-app target app list before changing any values.
- System hooks now try to resolve the caller package and spoof only selected target apps.

## Location Spoofing Coverage

- Hooks `LocationManagerService.getLastLocation`.
- Hooks Android 12 `LocationProviderManager.onReportLocation`.
- Hooks location receiver callbacks where available.
- Supports MIUI location wrappers:
  - `MiuiBlurLocationManagerImpl.getBlurryLocation`
  - `MiuiBlurLocationManagerImpl.getBlurryCellLocation`
  - `MiuiBlurLocationManagerImpl.getBlurryCellInfos`
  - `MiuiBlurLocationManagerImpl.handleGpsLocationChangedLocked`
- Blocks GNSS-related registration for selected target apps.
- Blocks geofence registration for selected target apps.
- Clears or replaces Wi-Fi location side-channel data for selected target apps.

## Target App Management

- Added `Target Apps` screen.
- Lists installed launcher apps.
- Supports searching by app label or package name.
- Stores selected packages in `target_apps`.
- Writes target app data to both DataStore and Xposed-readable SharedPreferences.

## Redmi K40 / MIUI 13 Notes

- The release is tuned for Android 12 service class names used by MIUI 13.
- `MiuiBlurLocationManagerImpl` is handled explicitly.
- Only apps selected inside XposedFakeLocation should receive spoofed location data.
- Non-selected apps should continue receiving normal system location data.

## Build Verification

Verified with:

```text
:app:assembleDebug
:app:assembleDebugUnitTest
:app:assembleDebugAndroidTest
BUILD SUCCESSFUL
```

## Known Notes

- Some Kotlin deprecation warnings remain from existing APIs such as `MODE_WORLD_READABLE`, `NeighboringCellInfo`, and older Compose icons.
- These warnings do not block packaging.
- Phone-process cell hooks remain available in code, but the default LSPosed scope is now `android` only.
