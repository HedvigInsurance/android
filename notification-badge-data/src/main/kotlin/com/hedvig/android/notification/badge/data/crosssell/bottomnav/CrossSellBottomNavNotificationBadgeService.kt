package com.hedvig.android.notification.badge.data.crosssell.bottomnav

import com.hedvig.android.notification.badge.data.crosssell.CrossSellBadgeType
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import kotlinx.coroutines.flow.Flow

internal class CrossSellBottomNavNotificationBadgeService(
  private val crossSellNotificationBadgeService: CrossSellNotificationBadgeService,
) {
  private val crossSellBottomNavBadgeType = CrossSellBadgeType.BottomNav

  fun showNotification(): Flow<Boolean> {
    return crossSellNotificationBadgeService.showNotification(crossSellBottomNavBadgeType)
  }

  suspend fun markAsSeen() {
    crossSellNotificationBadgeService.markCurrentCrossSellsAsSeen(crossSellBottomNavBadgeType)
  }
}
