package com.hedvig.android.notification.badge.data.tab

import com.hedvig.android.notification.badge.data.crosssell.bottomnav.CrossSellBottomNavNotificationBadgeService
import com.hedvig.android.notification.badge.data.referrals.ReferralsNotificationBadgeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TabNotificationBadgeService internal constructor(
  private val crossSellBottomNavNotificationBadgeService: CrossSellBottomNavNotificationBadgeService,
  private val referralsNotificationBadgeService: ReferralsNotificationBadgeService,
) {
  fun unseenTabNotificationBadges(): Flow<Set<BottomNavTab>> {
    return combine(
      crossSellBottomNavNotificationBadgeService.showNotification(),
      referralsNotificationBadgeService.showNotification(),
    ) { showCrossSellNotification: Boolean, showReferralNotification: Boolean ->
      buildSet {
        if (showCrossSellNotification) add(BottomNavTab.INSURANCE)
        if (showReferralNotification) add(BottomNavTab.FOREVER)
      }
    }
  }

  suspend fun visitTab(tab: BottomNavTab) {
    when (tab) {
      BottomNavTab.INSURANCE -> {
        crossSellBottomNavNotificationBadgeService.markAsSeen()
      }
      BottomNavTab.FOREVER -> {
        referralsNotificationBadgeService.markAsSeen()
      }
      else -> {}
    }
  }
}
