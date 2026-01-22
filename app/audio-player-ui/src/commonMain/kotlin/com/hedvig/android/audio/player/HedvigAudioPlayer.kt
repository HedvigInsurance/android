package com.hedvig.android.audio.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.audio.player.internal.FakeWaveAudioPlayerCard
import com.hedvig.android.audio.player.internal.waveWidthPercentOfSpaceAvailable
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.audio.player.data.AudioPlayer
import kotlin.math.roundToInt

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
