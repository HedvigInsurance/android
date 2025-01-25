package com.hedvig.android.design.system.hedvig.videoplayer


import androidx.annotation.IntDef
import android.view.Surface
import androidx.compose.foundation.AndroidEmbeddedExternalSurface
import androidx.compose.foundation.AndroidExternalSurface
import androidx.compose.foundation.AndroidExternalSurfaceScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun VideoPlayerExample(uri: String) {
  Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val context = LocalContext.current
    val exoPlayer = remember {
      ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(uri))
        prepare()
        playWhenReady = true
        repeatMode = Player.REPEAT_MODE_OFF
      }
    }
    VideoPlayerSurface(
      player = exoPlayer,
      surfaceType = SURFACE_TYPE_SURFACE_VIEW,
      modifier = Modifier.align(Alignment.CenterHorizontally),
    )
  }
}

@Composable
fun VideoPlayerSurface(player: Player, surfaceType: @SurfaceType Int, modifier: Modifier = Modifier) {
  val onSurfaceCreated: (Surface) -> Unit = { surface -> player.setVideoSurface(surface) }
  val onSurfaceDestroyed: () -> Unit = { player.setVideoSurface(null) }
  val onSurfaceInitialized: AndroidExternalSurfaceScope.() -> Unit = {
    onSurface { surface, _, _ ->
      onSurfaceCreated(surface)
      surface.onDestroyed { onSurfaceDestroyed() }
    }
  }

  when (surfaceType) {
    SURFACE_TYPE_SURFACE_VIEW ->
      AndroidExternalSurface(modifier = modifier, onInit = onSurfaceInitialized)
    SURFACE_TYPE_TEXTURE_VIEW ->
      AndroidEmbeddedExternalSurface(modifier = modifier, onInit = onSurfaceInitialized)
    else -> throw IllegalArgumentException("Unrecognized surface type: $surfaceType")
  }
}


@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.TYPE_PARAMETER)
@IntDef(SURFACE_TYPE_SURFACE_VIEW, SURFACE_TYPE_TEXTURE_VIEW)
annotation class SurfaceType

/** Surface type equivalent to [SurfaceView] . */
const val SURFACE_TYPE_SURFACE_VIEW = 1
/** Surface type equivalent to [TextureView]. */
const val SURFACE_TYPE_TEXTURE_VIEW = 2
