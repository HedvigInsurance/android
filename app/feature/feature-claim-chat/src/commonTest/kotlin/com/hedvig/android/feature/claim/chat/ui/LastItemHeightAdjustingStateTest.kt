package com.hedvig.android.feature.claim.chat.ui

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.data.StepId
import kotlin.test.Test

internal class LastItemHeightAdjustingStateTest {
  @Test
  fun `a previous answer taller than the viewport does not make the preferred min height negative`() {
    val state = stateWith(twoSteps)
    state.onContainerSizeChanged(IntSize(1080, viewportHeightPx))
    // A long free text answer, or a many option select rendered as pills, easily outgrows the viewport.
    state.onItemHeightChanged(firstStepId, IntSize(1080, viewportHeightPx * 2))

    assertThat(state.preferredMinHeightForFullScreenItem).isEqualTo(0.dp)
  }

  @Test
  fun `a previous answer shorter than the viewport still leaves the remaining height`() {
    val state = stateWith(twoSteps)
    state.onContainerSizeChanged(IntSize(1080, viewportHeightPx))
    state.onItemHeightChanged(firstStepId, IntSize(1080, 200))

    // 800.dp viewport, minus the 8.dp between items, minus the 100.dp the previous answer takes.
    assertThat(state.preferredMinHeightForFullScreenItem).isEqualTo(692.dp)
  }

  @Test
  fun `the height reported for the current step is exposed as the last item bottom content height`() {
    val state = stateWith(twoSteps)
    state.onItemHeightChanged(secondStepId, IntSize(1080, 300))

    assertThat(state.lastItemBottomContentHeight).isEqualTo(150.dp)
  }

  @Test
  fun `a height reported for a previous step is not the last item bottom content height`() {
    val state = stateWith(twoSteps)
    state.onItemHeightChanged(firstStepId, IntSize(1080, 300))

    assertThat(state.lastItemBottomContentHeight).isEqualTo(0.dp)
  }

  private fun stateWith(steps: List<ClaimIntentStep>) = LastItemHeightAdjustingState(
    heightOfItemBottomContentMap = mutableStateMapOf(),
    density = Density(density = 2f, fontScale = 1f),
    spaceBetweenItems = 8.dp,
    steps = { steps },
  )
}

private const val viewportHeightPx = 1600

private val firstStepId = StepId("first")

private val secondStepId = StepId("second")

private val twoSteps = listOf(
  stepWithId(firstStepId),
  stepWithId(secondStepId),
)

private fun stepWithId(id: StepId) = ClaimIntentStep(
  id = id,
  text = null,
  stepContent = StepContent.Form(fields = emptyList(), isSkippable = false),
  isRegrettable = false,
  hint = null,
)
