package com.hedvig.android.feature.home.yearinreview.ui

import android.graphics.Color
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

@OptIn(UnstableApi::class)
@Composable
internal actual fun YearInReviewVideo(url: String, isPaused: Boolean, modifier: Modifier) {
  val context = LocalContext.current
  val player = remember(context, url) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(url))
      repeatMode = Player.REPEAT_MODE_ONE
      volume = 0f
      prepare()
    }
  }
  DisposableEffect(player) {
    val listener = object : Player.Listener {
      override fun onPlayerError(error: PlaybackException) {
        logcat(LogPriority.WARN, error) { "Year in review video failed to play" }
      }
    }
    player.addListener(listener)
    onDispose {
      player.removeListener(listener)
      player.release()
    }
  }
  val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
  val shouldPlay = !isPaused && lifecycleState.isAtLeast(Lifecycle.State.STARTED)
  LaunchedEffect(player, shouldPlay) {
    player.playWhenReady = shouldPlay
  }
  AndroidView(
    factory = { viewContext ->
      PlayerView(viewContext).apply {
        this.player = player
        useController = false
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        // Leaves the placeholder behind the player visible until the first frame is drawn.
        setShutterBackgroundColor(Color.TRANSPARENT)
      }
    },
    modifier = modifier,
  )
}
