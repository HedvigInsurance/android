package com.hedvig.app.feature.chat.service

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
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.tracking.NotificationOpenedTrackingActivity
import com.hedvig.app.service.push.getMutablePendingIntentFlags
import com.hedvig.app.service.push.senders.NotificationSender
import com.hedvig.app.service.push.setupNotificationChannel
import com.hedvig.app.util.extensions.getStoredBoolean

class ChatNotificationSender(
  private val context: Context,
) : NotificationSender {
  override fun createChannel() {
    setupNotificationChannel(
      context,
      CHAT_CHANNEL_ID,
      context.resources.getString(hedvig.resources.R.string.NOTIFICATION_CHAT_CHANNEL_NAME),
      context.resources.getString(hedvig.resources.R.string.NOTIFICATION_CHAT_CHANNEL_DESCRIPTION),
    )
  }

  override fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    if (context.getStoredBoolean(ChatActivity.ACTIVITY_IS_IN_FOREGROUND)) {
      return
    }

    val hedvigPerson = hedvigPerson()
    val messageText =
      remoteMessage.data[DATA_NEW_MESSAGE_BODY] ?: return
    val message = NotificationCompat.MessagingStyle.Message(
      messageText,
      System.currentTimeMillis(),
      hedvigPerson,
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
        } ?: defaultMessagingStyle(message)
    } else {
      defaultMessagingStyle(message)
    }

    sendChatNotificationInner(
      context,
      messagingStyle,
    )
  }

  override fun handlesNotificationType(notificationType: String) = notificationType == NOTIFICATION_TYPE_NEW_MESSAGE

  private fun defaultMessagingStyle(
    message: NotificationCompat.MessagingStyle.Message,
  ) = NotificationCompat.MessagingStyle(youPerson()).addMessage(message)

  private fun sendChatNotificationInner(
    context: Context,
    style: NotificationCompat.MessagingStyle,
    alertOnlyOnce: Boolean = false,
  ) {
    val chatIntent = Intent(context, ChatActivity::class.java)
    chatIntent.putExtra(ChatActivity.EXTRA_SHOW_CLOSE, true)

    val flags = getMutablePendingIntentFlags()

    val pendingIntent: PendingIntent? = TaskStackBuilder
      .create(context)
      .run {
        addNextIntentWithParentStack(chatIntent)
        addNextIntentWithParentStack(
          NotificationOpenedTrackingActivity.newInstance(context, NOTIFICATION_TYPE_NEW_MESSAGE),
        )
        getPendingIntent(0, flags)
      }
    val replyRemoteInput = RemoteInput.Builder(CHAT_REPLY_KEY)
      .setLabel(context.getString(hedvig.resources.R.string.notifications_chat_reply_action))
      .build()

    val replyPendingIntent = PendingIntent.getBroadcast(
      context,
      CHAT_REPLY_REQUEST_CODE,
      Intent(context, ChatMessageNotificationReceiver::class.java).apply {
        putExtra(
          CHAT_REPLY_DATA_NOTIFICATION_ID,
          CHAT_NOTIFICATION_ID,
        )
      },
      flags,
    )

    val replyAction = NotificationCompat.Action.Builder(
      android.R.drawable.ic_menu_send,
      context.getString(hedvig.resources.R.string.notifications_chat_reply_action),
      replyPendingIntent,
    )
      .addRemoteInput(replyRemoteInput)
      .build()

    val notification = NotificationCompat
      .Builder(
        context,
        CHAT_CHANNEL_ID,
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
    replyText: CharSequence,
  ) {
    val notificationManager = context.getSystemService<NotificationManager>() ?: return

    val existingChatNotification = notificationManager
      .activeNotifications
      .firstOrNull { it.id == notificationId }
      ?.notification ?: return

    val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
      existingChatNotification,
    ) ?: return
    style.addMessage(replyText, System.currentTimeMillis(), style.user)

    sendChatNotificationInner(
      context,
      style,
      alertOnlyOnce = true,
    )
  }

  private fun hedvigPerson() = Person.Builder()
    .setName(context.getString(hedvig.resources.R.string.NOTIFICATION_CHAT_TITLE))
    .setImportant(true)
    .setKey(HEDVIG_PERSON_KEY)
    .setIcon(IconCompat.createWithResource(context, R.drawable.ic_hedvig_h))
    .build()

  private fun youPerson() = Person.Builder()
    .setName(context.getString(hedvig.resources.R.string.notifications_chat_you))
    .setImportant(true)
    .setKey(YOU_PERSON_KEY)
    .build()

  companion object {
    private const val CHAT_CHANNEL_ID = "hedvig-chat"
    private const val CHAT_NOTIFICATION_ID = 1
    private const val HEDVIG_PERSON_KEY = "HEDVIG"
    private const val YOU_PERSON_KEY = "YOU"

    internal const val CHAT_REPLY_KEY = "CHAT_REPLY_KEY"
    internal const val CHAT_REPLY_DATA_NOTIFICATION_ID = "CHAT_REPLY_DATA_NOTIFICATION_ID"
    private const val CHAT_REPLY_REQUEST_CODE = 2380

    internal const val DATA_NEW_MESSAGE_BODY = "DATA_NEW_MESSAGE_BODY"

    private const val NOTIFICATION_TYPE_NEW_MESSAGE = "NEW_MESSAGE"
  }
}
