package com.hedvig.android.notification.badge.data.crosssell.card

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


internal class CrossSellCardNotificationBadgeDemoServiceImpl(
) : CrossSellCardNotificationBadgeService {

  override fun showNotification(): Flow<Boolean> {
    return flow { false }
  }

  override suspend fun markAsSeen() {}
}
