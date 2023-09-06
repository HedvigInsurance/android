package com.hedvig.android.feature.home.claimdetail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.SignedAudioUrl
import com.hedvig.android.audio.player.state.AudioPlayerState
import com.hedvig.android.audio.player.state.PlayableAudioSource
import com.hedvig.android.audio.player.state.rememberAudioPlayer

@Composable
internal fun ClaimDetailHedvigAudioPlayerItem(
  onPlayClick: () -> Unit,
  signedAudioUrl: SignedAudioUrl,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    val audioPlayer = rememberAudioPlayer(playableAudioSource = PlayableAudioSource.RemoteUrl(signedAudioUrl))
    HedvigAudioPlayer(audioPlayer = audioPlayer, onPlayClick = onPlayClick)
    Spacer(Modifier.height(8.dp))
    val audioPlayerState by audioPlayer.audioPlayerState.collectAsStateWithLifecycle()
    AnimatedVisibility(visible = audioPlayerState !is AudioPlayerState.Failed) {
      Text(
        text = stringResource(hedvig.resources.R.string.claim_status_files_claim_audio_footer),
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
      )
    }
  }
}
