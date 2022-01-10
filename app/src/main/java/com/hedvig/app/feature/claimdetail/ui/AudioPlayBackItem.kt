package com.hedvig.app.feature.claimdetail.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.service.audioplayer.AudioPlayer
import com.hedvig.app.service.audioplayer.AudioPlayerImpl
import com.hedvig.app.ui.compose.rememberCloseable

@Composable
fun AudioPlayBackItem(
    signedAudioUrl: String,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val audioPlayer: AudioPlayer = rememberCloseable(signedAudioUrl, lifecycleOwner) {
        AudioPlayerImpl(signedAudioUrl, lifecycleOwner)
    }

    Column(modifier) {
        val audioPlayerState by audioPlayer.audioPlayerState.collectAsState()
        FakeWaveAudioPlayerCard(
            audioPlayerState,
            audioPlayer::startPlayer,
            audioPlayer::pausePlayer,
            waveInteraction = { horizontalPercentage: Float ->
                if (audioPlayerState.isSeekable) {
                    audioPlayer.seekTo(horizontalPercentage)
                } else {
                    audioPlayer.startPlayer()
                }
            },
        )
        Spacer(Modifier.height(8.dp))
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = stringResource(R.string.claim_status_files_claim_audio_footer),
                style = MaterialTheme.typography.caption,
            )
        }
    }
}
