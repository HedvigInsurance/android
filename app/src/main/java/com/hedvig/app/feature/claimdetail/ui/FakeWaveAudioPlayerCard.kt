package com.hedvig.app.feature.claimdetail.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.google.android.material.math.MathUtils.lerp
import com.hedvig.app.R
import com.hedvig.app.service.audioplayer.AudioPlayerState
import com.hedvig.app.service.audioplayer.AudioPlayerState.Ready.ReadyState
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.onWarning
import com.hedvig.app.ui.compose.theme.warning
import com.hedvig.app.util.ProgressPercentage
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.random.Random

fun interface WaveInteraction {
    /**
     * [horizontalProgressPercentage] is a value that shows where in the horizontal spectrum the wave was interacted
     * with.
     * Ranges from 0.0f when interacted on the far left to 1.0f on the far right.
     */
    fun onInteraction(horizontalProgressPercentage: ProgressPercentage)
}

@Composable
fun FakeWaveAudioPlayerCard(
    audioPlayerState: AudioPlayerState,
    startPlaying: () -> Unit,
    pause: () -> Unit,
    retryLoadingAudio: () -> Unit,
    waveInteraction: WaveInteraction,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        cardColor(audioPlayerState),
        animationSpec = tween(500)
    )
    val contentColor by animateColorAsState(
        cardContentColor(audioPlayerState),
        animationSpec = tween(500)
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = color,
        contentColor = contentColor,
    ) {
        FakeWaveAudioPlayerCardContent(
            audioPlayerState = audioPlayerState,
            startPlaying = startPlaying,
            pause = pause,
            retryLoadingAudio = retryLoadingAudio,
            waveInteraction = waveInteraction,
        )
    }
}

@Composable
private fun cardColor(audioPlayerState: AudioPlayerState): Color = when {
    audioPlayerState.isFailed -> MaterialTheme.colors.warning
    isSystemInDarkTheme() -> MaterialTheme.colors.secondary
    else -> colorResource(R.color.lavender_200)
}

@Composable
private fun cardContentColor(audioPlayerState: AudioPlayerState): Color = when {
    audioPlayerState.isFailed -> MaterialTheme.colors.onWarning
    isSystemInDarkTheme() -> colorResource(R.color.lavender_200)
    else -> MaterialTheme.colors.secondary
}

@Composable
private fun FakeWaveAudioPlayerCardContent(
    audioPlayerState: AudioPlayerState,
    startPlaying: () -> Unit,
    pause: () -> Unit,
    retryLoadingAudio: () -> Unit,
    waveInteraction: WaveInteraction,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.animateContentSize(animationSpec = spring(stiffness = 400f)),
    ) {
        when (audioPlayerState) {
            AudioPlayerState.Failed -> {
                FailedAudioPlayerCard(tryAgain = retryLoadingAudio)
            }
            AudioPlayerState.Preparing,
            is AudioPlayerState.Ready,
            -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(64.dp)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow))
                ) {
                    AudioPlayerActionOrLoadingIcon(
                        audioPlayerState = audioPlayerState,
                        startPlaying = startPlaying,
                        pause = pause,
                        modifier = Modifier.size(48.dp),
                    )
                    if (audioPlayerState is AudioPlayerState.Ready) {
                        FakeAudioWaves(
                            progressPercentage = audioPlayerState.progressPercentage,
                            playedColor = if (isSystemInDarkTheme()) {
                                colorResource(R.color.hedvig_off_white)
                            } else {
                                colorResource(R.color.lavender_600)
                            },
                            notPlayedColor = LocalContentColor.current,
                            waveInteraction = waveInteraction,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioPlayerActionOrLoadingIcon(
    audioPlayerState: AudioPlayerState, // AudioPlayerState.Perparing | AudioPlayerState.Ready
    startPlaying: () -> Unit,
    pause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    require(audioPlayerState !is AudioPlayerState.Failed)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (audioPlayerState) {
            is AudioPlayerState.Preparing -> {
                CircularProgressIndicator(
                    color = LocalContentColor.current,
                    modifier = Modifier.size(24.dp)
                )
            }
            is AudioPlayerState.Ready -> {
                IconButton(
                    onClick = when (audioPlayerState.readyState) {
                        is ReadyState.Playing -> pause
                        else -> startPlaying
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            when (audioPlayerState.readyState) {
                                ReadyState.Playing -> R.drawable.ic_pause
                                ReadyState.Seeking -> R.drawable.ic_pause
                                else -> R.drawable.ic_play
                            }
                        ),
                        contentDescription = null,
                    )
                }
            }
            is AudioPlayerState.Failed -> throw IllegalArgumentException("Impossible")
        }
    }
}

private const val numberOfWaves = 50
private const val waveWidthPercentOfSpaceAvailable = 0.5f

@Composable
private fun FakeAudioWaves(
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
        val maxHeightFraction = lerp(
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

@Preview("Interactive Preview")
@Composable
fun FakeWaveAudioPlayerCardAnimationPreview() {
    HedvigTheme {
        Surface(color = MaterialTheme.colors.background) {
            var audioPlayerState: AudioPlayerState by remember {
                mutableStateOf(AudioPlayerState.Preparing)
            }
            FakeWaveAudioPlayerCard(
                audioPlayerState = audioPlayerState,
                {},
                {},
                {},
                {},
                modifier = Modifier.clickable {
                    audioPlayerState = when (audioPlayerState) {
                        AudioPlayerState.Failed -> AudioPlayerState.Preparing
                        AudioPlayerState.Preparing -> AudioPlayerState.Ready.done()
                        is AudioPlayerState.Ready -> AudioPlayerState.Failed
                    }
                }
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FakeWaveAudioPlayerCardPreview(
    @PreviewParameter(AudioPlayerStateProvider::class) audioPlayerState: AudioPlayerState,
) {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            FakeWaveAudioPlayerCard(audioPlayerState, {}, {}, {}, {})
        }
    }
}

class AudioPlayerStateProvider : CollectionPreviewParameterProvider<AudioPlayerState>(
    listOf(
        AudioPlayerState.Failed,
        AudioPlayerState.Ready(ReadyState.Paused, ProgressPercentage(0.4f)),
        AudioPlayerState.Ready(ReadyState.Playing, ProgressPercentage(0.6f)),
    )
)
