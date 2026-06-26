package com.hedvig.android.app.navigation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import kotlinx.serialization.Serializable
import org.junit.Test

@Serializable private data class Drill(val id: String) : HedvigNavKey

class TopLevelRunLogicTest {
  @Test fun topLevelTabOrNull_mapsTabRoots() {
    assertThat(HomeKey.topLevelTabOrNull()).isEqualTo(TopLevelTab.Home)
    assertThat(InsurancesKey.topLevelTabOrNull()).isEqualTo(TopLevelTab.Insurances)
    assertThat(PaymentsKey.topLevelTabOrNull()).isEqualTo(TopLevelTab.Payments)
    assertThat(ProfileKey.topLevelTabOrNull()).isEqualTo(TopLevelTab.Profile)
    assertThat(Drill("x").topLevelTabOrNull()).isNull()
  }

  @Test fun nearestTopLevelTab_isOwnerOfTop() {
    assertThat(nearestTopLevelTab(listOf(HomeKey, Drill("h"), InsurancesKey, Drill("i"))))
      .isEqualTo(TopLevelTab.Insurances)
    assertThat(nearestTopLevelTab(listOf(HomeKey, Drill("h")))).isEqualTo(TopLevelTab.Home)
    assertThat(nearestTopLevelTab(listOf(Drill("login")))).isNull()
  }

  @Test fun collapseToHome_keepsHomeRunDiscardsSideRuns() {
    val stack = listOf(HomeKey, Drill("h"), InsurancesKey, Drill("i"), PaymentsKey)
    assertThat(collapseToHome(stack)).isEqualTo(listOf(HomeKey, Drill("h")))
    assertThat(collapseToHome(listOf(HomeKey, InsurancesKey))).isEqualTo(listOf(HomeKey))
    assertThat(collapseToHome(listOf(HomeKey))).isEqualTo(listOf(HomeKey))
  }

  @Test fun popTopRunToStart_dropsTopRunDrilldowns() {
    assertThat(popTopRunToStart(listOf(HomeKey, InsurancesKey, Drill("i1"), Drill("i2"))))
      .isEqualTo(listOf(HomeKey, InsurancesKey))
    assertThat(popTopRunToStart(listOf(HomeKey, Drill("h")))).isEqualTo(listOf(HomeKey))
  }

  @Test fun activeSideRun_isEmptyOnHome() {
    assertThat(activeSideRun(listOf(HomeKey))).isEqualTo(emptyList<HedvigNavKey>())
    assertThat(activeSideRun(listOf(HomeKey, Drill("h")))).isEqualTo(emptyList<HedvigNavKey>())
  }

  @Test fun activeSideRun_returnsSideTabKeyAndItsDrilldowns() {
    val stack = listOf(HomeKey, Drill("h"), InsurancesKey, Drill("i1"), Drill("i2"))
    assertThat(activeSideRun(stack)).isEqualTo(listOf(InsurancesKey, Drill("i1"), Drill("i2")))
  }

  @Test fun activeSideRun_sideTabRootOnly() {
    assertThat(activeSideRun(listOf(HomeKey, ProfileKey))).isEqualTo(listOf(ProfileKey))
  }

  @Test fun shouldFadeThrough_trueWhenTabsDiffer() {
    assertThat(shouldFadeThrough(TopLevelTab.Payments, TopLevelTab.Insurances)).isTrue()
    assertThat(shouldFadeThrough(TopLevelTab.Home, TopLevelTab.Profile)).isTrue()
  }

  @Test fun shouldFadeThrough_falseWithinSameTab() {
    assertThat(shouldFadeThrough(TopLevelTab.Insurances, TopLevelTab.Insurances)).isFalse()
  }

  @Test fun shouldFadeThrough_falseWhenEitherTabIsNull() {
    // A tab-less screen (e.g. pre-login Login) is never a tab change.
    assertThat(shouldFadeThrough(null, TopLevelTab.Home)).isFalse()
    assertThat(shouldFadeThrough(TopLevelTab.Home, null)).isFalse()
    assertThat(shouldFadeThrough(null, null)).isFalse()
  }
}
