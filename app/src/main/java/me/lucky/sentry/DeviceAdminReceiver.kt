package me.lucky.sentry

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserHandle
import android.os.UserManager

class DeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onPasswordFailed(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordFailed(context, intent, user)
        val prefs = Preferences(context, encrypted = Build.VERSION.SDK_INT < Build.VERSION_CODES.N
                || context.getSystemService(UserManager::class.java)?.isUserUnlocked == true)
        val maxFailedPasswordAttempts = prefs.maxFailedPasswordAttempts
        if (!prefs.isEnabled || maxFailedPasswordAttempts <= 0) return
        val admin = DeviceAdminManager(context)
        if (admin.getCurrentFailedPasswordAttempts() >= maxFailedPasswordAttempts)
            try { admin.wipeData() } catch (exc: SecurityException) {}
    }
}