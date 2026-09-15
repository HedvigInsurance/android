package com.hedvig.android.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavEntry
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.help.center.navigation.HelpCenterKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.common.contentKey
import org.junit.Test

/**
 * Locks the agreement between the content key an entry carries and the one [BackstackController]
 * derives for the same destination.
 *
 * Nothing else catches a break here. navigation3 `1.2.0-rc01` changed its own `defaultContentKey`
 * from `key.toString()` to `"$key:${key::class}"`; because the controller rebuilds content keys for
 * destinations that have no live entry, the two sides silently stopped matching, and the only visible
 * symptoms were lost `ViewModel`s on pop and a transition classifier that always returned null.
 */
internal class HedvigContentKeysTest {
  private val entries = withHedvigContentKeys { key -> NavEntry(key = key) { } }

  @Test
  fun `a wrapped entry carries our derivation, not navigation3's default`() {
    assertThat(entries(HomeKey).contentKey).isEqualTo(HomeKey.contentKey())
  }

  @Test
  fun `a rendered destination's entry key is one the controller reports as live`() {
    val controller = BackstackController(
      mutableStateListOf(HomeKey, HelpCenterKey),
      mutableStateMapOf(),
      mutableStateOf(null),
      mutableStateOf(null),
    )
    assertThat(controller.allLiveContentKeys).contains(entries(HelpCenterKey).contentKey)
  }

  @Test
  fun `a parked destination's entry key is one the controller reports as live`() {
    val controller = BackstackController(
      mutableStateListOf(HomeKey),
      mutableStateMapOf(TopLevelTab.Insurances to listOf(HelpCenterKey)),
      mutableStateOf(null),
      mutableStateOf(null),
    )
    assertThat(controller.allLiveContentKeys).contains(entries(HelpCenterKey).contentKey)
  }

  @Test
  fun `the tab owner of a destination resolves from an entry's content key`() {
    val controller = BackstackController(
      mutableStateListOf(HomeKey),
      mutableStateMapOf(TopLevelTab.Insurances to listOf(HelpCenterKey)),
      mutableStateOf(null),
      mutableStateOf(null),
    )
    val owner = controller.owningTopLevelTabForContentKey(entries(HelpCenterKey).contentKey)
    assertThat(owner).isEqualTo(TopLevelTab.Insurances)
  }
}
