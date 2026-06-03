package com.hedvig.android.app.navigation

import androidx.compose.runtime.mutableStateListOf
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.feature.help.center.navigation.HelpCenterKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.core.TopLevelGraph
import org.junit.Test

/**
 * End-to-end coverage of [BackstackController] against the Nav2 behavior oracle: system-back
 * dispatch ([BackstackController.handleBack]) and rail/bar taps
 * ([BackstackController.selectTopLevel]). The pure run-logic functions these compose are
 * covered by [TopLevelRunLogicTest]; this guards the controller wiring around them.
 */
internal class BackstackControllerTest {
  private fun controllerWith(vararg keys: HedvigNavKey) = BackstackController(mutableStateListOf(*keys))

  @Test
  fun `system-back at a drill-down pops one entry`() {
    val controller = controllerWith(HomeKey, HelpCenterKey)

    val handled = controller.handleBack()

    assertThat(handled).isTrue()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `system-back at a side-tab root collapses to Home`() {
    val controller = controllerWith(HomeKey, InsurancesKey)

    val handled = controller.handleBack()

    assertThat(handled).isTrue()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `system-back at the Home root exits the app`() {
    val controller = controllerWith(HomeKey)

    val handled = controller.handleBack()

    assertThat(handled).isFalse()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `re-tapping the current tab pops its run to the root`() {
    val controller = controllerWith(HomeKey, HelpCenterKey)

    controller.selectTopLevel(TopLevelGraph.Home)

    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `switching to another side tab moves its run to the top`() {
    val controller = controllerWith(HomeKey, InsurancesKey)

    controller.selectTopLevel(TopLevelGraph.Profile)

    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey, ProfileKey)
  }

  @Test
  fun `selecting Home from a side tab collapses to Home`() {
    val controller = controllerWith(HomeKey, InsurancesKey)

    controller.selectTopLevel(TopLevelGraph.Home)

    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `setLoggedIn pins Home as the only entry`() {
    val controller = controllerWith(LoginKey)

    controller.setLoggedIn()

    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.isLoggedIn).isTrue()
  }

  @Test
  fun `setLoggedOut drops back to the login root`() {
    val controller = controllerWith(HomeKey, InsurancesKey)

    controller.setLoggedOut()

    assertThat(controller.entries.toList()).containsExactly(LoginKey)
    assertThat(controller.isLoggedIn).isFalse()
  }

  @Test
  fun `currentTopLevel tracks the nearest tab below the top`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)

    assertThat(controller.currentTopLevel).isEqualTo(TopLevelGraph.Insurances)
  }
}
