package com.hedvig.android.app.chat.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.TaskStackBuilder
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.getMutablePendingIntentFlags
import com.hedvig.android.core.common.android.notification.setupNotificationChannel
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R

class ChatNotificationSender(
  private val context: Context,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
) : NotificationSender {
  override fun createChannel() {
    setupNotificationChannel(
      context,
      CHAT_CHANNEL_ID,
      context.resources.getString(R.string.NOTIFICATION_CHAT_CHANNEL_NAME),
      context.resources.getString(R.string.NOTIFICATION_CHAT_CHANNEL_DESCRIPTION),
    )
  }

  override fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    // todo chat: Consider still not showing the notification when chat is on the foreground
//    if (context.getStoredBoolean(ChatFragment.ACTIVITY_IS_IN_FOREGROUND)) {
//      logcat(LogPriority.INFO) { "ChatNotificationSender ignoring notification since chat is open" }
//      return
//    }

    val hedvigPerson = hedvigPerson.toBuilder()
      .also { person ->
        val overriddenTitle = remoteMessage.data.titleFromCustomerIoData()
        if (overriddenTitle != null) {
          person.setName(overriddenTitle)
        }
      }
      .build()

    val messageText = context.getString(R.string.NOTIFICATION_CHAT_NEW_MESSAGE_BODY)

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

  private fun defaultMessagingStyle(message: NotificationCompat.MessagingStyle.Message) =
    NotificationCompat.MessagingStyle(youPerson).addMessage(message)

  private fun sendChatNotificationInner(
    context: Context,
    style: NotificationCompat.MessagingStyle,
    alertOnlyOnce: Boolean = false,
  ) {
    val chatIntent = Intent(Intent.ACTION_VIEW, Uri.parse(hedvigDeepLinkContainer.chat))

    val pendingIntent: PendingIntent? = TaskStackBuilder
      .create(context)
      .addNextIntent(chatIntent)
      .getPendingIntent(0, getMutablePendingIntentFlags())

    val notification = NotificationCompat
      .Builder(
        context,
        CHAT_CHANNEL_ID,
      )
      .setSmallIcon(R.drawable.ic_hedvig_h)
      .setStyle(style)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setAutoCancel(true)
      .setChannelId(CHAT_CHANNEL_ID)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
      .setContentIntent(pendingIntent)
      .setOnlyAlertOnce(alertOnlyOnce)
      .build()

    sendHedvigNotification(
      context = context,
      notificationSender = "ChatNotificationSender",
      notificationId = CHAT_NOTIFICATION_ID,
      notification = notification,
    )
  }

  private val hedvigPerson: Person = Person.Builder()
    .setName(context.getString(R.string.NOTIFICATION_CHAT_TITLE))
    .setImportant(true)
    .setKey(HEDVIG_PERSON_KEY)
    .setIcon(IconCompat.createWithResource(context, R.drawable.ic_hedvig_h))
    .build()

  private val youPerson: Person = Person.Builder()
    .setName(context.getString(R.string.notifications_chat_you))
    .setImportant(true)
    .setKey(YOU_PERSON_KEY)
    .build()

  companion object {
    private const val CHAT_CHANNEL_ID = "hedvig-chat"
    private const val CHAT_NOTIFICATION_ID = 1
    private const val HEDVIG_PERSON_KEY = "HEDVIG"
    private const val YOU_PERSON_KEY = "YOU"

    private const val NOTIFICATION_TYPE_NEW_MESSAGE = "NEW_MESSAGE"

    private fun Map<String, String>.titleFromCustomerIoData(): String? {
      // From customerIO https://www.customer.io/docs/send-push/#standard-payload
      return get("title")
    }

    private fun Map<String, String>.conversationIdFromCustomerIoData(): String? {
      return get("conversationId")
    }
  }
}
