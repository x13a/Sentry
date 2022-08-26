package me.lucky.sentry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import android.service.notification.NotificationListenerService
import androidx.annotation.RequiresApi

import me.lucky.sentry.admin.DeviceAdminManager

class NotificationListenerService : NotificationListenerService() {
    companion object {
        private const val DATA_SCHEME = "package"
    }

    private lateinit var prefs: Preferences
    private val lockReceiver = LockReceiver()
    private val packageReceiver = PackageReceiver()

    override fun onCreate() {
        super.onCreate()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        deinit()
    }

    private fun init() {
        prefs = Preferences(this)
        if (prefs.isUsbDataSignalingCtlEnabled)
            registerReceiver(lockReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_SCREEN_OFF)
            })
        if (prefs.monitor.and(Monitor.INTERNET.value) != 0)
            registerReceiver(packageReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
                addDataScheme(DATA_SCHEME)
            })
    }

    private fun deinit() {
        val unregister = { it: BroadcastReceiver ->
            try { unregisterReceiver(it) } catch (_: IllegalArgumentException) {}
        }
        unregister(lockReceiver)
        unregister(packageReceiver)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            migrateNotificationFilter(0, null)
    }

    private class LockReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                !Preferences(context ?: return).isEnabled) return
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> setUsbDataSignalingEnabled(context, true)
                Intent.ACTION_SCREEN_OFF -> setUsbDataSignalingEnabled(context, false)
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun setUsbDataSignalingEnabled(ctx: Context, enabled: Boolean) {
            try { DeviceAdminManager(ctx).setUsbDataSignalingEnabled(enabled) }
            catch (_: Exception) {}
        }
    }

    private class PackageReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Thread(Runner(context ?: return, intent ?: return, goAsync())).start()
        }

        private class Runner(
            private val ctx: Context,
            private val intent: Intent,
            private val pendingResult: PendingResult,
        ) : Runnable {
            override fun run() {
                process()
                pendingResult.finish()
            }

            private fun process() {
                val db by lazy { AppDatabase.getInstance(ctx).packageDao() }
                when (intent.action) {
                    Intent.ACTION_PACKAGE_ADDED -> {
                        if (intent.extras?.get(Intent.EXTRA_REPLACING) == true) return
                        val packageName = getPackageName(intent) ?: return
                        if (Utils.hasInternet(ctx, packageName)) return
                        try { db.insert(Package(0, packageName)) }
                        catch (_: SQLiteConstraintException) {}
                    }
                    Intent.ACTION_PACKAGE_REPLACED -> {
                        val packageName = getPackageName(intent) ?: return
                        db.select(packageName) ?: return
                        if (!Utils.hasInternet(ctx, packageName)) return
                        db.delete(packageName)
                        if (!Preferences(ctx).isEnabled) return
                        NotificationManager(ctx).notifyInternet(packageName)
                    }
                    Intent.ACTION_PACKAGE_FULLY_REMOVED ->
                        db.delete(getPackageName(intent) ?: return)
                }
            }

            private fun getPackageName(intent: Intent) = intent.data?.schemeSpecificPart
        }
    }
}