package me.lucky.sentry

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

import me.lucky.sentry.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Preferences
    private lateinit var prefsdb: Preferences
    private lateinit var admin: DeviceAdminManager

    private val registerForDeviceAdmin =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) setOff() else setOn()
        }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        prefs.copyTo(prefsdb, key)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init1()
        if (initBiometric()) return
        init2()
        setup()
    }

    override fun onStart() {
        super.onStart()
        prefs.registerListener(prefsListener)
        update()
    }

    override fun onStop() {
        super.onStop()
        prefs.unregisterListener(prefsListener)
    }

    private fun initBiometric(): Boolean {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        when (BiometricManager
            .from(this)
            .canAuthenticate(authenticators))
        {
            BiometricManager.BIOMETRIC_SUCCESS -> {}
            else -> return false
        }
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback()
        {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                finishAndRemoveTask()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                init2()
                setup()
            }
        })
        try {
            prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.authentication))
                .setConfirmationRequired(false)
                .setAllowedAuthenticators(authenticators)
                .build())
        } catch (exc: Exception) { return false }
        return true
    }

    private fun init1() {
        prefs = Preferences(this)
        prefsdb = Preferences(this, encrypted = false)
        prefs.copyTo(prefsdb)
        admin = DeviceAdminManager(this)
    }

    private fun init2() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            !admin.canUsbDataSignalingBeDisabled() ||
            !admin.isDeviceOwner())
                disableUsbDataSignaling()
        binding.apply {
            maxFailedPasswordAttempts.value = prefs.maxFailedPasswordAttempts.toFloat()
            usbDataSignaling.isChecked = isUsbDataSignalingEnabled()
            toggle.isChecked = prefs.isEnabled
        }
    }

    private fun setup() = binding.apply {
        maxFailedPasswordAttempts.addOnChangeListener { _, value, _ ->
            prefs.maxFailedPasswordAttempts = value.toInt()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            usbDataSignaling.setOnCheckedChangeListener { _, isChecked ->
                try { admin.setUsbDataSignalingEnabled(isChecked) } catch (exc: Exception) {
                    Snackbar.make(
                        usbDataSignaling,
                        R.string.usb_data_signaling_change_failed_popup,
                        Snackbar.LENGTH_SHORT,
                    ).show()
                    usbDataSignaling.isChecked = !isChecked
                }
            }
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) requestAdmin() else setOff()
        }
    }

    private fun disableUsbDataSignaling() { binding.usbDataSignaling.isEnabled = false }

    private fun setOn() {
        prefs.isEnabled = true
        binding.toggle.isChecked = true
    }

    private fun setOff() {
        prefs.isEnabled = false
        try { admin.remove() } catch (exc: SecurityException) {}
        binding.toggle.isChecked = false
    }

    private fun update() { binding.usbDataSignaling.isChecked = isUsbDataSignalingEnabled() }

    private fun isUsbDataSignalingEnabled() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        admin.isUsbDataSignalingEnabled() else true

    private fun requestAdmin() = registerForDeviceAdmin.launch(admin.makeRequestIntent())
}