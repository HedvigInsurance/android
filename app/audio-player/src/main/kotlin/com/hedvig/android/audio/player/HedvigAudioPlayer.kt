package com.hedvig.android.audio.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.audio.player.internal.FakeWaveAudioPlayerCard
import com.example.audio_player_data.AudioPlayer

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
