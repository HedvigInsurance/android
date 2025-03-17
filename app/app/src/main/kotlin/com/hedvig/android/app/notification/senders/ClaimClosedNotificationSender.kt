package com.hedvig.android.app.notification.senders

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_EVENT
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.PendingIntentCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.DATA_MESSAGE_BODY
import com.hedvig.android.app.notification.DATA_MESSAGE_TITLE
import com.hedvig.android.app.notification.intentForNotification
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R.drawable.ic_hedvig_h

class ClaimClosedNotificationSender(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val claimId = remoteMessage.data.claimIdFromData()
    val intentUri = if (claimId != null) {
      hedvigDeepLinkContainer.claimDetails.first().replace("{claimId}", claimId)
    } else {
      hedvigDeepLinkContainer.home.first()
    }.toUri()
    logcat { "ChatNotificationSender sending notification with deeplink uri:$intentUri" }
    val claimClosedPendingIntent: PendingIntent? = PendingIntentCompat.getActivity(
      context,
      0,
      buildConstants.intentForNotification(intentUri),
      FLAG_UPDATE_CURRENT,
      true,
    )
    val title = remoteMessage.titleFromCustomerIoData() ?: remoteMessage.data[DATA_MESSAGE_TITLE]
    val body = remoteMessage.bodyFromCustomerIoData() ?: remoteMessage.data[DATA_MESSAGE_BODY]
    logcat { "Mariia: remoteMessage ${remoteMessage.data}" }
    val notification = NotificationCompat
      .Builder(context, notificationChannel.channelId)
      .setSmallIcon(ic_hedvig_h)
      .setContentTitle(title)
      .setContentText(body)
      .setPriority(PRIORITY_MAX)
      .setAutoCancel(true)
      .setCategory(CATEGORY_EVENT)
      .setVisibility(VISIBILITY_PRIVATE)
      .setContentIntent(claimClosedPendingIntent)
      .build()
    sendHedvigNotification(
      context = context,
      notificationId = CLAIM_CLOSED_NOTIFICATION_ID,
      notification = notification,
      notificationChannel = notificationChannel,
      notificationSenderName = "ClaimClosedNotificationSender",
    )
  }

  override fun handlesNotificationType(notificationType: String): Boolean {
    return notificationType == NOTIFICATION_TYPE_CLAIM_CLOSED
  }

  companion object {
    private const val CLAIM_CLOSED_NOTIFICATION_ID = 9864

    private const val NOTIFICATION_TYPE_CLAIM_CLOSED = "CLAIM_CLOSED"

    private fun Map<String, String>.claimIdFromData(): String? {
      return get("claimId")
    }
  }
}
