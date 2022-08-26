package me.lucky.sentry.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import me.lucky.sentry.*
import me.lucky.sentry.admin.DeviceAdminManager
import me.lucky.sentry.databinding.FragmentUserRestrictionsBinding

class UserRestrictionsFragment : Fragment() {
    private lateinit var binding: FragmentUserRestrictionsBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences
    private val admin by lazy { DeviceAdminManager(ctx) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUserRestrictionsBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    private fun init() {
        ctx = requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            safeBoot.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                    admin.isDeviceOwner()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                safeBoot.isChecked =
                    try {
                        admin
                            .getUserRestrictions()
                            ?.getBoolean(UserManager.DISALLOW_SAFE_BOOT) == true
                    } catch (_: SecurityException) { false }
        }
    }

    private fun setup() = binding.apply {
        safeBoot.setOnCheckedChangeListener { _, isChecked ->
            val v = UserManager.DISALLOW_SAFE_BOOT
            try { if (isChecked) admin.addUserRestriction(v) else admin.clearUserRestriction(v) }
            catch (_: SecurityException) {}
        }
    }
}