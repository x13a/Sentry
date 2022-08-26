package me.lucky.sentry.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle

import me.lucky.sentry.Monitor
import me.lucky.sentry.NotificationManager
import me.lucky.sentry.Preferences

class DeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onPasswordFailed(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordFailed(context, intent, user)
        val prefs = Preferences(context)
        if (!prefs.isEnabled) return
        if (prefs.monitor.and(Monitor.PASSWORD.value) != 0)
            NotificationManager(context).notifyPassword()
        if (prefs.isMaxFailedPasswordAttemptsDefaultApiChecked) return
        val maxFailedPasswordAttempts = prefs.maxFailedPasswordAttempts
        if (maxFailedPasswordAttempts <= 0) return
        val admin = DeviceAdminManager(context)
        if (admin.getCurrentFailedPasswordAttempts() >= maxFailedPasswordAttempts)
            try { admin.wipeData() } catch (_: SecurityException) {}
    }
}