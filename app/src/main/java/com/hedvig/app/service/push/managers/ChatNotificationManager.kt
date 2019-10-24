package com.hedvig.app.service.push.managers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.R
import com.hedvig.app.SplashActivity
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.service.push.DATA_MESSAGE_BODY
import com.hedvig.app.service.push.DATA_MESSAGE_TITLE
import com.hedvig.app.service.push.PushNotificationService
import com.hedvig.app.service.push.setupNotificationChannel
import com.hedvig.app.util.extensions.getStoredBoolean

object ChatNotificationManager {
    fun sendChatNotification(context: Context) {
        createChannel(context)

        if (context.getStoredBoolean(ChatActivity.ACTIVITY_IS_IN_FOREGROUND)) {
            return
        }

        val chatIntent = Intent(context, ChatActivity::class.java)
        chatIntent.putExtra(ChatActivity.EXTRA_SHOW_CLOSE, true)

        val pendingIntent: PendingIntent? = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(chatIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        val notification = NotificationCompat
            .Builder(
                context,
                CHAT_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_symbol_android)
            .setContentTitle(context.resources.getString(R.string.NOTIFICATION_CHAT_TITLE))
            .setContentText(context.resources.getString(R.string.NOTIFICATION_CHAT_BODY))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setChannelId(CHAT_CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(CHAT_NOTIFICATION_ID, notification)
    }

    fun sendDefaultNotification(context: Context, remoteMessage: RemoteMessage) {
        createChannel(context)

        val title = remoteMessage.data[DATA_MESSAGE_TITLE]
            ?: context.resources.getString(R.string.NOTIFICATION_CHAT_TITLE)
        val body = remoteMessage.data[DATA_MESSAGE_BODY]
            ?: context.resources.getString(R.string.NOTIFICATION_CHAT_BODY)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, SplashActivity::class.java),
            PendingIntent.FLAG_ONE_SHOT
        )

        val notification = NotificationCompat
            .Builder(
                context,
                PushNotificationService.NOTIFICATION_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_symbol_android)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setChannelId(PushNotificationService.NOTIFICATION_CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(PushNotificationService.NOTIFICATION_ID, notification)
    }

    fun createChannel(context: Context) {
        setupNotificationChannel(
            context,
            CHAT_CHANNEL_ID,
            context.resources.getString(R.string.NOTIFICATION_CHAT_CHANNEL_NAME),
            context.resources.getString(R.string.NOTIFICATION_CHAT_CHANNEL_DESCRIPTION)
        )
    }

    private const val CHAT_CHANNEL_ID = "hedvig-chat"
    private const val CHAT_NOTIFICATION_ID = 1

}
