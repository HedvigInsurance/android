package com.hedvig.feature.claim.chat.audiorecorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.feature.claim.chat.audiorecorder.audioplayer.AudioPlayer
import com.hedvig.feature.claim.chat.audiorecorder.audioplayer.audiowaves.FakeWaveAudioPlayerCard

/**
 * https://www.figma.com/file/e0lnWjMtp8x5Typlt5b33i/Claim-status-Android?node-id=1224%3A82&t=RgoySHgQiM6RyYNI-1
 */
@Composable
fun HedvigAudioPlayer(audioPlayer: AudioPlayer, modifier: Modifier = Modifier, onPlayClick: () -> Unit = {}) {
  val audioPlayerState by audioPlayer.audioPlayerState.collectAsStateWithLifecycle()
  FakeWaveAudioPlayerCard(
    audioPlayerState = audioPlayerState,
    startPlaying = {
      onPlayClick()
      audioPlayer.startPlayer()
    },
    pause = audioPlayer::pausePlayer,
    retryLoadingAudio = audioPlayer::retryLoadingAudio,
    waveInteraction = audioPlayer::seekTo,
    modifier = modifier,
  )
}
