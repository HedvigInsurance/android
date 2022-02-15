package com.hedvig.app.service.push.senders

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.R
import com.hedvig.app.SplashActivity
import com.hedvig.app.feature.tracking.NotificationOpenedTrackingActivity
import com.hedvig.app.service.push.DATA_MESSAGE_BODY
import com.hedvig.app.service.push.DATA_MESSAGE_TITLE
import com.hedvig.app.service.push.getImmutablePendingIntentFlags
import com.hedvig.app.service.push.setupNotificationChannel
import java.util.concurrent.atomic.AtomicInteger

class GenericNotificationSender(
    private val context: Context,
) : NotificationSender {
    private val id = AtomicInteger(100)

    override fun createChannel() {
        setupNotificationChannel(
            context,
            GENERIC_CHANNEL_ID,
            context.resources.getString(R.string.NOTIFICATION_CHANNEL_GENERIC_TITLE)
        )
    }

    override fun sendNotification(type: String, remoteMessage: RemoteMessage) {
        val title = remoteMessage.data[DATA_MESSAGE_TITLE]
        val body = remoteMessage.data[DATA_MESSAGE_BODY]
        val pendingIntent = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(
                    Intent(
                        context,
                        SplashActivity::class.java
                    )
                )
                addNextIntentWithParentStack(
                    NotificationOpenedTrackingActivity.newInstance(context, NOTIFICATION_TYPE_GENERIC_COMMUNICATION)
                )
                getPendingIntent(0, getImmutablePendingIntentFlags())
            }
        val notification = NotificationCompat
            .Builder(
                context,
                GENERIC_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_h)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setChannelId(GENERIC_CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                id.getAndIncrement(),
                notification
            )
    }

    override fun handlesNotificationType(notificationType: String) =
        notificationType == NOTIFICATION_TYPE_GENERIC_COMMUNICATION

    companion object {
        const val NOTIFICATION_TYPE_GENERIC_COMMUNICATION = "GENERIC_COMMUNICATION"

        private const val GENERIC_CHANNEL_ID = "hedvig-generic"
    }
}
