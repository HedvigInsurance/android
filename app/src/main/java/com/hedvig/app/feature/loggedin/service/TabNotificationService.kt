package com.hedvig.app.feature.loggedin.service

import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.service.badge.NotificationBadge
import com.hedvig.app.service.badge.NotificationBadgeService
import com.hedvig.app.service.badge.Seen
import com.hedvig.app.service.badge.isSeen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TabNotificationService(
    private val getCrossSellsUseCase: GetCrossSellsUseCase,
    private val notificationBadgeService: NotificationBadgeService,
) {

    suspend fun unseenTabNotifications(): Flow<Set<LoggedInTabs>> {
        val potentialCrossSells = getCrossSellsUseCase.invoke()
        val tabNotifications = NotificationBadge.fromPotentialCrossSells(potentialCrossSells)

        return notificationBadgeService.seenStatus(tabNotifications)
            .map { notificationBadgeToSeenPairs ->
                notificationBadgeToSeenPairs
                    .filter { (_, seen) ->
                        seen.isSeen().not()
                    }
                    .mapNotNull { (notificationBadge, _) ->
                        notificationToTabMap[notificationBadge]
                    }
                    .toSet()
            }
    }

    suspend fun visitTab(tab: LoggedInTabs) {
        val associatedNotification = tabToNotificationMap[tab] ?: return
        notificationBadgeService.setSeenStatus(associatedNotification, Seen.seen())
    }

    companion object {
        val notificationToTabMap = mapOf<NotificationBadge.BottomNav, LoggedInTabs>(
            NotificationBadge.BottomNav.CrossSellOnInsuranceFragment to LoggedInTabs.INSURANCE
        )

        val tabToNotificationMap = notificationToTabMap.map {
            it.value to it.key
        }.toMap()
    }
}
