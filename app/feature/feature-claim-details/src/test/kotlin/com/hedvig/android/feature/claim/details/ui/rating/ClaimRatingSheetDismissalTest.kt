package com.hedvig.android.feature.claim.details.ui.rating

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ClaimRatingSheetDismissalTest {
  @Test
  fun `a sheet restored as visible and then hidden by the design system is not a dismissal`() = runTest {
    assertThat(flowOf(true, false).userDismissals().toList()).isEmpty()
  }

  @Test
  fun `a sheet shown by this composable and then closed is a dismissal`() = runTest {
    assertThat(flowOf(false, true, false).userDismissals().toList()).hasSize(1)
  }

  @Test
  fun `a restored sheet hidden by the design system and then shown and closed is one dismissal`() = runTest {
    assertThat(flowOf(true, false, true, false).userDismissals().toList()).hasSize(1)
  }

  @Test
  fun `a sheet that never opens is not a dismissal`() = runTest {
    flowOf(false).userDismissals().test { awaitComplete() }
  }
}
