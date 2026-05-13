package com.noobexon.xposedfakelocation.manager.control

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log
import com.noobexon.xposedfakelocation.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ControlReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ControlReceiver"

        const val PERMISSION_CONTROL = "com.noobexon.xposedfakelocation.permission.CONTROL"

        const val ACTION_START = "com.noobexon.xposedfakelocation.action.START"
        const val ACTION_STOP = "com.noobexon.xposedfakelocation.action.STOP"
        const val ACTION_SET_LOCATION = "com.noobexon.xposedfakelocation.action.SET_LOCATION"

        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_ACCURACY = "accuracy"
        const val EXTRA_START = "start"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (!isCallerAuthorized(context)) {
            Log.w(TAG, "Rejected intent ${intent.action}: caller not authorized")
            return
        }

        val action = intent.action ?: return
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val repository = PreferencesRepository(appContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_START -> handleStart(intent, repository)
                    ACTION_STOP -> repository.saveIsPlaying(false)
                    ACTION_SET_LOCATION -> handleSetLocation(intent, repository)
                    else -> Log.w(TAG, "Unknown action: $action")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling $action: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun isCallerAuthorized(context: Context): Boolean {
        val myUid = Process.myUid()
        val callingUid = sentFromUidOrNoOp()
        if (callingUid == myUid) return true
        val granted = context.checkCallingOrSelfPermission(PERMISSION_CONTROL)
        return granted == PackageManager.PERMISSION_GRANTED
    }

    private fun sentFromUidOrNoOp(): Int = try {
        android.os.Binder.getCallingUid()
    } catch (e: Throwable) {
        -1
    }

    private suspend fun handleStart(intent: Intent, repository: PreferencesRepository) {
        if (intent.hasExtra(EXTRA_LATITUDE) && intent.hasExtra(EXTRA_LONGITUDE)) {
            val lat = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN)
            val lon = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN)
            if (lat.isFinite() && lon.isFinite()) {
                repository.saveLastClickedLocation(lat, lon)
            }
        }
        repository.saveIsPlaying(true)
    }

    private suspend fun handleSetLocation(intent: Intent, repository: PreferencesRepository) {
        val lat = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN)
        val lon = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN)
        if (!lat.isFinite() || !lon.isFinite()) {
            Log.w(TAG, "SET_LOCATION missing or invalid latitude/longitude")
            return
        }
        repository.saveLastClickedLocation(lat, lon)

        if (intent.hasExtra(EXTRA_ACCURACY)) {
            val accuracy = intent.getFloatExtra(EXTRA_ACCURACY, Float.NaN)
            if (accuracy.isFinite()) {
                repository.saveUseAccuracy(true)
                repository.saveAccuracy(accuracy.toDouble())
            }
        }

        if (intent.getBooleanExtra(EXTRA_START, false)) {
            repository.saveIsPlaying(true)
        }
    }
}
