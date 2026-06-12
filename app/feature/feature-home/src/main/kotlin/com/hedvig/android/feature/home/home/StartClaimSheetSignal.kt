package com.hedvig.android.feature.home.home

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-scoped one-shot signal raised when the `submit-claim` deep link is opened, asking the Home
 * screen to auto-open the "start claim" bottom sheet.
 *
 * Modeled as a persistent flag (not an event stream) on purpose: the deep link may be routed while
 * logged out, long before Home composes after login. The flag holds until Home actually reads it and
 * flips it back via [consume], so the sheet opens exactly once regardless of that timing gap.
 */
@Inject
@SingleIn(AppScope::class)
class StartClaimSheetSignal {
  private val _pending = MutableStateFlow(false)
  val pending: StateFlow<Boolean> = _pending.asStateFlow()

  fun request() {
    _pending.value = true
  }

  fun consume() {
    _pending.value = false
  }
}
