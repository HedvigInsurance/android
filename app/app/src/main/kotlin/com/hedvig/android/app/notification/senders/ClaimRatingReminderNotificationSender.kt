package com.hedvig.android.app.notification.senders

import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.PendingIntentCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.android.app.notification.intentForNotification
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.notification.core.HedvigNotificationChannel
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.core.sendHedvigNotification
import com.hedvig.android.permission.PermissionManager
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import hedvig.resources.R

/** Opens the claim with the rating sheet up, for members who have not rated a claim closed a few days ago. */
@ContributesIntoSet(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class ClaimRatingReminderNotificationSender(
  private val context: Context,
  private val permissionManager: PermissionManager,
  private val buildConstants: HedvigBuildConstants,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  private val languageService: LanguageService,
) : NotificationSender {
  private val notificationChannel = HedvigNotificationChannel.Other

  override suspend fun sendNotification(type: String, remoteMessage: RemoteMessage) {
    val claimId = remoteMessage.data["claimId"]
    val intentUri = if (claimId != null) {
      hedvigDeepLinkContainer.claimDetails
        .first { it.contains("{openRating}") }
        .replace("{claimId}", claimId)
        .replace("{openRating}", "true")
    } else {
      logcat(LogPriority.WARN) { "ClaimRatingReminderNotificationSender: claimId from remoteMessage is null" }
      hedvigDeepLinkContainer.home.first()
    }.toUri()
    val pendingIntent = PendingIntentCompat.getActivity(
      context,
      0,
      buildConstants.intentForNotification(intentUri),
      FLAG_UPDATE_CURRENT,
      true,
    )
    val notification = NotificationCompat
      .Builder(context, notificationChannel.channelId)
      .setSmallIcon(R.drawable.ic_hedvig_h)
      .setContentTitle(remoteMessage.titleFromCustomerIoData())
      .setContentText(remoteMessage.bodyFromCustomerIoData())
      .setPriority(PRIORITY_MAX)
      .setAutoCancel(true)
      .setVisibility(VISIBILITY_PRIVATE)
      .setContentIntent(pendingIntent)
      .build()
    sendHedvigNotification(
      context = context,
      permissionManager = permissionManager,
      notificationId = CLAIM_RATING_REMINDER_NOTIFICATION_ID,
      notification = notification,
      notificationChannel = notificationChannel,
      notificationSenderName = "ClaimRatingReminderNotificationSender",
      language = languageService.getLanguage(),
    )
  }

  override fun handlesNotificationType(notificationType: String): Boolean {
    return notificationType == NOTIFICATION_TYPE_CLAIM_RATING_REMINDER
  }

  companion object {
    private const val CLAIM_RATING_REMINDER_NOTIFICATION_ID = 9865

    private const val NOTIFICATION_TYPE_CLAIM_RATING_REMINDER = "CLAIM_RATING_REMINDER"
  }
}
