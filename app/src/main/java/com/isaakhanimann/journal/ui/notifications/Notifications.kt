package com.isaakhanimann.journal.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.isaakhanimann.journal.MainActivity
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.localization.I18n
import java.time.Instant
import java.time.ZoneId

/** Intent extras used by notification taps to steer the app to a target screen. */
const val EXTRA_NAVIGATE_TO = "navigateTo"
const val EXTRA_EXPERIENCE_ID = "experienceId"
const val NAV_QUICK_NOTE = "quick_note"
const val NAV_TIME_CAPSULE = "time_capsule"

/** Cancels the effect notification for an experience; used by the "End" action. */
class EffectNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val experienceId = intent.getIntExtra(EXTRA_EXPERIENCE_ID, -1)
        if (experienceId > 0) {
            NotificationManagerCompat.from(context)
                .cancel(EFFECT_NOTIFICATION_ID_BASE + experienceId)
        }
    }
}

/**
 * Notification helpers: "active effects" notification (stays visible while effects
 * last, with a quick-note action) and the "time capsule" daily reminder.
 */
object Notifications {

    const val CHANNEL_EFFECTS = "effects"
    const val CHANNEL_TIME_CAPSULE = "time_capsule"
    private const val EFFECT_NOTIFICATION_ID_BASE = 1000
    const val TIME_CAPSULE_NOTIFICATION_ID = 2001
    private const val REQUEST_CODE_BASE = 3000

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_EFFECTS,
                I18n.translate(context, "effect_notification_channel"),
                NotificationManager.IMPORTANCE_LOW
            )
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_TIME_CAPSULE,
                I18n.translate(context, "time_capsule_channel"),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    fun hasNotificationPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            NotificationManagerCompat.from(context).areNotificationsEnabled()

    /**
     * Shows (or refreshes) the "effects in progress" notification for an experience.
     */
    fun showEffectNotification(
        context: Context,
        experienceId: Int,
        substanceName: String,
        ingestionTime: Instant
    ) {
        if (!hasNotificationPermission(context)) return
        val timeText = ingestionTime.atZone(ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        val text = I18n.translate(
            context,
            "effect_notification_text",
            mapOf("substance" to substanceName, "time" to timeText)
        )

        // Quick-note action: opens the app directly on the quick-note screen.
        val noteIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, NAV_QUICK_NOTE)
            putExtra(EXTRA_EXPERIENCE_ID, experienceId)
        }
        val notePendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_BASE + experienceId,
            noteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // End action: cancels the notification via a broadcast receiver.
        val stopIntent = Intent(context, EffectNotificationReceiver::class.java).apply {
            putExtra(EXTRA_EXPERIENCE_ID, experienceId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + experienceId + 100,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_EFFECTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(I18n.translate(context, "effect_notification_title"))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(notePendingIntent)
            .addAction(
                0,
                I18n.translate(context, "effect_notification_action_note"),
                notePendingIntent
            )
            .addAction(
                0,
                I18n.translate(context, "effect_notification_action_stop"),
                stopPendingIntent
            )
            .build()
        NotificationManagerCompat.from(context)
            .notify(EFFECT_NOTIFICATION_ID_BASE + experienceId, notification)
    }

    fun cancelEffectNotification(context: Context, experienceId: Int) {
        NotificationManagerCompat.from(context)
            .cancel(EFFECT_NOTIFICATION_ID_BASE + experienceId)
    }

    /**
     * Shows the daily "time capsule" notification: something was recorded exactly
     * one year ago today.
     */
    fun showTimeCapsuleNotification(context: Context, experienceCount: Int) {
        if (!hasNotificationPermission(context)) return
        val text = I18n.translate(
            context,
            "time_capsule_notification_text",
            mapOf("count" to experienceCount.toString())
        )
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, NAV_TIME_CAPSULE)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_BASE + 200,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_TIME_CAPSULE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(I18n.translate(context, "time_capsule_notification_title"))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        NotificationManagerCompat.from(context)
            .notify(TIME_CAPSULE_NOTIFICATION_ID, notification)
    }
}
