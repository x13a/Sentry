package me.lucky.sentry

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class Preferences(ctx: Context, encrypted: Boolean = true) {
    companion object {
        private const val ENABLED = "enabled"
        private const val MAX_FAILED_PASSWORD_ATTEMPTS = "max_failed_password_attempts"
        private const val MAX_FAILED_PASSWORD_ATTEMPTS_WARNING =
            "max_failed_password_attempts_warning"
        private const val USB_DATA_SIGNALING_CTL_ENABLED = "usb_data_signaling_ctl_enabled"
        private const val MONITOR = "monitor"

        private const val FILE_NAME = "sec_shared_prefs"
        // migration
        private const val SERVICE_ENABLED = "service_enabled"
    }

    private val prefs: SharedPreferences = if (encrypted) {
        val mk = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            FILE_NAME,
            mk,
            ctx,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } else {
        val context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            ctx.createDeviceProtectedStorageContext() else ctx
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    var isEnabled: Boolean
        get() = prefs.getBoolean(ENABLED, prefs.getBoolean(SERVICE_ENABLED, false))
        set(value) = prefs.edit { putBoolean(ENABLED, value) }

    var maxFailedPasswordAttempts: Int
        get() = prefs.getInt(MAX_FAILED_PASSWORD_ATTEMPTS, 0)
        set(value) = prefs.edit { putInt(MAX_FAILED_PASSWORD_ATTEMPTS, value) }

    var isMaxFailedPasswordAttemptsWarningChecked: Boolean
        get() = prefs.getBoolean(MAX_FAILED_PASSWORD_ATTEMPTS_WARNING, false)
        set(value) = prefs.edit { putBoolean(MAX_FAILED_PASSWORD_ATTEMPTS_WARNING, value) }

    var isUsbDataSignalingCtlEnabled: Boolean
        get() = prefs.getBoolean(USB_DATA_SIGNALING_CTL_ENABLED, false)
        set(value) = prefs.edit { putBoolean(USB_DATA_SIGNALING_CTL_ENABLED, value) }

    var monitor: Int
        get() = prefs.getInt(MONITOR, 0)
        set(value) = prefs.edit { putInt(MONITOR, value) }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs.registerOnSharedPreferenceChangeListener(listener)

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs.unregisterOnSharedPreferenceChangeListener(listener)

    fun copyTo(dst: Preferences, key: String? = null) = dst.prefs.edit {
        for (entry in prefs.all.entries) {
            val k = entry.key
            if (key != null && k != key) continue
            val v = entry.value ?: continue
            when (v) {
                is Boolean -> putBoolean(k, v)
                is Int -> putInt(k, v)
            }
        }
    }
}

enum class Monitor(val value: Int) {
    PASSWORD(1),
    INTERNET(1 shl 1),
}