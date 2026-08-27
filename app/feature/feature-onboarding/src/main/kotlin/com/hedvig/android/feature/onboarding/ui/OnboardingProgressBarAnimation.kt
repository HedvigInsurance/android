package com.hedvig.android.feature.onboarding.ui

import androidx.compose.runtime.mutableStateMapOf
import com.hedvig.android.core.common.di.ActivityRetainedScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Single source of truth for the onboarding progress bar's fill, shared by every step so the fill
 * moves as one continuous value across the header's shared-element transition.
 *
 * Each step is a separate composition. While a transition runs, both the outgoing and incoming steps
 * register themselves here with their step number and how visible each one currently is (1 when fully
 * on screen, 0 when gone). [filledStepCount] averages the step numbers by visibility, so:
 * - both bars render the identical value every frame, hiding the `sharedBounds` crossfade,
 * - the value sits between the outgoing and incoming steps and follows the transition, including a
 *   predictive-back gesture and its seek,
 * - a cancelled gesture returns smoothly as the visibilities return, with nothing left stranded,
 * - a single step on screen simply shows its own number, so fresh launches and process-death restores
 *   land on the right fill with no sweep.
 *
 * Scoped to the onboarding flow's lifetime, like
 * [com.hedvig.android.feature.onboarding.data.OnboardingSessionStore].
 */
@Inject
@SingleIn(ActivityRetainedScope::class)
internal class OnboardingProgressBarAnimation {
  private val visibleSteps = mutableStateMapOf<Any, VisibleStep>()

  /**
   * How many steps' worth of the bar to fill, possibly fractional. It is the [VisibleStep.stepNumber]
   * of every on-screen step averaged by how visible each one is, so during a transition it sits
   * between the outgoing and incoming steps (2.7 means "70% of the way from step 2 to step 3"). With
   * a single step on screen it is simply that step's number.
   */
  val filledStepCount: Float
    get() {
      var stepNumberSum = 0f
      var visibleSum = 0f
      for (step in visibleSteps.values) {
        stepNumberSum += step.stepNumber * step.visibleAmount
        visibleSum += step.visibleAmount
      }
      if (visibleSum > MinimumVisible) return stepNumberSum / visibleSum
      // Nothing is really visible yet (e.g. the very first frame): fall back to the plain average so
      // the bar never flashes empty.
      if (visibleSteps.isEmpty()) return 0f
      return visibleSteps.values.sumOf { it.stepNumber.toDouble() }.toFloat() / visibleSteps.size
    }

  fun setVisibleStep(key: Any, stepNumber: Int, visibleAmount: Float) {
    visibleSteps[key] = VisibleStep(stepNumber, visibleAmount)
  }

  fun removeStep(key: Any) {
    visibleSteps.remove(key)
  }

  private data class VisibleStep(val stepNumber: Int, val visibleAmount: Float)

  private companion object {
    const val MinimumVisible = 0.0001f
  }
}
