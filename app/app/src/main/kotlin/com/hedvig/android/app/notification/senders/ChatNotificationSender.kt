package com.hedvig.android.app.notification.senders

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_MESSAGE
import androidx.core.app.NotificationCompat.MessagingStyle
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.PendingIntentCompat
import androidx.core.app.Person
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavDestination
import com.benasher44.uuid.Uuid
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.intentForNotification
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.chat.navigation.ChatDestinations
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailDestination
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.typedHasRoute
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R
import hedvig.resources.R.drawable.ic_hedvig_h

/**
 * An in-memory storage of the current route, used to *not* show the chat notification if we are in a select list of
 * screens where we do not want to show the system notification, but we want to let the in-app screen indicate that
 * there is a new message.
 * This is not persistent storage, and will just be wiped in scenarios like the process being killed, but this is part
 * of what we want, since we only care to do this if the app is resumed anyway. On top of this, we'd rather experience
 * cases where we show the notification when we shouldn't rather than cases where we do not show the notification even
 * thought we should.
 */
object CurrentDestinationInMemoryStorage {
  var currentDestination: NavDestination? = null
}

private val listOfDestinationsWhichShouldNotShowChatNotification = setOf(
  ChatDestinations.Chat::class,
  ChatDestinations.Inbox::class,
  HomeDestination.Home::class,
  ClaimDetailDestination.ClaimOverviewDestination::class,
)

class ChatNotificationSender(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  override fun handlesNotificationType(notificationType: String) = notificationType == NOTIFICATION_TYPE_NEW_MESSAGE

  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val isAppForegrounded = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    val currentDestination = CurrentDestinationInMemoryStorage.currentDestination
    val currentlyOnDestinationWhichForbidsShowingChatNotification =
      listOfDestinationsWhichShouldNotShowChatNotification.any { clazz ->
        currentDestination?.typedHasRoute(clazz) == true
      }
    if (currentlyOnDestinationWhichForbidsShowingChatNotification && isAppForegrounded) {
      logcat { "ChatNotificationSender ignoring notification since we are showing destination $currentDestination" }
      return
    }

    val conversationId = remoteMessage.data.conversationIdFromCustomerIoData()
    val isValidUuid = conversationId.isValidUuid()
    val intentUri = if (conversationId != null && isValidUuid) {
      hedvigDeepLinkContainer.conversation.first().replace("{conversationId}", conversationId)
    } else {
      hedvigDeepLinkContainer.inbox.first()
    }.toUri()
    logcat { "ChatNotificationSender sending notification with deeplink uri:$intentUri" }
    val chatPendingIntent: PendingIntent? = PendingIntentCompat.getActivity(
      context,
      0,
      buildConstants.intentForNotification(intentUri),
      FLAG_UPDATE_CURRENT,
      true,
    )
    val messagingStyle = createMessagingStyle(remoteMessage)
    val notification = NotificationCompat
      .Builder(context, notificationChannel.channelId)
      .setSmallIcon(ic_hedvig_h)
      .setStyle(messagingStyle)
      .setPriority(PRIORITY_MAX)
      .setAutoCancel(true)
      .setCategory(CATEGORY_MESSAGE)
      .setVisibility(VISIBILITY_PRIVATE)
      .setContentIntent(chatPendingIntent)
      .build()
    sendHedvigNotification(
      context = context,
      notificationId = CHAT_NOTIFICATION_ID,
      notification = notification,
      notificationChannel = notificationChannel,
      notificationSenderName = "ChatNotificationSender",
    )
  }

  private fun createMessagingStyle(remoteMessage: RemoteMessage): MessagingStyle {
    val hedvigPerson = hedvigPerson.toBuilder()
      .also { person ->
        val overriddenTitle = remoteMessage.data.titleFromCustomerIoData()
        if (overriddenTitle != null) {
          person.setName(overriddenTitle)
        }
      }
      .build()

    val messageText = context.getString(R.string.NOTIFICATION_CHAT_NEW_MESSAGE_BODY)

    val message = MessagingStyle.Message(
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
          MessagingStyle
            .extractMessagingStyleFromNotification(existingNotification)
            ?.addMessage(message)
        } ?: defaultMessagingStyle(message)
    } else {
      defaultMessagingStyle(message)
    }
    return messagingStyle
  }

  private fun defaultMessagingStyle(message: NotificationCompat.MessagingStyle.Message): MessagingStyle =
    NotificationCompat.MessagingStyle(youPerson).addMessage(message)

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

  private fun String?.isValidUuid(): Boolean {
    if (this == null) {
      return false
    }
    return try {
      Uuid.fromString(this)
      true
    } catch (e: IllegalArgumentException) {
      logcat(ERROR, e) {
        "ChatNotificationSender got a conersationId which was not a UUID: $this"
      }
      false
    }
  }

  companion object {
    private const val CHAT_NOTIFICATION_ID = 1
    private const val HEDVIG_PERSON_KEY = "HEDVIG"
    private const val YOU_PERSON_KEY = "YOU"

    private const val NOTIFICATION_TYPE_NEW_MESSAGE = "NEW_MESSAGE"

    // From customerIO https://www.customer.io/docs/send-push/#standard-payload part of our custom payload there
    private fun Map<String, String>.conversationIdFromCustomerIoData(): String? {
      return get("conversationId")
    }
  }
}
