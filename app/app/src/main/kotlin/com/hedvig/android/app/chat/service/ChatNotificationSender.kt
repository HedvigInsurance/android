package com.hedvig.android.app.chat.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.core.common.android.notification.setupNotificationChannel
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import com.hedvig.app.service.push.getMutablePendingIntentFlags
import hedvig.resources.R
import kotlinx.datetime.Instant

class ChatNotificationSender(
  private val context: Context,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
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

    val messageText = remoteMessage.data.bodyFromCustomerIoData() ?: remoteMessage.data[DATA_NEW_MESSAGE_BODY]
    if (messageText == null) {
      logcat(LogPriority.ERROR) { "GCM message came without a valid message. Data:${remoteMessage.data}" }
      return
    }

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

    val replyPendingIntent = PendingIntentCompat.getBroadcast(
      context,
      CHAT_REPLY_REQUEST_CODE,
      Intent(context, ChatMessageNotificationReceiver::class.java).apply {
        putExtra(
          CHAT_REPLY_DATA_NOTIFICATION_ID,
          CHAT_NOTIFICATION_ID,
        )
      },
      PendingIntent.FLAG_UPDATE_CURRENT,
      true,
    )

    val replyAction = NotificationCompat.Action.Builder(
      android.R.drawable.ic_menu_send,
      context.getString(hedvig.resources.R.string.notifications_chat_reply_action),
      replyPendingIntent,
    )
      .addRemoteInput(
        RemoteInput.Builder(CHAT_REPLY_KEY)
          .setLabel(context.getString(R.string.notifications_chat_reply_action))
          .build(),
      )
      .build()

    val notification = NotificationCompat
      .Builder(
        context,
        CHAT_CHANNEL_ID,
      )
      .setSmallIcon(hedvig.resources.R.drawable.ic_hedvig_h)
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

    sendHedvigNotification(
      context = context,
      notificationSender = "ChatNotificationSender",
      notificationId = CHAT_NOTIFICATION_ID,
      notification = notification,
    )
  }

  @RequiresApi(Build.VERSION_CODES.N)
  fun addReplyToExistingChatNotification(
    context: Context,
    notificationId: Int,
    replyText: String,
    replyTimestamp: Instant,
  ) {
    val notificationManager = context.getSystemService<NotificationManager>() ?: return

    val existingChatNotification = notificationManager
      .activeNotifications
      .firstOrNull { it.id == notificationId }
      ?.notification ?: return

    val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
      existingChatNotification,
    ) ?: return
    style.addMessage(replyText, replyTimestamp.toEpochMilliseconds(), style.user)

    sendChatNotificationInner(
      context,
      style,
      alertOnlyOnce = true,
    )
  }

  private val hedvigPerson: Person = Person.Builder()
    .setName(context.getString(hedvig.resources.R.string.NOTIFICATION_CHAT_TITLE))
    .setImportant(true)
    .setKey(HEDVIG_PERSON_KEY)
    .setIcon(IconCompat.createWithResource(context, hedvig.resources.R.drawable.ic_hedvig_h))
    .build()

  private val youPerson: Person = Person.Builder()
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

    private fun Map<String, String>.bodyFromCustomerIoData(): String? {
      // From customerIO https://www.customer.io/docs/send-push/#standard-payload
      return get("body")
    }

    private fun Map<String, String>.titleFromCustomerIoData(): String? {
      // From customerIO https://www.customer.io/docs/send-push/#standard-payload
      return get("title")
    }
  }
}
