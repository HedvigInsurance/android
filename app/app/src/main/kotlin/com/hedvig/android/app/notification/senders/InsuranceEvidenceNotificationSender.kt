package com.hedvig.android.app.notification.senders

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.PendingIntentCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.intentForNotification
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import hedvig.resources.R.drawable.ic_hedvig_h

class InsuranceEvidenceNotificationSender(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val intentUri = hedvigDeepLinkContainer.insuranceEvidence.first().toUri()
    logcat { "InsuranceEvidenceNotificationSender sending notification with deeplink uri:$intentUri" }
    val intent: PendingIntent? = PendingIntentCompat.getActivity(
      context,
      0,
      buildConstants.intentForNotification(intentUri),
      FLAG_UPDATE_CURRENT,
      true,
    )
    val title = remoteMessage.titleFromCustomerIoData()
    val body = remoteMessage.bodyFromCustomerIoData()
    val notification = NotificationCompat
      .Builder(context, notificationChannel.channelId)
      .setSmallIcon(ic_hedvig_h)
      .setContentTitle(title)
      .setContentText(body)
      .setAutoCancel(true)
      .setVisibility(VISIBILITY_PRIVATE)
      .setContentIntent(intent)
      .build()
    sendHedvigNotification(
      context = context,
      notificationId = INSURANCE_EVIDENCE_NOTIFICATION_ID,
      notification = notification,
      notificationChannel = notificationChannel,
      notificationSenderName = "InsuranceEvidenceNotificationSender",
    )
  }

  override fun handlesNotificationType(notificationType: String): Boolean {
    return notificationType == NOTIFICATION_TYPE_INSURANCE_EVIDENCE
  }

  companion object {
    private const val INSURANCE_EVIDENCE_NOTIFICATION_ID = 91760
    private const val NOTIFICATION_TYPE_INSURANCE_EVIDENCE = "INSURANCE_EVIDENCE"
  }
}
