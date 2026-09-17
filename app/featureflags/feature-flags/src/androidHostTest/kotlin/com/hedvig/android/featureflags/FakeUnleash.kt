package com.hedvig.android.featureflags

import io.getunleash.android.Unleash
import io.getunleash.android.data.Toggle
import io.getunleash.android.data.UnleashContext
import io.getunleash.android.data.Variant
import io.getunleash.android.disabledVariant
import io.getunleash.android.events.UnleashListener
import io.getunleash.android.events.UnleashReadyListener
import io.getunleash.android.events.UnleashStateListener
import java.io.File

/**
 * Stands in for the Unleash SDK, modelling the two pieces of its behaviour the client depends on:
 * a toggle set that only holds the toggles a fetch actually returned, and a readiness flag that the
 * SDK raises off the first non-empty set regardless of which context produced it.
 */
internal class FakeUnleash : Unleash {
  val contexts = mutableListOf<UnleashContext>()
  var started = false
    private set

  private var toggles: Map<String, Boolean> = emptyMap()
  private var ready = false
  private val listeners = mutableListOf<UnleashListener>()

  /**
   * Delivers the toggle set a fetch returned. Only enabled toggles reach the client in the frontend
   * API's response, so a flag left out of [enabledToggles] is one the backend did not send.
   */
  fun completeFetch(enabledToggles: Set<String>) {
    toggles = enabledToggles.associateWith { true }
    val becameReady = !ready && enabledToggles.isNotEmpty()
    ready = ready || enabledToggles.isNotEmpty()
    listeners.filterIsInstance<UnleashStateListener>().forEach { it.onStateChanged() }
    if (becameReady) {
      listeners.filterIsInstance<UnleashReadyListener>().forEach { it.onReady() }
    }
  }

  override fun isEnabled(toggleName: String): Boolean = toggles[toggleName] ?: false

  @Deprecated("Use isEnabled(toggleName: String) instead.", ReplaceWith("isEnabled(toggleName)"))
  override fun isEnabled(toggleName: String, defaultValue: Boolean): Boolean = toggles[toggleName] ?: defaultValue

  override fun isReady(): Boolean = ready

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
    eventListeners.forEach(::addUnleashEventListener)
  }

  override fun getVariant(toggleName: String): Variant = disabledVariant

  @Deprecated("Use getVariant(toggleName: String) instead.", ReplaceWith("getVariant(toggleName)"))
  override fun getVariant(toggleName: String, defaultValue: Variant): Variant = defaultValue

  override fun refreshTogglesNow() = Unit

  override fun refreshTogglesNowAsync() = Unit

  override fun sendMetricsNow() = Unit

  override fun sendMetricsNowAsync() = Unit

  override fun close() = Unit
}
