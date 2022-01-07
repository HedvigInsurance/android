package com.hedvig.app.feature.claimdetail.ui

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.material.math.MathUtils.lerp
import com.hedvig.app.R
import com.hedvig.app.feature.claimdetail.data.AudioPlayerState
import com.hedvig.app.ui.compose.theme.HedvigTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun FakeWaveAudioPlayerCard(
    audioPlayerState: AudioPlayerState,
    startPlaying: () -> Unit,
    pause: () -> Unit,
    seekTo: (percentage: Float) -> Unit,
) {
    Surface(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSystemInDarkTheme()) {
            MaterialTheme.colors.secondary
        } else {
            colorResource(R.color.lavender_200)
        },
        contentColor = if (isSystemInDarkTheme()) {
            colorResource(R.color.lavender_200)
        } else {
            MaterialTheme.colors.secondary
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            when (audioPlayerState) {
                AudioPlayerState.Preparing -> {
                    CircularProgressIndicator(
                        color = LocalContentColor.current,
                        modifier = Modifier.size(24.dp)
                    )
                }
                is AudioPlayerState.Ready -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = when (audioPlayerState.readyState) {
                                is AudioPlayerState.Ready.ReadyState.Playing -> pause
                                else -> startPlaying
                            }
                        ) {
                            Icon(
                                painter = painterResource(
                                    when (audioPlayerState.readyState) {
                                        AudioPlayerState.Ready.ReadyState.Playing -> R.drawable.ic_pause
                                        else -> R.drawable.ic_play
                                    }
                                ),
                                contentDescription = null,
                            )
                        }
                        FakeAudioWaves(
                            progress = audioPlayerState.progress,
                            isPlaying = audioPlayerState.readyState is AudioPlayerState.Ready.ReadyState.Playing,
                            playedColor = LocalContentColor.current,
                            notPlayedColor = MaterialTheme.colors.primary.copy(alpha = 0.12f),
                            seekTo = seekTo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                AudioPlayerState.Failed -> {
                    Text(
                        text = stringResource(R.string.CHAT_AUDIO__PLAYBACK_FAILED),
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colors.onSecondary,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}

private const val numberOfWaves = 50

@Composable
private fun FakeAudioWaves(
    progress: Float,
    isPlaying: Boolean,
    playedColor: Color,
    notPlayedColor: Color,
    seekTo: (percentageOfWidth: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val updatedSeekTo by rememberUpdatedState(seekTo)
        val waveWidth = (maxWidth / numberOfWaves.toFloat()) * (10f / 14f)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    val sendXPositionPercentageComparedToMaxWidth = { xPosition: Dp ->
                        val percentageComparedToMaxWidth = xPosition / maxWidth
                        updatedSeekTo(percentageComparedToMaxWidth)
                    }
                    coroutineScope {
                        launch {
                            detectTapGestures { offset ->
                                sendXPositionPercentageComparedToMaxWidth(offset.x.toDp())
                            }
                        }
                        launch {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    sendXPositionPercentageComparedToMaxWidth(offset.x.toDp())
                                }
                            ) { change: PointerInputChange, _ ->
                                sendXPositionPercentageComparedToMaxWidth(change.position.x.toDp())
                            }
                        }
                    }
                }
        ) {
            repeat(numberOfWaves) { waveIndex ->
                FakeAudioWave(
                    progress = progress,
                    numberOfWaves = numberOfWaves,
                    waveIndex = waveIndex,
                    notPlayedColor = notPlayedColor,
                    playedColor = playedColor,
                    isPlaying = isPlaying,
                    modifier = Modifier.width(waveWidth)
                )
            }
        }
    }
}

private const val minWaveHeightFraction = 0.2f
private const val maxWaveHeightFractionForSideWaves = 0.5f
private const val maxWaveHeightFraction = 1.0f

@Composable
private fun FakeAudioWave(
    progress: Float,
    @Suppress("SameParameterValue")
    numberOfWaves: Int,
    waveIndex: Int,
    notPlayedColor: Color,
    playedColor: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val heightFractionAnimation = remember { Animatable(minWaveHeightFraction) }
    var expanding by remember { mutableStateOf(true) }
    LaunchedEffect(isPlaying, expanding) {
        if (!isPlaying) {
            heightFractionAnimation.animateTo(minWaveHeightFraction)
            expanding = true
            return@LaunchedEffect
        }
        if (expanding) {
            val wavePosition = waveIndex + 1
            val centerPoint = numberOfWaves / 2
            val distanceFromCenterPoint = abs(centerPoint - wavePosition)
            val percentageToCenterOfTheWaves = ((centerPoint - distanceFromCenterPoint).toFloat() / centerPoint)
            val maxHeightFraction = lerp(
                maxWaveHeightFractionForSideWaves,
                maxWaveHeightFraction,
                percentageToCenterOfTheWaves
            )
            heightFractionAnimation.animateTo(maxHeightFraction)
        } else {
            heightFractionAnimation.animateTo(minWaveHeightFraction)
        }
        expanding = !expanding
    }
    val hasPlayedThisWave = progress * numberOfWaves > waveIndex
    Surface(
        shape = CircleShape,
        color = if (hasPlayedThisWave) playedColor else notPlayedColor,
        modifier = modifier.fillMaxHeight(fraction = heightFractionAnimation.value),
    ) {}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FakeWaveAudioPlayerItemPreview(
    @PreviewParameter(AudioPlayerStateProvider::class) audioPlayerState: AudioPlayerState,
) {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            FakeWaveAudioPlayerCard(audioPlayerState, {}, {}, {})
        }
    }
}

class AudioPlayerStateProvider : CollectionPreviewParameterProvider<AudioPlayerState>(
    listOf(
        AudioPlayerState.Preparing,
        AudioPlayerState.Failed,
        AudioPlayerState.Ready.notStarted(),
        AudioPlayerState.Ready.paused(0.4f),
        AudioPlayerState.Ready.done(),
        AudioPlayerState.Ready.playing(0.6f),
        AudioPlayerState.Ready.seeking(0.1f),
    )
)
