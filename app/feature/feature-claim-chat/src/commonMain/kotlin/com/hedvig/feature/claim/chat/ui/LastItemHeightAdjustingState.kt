package com.hedvig.feature.claim.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.StepId

@Composable
internal fun rememberLastItemHeightAdjustingState(
  density: Density,
  spaceBetweenItems: Dp,
  steps: List<ClaimIntentStep>,
): LastItemHeightAdjustingState {
  val heightOfItemBottomContentMap: SnapshotStateMap<StepId, IntSize> = remember { mutableStateMapOf() }
  var minHeightForFullScreenItem by remember { mutableStateOf(0.dp) }

  val preferredMinHeightForFullScreenItem by remember(density, steps) {
    derivedStateOf {
      minHeightForFullScreenItem - spaceBetweenItems - if (steps.size < 2) {
        0.dp
      } else {
        val stepId = steps.dropLast(1).last().id
        with(density) {
          heightOfItemBottomContentMap[stepId]?.height?.toDp() ?: 0.dp
        }
      }
    }
  }

  LaunchedEffect(steps) {
    Snapshot.withMutableSnapshot {
      val prunedFromDeletedStepsMap = heightOfItemBottomContentMap.filter { (stepId, _) ->
        stepId in steps.map { it.id }
      }
      heightOfItemBottomContentMap.clear()
      heightOfItemBottomContentMap.putAll(prunedFromDeletedStepsMap)
    }
  }

  return remember(density) {
    LastItemHeightAdjustingState(
      heightOfItemBottomContentMap = heightOfItemBottomContentMap,
      density = density,
      onMinHeightForFullScreenItemChanged = { minHeightForFullScreenItem = it },
      preferredMinHeightForFullScreenItemProvider = { preferredMinHeightForFullScreenItem },
    )
  }
}

internal class LastItemHeightAdjustingState(
  private val heightOfItemBottomContentMap: SnapshotStateMap<StepId, IntSize>,
  private val density: Density,
  private val onMinHeightForFullScreenItemChanged: (Dp) -> Unit,
  private val preferredMinHeightForFullScreenItemProvider: () -> Dp,
) {
  val preferredMinHeightForFullScreenItem: Dp
    get() = preferredMinHeightForFullScreenItemProvider()

  fun onContainerSizeChanged(size: IntSize) {
    with(density) { onMinHeightForFullScreenItemChanged(size.height.toDp()) }
  }

  fun onItemHeightChanged(stepId: StepId, size: IntSize) {
    heightOfItemBottomContentMap[stepId] = size
  }
}
