package com.hedvig.android.notification.badge.data.crosssell.card

import com.hedvig.android.notification.badge.data.crosssell.CrossSellBadgeType
import com.hedvig.android.notification.badge.data.crosssell.CrossSellNotificationBadgeService
import kotlinx.coroutines.flow.Flow

interface CrossSellCardNotificationBadgeService {
  fun showNotification(): Flow<Boolean>

  suspend fun markAsSeen()
}

internal class CrossSellCardNotificationBadgeServiceImpl(
  private val crossSellNotificationBadgeService: CrossSellNotificationBadgeService,
) : CrossSellCardNotificationBadgeService {
  private val crossSellCardBadgeType = CrossSellBadgeType.InsuranceFragmentCard

  override fun showNotification(): Flow<Boolean> {
    return crossSellNotificationBadgeService.showNotification(crossSellCardBadgeType)
  }

  override suspend fun markAsSeen() {
    crossSellNotificationBadgeService.markCurrentCrossSellsAsSeen(crossSellCardBadgeType)
  }
}
