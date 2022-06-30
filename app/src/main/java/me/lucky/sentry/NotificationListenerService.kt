package me.lucky.sentry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.notification.NotificationListenerService
import androidx.annotation.RequiresApi

class NotificationListenerService : NotificationListenerService() {
    private val lockReceiver = LockReceiver()

    override fun onCreate() {
        super.onCreate()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        deinit()
    }

    private fun init() {
        val admin = DeviceAdminManager(this)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            !admin.canUsbDataSignalingBeDisabled() ||
            !admin.isDeviceOwner()) { return }
        registerReceiver(lockReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    private fun deinit() {
        try { unregisterReceiver(lockReceiver) } catch (exc: IllegalArgumentException) {}
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
            catch (exc: Exception) {}
        }
    }
}