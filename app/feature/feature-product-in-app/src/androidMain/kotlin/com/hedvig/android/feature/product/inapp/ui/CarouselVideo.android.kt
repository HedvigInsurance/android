package com.hedvig.android.feature.product.inapp.ui

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

@Composable
internal actual fun CarouselVideo(url: String, loop: Boolean, modifier: Modifier) {
  val context = LocalContext.current
  val player = remember(context, url, loop) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(url))
      repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
      volume = 0f
      playWhenReady = true
      prepare()
    }
  }
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(player, lifecycleOwner) {
    val errorListener = object : Player.Listener {
      override fun onPlayerError(error: PlaybackException) {
        logcat(LogPriority.WARN, error) { "Cross-sell carousel video failed to play" }
      }
    }
    val lifecycleObserver = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> {
          player.play()
        }

        Lifecycle.Event.ON_PAUSE -> {
          player.pause()
        }

        else -> {}
      }
    }
    player.addListener(errorListener)
    lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
      player.removeListener(errorListener)
      player.release()
    }
  }
  AndroidView(
    factory = { viewContext ->
      PlayerView(viewContext).apply {
        useController = false
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        setShutterBackgroundColor(Color.TRANSPARENT)
      }
    },
    update = { playerView -> playerView.player = player },
    modifier = modifier,
  )
}
