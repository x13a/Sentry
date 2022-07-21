package me.lucky.sentry.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import me.lucky.sentry.*
import me.lucky.sentry.databinding.FragmentMonitorBinding

class MonitorFragment : Fragment() {
    private lateinit var binding: FragmentMonitorBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences
    private lateinit var prefsdb: Preferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMonitorBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        prefs.registerListener(prefsListener)
    }

    override fun onStop() {
        super.onStop()
        prefs.unregisterListener(prefsListener)
    }

    private fun init() {
        ctx = requireContext()
        prefs = Preferences(ctx)
        prefsdb = Preferences(ctx, encrypted = false)
        NotificationManager(ctx).createNotificationChannels()
        binding.apply {
            val opts = prefs.monitor
            password.isChecked = opts.and(Monitor.PASSWORD.value) != 0
            internet.isChecked = opts.and(Monitor.INTERNET.value) != 0
        }
    }

    private fun setup() = binding.apply {
        password.setOnCheckedChangeListener { _, isChecked ->
            prefs.monitor = Utils.setFlag(prefs.monitor, Monitor.PASSWORD.value, isChecked)
        }
        internet.setOnCheckedChangeListener { _, isChecked ->
            prefs.monitor = Utils.setFlag(prefs.monitor, Monitor.INTERNET.value, isChecked)
            if (isChecked) updateDatabase()
        }
        gotoBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun updateDatabase() {
        val db = AppDatabase.getInstance(ctx).packageDao()
        db.deleteAll()
        for (app in ctx.packageManager
            .getInstalledApplications(0)
            .filterNot { Utils.hasInternet(ctx, it.packageName) })
        {
            try { db.insert(Package(0, app.packageName)) }
            catch (exc: SQLiteConstraintException) {}
        }
    }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        prefs.copyTo(prefsdb, key)
    }
}