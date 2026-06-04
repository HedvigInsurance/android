package com.hedvig.android.app.navigation

import assertk.assertThat
import assertk.assertions.containsExactly
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.DeepLinkAncestry
import com.hedvig.android.navigation.common.TopLevelGraph
import kotlinx.serialization.Serializable
import org.junit.Test

internal class SyntheticStackTest {
  @Serializable
  private data object FakeParentKey : HedvigNavKey

  @Serializable
  private data object FakeLeafKey : HedvigNavKey, DeepLinkAncestry {
    override val owningTab = TopLevelGraph.Insurances
    override val syntheticParents = listOf<HedvigNavKey>(FakeParentKey)
  }

  @Serializable
  private data object PlainKey : HedvigNavKey

  @Test
  fun `plain key with no ancestry is tab-rooted to Home`() {
    assertThat(syntheticStackFor(PlainKey)).containsExactly(HomeKey, PlainKey)
  }

  @Test
  fun `DeepLinkAncestry key builds Home, tab root, parents, then itself`() {
    assertThat(syntheticStackFor(FakeLeafKey))
      .containsExactly(HomeKey, InsurancesKey, FakeParentKey, FakeLeafKey)
  }

  @Test
  fun `tab-root key collapses to Home plus itself`() {
    assertThat(syntheticStackFor(InsurancesKey)).containsExactly(HomeKey, InsurancesKey)
  }

  @Test
  fun `HomeKey collapses to a lone Home`() {
    assertThat(syntheticStackFor(HomeKey)).containsExactly(HomeKey)
  }
}
