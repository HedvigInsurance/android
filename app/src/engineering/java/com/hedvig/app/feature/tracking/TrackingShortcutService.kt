package com.hedvig.app.feature.tracking

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.hedvig.app.R
import com.hedvig.app.service.push.setupNotificationChannel

class TrackingShortcutService : Service() {

    override fun onBind(intent: Intent?): Nothing? = null

    override fun onCreate() {
        showNotification()
    }

    private fun showNotification() {
        setupNotificationChannel(this, TRACKING_LOG_CHANNEL_ID, TRACKING_LOG_CHANNEL_NAME)
        val notification = NotificationCompat.Builder(this, TRACKING_LOG_CHANNEL_ID)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setShowWhen(false)
            .setLocalOnly(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    TrackingLogActivity.newInstance(this),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setSmallIcon(R.drawable.ic_hedvig_h)
            .setAutoCancel(false)
            .setContentTitle("Open Tracking Log")
            .setContentText("See all tracking events so far")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun removeNotification() {
        stopForeground(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val show = intent?.getBooleanExtra(SHOW, false) ?: false
        if (show) {
            showNotification()
        } else {
            removeNotification()
        }
        return START_NOT_STICKY
    }

    companion object {
        private const val NOTIFICATION_ID = 9888
        private const val TRACKING_LOG_CHANNEL_ID = "TRACKING_LOG_CHANNEL"
        private const val TRACKING_LOG_CHANNEL_NAME = "Tracking Log-Shortcut"

        private const val SHOW = "SHOW"

        fun newInstance(context: Context, show: Boolean) =
            Intent(context, TrackingShortcutService::class.java).apply {
                putExtra(SHOW, show)
            }
    }
}
