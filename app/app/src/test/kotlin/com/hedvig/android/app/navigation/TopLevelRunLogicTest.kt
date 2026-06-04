package com.hedvig.android.app.navigation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.feature.forever.navigation.ForeverKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelGraph
import kotlinx.serialization.Serializable
import org.junit.Test

@Serializable private data class Drill(val id: String) : HedvigNavKey

class TopLevelRunLogicTest {
  @Test fun topLevelGraphOrNull_mapsTabRoots() {
    assertThat(HomeKey.topLevelGraphOrNull()).isEqualTo(TopLevelGraph.Home)
    assertThat(InsurancesKey.topLevelGraphOrNull()).isEqualTo(TopLevelGraph.Insurances)
    assertThat(ForeverKey.topLevelGraphOrNull()).isEqualTo(TopLevelGraph.Forever)
    assertThat(PaymentsKey.topLevelGraphOrNull()).isEqualTo(TopLevelGraph.Payments)
    assertThat(ProfileKey.topLevelGraphOrNull()).isEqualTo(TopLevelGraph.Profile)
    assertThat(Drill("x").topLevelGraphOrNull()).isNull()
  }

  @Test fun nearestTopLevelGraph_isOwnerOfTop() {
    assertThat(nearestTopLevelGraph(listOf(HomeKey, Drill("h"), InsurancesKey, Drill("i"))))
      .isEqualTo(TopLevelGraph.Insurances)
    assertThat(nearestTopLevelGraph(listOf(HomeKey, Drill("h")))).isEqualTo(TopLevelGraph.Home)
    assertThat(nearestTopLevelGraph(listOf(Drill("login")))).isNull()
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
    assertThat(shouldFadeThrough(TopLevelGraph.Forever, TopLevelGraph.Insurances)).isTrue()
    assertThat(shouldFadeThrough(TopLevelGraph.Home, TopLevelGraph.Profile)).isTrue()
  }

  @Test fun shouldFadeThrough_falseWithinSameTab() {
    assertThat(shouldFadeThrough(TopLevelGraph.Insurances, TopLevelGraph.Insurances)).isFalse()
  }

  @Test fun shouldFadeThrough_falseWhenEitherTabIsNull() {
    // A tab-less screen (e.g. pre-login Login) is never a tab change.
    assertThat(shouldFadeThrough(null, TopLevelGraph.Home)).isFalse()
    assertThat(shouldFadeThrough(TopLevelGraph.Home, null)).isFalse()
    assertThat(shouldFadeThrough(null, null)).isFalse()
  }
}
