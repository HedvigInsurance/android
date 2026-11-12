package com.hedvig.android.feature.payin.account.navigation

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test

internal class SelectPrimaryPayinMethodKeySerializationTest {
  private val json = Json {
    serializersModule = SerializersModule {
      polymorphic(HedvigNavKey::class) {
        subclass(SelectPrimaryPayinMethodKey::class)
        subclass(PayinMethodDetailsKey::class)
        subclass(SelectPayinMethodKey::class)
      }
    }
  }

  @Test
  fun `select primary payin method key survives a serialization round trip`() {
    val key = SelectPrimaryPayinMethodKey

    val encoded = json.encodeToString(PolymorphicSerializer(HedvigNavKey::class), key)

    assertThat(json.decodeFromString(PolymorphicSerializer(HedvigNavKey::class), encoded)).isEqualTo(key)
  }

  @Test
  fun `payin method details key survives a serialization round trip for every method id`() {
    for (methodId in PayinMethodId.entries) {
      val key = PayinMethodDetailsKey(methodId)

      val encoded = json.encodeToString(PolymorphicSerializer(HedvigNavKey::class), key)

      assertThat(json.decodeFromString(PolymorphicSerializer(HedvigNavKey::class), encoded)).isEqualTo(key)
    }
  }

  @Test
  fun `select payin method key survives a serialization round trip`() {
    val key = SelectPayinMethodKey

    val encoded = json.encodeToString(PolymorphicSerializer(HedvigNavKey::class), key)

    assertThat(json.decodeFromString(PolymorphicSerializer(HedvigNavKey::class), encoded)).isEqualTo(key)
  }
}
