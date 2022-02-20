package me.lucky.sentry

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class Preferences(ctx: Context) {
    companion object {
        private const val SERVICE_ENABLED = "service_enabled"
        private const val MAX_FAILED_PASSWORD_ATTEMPTS = "max_failed_password_attempts"

        private const val FILE_NAME = "sec_shared_prefs"
    }

    private val mk = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs = EncryptedSharedPreferences.create(
        FILE_NAME,
        mk,
        ctx,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(SERVICE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(SERVICE_ENABLED, value) }

    var maxFailedPasswordAttempts: Int
        get() = prefs.getInt(MAX_FAILED_PASSWORD_ATTEMPTS, 0)
        set(value) = prefs.edit { putInt(MAX_FAILED_PASSWORD_ATTEMPTS, value) }
}
