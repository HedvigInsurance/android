package com.hedvig.android.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import assertk.assertThat
import assertk.assertions.containsExactly
import com.hedvig.android.feature.help.center.navigation.HelpCenterKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.navigation.common.HedvigNavKey
import org.junit.Test

/**
 * Guards [BackstackController.navigateToInAppLink] against re-introducing the value-equal
 * duplicate-key crash: Nav3's `NavDisplay` renders every entry under `key.toString()`, so two
 * value-equal keys in the stack crash with "key … used multiple times". A deep link that resolves
 * to a key already on the stack must therefore never blind-append.
 */
internal class DeepLinkNavigationTest {
  private fun controllerWith(vararg keys: HedvigNavKey) = BackstackController(
    mutableStateListOf(*keys),
    mutableStateMapOf(),
    mutableStateOf(null), // pendingDeepLink
    mutableStateOf(null), // stashedSession
  )

  @Test
  fun `deep link to the current tab root does not duplicate it`() {
    val controller = controllerWith(HomeKey)

    controller.navigateToInAppLink(HomeKey)

    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `deep link to an already-present side tab does not duplicate it`() {
    val controller = controllerWith(HomeKey, InsurancesKey)

    controller.navigateToInAppLink(InsurancesKey)

    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey)
  }

  @Test
  fun `deep link to a non-tab key already on the stack moves it to the top instead of duplicating`() {
    val controller = controllerWith(HomeKey, HelpCenterKey)

    controller.navigateToInAppLink(HelpCenterKey)

    assertThat(controller.entries.toList()).containsExactly(HomeKey, HelpCenterKey)
  }

  @Test
  fun `deep link to a new non-tab key appends it`() {
    val controller = controllerWith(HomeKey)

    controller.navigateToInAppLink(HelpCenterKey)

    assertThat(controller.entries.toList()).containsExactly(HomeKey, HelpCenterKey)
  }

  @Test
  fun `deep link to an absent side tab switches to it`() {
    val controller = controllerWith(HomeKey)

    controller.navigateToInAppLink(InsurancesKey)

    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey)
  }
}
