package me.lucky.sentry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import me.lucky.sentry.databinding.ActivityMainBinding
import me.lucky.sentry.fragment.MainFragment
import me.lucky.sentry.fragment.MonitorFragment

open class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (initBiometric()) return
        init()
        setup()
    }

    private fun init() {
        replaceFragment(MainFragment())
    }

    private fun setup() = binding.apply {
        appBar.setNavigationOnClickListener {
            drawer.open()
        }
        navigation.setNavigationItemSelectedListener {
            replaceFragment(getFragment(it.itemId))
            it.isChecked = true
            drawer.close()
            true
        }
    }

    private fun replaceFragment(f: Fragment) =
        supportFragmentManager
            .beginTransaction()
            .replace(binding.fragment.id, f)
            .commit()

    private fun getFragment(id: Int) = when (id) {
        R.id.nav_main -> MainFragment()
        R.id.nav_monitor -> MonitorFragment()
        else -> MainFragment()
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
                init()
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
}