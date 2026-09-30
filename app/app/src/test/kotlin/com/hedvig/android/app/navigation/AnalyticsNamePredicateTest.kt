package com.hedvig.android.app.navigation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.navigation.common.AnalyticsNamed
import com.hedvig.android.navigation.common.HedvigNavKey
import org.junit.Test

internal class AnalyticsNamePredicateTest {
  @Test
  fun `a pinned key reports its pinned name verbatim`() {
    assertThat(AnalyticsNamePredicate.getViewName(PinnedKey)).isEqualTo(PinnedKey.ANALYTICS_NAME)
  }

  @Test
  fun `an unpinned key defers to the Datadog default`() {
    assertThat(AnalyticsNamePredicate.getViewName(UnpinnedKey)).isNull()
  }

  @Test
  fun `every key is accepted as a view`() {
    assertThat(AnalyticsNamePredicate.accept(PinnedKey)).isTrue()
    assertThat(AnalyticsNamePredicate.accept(UnpinnedKey)).isTrue()
  }

  private data object PinnedKey : HedvigNavKey, AnalyticsNamed {
    const val ANALYTICS_NAME = "com.hedvig.android.feature.fake.navigation.PinnedKey"
    override val analyticsName = ANALYTICS_NAME
  }

  private data object UnpinnedKey : HedvigNavKey
}
