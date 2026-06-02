package com.hedvig.android.navigation.common

import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Exercises the real [encodeToSavedState] / [decodeFromSavedState] + [SavedStateConfiguration] path
 * that `rememberHedvigTopLevelBackStacks` uses for process-death persistence. On JVM/iOS the
 * `SavedState` is a Map-backed implementation, so this runs without a `Bundle` or Robolectric.
 *
 * The real feature keys live in Android feature modules and aren't reachable here, so these
 * stand-ins reproduce the polymorphic shape. Registration of the actual keys is guarded separately
 * by the JSON round-trip test in :app.
 */
@Serializable
private data object TestHomeKey : HedvigNavKey

@Serializable
private data class TestDetailKey(val id: String) : HedvigNavKey

internal class HedvigNavKeySavedStateTest {
  private val configuration = SavedStateConfiguration {
    serializersModule = SerializersModule {
      polymorphic(HedvigNavKey::class) {
        subclass(TestHomeKey::class)
        subclass(TestDetailKey::class)
      }
    }
  }

  private val serializer = ListSerializer(PolymorphicSerializer(HedvigNavKey::class))

  @Test
  fun backStackRoundTripsThroughRealSavedState() {
    val original: List<HedvigNavKey> = listOf(TestHomeKey, TestDetailKey("abc"), TestHomeKey)

    val savedState = encodeToSavedState(serializer, original, configuration)
    val restored = decodeFromSavedState(serializer, savedState, configuration)

    assertEquals(original, restored)
  }
}
