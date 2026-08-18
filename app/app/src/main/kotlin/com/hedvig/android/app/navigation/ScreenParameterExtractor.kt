package com.hedvig.android.app.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TrackedScreen
import com.hedvig.android.navigation.compose.merge
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.modules.SerializersModule

/**
 * Derives the analytics parameters attached to a destination's `screen_view`. By default it reflects
 * the key's own [kotlinx.serialization]-serialized properties (every `@Serializable` `val` becomes a
 * parameter), reusing the same merged [SerializersModule]s that persist the back stack — so any key
 * that survives process death automatically has reportable parameters with no per-screen wiring. A key
 * may instead implement [TrackedScreen] to take over entirely.
 *
 * Parameters ride along the single `screen_view` event keyed by screen name, so they act as breakdown
 * dimensions rather than fragmenting a screen into separate entries.
 */
@SingleIn(AppScope::class)
@Inject
internal class ScreenParameterExtractor(
  serializersModules: Set<SerializersModule>,
) {
  private val json = Json {
    serializersModule = serializersModules.merge()
    encodeDefaults = true
  }

  fun parametersFor(destination: HedvigNavKey): Map<String, Any?> {
    if (destination is TrackedScreen) {
      return destination.screenParameters
    }
    val element = runCatching {
      json.encodeToJsonElement(PolymorphicSerializer(HedvigNavKey::class), destination)
    }.getOrNull() as? JsonObject ?: return emptyMap()
    return element
      .filterKeys { it != CLASS_DISCRIMINATOR }
      .mapValues { (_, value) -> value.toPrimitiveOrNull() }
  }

  // The default polymorphic discriminator kotlinx.serialization writes to identify the concrete key
  // type; it duplicates the screen name/class, so it is dropped from the reported parameters.
  private companion object {
    const val CLASS_DISCRIMINATOR = "type"
  }
}

private fun JsonElement.toPrimitiveOrNull(): Any? = when (this) {
  is JsonNull -> null
  is JsonPrimitive -> if (isString) content else (booleanOrNull ?: longOrNull ?: doubleOrNull ?: content)
  else -> toString()
}
