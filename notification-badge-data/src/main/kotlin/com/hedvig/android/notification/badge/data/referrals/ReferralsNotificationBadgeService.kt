package com.hedvig.android.notification.badge.data.referrals

import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ReferralsNotificationBadgeService(
  private val notificationBadgeStorage: NotificationBadgeStorage,
  private val featureManager: FeatureManager,
) {
  private val referralNotificationBadge = NotificationBadge.BottomNav.ReferralCampaign

  fun showNotification(): Flow<Boolean> {
    return notificationBadgeStorage.getValue(referralNotificationBadge)
      .map { it ?: false }
      .map { hasSeenCampaign ->
        !hasSeenCampaign && featureManager.isFeatureEnabled(Feature.REFERRAL_CAMPAIGN)
      }
  }

  suspend fun markAsSeen() {
    notificationBadgeStorage.setValue(referralNotificationBadge, true)
  }
}
