package com.hedvig.app.service.badge

import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ReferralsNotificationBadgeService(
    private val notificationBadgeService: NotificationBadgeService,
    private val featureManager: FeatureManager,
) {
    fun shouldShowNotification(): Flow<Boolean> {
        return flow {
            emitAll(
                notificationBadgeService.getValue(NotificationBadge.BottomNav.ReferralCampaign)
                    .map { it ?: false }
                    .map { hasSeenCampaign ->
                        !hasSeenCampaign && featureManager.isFeatureEnabled(Feature.REFERRAL_CAMPAIGN)
                    }
            )
        }
    }

    suspend fun markAsSeen() {
        notificationBadgeService.setValue(NotificationBadge.BottomNav.ReferralCampaign, true)
    }
}
