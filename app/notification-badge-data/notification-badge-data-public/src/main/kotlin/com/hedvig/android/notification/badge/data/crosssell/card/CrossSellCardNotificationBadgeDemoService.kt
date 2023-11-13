package com.hedvig.android.notification.badge.data.crosssell.card

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class DemoCrossSellCardNotificationBadgeService() : CrossSellCardNotificationBadgeService {
  var showNotification = true

  override fun showNotification(): Flow<Boolean> {
    return flowOf(showNotification)
  }

  override suspend fun markAsSeen() {
    showNotification = false
  }
}
