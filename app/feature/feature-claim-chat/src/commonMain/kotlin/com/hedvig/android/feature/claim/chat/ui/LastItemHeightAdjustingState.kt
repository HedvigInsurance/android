package com.hedvig.android.feature.claim.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hedvig.android.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.data.StepId

@Composable
internal fun rememberLastItemHeightAdjustingState(
  density: Density,
  spaceBetweenItems: Dp,
  steps: List<ClaimIntentStep>,
): LastItemHeightAdjustingState {
  val heightOfItemBottomContentMap: SnapshotStateMap<StepId, IntSize> = remember { mutableStateMapOf() }
  val stepsState by rememberUpdatedState(steps)

  LaunchedEffect(steps) {
    Snapshot.withMutableSnapshot {
      val prunedFromDeletedStepsMap = heightOfItemBottomContentMap.filter { (stepId, _) ->
        stepId in steps.map { it.id }
      }
      heightOfItemBottomContentMap.clear()
      heightOfItemBottomContentMap.putAll(prunedFromDeletedStepsMap)
    }
  }

  return remember(density, spaceBetweenItems) {
    LastItemHeightAdjustingState(
      heightOfItemBottomContentMap = heightOfItemBottomContentMap,
      density = density,
      spaceBetweenItems = spaceBetweenItems,
      steps = { stepsState },
    )
  }
}

internal class LastItemHeightAdjustingState(
  private val heightOfItemBottomContentMap: SnapshotStateMap<StepId, IntSize>,
  private val density: Density,
  private val spaceBetweenItems: Dp,
  private val steps: () -> List<ClaimIntentStep>,
) {
  private var minHeightForFullScreenItem by mutableStateOf(0.dp)
  private var leadingItemHeight by mutableStateOf(0.dp)

  val preferredMinHeightForFullScreenItem: Dp by derivedStateOf {
    val heightTakenByPreviousStep = if (steps().size < 2) {
      // The first step has no step above it, but it does have the AI disclaimer, which sits in the list with it.
      // Without this the step asks for the whole viewport and the re-pin scrolls the disclaimer out of sight.
      leadingItemHeight
    } else {
      val isPreviousStepTask = steps().dropLast(1).last().stepContent is StepContent.Task
      val stepId = steps()
        .filter { if (isPreviousStepTask) it.stepContent !is StepContent.Task else true }
        .dropLast(1).last().id
      with(density) {
        val adjustmentForTask =
          if (isPreviousStepTask) {
            animationSize.toDp() + 12.dp
          } else {
            0.dp
          }
        (heightOfItemBottomContentMap[stepId]?.height?.toDp() ?: 0.dp) + adjustmentForTask
      }
    }
    // A previous answer taller than the viewport makes this subtraction negative, which is not a meaningful
    // minimum height. requiredHeightIn coerces a negative minimum to zero itself, so this changes no layout
    // today. It keeps the value honest for anything else that reads it, such as a key on a scroll effect.
    (minHeightForFullScreenItem - spaceBetweenItems - heightTakenByPreviousStep).coerceAtLeast(0.dp)
  }

  /** The height last reported for the current step's answer area, 0.dp before it has reported one. */
  val lastItemBottomContentHeight: Dp by derivedStateOf {
    val lastStepId = steps().lastOrNull()?.id ?: return@derivedStateOf 0.dp
    with(density) { heightOfItemBottomContentMap[lastStepId]?.height?.toDp() ?: 0.dp }
  }

  fun onContainerSizeChanged(size: IntSize) {
    minHeightForFullScreenItem = with(density) { size.height.toDp() }
  }

  /** The height the list gives the item above the first step, only counted while there is a single step. */
  fun onLeadingItemHeightChanged(height: Int) {
    leadingItemHeight = with(density) { height.toDp() }
  }

  fun onItemHeightChanged(stepId: StepId, size: IntSize) {
    heightOfItemBottomContentMap[stepId] = size
  }
}
