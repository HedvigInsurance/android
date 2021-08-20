package com.hedvig.app.feature.chat.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
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
    fun sendChatNotification(context: Context, remoteMessage: RemoteMessage) {
        createChannel(context)

        if (context.getStoredBoolean(ChatActivity.ACTIVITY_IS_IN_FOREGROUND)) {
            return
        }

        val hedvigPerson = hedvigPerson(context)
        val messageText =
            remoteMessage.data[DATA_NEW_MESSAGE_BODY] ?: return
        val message = NotificationCompat.MessagingStyle.Message(
            messageText,
            System.currentTimeMillis(),
            hedvigPerson
        )

        val messagingStyle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notificationManager = context.getSystemService<NotificationManager>()

            notificationManager
                ?.activeNotifications
                ?.firstOrNull { it.id == CHAT_NOTIFICATION_ID }
                ?.notification
                ?.let { existingNotification ->
                    NotificationCompat.MessagingStyle
                        .extractMessagingStyleFromNotification(existingNotification)
                        ?.addMessage(message)
                } ?: defaultMessagingStyle(context, message)
        } else {
            defaultMessagingStyle(context, message)
        }

        sendChatNotificationInner(
            context,
            messagingStyle
        )
    }

    private fun defaultMessagingStyle(
        context: Context,
        message: NotificationCompat.MessagingStyle.Message
    ) = NotificationCompat.MessagingStyle(youPerson(context)).addMessage(message)

    @SuppressLint("UnspecifiedImmutableFlag") // Remove this lint warning when targeting SDK 31
    private fun sendChatNotificationInner(
        context: Context,
        style: NotificationCompat.MessagingStyle,
        alertOnlyOnce: Boolean = false
    ) {
        val chatIntent = Intent(context, ChatActivity::class.java)
        chatIntent.putExtra(ChatActivity.EXTRA_SHOW_CLOSE, true)

        val pendingIntent: PendingIntent? = TaskStackBuilder
            .create(context)
            .run {
                addNextIntentWithParentStack(chatIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        val replyRemoteInput = RemoteInput.Builder(CHAT_REPLY_KEY)
            .setLabel(context.getString(R.string.notifications_chat_reply_action))
            .build()

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            CHAT_REPLY_REQUEST_CODE,
            Intent(context, ChatMessageNotificationReceiver::class.java).apply {
                putExtra(
                    CHAT_REPLY_DATA_NOTIFICATION_ID,
                    CHAT_NOTIFICATION_ID
                )
            },
            PendingIntent.FLAG_UPDATE_CURRENT // Add `PendingIntent.FLAG_MUTABLE` when targeting SDK 31`
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            context.getString(R.string.notifications_chat_reply_action),
            replyPendingIntent
        )
            .addRemoteInput(replyRemoteInput)
            .build()

        val notification = NotificationCompat
            .Builder(
                context,
                CHAT_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_h)
            .setStyle(style)
            .addAction(replyAction)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setChannelId(CHAT_CHANNEL_ID)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(alertOnlyOnce)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(CHAT_NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun addReplyToExistingChatNotification(
        context: Context,
        notificationId: Int,
        replyText: CharSequence
    ) {
        val notificationManager = context.getSystemService<NotificationManager>() ?: return

        val existingChatNotification = notificationManager
            .activeNotifications
            .firstOrNull { it.id == notificationId }
            ?.notification ?: return

        val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
            existingChatNotification
        ) ?: return
        style.addMessage(replyText, System.currentTimeMillis(), style.user)

        sendChatNotificationInner(
            context,
            style,
            alertOnlyOnce = true
        )
    }

    fun sendDefaultNotification(context: Context, remoteMessage: RemoteMessage) {
        createChannel(
            context
        )

        val title = remoteMessage.data[DATA_MESSAGE_TITLE]
            ?: context.resources.getString(R.string.NOTIFICATION_CHAT_TITLE)
        val body = remoteMessage.data[DATA_MESSAGE_BODY]
            ?: context.resources.getString(R.string.NOTIFICATION_CHAT_BODY)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, SplashActivity::class.java),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat
            .Builder(
                context,
                PushNotificationService.NOTIFICATION_CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_hedvig_h)
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

    private const val HEDVIG_PERSON_KEY = "HEDVIG"
    private const val YOU_PERSON_KEY = "YOU"

    const val CHAT_REPLY_KEY = "CHAT_REPLY_KEY"
    const val CHAT_REPLY_DATA_NOTIFICATION_ID = "CHAT_REPLY_DATA_NOTIFICATION_ID"
    private const val CHAT_REPLY_REQUEST_CODE = 2380

    internal const val DATA_NEW_MESSAGE_BODY = "DATA_NEW_MESSAGE_BODY"

    private fun hedvigPerson(context: Context) = Person.Builder()
        .setName(context.getString(R.string.NOTIFICATION_CHAT_TITLE))
        .setImportant(true)
        .setKey(HEDVIG_PERSON_KEY)
        .setIcon(IconCompat.createWithResource(context, R.drawable.ic_hedvig_h))
        .build()

    private fun youPerson(context: Context) = Person.Builder()
        .setName(context.getString(R.string.notifications_chat_you))
        .setImportant(true)
        .setKey(YOU_PERSON_KEY)
        .build()
}
