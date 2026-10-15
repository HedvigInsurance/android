package com.hedvig.android.featureflags

import io.getunleash.android.cache.ToggleCache
import io.getunleash.android.data.Toggle
import io.getunleash.android.data.UnleashState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The SDK's toggle cache, kept as a whole [UnleashState] so the context each toggle set was fetched for
 * stays readable next to the toggles. Every state the SDK writes is also saved to [store].
 */
internal class ToggleStateCache(private val store: ToggleStateStore) : ToggleCache {
  private val mutableState = MutableStateFlow<UnleashState?>(null)

  /** The last fetched or restored state, or null while there is neither. */
  val state: StateFlow<UnleashState?> = mutableState.asStateFlow()

  override fun read(): Map<String, Toggle> = mutableState.value?.toggles.orEmpty()

  override fun get(key: String): Toggle? = mutableState.value?.toggles?.get(key)

  override fun write(state: UnleashState) {
    mutableState.value = state
    store.save(state)
  }

  /** Loads the state a previous process saved. */
  suspend fun restore() {
    store.load()?.let { mutableState.value = it }
  }
}
