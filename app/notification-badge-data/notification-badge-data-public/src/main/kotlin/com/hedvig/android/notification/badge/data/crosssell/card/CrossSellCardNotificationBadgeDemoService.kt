package com.hedvig.android.notification.badge.data.crosssell.card

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class CrossSellCardNotificationBadgeDemoServiceImpl() : CrossSellCardNotificationBadgeService {
  override fun showNotification(): Flow<Boolean> {
    return flowOf(false)
  }

  override suspend fun markAsSeen() {}
}
