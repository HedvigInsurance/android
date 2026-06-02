package com.hedvig.android.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the same serializer + SavedStateConfiguration wiring used by
 * [rememberHedvigTopLevelBackStacks] round-trips a flat back stack of polymorphic
 * [HedvigNavKey]s. Exercises the public top-level keys plus [LoginKey], which a
 * logged-out cold start depends on.
 */
@RunWith(AndroidJUnit4::class)
internal class BackStackSerializationTest {
  private val configuration = SavedStateConfiguration {
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

  private val serializer = PolymorphicSerializer(HedvigNavKey::class)

  @Test
  fun `logged-in back stack round-trips through saved state`() {
    val original: List<HedvigNavKey> = listOf(HomeKey, InsurancesKey, ProfileKey)

    val restored = original.map { key ->
      val encoded = encodeToSavedState(serializer, key, configuration)
      decodeFromSavedState(serializer, encoded, configuration)
    }

    assertThat(mutableStateListOf<HedvigNavKey>().apply { addAll(restored) })
      .containsExactly(HomeKey, InsurancesKey, ProfileKey)
  }

  @Test
  fun `logged-out back stack round-trips through saved state`() {
    val encoded = encodeToSavedState(serializer, LoginKey, configuration)
    val restored = decodeFromSavedState(serializer, encoded, configuration)

    assertThat(listOf(restored)).containsExactly(LoginKey)
  }
}
