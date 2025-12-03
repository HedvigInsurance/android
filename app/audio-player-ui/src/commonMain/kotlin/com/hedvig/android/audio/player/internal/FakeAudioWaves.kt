@file:Suppress("ConstPropertyName")

package com.hedvig.android.audio.player.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.audio.player.data.ProgressPercentage
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random

private const val waveWidthPercentOfSpaceAvailable = 0.5f

@Composable
internal fun FakeAudioWaves(
  progressPercentage: ProgressPercentage,
  playedColor: Color,
  notPlayedColor: Color,
  waveInteraction: WaveInteraction,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(modifier) {
    val updatedWaveInteraction by rememberUpdatedState(waveInteraction)
    val numberOfWaves = remember(maxWidth) {
      (maxWidth / 5f).value.roundToInt()
    }
    val waveWidth = remember(maxWidth) {
      (maxWidth / numberOfWaves.toFloat()) * waveWidthPercentOfSpaceAvailable
    }
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .height(maxHeight)
        .pointerInput(Unit) {
          detectTapGestures { offset ->
            updatedWaveInteraction.onInteraction(
              ProgressPercentage.of(current = offset.x.toDp().value, target = maxWidth.value),
            )
          }
        }
        .pointerInput(Unit) {
          detectHorizontalDragGestures { change: PointerInputChange, dragAmount: Float ->
            // Do not trigger on minuscule movements
            if (dragAmount.absoluteValue < 1f) return@detectHorizontalDragGestures
            updatedWaveInteraction.onInteraction(
              ProgressPercentage.of(current = change.position.x.toDp().value, target = maxWidth.value),
            )
          }
        },
    ) {
      repeat(numberOfWaves) { waveIndex ->
        FakeAudioWavePill(
          progressPercentage = progressPercentage,
          numberOfWaves = numberOfWaves,
          waveIndex = waveIndex,
          playedColor = playedColor,
          notPlayedColor = notPlayedColor,
          modifier = Modifier.width(waveWidth),
        )
      }
    }
  }
}

private const val minWaveHeightFraction = 0.1f
private const val maxWaveHeightFractionForSideWaves = 0.1f
private const val maxWaveHeightFraction = 1.0f

@Composable
private fun FakeAudioWavePill(
  progressPercentage: ProgressPercentage,
  numberOfWaves: Int,
  waveIndex: Int,
  playedColor: Color,
  notPlayedColor: Color,
  modifier: Modifier = Modifier,
) {
  val height = remember(waveIndex, numberOfWaves) {
    val wavePosition = waveIndex + 1
    val centerPoint = numberOfWaves / 2
    val distanceFromCenterPoint = abs(centerPoint - wavePosition)
    val percentageToCenterPoint = ((centerPoint - distanceFromCenterPoint).toFloat() / centerPoint)
    val maxHeightFraction = lerp(
      maxWaveHeightFractionForSideWaves,
      maxWaveHeightFraction,
      percentageToCenterPoint,
    )
    if (maxHeightFraction <= minWaveHeightFraction) {
      maxHeightFraction
    } else {
      Random.nextDouble(minWaveHeightFraction.toDouble(), maxHeightFraction.toDouble()).toFloat()
    }
  }
  val hasPlayedThisWave = remember(progressPercentage, numberOfWaves, waveIndex) {
    progressPercentage.value * numberOfWaves > waveIndex
  }
  Box(
    modifier = modifier.fillMaxHeight(fraction = height)
      .clip(CircleShape)
      .background(if (hasPlayedThisWave) playedColor else notPlayedColor),
  )
}

@HedvigPreview
@Composable
private fun PreviewFakeAudioWaves() {
  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.surfacePrimary,
      modifier = Modifier.height(150.dp),
    ) {
      FakeAudioWaves(
        ProgressPercentage(0.2f),
        LocalContentColor.current,
        LocalContentColor.current.copy(0.38f).compositeOver(HedvigTheme.colorScheme.surfacePrimary),
        {},
      )
    }
  }
}
