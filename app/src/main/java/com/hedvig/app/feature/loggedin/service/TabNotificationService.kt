package com.hedvig.app.feature.loggedin.service

import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.service.badge.CrossSellNotificationBadgeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TabNotificationService(
    private val crossSellNotificationBadgeService: CrossSellNotificationBadgeService,
) {

    suspend fun unseenTabNotifications(): Flow<Set<LoggedInTabs>> {
        return crossSellNotificationBadgeService
            .getUnseenCrossSells(CrossSellNotificationBadgeService.CrossSellBadgeType.BottomNav)
            .map { contracts ->
                if (contracts.isNotEmpty()) {
                    setOf(LoggedInTabs.INSURANCE)
                } else {
                    emptySet()
                }
            }
    }

    suspend fun visitTab(tab: LoggedInTabs) {
        if (tab == LoggedInTabs.INSURANCE) {
            crossSellNotificationBadgeService.markCurrentCrossSellsAsSeen(
                CrossSellNotificationBadgeService.CrossSellBadgeType.BottomNav
            )
        }
    }
}
