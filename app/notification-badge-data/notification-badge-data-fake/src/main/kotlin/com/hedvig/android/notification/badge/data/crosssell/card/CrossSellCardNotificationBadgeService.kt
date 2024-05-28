package com.hedvig.android.notification.badge.data.crosssell.card

import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class FakeCrossSellCardNotificationBadgeService : CrossSellCardNotificationBadgeService {
  val showNotification = Turbine<Boolean>()

  override fun showNotification(): Flow<Boolean> {
    return showNotification.asChannel().receiveAsFlow()
  }

  override suspend fun markAsSeen() {
    showNotification.add(false)
  }
}
