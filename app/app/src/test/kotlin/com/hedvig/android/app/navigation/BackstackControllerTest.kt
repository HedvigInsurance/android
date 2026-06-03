package com.hedvig.android.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.feature.help.center.navigation.HelpCenterKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.LoneDeepLinkChrome
import com.hedvig.android.navigation.core.TopLevelGraph
import org.junit.Test

internal class BackstackControllerTest {
  private fun controllerWith(vararg keys: HedvigNavKey) =
    BackstackController(mutableStateListOf(*keys), mutableStateMapOf(), mutableStateOf(null))

  @Test
  fun `system-back at a drill-down pops one entry`() {
    val controller = controllerWith(HomeKey, HelpCenterKey)
    assertThat(controller.handleBack()).isTrue()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `system-back at a side-tab root returns to Home and parks nothing`() {
    val controller = controllerWith(HomeKey, InsurancesKey)
    assertThat(controller.handleBack()).isTrue()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.parkedRuns).isEmpty()
  }

  @Test
  fun `system-back draining a drilled side tab drops it completely, parking nothing`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    assertThat(controller.handleBack()).isTrue() // pop Help
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey)
    assertThat(controller.parkedRuns).isEmpty()
    assertThat(controller.handleBack()).isTrue() // pop Insurances root
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.parkedRuns).isEmpty()
  }

  @Test
  fun `system-back never parks the active tab but leaves other parked runs intact`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Profile) // park Insurances run, render Profile root
    controller.handleBack() // drain Profile back to Home
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.parkedRuns[TopLevelGraph.Profile]).isEqualTo(null)
    assertThat(controller.parkedRuns[TopLevelGraph.Insurances])
      .isEqualTo(listOf(InsurancesKey, HelpCenterKey))
  }

  @Test
  fun `system-back at the Home root exits the app`() {
    val controller = controllerWith(HomeKey)
    assertThat(controller.handleBack()).isFalse()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `re-tapping the current tab pops its run to the root`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Insurances)
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey)
  }

  @Test
  fun `switching from a drilled side tab stashes its full run`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Profile)
    assertThat(controller.entries.toList()).containsExactly(HomeKey, ProfileKey)
    assertThat(controller.parkedRuns[TopLevelGraph.Insurances])
      .isEqualTo(listOf(InsurancesKey, HelpCenterKey))
  }

  @Test
  fun `returning to a stashed tab restores its whole run`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Profile) // stash Insurances run
    controller.selectTopLevel(TopLevelGraph.Insurances) // restore it
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey, HelpCenterKey)
    assertThat(controller.parkedRuns[TopLevelGraph.Profile]).isEqualTo(listOf(ProfileKey))
    assertThat(controller.parkedRuns[TopLevelGraph.Insurances]).isEqualTo(null)
  }

  @Test
  fun `selecting Home from a side tab stashes the side run instead of discarding it`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Home)
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.parkedRuns[TopLevelGraph.Insurances])
      .isEqualTo(listOf(InsurancesKey, HelpCenterKey))
  }

  @Test
  fun `re-selecting a side tab after going Home restores its run`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Home)
    controller.selectTopLevel(TopLevelGraph.Insurances)
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey, HelpCenterKey)
  }

  @Test
  fun `switching to a never-visited side tab starts a fresh run`() {
    val controller = controllerWith(HomeKey)
    controller.selectTopLevel(TopLevelGraph.Payments)
    assertThat(controller.entries.toList()).containsExactly(HomeKey, PaymentsKey)
  }

  @Test
  fun `allLiveContentKeys includes parked runs`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Home) // Insurances run now parked
    assertThat(controller.allLiveContentKeys).containsExactlyInAnyOrder(
      HomeKey.toString(),
      InsurancesKey.toString(),
      HelpCenterKey.toString(),
    )
  }

  @Test
  fun `setLoggedIn pins Home and clears parked runs`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Home) // park Insurances
    controller.setLoggedIn()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
    assertThat(controller.parkedRuns).isEmpty()
    assertThat(controller.isLoggedIn).isTrue()
  }

  @Test
  fun `setLoggedOut drops to login root and clears parked runs`() {
    val controller = controllerWith(HomeKey, InsurancesKey)
    controller.selectTopLevel(TopLevelGraph.Home)
    controller.setLoggedOut()
    assertThat(controller.entries.toList()).containsExactly(LoginKey)
    assertThat(controller.parkedRuns).isEmpty()
    assertThat(controller.isLoggedIn).isFalse()
  }

  @Test
  fun `currentTopLevel tracks the nearest tab below the top`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    assertThat(controller.currentTopLevel).isEqualTo(TopLevelGraph.Insurances)
  }

  @Test
  fun `isLoggedIn is true for a lone non-login deep link`() {
    val controller = controllerWith(HelpCenterKey)
    assertThat(controller.isLoggedIn).isTrue()
  }

  @Test
  fun `isLoggedIn is false when rooted at login`() {
    val controller = controllerWith(LoginKey)
    assertThat(controller.isLoggedIn).isFalse()
  }

  @Test
  fun `navigateUp on a lone side-tab root rebuilds to Home`() {
    val controller = controllerWith(InsurancesKey)
    assertThat(controller.navigateUp()).isTrue()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `navigateUp on a normal stack pops one entry like back`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    assertThat(controller.navigateUp()).isTrue()
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey)
  }

  @Test
  fun `navigateUp at the Home root returns false`() {
    val controller = controllerWith(HomeKey)
    assertThat(controller.navigateUp()).isFalse()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `navigateToDeepLink while logged out stashes and leaves the stack untouched`() {
    val controller = controllerWith(LoginKey)
    controller.navigateToDeepLink(HelpCenterKey)
    assertThat(controller.entries.toList()).containsExactly(LoginKey)
    assertThat(controller.pendingDeepLink).isEqualTo(HelpCenterKey)
  }

  @Test
  fun `setLoggedIn consumes the stash and lands the target alone`() {
    val controller = controllerWith(LoginKey)
    controller.navigateToDeepLink(InsurancesKey)
    controller.setLoggedIn()
    assertThat(controller.entries.toList()).containsExactly(InsurancesKey)
    assertThat(controller.pendingDeepLink).isEqualTo(null)
    assertThat(controller.parkedRuns).isEmpty()
  }

  @Test
  fun `setLoggedIn without a stash lands on Home`() {
    val controller = controllerWith(LoginKey)
    controller.setLoggedIn()
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `navigateToDeepLink while logged in appends onto the live stack`() {
    val controller = controllerWith(HomeKey, InsurancesKey)
    controller.navigateToDeepLink(HelpCenterKey)
    assertThat(controller.entries.toList())
      .containsExactly(HomeKey, InsurancesKey, HelpCenterKey)
    assertThat(controller.pendingDeepLink).isEqualTo(null)
  }

  @Test
  fun `loneDeepLinkChrome is ShowUpBar for a lone tab root`() {
    assertThat(controllerWith(InsurancesKey).loneDeepLinkChrome).isEqualTo(LoneDeepLinkChrome.ShowUpBar)
  }

  @Test
  fun `loneDeepLinkChrome is ShowNothing for a lone deep non-tab key`() {
    assertThat(controllerWith(HelpCenterKey).loneDeepLinkChrome)
      .isEqualTo(LoneDeepLinkChrome.ShowNothing)
  }

  @Test
  fun `loneDeepLinkChrome is ShowSuite for lone Home, login, and multi-entry stacks`() {
    assertThat(controllerWith(HomeKey).loneDeepLinkChrome).isEqualTo(LoneDeepLinkChrome.ShowSuite)
    assertThat(controllerWith(LoginKey).loneDeepLinkChrome).isEqualTo(LoneDeepLinkChrome.ShowSuite)
    assertThat(controllerWith(HomeKey, InsurancesKey).loneDeepLinkChrome)
      .isEqualTo(LoneDeepLinkChrome.ShowSuite)
  }
}
