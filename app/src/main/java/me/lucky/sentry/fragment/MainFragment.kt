package me.lucky.sentry.fragment

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

import me.lucky.sentry.Preferences
import me.lucky.sentry.R
import me.lucky.sentry.admin.DeviceAdminManager
import me.lucky.sentry.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences
    private val admin by lazy { DeviceAdminManager(ctx) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        update()
    }

    private fun init() {
        ctx = requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            maxFailedPasswordAttempts.editText?.setText(prefs.maxFailedPasswordAttempts.toString())
            maxFailedPasswordAttemptsDefaultApi.isChecked =
                prefs.isMaxFailedPasswordAttemptsDefaultApiChecked
            val canChangeUsbDataSignaling = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    admin.canUsbDataSignalingBeDisabled() &&
                    admin.isDeviceOwner()
            usbDataSignaling.isEnabled = canChangeUsbDataSignaling
            usbDataSignaling.isChecked = isUsbDataSignalingEnabled()
            usbDataSignalingCtl.isEnabled = canChangeUsbDataSignaling
            usbDataSignalingCtl.isChecked = prefs.isUsbDataSignalingCtlEnabled
            toggle.isChecked = prefs.isEnabled
        }
    }

    private fun setup() = binding.apply {
        maxFailedPasswordAttempts.editText?.doAfterTextChanged {
            prefs.maxFailedPasswordAttempts = it?.toString()?.toIntOrNull() ?:
                return@doAfterTextChanged
            setMaximumFailedPasswordAttempts()
        }
        maxFailedPasswordAttemptsDefaultApi.setOnCheckedChangeListener { _, isChecked ->
            prefs.isMaxFailedPasswordAttemptsDefaultApiChecked = isChecked
            setMaximumFailedPasswordAttempts()
        }
        usbDataSignaling.setOnCheckedChangeListener { _, isChecked ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@setOnCheckedChangeListener
            try { admin.setUsbDataSignalingEnabled(isChecked) } catch (_: Exception) {
                Snackbar.make(
                    usbDataSignaling,
                    R.string.usb_data_signaling_change_failed_popup,
                    Snackbar.LENGTH_SHORT,
                ).show()
                usbDataSignaling.isChecked = !isChecked
            }
        }
        usbDataSignalingCtl.setOnCheckedChangeListener { _, isChecked ->
            prefs.isUsbDataSignalingCtlEnabled = isChecked
        }
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) requestAdmin() else setOff()
        }
    }

    private fun setOn() {
        setMaximumFailedPasswordAttempts()
        prefs.isEnabled = true
        binding.toggle.isChecked = true
    }

    private fun setOff() {
        prefs.isEnabled = false
        try { admin.remove() } catch (_: SecurityException) {}
        binding.toggle.isChecked = false
    }

    private fun update() { binding.usbDataSignaling.isChecked = isUsbDataSignalingEnabled() }
    private fun requestAdmin() = registerForDeviceAdmin.launch(admin.makeRequestIntent())

    private fun isUsbDataSignalingEnabled() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        admin.isUsbDataSignalingEnabled() else true

    private val registerForDeviceAdmin =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) setOff() else setOn()
        }

    private fun setMaximumFailedPasswordAttempts() = try {
        admin.setMaximumFailedPasswordsForWipe(
            if (prefs.isMaxFailedPasswordAttemptsDefaultApiChecked)
                prefs.maxFailedPasswordAttempts
            else 0
        )
    } catch (_: SecurityException) {}
}