package com.hedvig.android.notification.badge.data.crosssell.card

import com.hedvig.android.notification.badge.data.crosssell.CrossSellBadgeType
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import kotlinx.coroutines.flow.Flow

class CrossSellCardNotificationBadgeService internal constructor(
  private val crossSellNotificationBadgeService: CrossSellNotificationBadgeService,
) {
  private val crossSellCardBadgeType = CrossSellBadgeType.InsuranceFragmentCard

  fun showNotification(): Flow<Boolean> {
    return crossSellNotificationBadgeService.showNotification(crossSellCardBadgeType)
  }

  suspend fun markAsSeen() {
    crossSellNotificationBadgeService.markCurrentCrossSellsAsSeen(crossSellCardBadgeType)
  }
}
