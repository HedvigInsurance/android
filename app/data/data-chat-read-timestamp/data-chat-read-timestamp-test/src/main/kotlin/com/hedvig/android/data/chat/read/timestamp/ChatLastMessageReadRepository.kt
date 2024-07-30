package com.hedvig.android.data.chat.read.timestamp

import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.Instant

class FakeChatLastMessageReadRepository : ChatLastMessageReadRepository {
  val isNewestMessageNewerThanLastReadTimestamp = Turbine<Boolean>()

  override suspend fun storeLatestReadTimestamp(timestamp: Instant) {
    error("Not implemented")
  }

  override fun isNewestMessageNewerThanLastReadTimestamp(): Flow<Boolean> {
    return isNewestMessageNewerThanLastReadTimestamp.asChannel().receiveAsFlow()
  }
}
