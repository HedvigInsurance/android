package com.hedvig.app.feature.loggedin.service

import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.service.badge.CrossSellNotificationBadgeService
import com.hedvig.app.service.badge.ReferralsNotificationBadgeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TabNotificationService(
    private val crossSellNotificationBadgeService: CrossSellNotificationBadgeService,
    private val referralsNotificationBadgeService: ReferralsNotificationBadgeService,
) {

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun unseenTabNotifications(): Flow<Set<LoggedInTabs>> {
        return combine(
            crossSellNotificationBadgeService.shouldShowTabNotification(),
            referralsNotificationBadgeService.shouldShowNotification()
        ) { shouldShowCrossSellNotification: Boolean,
            shouldShowReferralNotification: Boolean ->

            buildSet {
                if (shouldShowCrossSellNotification) add(LoggedInTabs.INSURANCE)
                if (shouldShowReferralNotification) add(LoggedInTabs.REFERRALS)
            }
        }
    }

    suspend fun visitTab(tab: LoggedInTabs) {
        when (tab) {
            LoggedInTabs.INSURANCE -> {
                crossSellNotificationBadgeService.markCurrentCrossSellsAsSeen(
                    CrossSellNotificationBadgeService.CrossSellBadgeType.BottomNav
                )
            }
            LoggedInTabs.REFERRALS -> {
                referralsNotificationBadgeService.markAsSeen()
            }
            LoggedInTabs.KEY_GEAR -> {}
            LoggedInTabs.HOME -> {}
            LoggedInTabs.PROFILE -> {}
        }
    }
}
