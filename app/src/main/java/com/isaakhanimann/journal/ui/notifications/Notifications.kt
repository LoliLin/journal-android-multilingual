package com.isaakhanimann.journal.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.isaakhanimann.journal.MainActivity
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.localization.I18n
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.Duration
import java.time.ZoneId

/** Intent extras used by notification taps to steer the app to a target screen. */
const val EXTRA_NAVIGATE_TO = "navigateTo"
const val EXTRA_EXPERIENCE_ID = "experienceId"
const val EXTRA_SUBSTANCE_NAME = "substanceName"
const val NAV_QUICK_NOTE = "quick_note"
const val NAV_TIME_CAPSULE = "time_capsule"
const val NAV_ADD_INGESTION = "widget_add_ingestion"
const val NAV_STATS = "widget_stats"
const val NAV_SUBSTANCE = "widget_substance"
const val NAV_CHOOSE_ROUTE = "widget_choose_route"
const val NAV_SUBSTANCE_COMPANION = "widget_substance_companion"

/** Cancels the effect notification for an experience; used by the "End" action. */
class EffectNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val experienceId = intent.getIntExtra(EXTRA_EXPERIENCE_ID, -1)
        if (experienceId > 0) {
            NotificationManagerCompat.from(context)
                .cancel(Notifications.EFFECT_NOTIFICATION_ID_BASE + experienceId)
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
    const val EFFECT_NOTIFICATION_ID_BASE = 1000
    const val TIME_CAPSULE_NOTIFICATION_ID = 2001
    private const val TAG = "Notifications"
    private const val REQUEST_CODE_BASE = 3000

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // IMPORTANCE_DEFAULT keeps BigPicture expansion (LOW makes OriginOS and
        // other skins drop the expand affordance); sound/vibration/lights are
        // off so updates never become heads-up banners.
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_EFFECTS,
                I18n.translate(context, "effect_notification_channel"),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
            }
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
        ingestionTime: Instant,
        effectEndTime: Instant = ingestionTime.plus(6, ChronoUnit.HOURS),
        timelineBitmap: android.graphics.Bitmap? = null
    ) {
        if (!hasNotificationPermission(context)) return
        val timeText = ingestionTime.atZone(ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern(com.isaakhanimann.journal.ui.utils.TimeFormat.timePattern))
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
        val style: NotificationCompat.Style =
            if (timelineBitmap != null) {
                // Timeline image attached: the text becomes the summary line and
                // the expanded view shows the picture instead of a large icon.
                NotificationCompat.BigPictureStyle()
                    .bigPicture(timelineBitmap)
                    .setSummaryText(text)
                    .bigLargeIcon(null as android.graphics.Bitmap?)
            } else {
                NotificationCompat.BigTextStyle().bigText(text)
            }
        if (timelineBitmap != null) {
            Log.d(
                TAG,
                "Timeline bitmap ${timelineBitmap.width}x${timelineBitmap.height}"
            )
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_EFFECTS)
            .setSmallIcon(R.drawable.ic_notification)
            // Substance names are sensitive: hide the content on the lock screen.
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(I18n.translate(context, "effect_notification_title"))
            .setContentText(text)
            .setStyle(style)
            .setOngoing(true)
            // DEFAULT keeps BigPicture expandable on OEM skins; all alert
            // channels are muted on the channel itself and here, and
            // setOnlyAlertOnce stops repeat banners on refreshes.
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(null)
            .setVibrate(null)
            .setDefaults(0)
            .setOnlyAlertOnce(true)
            // Auto-expire once the effect window is over (API 26+).
            .setTimeoutAfter(
                maxOf(
                    1000L,
                    Duration.between(Instant.now(), effectEndTime).toMillis()
                )
            )
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

    fun cancelAllEffectNotifications(context: Context) {
        // Effect notifications cannot be enumerated (id = base + experienceId);
        // cancel everything shown by the app. This also clears the day's time
        // capsule notification, which is acceptable when effects are disabled.
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancelAll()
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
