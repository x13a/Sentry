package me.lucky.sentry.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserHandle
import android.os.UserManager

import me.lucky.sentry.Monitor
import me.lucky.sentry.NotificationManager
import me.lucky.sentry.Preferences

class DeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onPasswordFailed(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordFailed(context, intent, user)
        val prefs = Preferences(context, encrypted = Build.VERSION.SDK_INT < Build.VERSION_CODES.N
                || context.getSystemService(UserManager::class.java)?.isUserUnlocked == true)
        if (prefs.monitor.and(Monitor.PASSWORD.value) != 0)
            NotificationManager(context).notifyPassword()
        if (prefs.isMaxFailedPasswordAttemptsWarningChecked) return
        val maxFailedPasswordAttempts = prefs.maxFailedPasswordAttempts
        if (!prefs.isEnabled || maxFailedPasswordAttempts <= 0) return
        val admin = DeviceAdminManager(context)
        if (admin.getCurrentFailedPasswordAttempts() >= maxFailedPasswordAttempts)
            try { admin.wipeData() } catch (exc: SecurityException) {}
    }
}