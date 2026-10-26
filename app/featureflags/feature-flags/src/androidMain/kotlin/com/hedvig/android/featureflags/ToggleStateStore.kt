package com.hedvig.android.featureflags

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import io.getunleash.android.data.Payload
import io.getunleash.android.data.Toggle
import io.getunleash.android.data.UnleashContext
import io.getunleash.android.data.UnleashState
import io.getunleash.android.data.Variant
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Keeps the last fetched toggle state, with the context it was fetched for, across processes. */
internal interface ToggleStateStore {
  suspend fun load(): UnleashState?

  fun save(state: UnleashState)
}

/**
 * A [ToggleStateStore] backed by a single JSON file. A missing or unreadable file loads as nothing, and a
 * failed write keeps the previous file, so neither can stop flags from resolving.
 */
internal class FileToggleStateStore(private val file: File) : ToggleStateStore {
  override suspend fun load(): UnleashState? = withContext(Dispatchers.IO) {
    if (!file.exists()) return@withContext null
    try {
      json.decodeFromString(StoredState.serializer(), file.readText()).toUnleashState()
    } catch (e: Exception) {
      logcat(LogPriority.WARN, e) { "Unleash: failed to read the stored toggle state" }
      null
    }
  }

  override fun save(state: UnleashState) {
    try {
      file.parentFile?.mkdirs()
      // Written next to the file and moved into place, so a process killed mid-write leaves the old file.
      val temporaryFile = File(file.parentFile, "${file.name}.tmp")
      temporaryFile.writeText(json.encodeToString(StoredState.serializer(), state.toStored()))
      if (!temporaryFile.renameTo(file)) {
        logcat(LogPriority.WARN) { "Unleash: could not move the stored toggle state into place" }
      }
    } catch (e: Exception) {
      logcat(LogPriority.WARN, e) { "Unleash: failed to store the toggle state" }
    }
  }
}

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class StoredState(
  val context: StoredContext,
  val toggles: List<StoredToggle>,
)

@Serializable
private data class StoredContext(
  val userId: String?,
  val sessionId: String?,
  val remoteAddress: String?,
  val properties: Map<String, String>,
)

@Serializable
private data class StoredToggle(
  val name: String,
  val enabled: Boolean,
  val impressionData: Boolean,
  val variant: StoredVariant,
)

@Serializable
private data class StoredVariant(
  val name: String,
  val enabled: Boolean,
  val featureEnabled: Boolean,
  val payload: StoredPayload?,
)

@Serializable
private data class StoredPayload(
  val type: String,
  val value: String,
)

private fun UnleashState.toStored() = StoredState(
  context = StoredContext(
    userId = context.userId,
    sessionId = context.sessionId,
    remoteAddress = context.remoteAddress,
    properties = context.properties,
  ),
  toggles = toggles.values.map { toggle ->
    StoredToggle(
      name = toggle.name,
      enabled = toggle.enabled,
      impressionData = toggle.impressionData,
      variant = StoredVariant(
        name = toggle.variant.name,
        enabled = toggle.variant.enabled,
        featureEnabled = toggle.variant.featureEnabled,
        payload = toggle.variant.payload?.let { StoredPayload(type = it.type, value = it.value) },
      ),
    )
  },
)

private fun StoredState.toUnleashState() = UnleashState(
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
