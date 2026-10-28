package com.hedvig.android.featureflags

import io.getunleash.android.data.UnleashState

internal class FakeToggleStateStore(var stored: UnleashState? = null) : ToggleStateStore {
  override suspend fun load(): UnleashState? = stored

  override fun save(state: UnleashState) {
    stored = state
  }
}
