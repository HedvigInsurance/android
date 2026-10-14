package com.hedvig.android.featureflags

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.getunleash.android.cache.ToggleCache
import io.getunleash.android.data.Payload
import io.getunleash.android.data.Toggle
import io.getunleash.android.data.UnleashContext
import io.getunleash.android.data.UnleashState
import io.getunleash.android.data.Variant
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Holds the toggle state Unleash last fetched, together with the [UnleashContext] it was evaluated for,
 * and keeps a copy on disk so the next process starts from it even while offline. Every write is
 * persisted, whatever its context.
 */
internal class PersistentToggleCache(
  private val file: File,
) : ToggleCache {
  private val state = MutableStateFlow<UnleashState?>(null)

  /**
   * The state last fetched from Unleash, either in this process or restored by [restore], or null if
   * none is known yet. The toggles are only meaningful for the [UnleashState.context] they came with.
   */
  val lastFetchedState: StateFlow<UnleashState?> = state.asStateFlow()

  override fun read(): Map<String, Toggle> = state.value?.toggles.orEmpty()

  override fun get(key: String): Toggle? = state.value?.toggles?.get(key)

  @Synchronized
  override fun write(state: UnleashState) {
    if (this.state.value == state) return
    this.state.value = state
    persist(state)
  }

  /**
   * Loads the state written by a previous process, unless this process has already received one, which
   * is necessarily newer.
   */
  fun restore() {
    val restored = load() ?: return
    state.compareAndSet(null, restored)
  }

  private fun persist(state: UnleashState) {
    try {
      file.parentFile?.mkdirs()
      val temporaryFile = File(file.parentFile, "${file.name}.tmp")
      temporaryFile.writeText(json.encodeToString(PersistedUnleashState.serializer(), state.toPersisted()))
      if (!temporaryFile.renameTo(file)) {
        logcat(LogPriority.WARN) { "Unleash: could not move the persisted toggle state into place" }
      }
    } catch (e: Exception) {
      logcat(LogPriority.WARN, e) { "Unleash: failed to persist toggle state" }
    }
  }

  private fun load(): UnleashState? {
    if (!file.exists()) return null
    return try {
      json.decodeFromString(PersistedUnleashState.serializer(), file.readText()).toUnleashState()
    } catch (e: Exception) {
      logcat(LogPriority.WARN, e) { "Unleash: failed to read persisted toggle state" }
      null
    }
  }
}

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class PersistedUnleashState(
  val context: PersistedContext,
  val toggles: List<PersistedToggle>,
)

@Serializable
private data class PersistedContext(
  val userId: String?,
  val sessionId: String?,
  val remoteAddress: String?,
  val properties: Map<String, String>,
)

@Serializable
private data class PersistedToggle(
  val name: String,
  val enabled: Boolean,
  val impressionData: Boolean,
  val variant: PersistedVariant,
)

@Serializable
private data class PersistedVariant(
  val name: String,
  val enabled: Boolean,
  val featureEnabled: Boolean,
  val payload: PersistedPayload?,
)

@Serializable
private data class PersistedPayload(
  val type: String,
  val value: String,
)

private fun UnleashState.toPersisted() = PersistedUnleashState(
  context = PersistedContext(
    userId = context.userId,
    sessionId = context.sessionId,
    remoteAddress = context.remoteAddress,
    properties = context.properties,
  ),
  toggles = toggles.values.map { toggle ->
    PersistedToggle(
      name = toggle.name,
      enabled = toggle.enabled,
      impressionData = toggle.impressionData,
      variant = PersistedVariant(
        name = toggle.variant.name,
        enabled = toggle.variant.enabled,
        featureEnabled = toggle.variant.featureEnabled,
        payload = toggle.variant.payload?.let { PersistedPayload(type = it.type, value = it.value) },
      ),
    )
  },
)

private fun PersistedUnleashState.toUnleashState() = UnleashState(
  context = UnleashContext(
    userId = context.userId,
    sessionId = context.sessionId,
    remoteAddress = context.remoteAddress,
    properties = context.properties,
  ),
  toggles = toggles.associate { toggle ->
    toggle.name to Toggle(
      name = toggle.name,
      enabled = toggle.enabled,
      impressionData = toggle.impressionData,
      variant = Variant(
        name = toggle.variant.name,
        enabled = toggle.variant.enabled,
        featureEnabled = toggle.variant.featureEnabled,
        payload = toggle.variant.payload?.let { Payload(type = it.type, value = it.value) },
      ),
    )
  },
)
