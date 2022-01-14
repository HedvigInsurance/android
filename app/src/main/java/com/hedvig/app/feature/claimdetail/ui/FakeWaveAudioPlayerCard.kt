package com.hedvig.app.feature.claimdetail.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.service.audioplayer.AudioPlayerState
import com.hedvig.app.service.audioplayer.AudioPlayerState.Ready.ReadyState
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.onWarning
import com.hedvig.app.ui.compose.theme.warning
import com.hedvig.app.util.ProgressPercentage

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
        cardColorForState(audioPlayerState),
        animationSpec = tween(500)
    )
    val contentColor by animateColorAsState(
        cardContentColorForState(audioPlayerState),
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
private fun cardColorForState(audioPlayerState: AudioPlayerState): Color = when {
    audioPlayerState.isFailed -> MaterialTheme.colors.warning
    isSystemInDarkTheme() -> MaterialTheme.colors.secondary
    else -> colorResource(R.color.lavender_200)
}

@Composable
private fun cardContentColorForState(audioPlayerState: AudioPlayerState): Color = when {
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
    audioPlayerState: AudioPlayerState, // AudioPlayerState.Preparing | AudioPlayerState.Ready
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
