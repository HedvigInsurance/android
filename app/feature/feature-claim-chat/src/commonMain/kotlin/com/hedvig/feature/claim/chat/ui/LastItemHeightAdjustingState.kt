package com.hedvig.feature.claim.chat.ui

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
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepId

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

  val preferredMinHeightForFullScreenItem: Dp by derivedStateOf {
    minHeightForFullScreenItem - spaceBetweenItems - if (steps().size < 2) {
      0.dp
    } else {
      val isPreviousStepTask = steps().dropLast(1).last().stepContent is StepContent.Task

      val stepId = steps().filter { it.stepContent !is StepContent.Task }
        .dropLast(1).last().id
      with(density) {
        val adjustmentForTask =
          if (isPreviousStepTask) 23.dp //todo!
          else 0.dp
        (heightOfItemBottomContentMap[stepId]?.height?.toDp() ?: 0.dp) + adjustmentForTask
      }
    }
  }

  fun onContainerSizeChanged(size: IntSize) {
    minHeightForFullScreenItem = with(density) { size.height.toDp() }
  }

  fun onItemHeightChanged(stepId: StepId, size: IntSize) {
    heightOfItemBottomContentMap[stepId] = size
  }
}
