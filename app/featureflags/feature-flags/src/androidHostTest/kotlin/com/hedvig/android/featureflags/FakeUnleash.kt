package com.hedvig.android.featureflags

import io.getunleash.android.Unleash
import io.getunleash.android.cache.ToggleCache
import io.getunleash.android.data.Toggle
import io.getunleash.android.data.UnleashContext
import io.getunleash.android.data.UnleashState
import io.getunleash.android.data.Variant
import io.getunleash.android.disabledVariant
import io.getunleash.android.events.UnleashFetcherHeartbeatListener
import io.getunleash.android.events.UnleashListener
import java.io.File

/**
 * Stands in for the Unleash SDK, reading toggles from the same [ToggleCache] the real SDK is given. A fetch is
 * modelled by [deliver], which writes the state the frontend API would return for a context into that cache,
 * the way the SDK does after a successful fetch.
 */
internal class FakeUnleash(private val cache: ToggleCache) : Unleash {
  val contexts = mutableListOf<UnleashContext>()

  /** The context in effect when [start] was called, which is the one the first fetch carries. */
  var contextAtStart: UnleashContext? = null
    private set

  var started = false
    private set

  /** The toggles the cache held when [start] was called. */
  var togglesAtStart: Map<String, Toggle>? = null
    private set

  private val listeners = mutableListOf<UnleashListener>()

  fun deliver(context: UnleashContext, vararg enabledToggles: Toggle) {
    cache.write(UnleashState(context, enabledToggles.associateBy { it.name }))
  }

  /** A fetch answering 304: the SDK writes nothing to the cache and only reports the check. */
  fun deliverNotModified() {
    listeners.filterIsInstance<UnleashFetcherHeartbeatListener>().forEach { it.togglesChecked() }
  }

  override fun isEnabled(toggleName: String): Boolean = cache.get(toggleName)?.enabled ?: false

  @Deprecated("Use isEnabled(toggleName: String) instead.", ReplaceWith("isEnabled(toggleName)"))
  override fun isEnabled(toggleName: String, defaultValue: Boolean): Boolean = isEnabled(toggleName)

  override fun getVariant(toggleName: String): Variant {
    val toggle = cache.get(toggleName)
    return if (toggle?.enabled == true) toggle.variant else disabledVariant
  }

  @Deprecated("Use getVariant(toggleName: String) instead.", ReplaceWith("getVariant(toggleName)"))
  override fun getVariant(toggleName: String, defaultValue: Variant): Variant = getVariant(toggleName)

  override fun isReady(): Boolean = error("The client must not rely on the SDK's readiness")

  override fun setContextAsync(context: UnleashContext) {
    contexts.add(context)
  }

  override fun setContext(context: UnleashContext) = setContextAsync(context)

  override fun setContextWithTimeout(context: UnleashContext, timeout: Long) = setContextAsync(context)

  override fun addUnleashEventListener(listener: UnleashListener) {
    listeners.add(listener)
  }

  override fun removeUnleashEventListener(listener: UnleashListener) {
    listeners.remove(listener)
  }

  override fun start(eventListeners: List<UnleashListener>, bootstrapFile: File?, bootstrap: List<Toggle>) {
    started = true
    contextAtStart = contexts.lastOrNull()
    togglesAtStart = cache.read()
  }

  override fun refreshTogglesNow() = Unit

  override fun refreshTogglesNowAsync() = Unit

  override fun sendMetricsNow() = Unit

  override fun sendMetricsNowAsync() = Unit

  override fun close() = Unit
}
