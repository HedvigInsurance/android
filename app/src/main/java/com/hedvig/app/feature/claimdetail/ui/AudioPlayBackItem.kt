package com.hedvig.app.feature.claimdetail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.feature.claimdetail.model.SignedAudioUrl
import com.hedvig.app.service.audioplayer.AudioPlayer
import com.hedvig.app.service.audioplayer.AudioPlayerImpl
import com.hedvig.app.service.audioplayer.AudioPlayerState

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AudioPlayBackItem(
    signedAudioUrl: SignedAudioUrl,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val audioPlayer: AudioPlayer = remember(signedAudioUrl, lifecycleOwner) {
        AudioPlayerImpl(signedAudioUrl, lifecycleOwner)
    }
    DisposableEffect(audioPlayer) {
        audioPlayer.initialize()
        onDispose {
            audioPlayer.close()
        }
    }

    Column(modifier) {
        val audioPlayerState by audioPlayer.audioPlayerState.collectAsState()
        FakeWaveAudioPlayerCard(
            audioPlayerState = audioPlayerState,
            startPlaying = audioPlayer::startPlayer,
            pause = audioPlayer::pausePlayer,
            retryLoadingAudio = audioPlayer::retryLoadingAudio,
            waveInteraction = audioPlayer::seekTo,
        )
        Spacer(Modifier.height(8.dp))
        AnimatedVisibility(visible = audioPlayerState !is AudioPlayerState.Failed) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = stringResource(R.string.claim_status_files_claim_audio_footer),
                    style = MaterialTheme.typography.caption,
                )
            }
        }
    }
}
