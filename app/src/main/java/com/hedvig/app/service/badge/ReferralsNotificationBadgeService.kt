package com.hedvig.app.service.badge

import com.hedvig.app.service.RemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ReferralsNotificationBadgeService(
    private val notificationBadgeService: NotificationBadgeService,
    private val remoteConfig: RemoteConfig,
) {
    fun shouldShowNotification(): Flow<Boolean> {
        return flow {
            val shouldShowCampaign = remoteConfig.fetch().campaignVisible
            emitAll(
                notificationBadgeService.getValue(NotificationBadge.BottomNav.ReferralCampaign)
                    .map { it ?: false }
                    .map { hasSeenCampaign -> !hasSeenCampaign && shouldShowCampaign }
            )
        }
    }

    suspend fun markAsSeen() {
        notificationBadgeService.setValue(NotificationBadge.BottomNav.ReferralCampaign, true)
    }
}
