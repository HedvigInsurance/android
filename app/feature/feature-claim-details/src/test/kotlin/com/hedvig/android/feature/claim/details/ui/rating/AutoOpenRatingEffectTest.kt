package com.hedvig.android.feature.claim.details.ui.rating

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AutoOpenRatingEffectTest {
  private var openCount = 0

  @Test
  fun `opening from the to-do opens the sheet once the claim has loaded`() = runTest {
    val ratingButton = MutableStateFlow<RatingButtonState?>(null)
    compose(openRatingOnLoad = true, ratingButton = ratingButton, registry = emptyRegistry())
    assertThat(openCount).isEqualTo(0)

    ratingButton.value = RatingButtonState.Enabled
    runCurrent()

    assertThat(openCount).isEqualTo(1)
  }

  @Test
  fun `the sheet does not open a second time when the claim emits again`() = runTest {
    val ratingButton = MutableStateFlow<RatingButtonState?>(RatingButtonState.Enabled)
    compose(openRatingOnLoad = true, ratingButton = ratingButton, registry = emptyRegistry())

    ratingButton.value = null
    runCurrent()
    ratingButton.value = RatingButtonState.Enabled
    runCurrent()

    assertThat(openCount).isEqualTo(1)
  }

  @Test
  fun `opening from the to-do leaves the sheet closed when the claim is already rated`() = runTest {
    val ratingButton = MutableStateFlow<RatingButtonState?>(RatingButtonState.Submitted)
    compose(openRatingOnLoad = true, ratingButton = ratingButton, registry = emptyRegistry())

    ratingButton.value = RatingButtonState.Enabled
    runCurrent()

    assertThat(openCount).isEqualTo(0)
  }

  @Test
  fun `opening the claim without the rating request never opens the sheet`() = runTest {
    compose(
      openRatingOnLoad = false,
      ratingButton = MutableStateFlow(RatingButtonState.Enabled),
      registry = emptyRegistry(),
    )

    assertThat(openCount).isEqualTo(0)
  }

  @Test
  fun `a screen restored from saved state does not open the sheet again`() = runTest {
    val ratingButton = MutableStateFlow<RatingButtonState?>(RatingButtonState.Enabled)
    val firstRegistry = emptyRegistry()
    val firstComposition = compose(openRatingOnLoad = true, ratingButton = ratingButton, registry = firstRegistry)
    val savedState = firstRegistry.performSave()
    firstComposition.cancel()

    compose(
      openRatingOnLoad = true,
      ratingButton = ratingButton,
      registry = SaveableStateRegistry(savedState) { true },
    )

    assertThat(openCount).isEqualTo(1)
  }

  private fun emptyRegistry() = SaveableStateRegistry(restoredValues = null) { true }

  private fun TestScope.compose(
    openRatingOnLoad: Boolean,
    ratingButton: StateFlow<RatingButtonState?>,
    registry: SaveableStateRegistry,
  ): Job {
    val job = backgroundScope.launch {
      moleculeFlow(RecompositionMode.Immediate) {
        CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
          AutoOpenRatingEffect(
            openRatingOnLoad = openRatingOnLoad,
            ratingButton = ratingButton.collectAsState().value,
            openRating = { openCount++ },
          )
        }
      }.collect {}
    }
    runCurrent()
    return job
  }
}
