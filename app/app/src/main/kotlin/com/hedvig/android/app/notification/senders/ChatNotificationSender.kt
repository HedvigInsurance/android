package com.hedvig.android.app.notification.senders

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.MessagingStyle
import androidx.core.app.Person
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavDestination
import com.benasher44.uuid.Uuid
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.getMutablePendingIntentFlags
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.chat.navigation.ChatDestinations
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailDestinations
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.typedHasRoute
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R
import kotlinx.coroutines.flow.first

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
  AppDestination.Chat::class,
  HomeDestination.Home::class,
  ClaimDetailDestinations.ClaimOverviewDestination::class,
)

class ChatNotificationSender(
  private val context: Context,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  private val featureManager: FeatureManager,
  private val buildConstants: HedvigBuildConstants,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
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
      context = context,
      style = messagingStyle,
      remoteMessage = remoteMessage,
      isCbmEnabled = featureManager.isFeatureEnabled(Feature.ENABLE_CBM).first(),
    )
  }

  override fun handlesNotificationType(notificationType: String) = notificationType == NOTIFICATION_TYPE_NEW_MESSAGE

  private fun defaultMessagingStyle(message: NotificationCompat.MessagingStyle.Message): MessagingStyle =
    NotificationCompat.MessagingStyle(youPerson).addMessage(message)

  private fun sendChatNotificationInner(
    context: Context,
    style: MessagingStyle,
    remoteMessage: RemoteMessage,
    isCbmEnabled: Boolean,
  ) {
    val intentUri = Uri.parse(
      if (isCbmEnabled) {
        val conversationId = remoteMessage.data.conversationIdFromCustomerIoData()
        val isValidUuid = conversationId.isValidUuid()
        if (conversationId != null && isValidUuid) {
          hedvigDeepLinkContainer.conversation.replace("{conversationId}", conversationId)
        } else {
          hedvigDeepLinkContainer.inbox
        }
      } else {
        hedvigDeepLinkContainer.chat
      },
    )
    logcat { "ChatNotificationSender sending notification with isCbmEnabled:$isCbmEnabled to uri:$intentUri" }
    val chatIntent = Intent().apply {
      action = Intent.ACTION_VIEW
      data = intentUri
      component = ComponentName(buildConstants.appId, MainActivityFullyQualifiedName)
    }

    val chatPendingIntent: PendingIntent? = PendingIntent.getActivity(
      context,
      0,
      chatIntent,
      getMutablePendingIntentFlags(),
    )

    val notification = NotificationCompat
      .Builder(context, notificationChannel.channelId)
      .setSmallIcon(R.drawable.ic_hedvig_h)
      .setStyle(style)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setAutoCancel(true)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
      .setContentIntent(chatPendingIntent)
      .build()

    sendHedvigNotification(
      context = context,
      notificationSender = "ChatNotificationSender",
      notificationId = CHAT_NOTIFICATION_ID,
      notification = notification,
      notificationChannel = notificationChannel,
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

  private fun String?.isValidUuid(): Boolean {
    if (this == null) {
      return false
    }
    return try {
      Uuid.fromString(this)
      true
    } catch (e: IllegalArgumentException) {
      logcat(ERROR) {
        "ChatNotificationSender got a conersationId which was not a UUID: $this"
      }
      false
    }
  }

  companion object {
    private const val MainActivityFullyQualifiedName = "com.hedvig.android.app.MainActivity"

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
