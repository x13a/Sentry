package me.lucky.sentry

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Preferences(ctx: Context) {
    companion object {
        private const val ENABLED = "enabled"
        private const val MAX_FAILED_PASSWORD_ATTEMPTS = "max_failed_password_attempts"
        private const val MAX_FAILED_PASSWORD_ATTEMPTS_DEFAULT_API =
            "max_failed_password_attempts_default_api"
        private const val USB_DATA_SIGNALING_CTL_ENABLED = "usb_data_signaling_ctl_enabled"
        private const val MONITOR = "monitor"

        // migration
        private const val SERVICE_ENABLED = "service_enabled"
        private const val MAX_FAILED_PASSWORD_ATTEMPTS_WARNING =
            "max_failed_password_attempts_warning"
    }

    private val context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        ctx.createDeviceProtectedStorageContext() else ctx
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var isEnabled: Boolean
        get() = prefs.getBoolean(ENABLED, prefs.getBoolean(SERVICE_ENABLED, false))
        set(value) = prefs.edit { putBoolean(ENABLED, value) }

    var maxFailedPasswordAttempts: Int
        get() = prefs.getInt(MAX_FAILED_PASSWORD_ATTEMPTS, 0)
        set(value) = prefs.edit { putInt(MAX_FAILED_PASSWORD_ATTEMPTS, value) }

    var isMaxFailedPasswordAttemptsDefaultApiChecked: Boolean
        get() = prefs.getBoolean(
            MAX_FAILED_PASSWORD_ATTEMPTS_DEFAULT_API,
            prefs.getBoolean(MAX_FAILED_PASSWORD_ATTEMPTS_WARNING, false),
        )
        set(value) = prefs.edit { putBoolean(MAX_FAILED_PASSWORD_ATTEMPTS_DEFAULT_API, value) }

    var isUsbDataSignalingCtlEnabled: Boolean
        get() = prefs.getBoolean(USB_DATA_SIGNALING_CTL_ENABLED, false)
        set(value) = prefs.edit { putBoolean(USB_DATA_SIGNALING_CTL_ENABLED, value) }

    var monitor: Int
        get() = prefs.getInt(MONITOR, 0)
        set(value) = prefs.edit { putInt(MONITOR, value) }
}

enum class Monitor(val value: Int) {
    PASSWORD(1),
    INTERNET(1 shl 1),
}