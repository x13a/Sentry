package me.lucky.sentry

import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationManager(private val ctx: Context) {
    companion object {
        private const val CHANNEL_PASSWORD_ID = "monitor_password"
        private const val CHANNEL_INTERNET_ID = "monitor_internet"
        private const val GROUP_KEY = "alert"
    }

    private val manager = NotificationManagerCompat.from(ctx)

    fun createNotificationChannels() {
        val build = { channelId: String, nameId: Int ->
            NotificationChannelCompat.Builder(
                channelId,
                NotificationManagerCompat.IMPORTANCE_HIGH,
            ).setName(ctx.getString(nameId)).build()
        }
        manager.createNotificationChannelsCompat(mutableListOf(
            build(CHANNEL_PASSWORD_ID, R.string.monitor_password),
            build(CHANNEL_INTERNET_ID, R.string.monitor_internet),
        ))
    }

    fun notifyInternet(packageName: String) =
        manager.notify(
            SystemClock.uptimeMillis().toInt(),
            buildNotification(NotificationCompat.Builder(ctx, CHANNEL_INTERNET_ID)
                .setContentText(formatInternetText(packageName))),)

    private fun buildNotification(b: NotificationCompat.Builder) = b
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(ctx.getString(R.string.notification_title))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_STATUS)
        .setShowWhen(true)
        .setAutoCancel(true)
        .setGroup(GROUP_KEY)
        .setGroupSummary(true)
        .build()

    fun notifyPassword() =
        manager.notify(
            SystemClock.uptimeMillis().toInt(),
            buildNotification(NotificationCompat.Builder(ctx, CHANNEL_PASSWORD_ID)
                .setContentText(ctx.getString(R.string.notification_password_text))
                .setSilent(true)),)

    private fun formatInternetText(packageName: String): String {
        var app: CharSequence = ctx.getString(R.string.unknown_app)
        try {
            app = ctx.packageManager
                .getApplicationLabel(ctx.packageManager.getApplicationInfo(packageName, 0))
        } catch (_: PackageManager.NameNotFoundException) {}
        return ctx.getString(R.string.notification_internet_text, app.toString(), packageName)
    }
}