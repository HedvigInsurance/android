package com.hedvig.app.feature.claimdetail.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.math.MathUtils
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.ProgressPercentage
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.random.Random

private const val numberOfWaves = 50
private const val waveWidthPercentOfSpaceAvailable = 0.5f

@Composable
fun FakeAudioWaves(
    progressPercentage: ProgressPercentage,
    playedColor: Color,
    notPlayedColor: Color,
    waveInteraction: WaveInteraction,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        val updatedWaveInteraction by rememberUpdatedState(waveInteraction)
        val waveWidth = remember(maxWidth) {
            (maxWidth / numberOfWaves.toFloat()) * waveWidthPercentOfSpaceAvailable
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        updatedWaveInteraction.onInteraction(
                            ProgressPercentage.of(current = offset.x.toDp(), target = maxWidth)
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change: PointerInputChange, dragAmount: Float ->
                        // Do not trigger on minuscule movements
                        if (dragAmount.absoluteValue < 1f) return@detectHorizontalDragGestures
                        updatedWaveInteraction.onInteraction(
                            ProgressPercentage.of(current = change.position.x.toDp(), target = maxWidth)
                        )
                    }
                }
        ) {
            repeat(numberOfWaves) { waveIndex ->
                FakeAudioWave(
                    progressPercentage = progressPercentage,
                    numberOfWaves = numberOfWaves,
                    waveIndex = waveIndex,
                    playedColor = playedColor,
                    notPlayedColor = notPlayedColor,
                    modifier = Modifier.width(waveWidth)
                )
            }
        }
    }
}

private const val minWaveHeightFraction = 0.1f
private const val maxWaveHeightFractionForSideWaves = 0.1f
private const val maxWaveHeightFraction = 1.0f

@Composable
private fun FakeAudioWave(
    progressPercentage: ProgressPercentage,
    @Suppress("SameParameterValue")
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
        val maxHeightFraction = MathUtils.lerp(
            maxWaveHeightFractionForSideWaves,
            maxWaveHeightFraction,
            percentageToCenterPoint
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
    Surface(
        shape = CircleShape,
        color = if (hasPlayedThisWave) playedColor else notPlayedColor,
        modifier = modifier.fillMaxHeight(fraction = height),
    ) {}
}

@Preview
@Composable
fun FakeAudioWavesPreview() {
    HedvigTheme {
        Surface(color = MaterialTheme.colors.background) {
            FakeAudioWaves(
                ProgressPercentage(0.2f),
                Color.Black,
                Color.Black.copy(alpha = 0.12f),
                {},
            )
        }
    }
}
