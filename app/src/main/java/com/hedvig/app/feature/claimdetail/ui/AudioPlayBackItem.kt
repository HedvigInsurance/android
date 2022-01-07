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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.feature.claimdetail.data.AudioPlayer

@Composable
fun AudioPlayBackItem(
    signedAudioUrl: String,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val audioPlayer: AudioPlayer = remember(signedAudioUrl, coroutineScope) {
        AudioPlayer(signedAudioUrl, coroutineScope)
    }
    val audioPlayerState by audioPlayer.audioPlayerState.collectAsState()

    DisposableEffect(audioPlayer) {
        onDispose {
            audioPlayer.cleanupMediaPlayer()
        }
    }

    Column(modifier) {
        FakeWaveAudioPlayerCard(
            audioPlayerState,
            audioPlayer::startPlayer,
            audioPlayer::pausePlayer,
            audioPlayer::seekTo,
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
