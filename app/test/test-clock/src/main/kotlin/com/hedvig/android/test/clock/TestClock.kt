package com.hedvig.android.test.clock

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class TestClock : Clock {
  private var now = Clock.System.now()

  fun advanceTimeBy(duration: Duration) {
    require(duration > Duration.ZERO)
    now += duration
  }

  override fun now(): Instant {
    return now
  }
}
