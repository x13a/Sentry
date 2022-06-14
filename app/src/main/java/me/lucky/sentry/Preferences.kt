package me.lucky.sentry

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class Preferences(ctx: Context) : Prefs {
    companion object {
        const val ENABLED = "enabled"
        const val MAX_FAILED_PASSWORD_ATTEMPTS = "max_failed_password_attempts"

        private const val FILE_NAME = "sec_shared_prefs"
        // migration
        private const val SERVICE_ENABLED = "service_enabled"
    }

    private val mk = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs = EncryptedSharedPreferences.create(
        FILE_NAME,
        mk,
        ctx,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override var isEnabled: Boolean
        get() = prefs.getBoolean(ENABLED, prefs.getBoolean(SERVICE_ENABLED, false))
        set(value) = prefs.edit { putBoolean(ENABLED, value) }

    override var maxFailedPasswordAttempts: Int
        get() = prefs.getInt(MAX_FAILED_PASSWORD_ATTEMPTS, 0)
        set(value) = prefs.edit { putInt(MAX_FAILED_PASSWORD_ATTEMPTS, value) }
}

class PreferencesProxy(ctx: Context) {
    private val prefs = Preferences(ctx)
    private val prefsdb = PreferencesDirectBoot(ctx)

    fun clone() {
        prefsdb.isEnabled = prefs.isEnabled
        prefsdb.maxFailedPasswordAttempts = prefsdb.maxFailedPasswordAttempts
    }

    var isEnabled: Boolean
        get() = prefs.isEnabled
        set(value) {
            prefs.isEnabled = value
            prefsdb.isEnabled = value
        }

    var maxFailedPasswordAttempts: Int
        get() = prefs.maxFailedPasswordAttempts
        set(value) {
            prefs.maxFailedPasswordAttempts = value
            prefsdb.maxFailedPasswordAttempts = value
        }
}

interface Prefs {
    var isEnabled: Boolean
    var maxFailedPasswordAttempts: Int
}

class PreferencesDirectBoot(ctx: Context) : Prefs {
    private val context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        ctx.createDeviceProtectedStorageContext() else ctx
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    override var isEnabled: Boolean
        get() = prefs.getBoolean(Preferences.ENABLED, false)
        set(value) = prefs.edit { putBoolean(Preferences.ENABLED, value) }

    override var maxFailedPasswordAttempts: Int
        get() = prefs.getInt(Preferences.MAX_FAILED_PASSWORD_ATTEMPTS, 0)
        set(value) = prefs.edit { putInt(Preferences.MAX_FAILED_PASSWORD_ATTEMPTS, value) }
}
