package com.hedvig.app.service.badge

import com.hedvig.app.service.RemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReferralsNotificationBadgeService(
    private val notificationBadgeService: NotificationBadgeService,
) {
    suspend fun shouldShowNotification(): Flow<Boolean> {
        val remoteConfig = RemoteConfig()
        val shouldShowCampaign = remoteConfig.fetch().campaignVisible
        return notificationBadgeService.getValue(NotificationBadge.BottomNav.ReferralCampaign)
            .map { it ?: false }
            .map { hasSeenCampaign -> !hasSeenCampaign && shouldShowCampaign }
    }

    suspend fun markAsSeen() {
        notificationBadgeService.setValue(NotificationBadge.BottomNav.ReferralCampaign, true)
    }
}
