package com.hedvig.android.featureflags

import io.getunleash.android.cache.ToggleCache
import io.getunleash.android.data.Toggle
import io.getunleash.android.data.UnleashState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The SDK's toggle cache, kept as a whole [UnleashState] so the context each toggle set was fetched for
 * stays readable next to the toggles. The SDK writes here on every fetched toggle set and on a backup
 * restore.
 */
internal class ToggleStateCache : ToggleCache {
  private val mutableState = MutableStateFlow<UnleashState?>(null)

  /** The last written state, or null while nothing has been fetched or restored in this process. */
  val state: StateFlow<UnleashState?> = mutableState.asStateFlow()

  override fun read(): Map<String, Toggle> = mutableState.value?.toggles.orEmpty()

  override fun get(key: String): Toggle? = mutableState.value?.toggles?.get(key)

  override fun write(state: UnleashState) {
    mutableState.value = state
  }
}
