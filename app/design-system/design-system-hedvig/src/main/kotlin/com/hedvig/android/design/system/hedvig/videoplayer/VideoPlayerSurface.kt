package com.hedvig.android.design.system.hedvig.videoplayer


import androidx.annotation.IntDef
import android.view.Surface
import androidx.annotation.OptIn
import androidx.compose.foundation.AndroidEmbeddedExternalSurface
import androidx.compose.foundation.AndroidExternalSurface
import androidx.compose.foundation.AndroidExternalSurfaceScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ControllerVisibilityListener
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Composable
fun VideoPlayerExample( exoPlayer: ExoPlayer) {
  Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Media3AndroidView(exoPlayer)
  }
}

@OptIn(UnstableApi::class)
@Composable
fun Media3AndroidView(player: ExoPlayer?) {
  val shape = HedvigTheme.shapes.cornerLarge
  val backgroundColor = HedvigTheme.colorScheme.surfacePrimary
  DisposableEffect(
    AndroidView(
      modifier = Modifier
        .height(250.dp)
        .clip(shape)
        .background(
          color = backgroundColor,
          shape = shape,
        )
        .fillMaxWidth(),
      factory = { context ->
        PlayerView(context).apply {
          this.player = player
          this.controllerAutoShow = false
          this.controllerHideOnTouch = true
          this.setShowNextButton(false)
          this.setShowPreviousButton(false)
          this.setShowFastForwardButton(false)
          this.setShowRewindButton(false)
          player?.addListener( object: Player.Listener {
            fun onIsPlayingChanged(){

            }
          } )
        }
      },
      update = { playerView ->
        playerView.player = player
      },
    ),
  ) {
    onDispose {
      player?.release()
    }
  }
}
