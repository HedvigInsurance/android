package com.hedvig.app.feature.claimdetail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.SignedAudioUrl
import com.hedvig.android.audio.player.state.AudioPlayerState
import com.hedvig.android.audio.player.state.rememberAudioPlayer

@Composable
fun ClaimDetailHedvigAudioPlayerItem(
  onPlayClick: () -> Unit,
  signedAudioUrl: SignedAudioUrl,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    val audioPlayer = rememberAudioPlayer(signedAudioUrl = signedAudioUrl)
    HedvigAudioPlayer(audioPlayer = audioPlayer, onPlayClick = onPlayClick)
    Spacer(Modifier.height(8.dp))
    val audioPlayerState by audioPlayer.audioPlayerState.collectAsStateWithLifecycle()
    AnimatedVisibility(visible = audioPlayerState !is AudioPlayerState.Failed) {
      CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
          text = stringResource(hedvig.resources.R.string.claim_status_files_claim_audio_footer),
          style = MaterialTheme.typography.caption,
        )
      }
    }
  }
}
