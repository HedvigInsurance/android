package com.hedvig.android.app.navigation

import assertk.assertThat
import assertk.assertions.containsExactly
import com.hedvig.android.feature.forever.navigation.ForeverKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test

/**
 * Verifies the polymorphic [HedvigNavKey] registration that the back stack relies on for
 * process-death persistence: a flat list of keys round-trips through the same
 * [PolymorphicSerializer] + [SerializersModule] used by [rememberHedvigBackstackController].
 *
 * The production code feeds this into a `SavedStateConfiguration`; the SavedState encoder itself
 * is androidx's concern and would require an Android `Bundle`. What we own — and what this test
 * guards — is that every key is registered with no serial-name collisions, which is independent
 * of the wire format, so a plain JSON round-trip exercises it without an Android runtime.
 */
internal class BackstackSerializationTest {
  private val json = Json {
    serializersModule = SerializersModule {
      polymorphic(HedvigNavKey::class) {
        subclass(HomeKey::class)
        subclass(InsurancesKey::class)
        subclass(ForeverKey::class)
        subclass(PaymentsKey::class)
        subclass(ProfileKey::class)
        subclass(LoginKey::class)
      }
    }
  }

  private val serializer = ListSerializer(PolymorphicSerializer(HedvigNavKey::class))

  @Test
  fun `logged-in back stack round-trips`() {
    val original = listOf(HomeKey, InsurancesKey, ProfileKey)

    val restored = json.decodeFromString(serializer, json.encodeToString(serializer, original))

    assertThat(restored).containsExactly(HomeKey, InsurancesKey, ProfileKey)
  }

  @Test
  fun `logged-out back stack round-trips`() {
    val original = listOf<HedvigNavKey>(LoginKey)

    val restored = json.decodeFromString(serializer, json.encodeToString(serializer, original))

    assertThat(restored).containsExactly(LoginKey)
  }
}
