package com.hedvig.android.data.chat.read.timestamp

import app.cash.turbine.Turbine
import kotlinx.datetime.Instant

class FakeChatLastMessageReadRepository : ChatLastMessageReadRepository {
  val isNewestMessageNewerThanLastReadTimestamp = Turbine<Boolean>()

  override suspend fun storeLatestReadTimestamp(timestamp: Instant) {
    error("Not implemented")
  }

  override suspend fun isNewestMessageNewerThanLastReadTimestamp(): Boolean {
    return isNewestMessageNewerThanLastReadTimestamp.awaitItem()
  }
}
