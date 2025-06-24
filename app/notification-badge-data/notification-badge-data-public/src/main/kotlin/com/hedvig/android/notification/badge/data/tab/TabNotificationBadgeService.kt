package com.hedvig.android.notification.badge.data.tab

import com.hedvig.android.notification.badge.data.referrals.ReferralsNotificationBadgeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TabNotificationBadgeService internal constructor(
  private val referralsNotificationBadgeService: ReferralsNotificationBadgeService,
) {
  fun unseenTabNotificationBadges(): Flow<Set<BottomNavTab>> {
    return referralsNotificationBadgeService.showNotification().map { showReferralNotification: Boolean ->
      buildSet {
        if (showReferralNotification) add(BottomNavTab.FOREVER)
      }
    }
  }

  suspend fun visitTab(tab: BottomNavTab) {
    when (tab) {
      BottomNavTab.FOREVER -> {
        referralsNotificationBadgeService.markAsSeen()
      }

      else -> {}
    }
  }
}
