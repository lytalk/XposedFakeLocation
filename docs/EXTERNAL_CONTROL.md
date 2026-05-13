# External Intent Control

XposedFakeLocation exposes a `BroadcastReceiver` that lets another app on the device control faking headlessly — start it, stop it, and update the fake coordinates — without opening the XposedFakeLocation UI.

## Security model

The receiver is guarded by a custom permission:

```
com.noobexon.xposedfakelocation.permission.CONTROL
```

It is declared with `android:protectionLevel="signature"`, meaning **only apps signed with the same signing key** as XposedFakeLocation are granted the permission and can deliver intents to the receiver. As a fallback, requests from the same UID (the manager app itself) are also accepted.

If you want a third-party app that is signed with a different key to control XposedFakeLocation, rebuild XposedFakeLocation with the permission's `protectionLevel` changed (for example to `signature|privileged` or remove the protection level), and have the calling app declare `<uses-permission android:name="com.noobexon.xposedfakelocation.permission.CONTROL" />`.

## Actions and extras

| Action | Extras | Effect |
|--------|--------|--------|
| `com.noobexon.xposedfakelocation.action.START` | optional `latitude` (double), `longitude` (double) | Sets `is_playing = true`. If lat/lon extras are provided, the active fake location is updated first. |
| `com.noobexon.xposedfakelocation.action.STOP`  | none | Sets `is_playing = false`. |
| `com.noobexon.xposedfakelocation.action.SET_LOCATION` | required `latitude` (double), `longitude` (double); optional `accuracy` (float); optional `start` (boolean) | Updates active fake location. If `accuracy` is provided, accuracy override is enabled. If `start=true`, faking is also started. |

All writes go through the same `PreferencesRepository` the in-app UI uses, so they propagate to `XSharedPreferences` consumed by the Xposed hooks automatically.

## Testing with adb

```sh
# Start faking using whatever location was last set
adb shell am broadcast \
  -a com.noobexon.xposedfakelocation.action.START \
  -n com.noobexon.xposedfakelocation/.manager.control.ControlReceiver

# Start faking at a specific location
adb shell am broadcast \
  -a com.noobexon.xposedfakelocation.action.START \
  -n com.noobexon.xposedfakelocation/.manager.control.ControlReceiver \
  --ed latitude 37.7749 --ed longitude -122.4194

# Set location only (does not start)
adb shell am broadcast \
  -a com.noobexon.xposedfakelocation.action.SET_LOCATION \
  -n com.noobexon.xposedfakelocation/.manager.control.ControlReceiver \
  --ed latitude 48.8566 --ed longitude 2.3522 --ef accuracy 5.0

# Set location and immediately start
adb shell am broadcast \
  -a com.noobexon.xposedfakelocation.action.SET_LOCATION \
  -n com.noobexon.xposedfakelocation/.manager.control.ControlReceiver \
  --ed latitude 48.8566 --ed longitude 2.3522 --ez start true

# Stop faking
adb shell am broadcast \
  -a com.noobexon.xposedfakelocation.action.STOP \
  -n com.noobexon.xposedfakelocation/.manager.control.ControlReceiver
```

Note: `adb shell am broadcast` runs from the `shell` UID, which does not hold the signature permission. To bypass the permission check during testing, either temporarily relax `protectionLevel` in the manifest, or add `--receiver-permission com.noobexon.xposedfakelocation.permission.CONTROL` only after granting it (not possible for signature-level). For real testing, send the broadcast from a co-signed app (see Kotlin snippet below).

## Caller snippet (Kotlin)

```kotlin
import android.content.Context
import android.content.Intent

object FakeLocationControl {
    private const val PKG = "com.noobexon.xposedfakelocation"
    private const val RECEIVER = "$PKG.manager.control.ControlReceiver"
    private const val PERMISSION = "$PKG.permission.CONTROL"

    fun start(context: Context, lat: Double? = null, lon: Double? = null) {
        val intent = Intent("$PKG.action.START").apply {
            setClassName(PKG, RECEIVER)
            if (lat != null && lon != null) {
                putExtra("latitude", lat)
                putExtra("longitude", lon)
            }
        }
        context.sendBroadcast(intent, PERMISSION)
    }

    fun stop(context: Context) {
        val intent = Intent("$PKG.action.STOP").apply {
            setClassName(PKG, RECEIVER)
        }
        context.sendBroadcast(intent, PERMISSION)
    }

    fun setLocation(
        context: Context,
        lat: Double,
        lon: Double,
        accuracy: Float? = null,
        startFaking: Boolean = false
    ) {
        val intent = Intent("$PKG.action.SET_LOCATION").apply {
            setClassName(PKG, RECEIVER)
            putExtra("latitude", lat)
            putExtra("longitude", lon)
            if (accuracy != null) putExtra("accuracy", accuracy)
            if (startFaking) putExtra("start", true)
        }
        context.sendBroadcast(intent, PERMISSION)
    }
}
```

The caller app must also declare in its own `AndroidManifest.xml`:

```xml
<uses-permission android:name="com.noobexon.xposedfakelocation.permission.CONTROL" />
```

and be signed with the same signing key as XposedFakeLocation.
