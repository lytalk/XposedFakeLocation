package com.noobexon.xposedfakelocation.manager.util

import android.content.Context
import android.widget.Toast
import com.noobexon.xposedfakelocation.data.repository.PreferencesRepository

/**
 * Shows a short Toast only when the user has left toast notifications enabled in Settings.
 */
object AppToast {
    fun showShort(context: Context, message: CharSequence) {
        val app = context.applicationContext
        if (!PreferencesRepository(app).getShowToastNotifications()) return
        Toast.makeText(app, message, Toast.LENGTH_SHORT).show()
    }
}
