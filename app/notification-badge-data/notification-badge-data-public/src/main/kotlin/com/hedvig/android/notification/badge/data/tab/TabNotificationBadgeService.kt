package com.hedvig.android.notification.badge.data.tab

import com.hedvig.android.notification.badge.data.crosssell.bottomnav.CrossSellBottomNavNotificationBadgeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TabNotificationBadgeService internal constructor(
  private val crossSellBottomNavNotificationBadgeService: CrossSellBottomNavNotificationBadgeService,
) {
  fun unseenTabNotificationBadges(): Flow<Set<BottomNavTab>> {
    return crossSellBottomNavNotificationBadgeService.showNotification().map { showCrossSellNotification: Boolean ->
      buildSet {
        if (showCrossSellNotification) add(BottomNavTab.INSURANCE)
      }
    }
  }

  suspend fun visitTab(tab: BottomNavTab) {
    when (tab) {
      BottomNavTab.INSURANCE -> {
        crossSellBottomNavNotificationBadgeService.markAsSeen()
      }

      else -> {}
    }
  }
}
