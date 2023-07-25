package com.hedvig.android.notification.badge.data.crosssell.card

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeCrossSellCardNotificationBadgeService(
  initialNotificationState: Boolean = true,
) : CrossSellCardNotificationBadgeService {
  val showNotification = MutableStateFlow<Boolean>(initialNotificationState)

  override fun showNotification(): Flow<Boolean> {
    return showNotification
  }

  override suspend fun markAsSeen() {
    showNotification.update { false }
  }
}
