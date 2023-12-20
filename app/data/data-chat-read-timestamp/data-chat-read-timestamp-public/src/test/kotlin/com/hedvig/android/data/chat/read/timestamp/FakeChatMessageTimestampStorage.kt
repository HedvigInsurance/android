package com.hedvig.android.data.chat.read.timestamp

import kotlinx.datetime.Instant

class FakeChatMessageTimestampStorage : ChatMessageTimestampStorage {
  private var latestReadTimestamp: Instant? = null

  override suspend fun getLatestReadTimestamp(): Instant? {
    return latestReadTimestamp
  }

  override suspend fun setLatestReadTimestamp(timestamp: Instant) {
    latestReadTimestamp = timestamp
  }

  override suspend fun clearLatestReadTimestamp() {
    latestReadTimestamp = null
  }
}
