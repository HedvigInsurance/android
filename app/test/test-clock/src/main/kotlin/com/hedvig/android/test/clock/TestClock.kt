package com.hedvig.android.test.clock

import kotlin.time.Duration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

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
