package com.hedvig.android.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.rememberPreviewSimpleCache
import com.hedvig.android.design.system.hedvig.videoplayer.Media
import com.hedvig.android.design.system.hedvig.videoplayer.MediaState
import com.hedvig.android.design.system.hedvig.videoplayer.ResizeMode
import com.hedvig.android.design.system.hedvig.videoplayer.ShowBuffering
import com.hedvig.android.design.system.hedvig.videoplayer.SimpleVideoController
import com.hedvig.android.design.system.hedvig.videoplayer.SurfaceType
import com.hedvig.android.design.system.hedvig.videoplayer.rememberControllerState
import com.hedvig.android.design.system.hedvig.videoplayer.rememberMediaState

internal actual typealias ChatVideoCache = Cache

internal actual typealias ChatSimpleVideoCache = SimpleCache

@Stable
internal actual class ChatVideoPlayers(
  val mediaStatesWithPlayers: SnapshotStateMap<String, MediaState>,
) {
  actual fun releaseAll() {
    mediaStatesWithPlayers.values.forEach { it.player?.release() }
    mediaStatesWithPlayers.clear() // todo: sure?
  }
}

@Composable
internal actual fun rememberChatVideoPlayers(): ChatVideoPlayers = remember {
  ChatVideoPlayers(mutableStateMapOf<String, MediaState>())
}

@Composable
internal actual fun ChatVideoMessage(
  videoPlayers: ChatVideoPlayers,
  messageId: String,
  url: String,
  videoCache: ChatVideoCache,
  openUrl: (String) -> Unit,
  onGoFullWidth: () -> Unit,
  onGoDefaultWidth: () -> Unit,
  showingFullWidth: Boolean,
  modifier: Modifier,
) {
  val mediaState = videoPlayers.mediaStatesWithPlayers.getOrPut(
    messageId,
    {
      videoPlayerMediaState(videoCache, url)
    },
  )
  VideoMessage(
    state = mediaState,
    onGoFullWidth = onGoFullWidth,
    onGoDefaultWidth = onGoDefaultWidth,
    showingFullWidth = showingFullWidth,
    modifier = modifier,
  )
}

@Composable
internal actual fun rememberPreviewChatVideoCache(): ChatVideoCache = rememberPreviewSimpleCache()

@Composable
private fun VideoMessage(
  state: MediaState,
  onGoFullWidth: () -> Unit,
  onGoDefaultWidth: () -> Unit,
  showingFullWidth: Boolean,
  modifier: Modifier = Modifier,
) {
  LocalLifecycleOwner.current.lifecycle.addObserver(
    object : LifecycleEventObserver {
      override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
          Lifecycle.Event.ON_STOP -> {
            state.player?.pause()
          }

          else -> {}
        }
      }
    },
  )
  val height = if (showingFullWidth) 350.dp else 220.dp
  Media(
    state = state,
    modifier = modifier
      .height(height)
      .clip(HedvigTheme.shapes.cornerLarge)
      .background(Color.Black),
    surfaceType = SurfaceType.TextureView,
    resizeMode = ResizeMode.Fit,
    keepContentOnPlayerReset = true,
    showBuffering = ShowBuffering.Always,
    buffering = {
      Box(Modifier.fillMaxSize(), Alignment.Center) {
        HedvigCircularProgressIndicator()
      }
    },
  ) { state ->
    val controllerState = rememberControllerState(state)
    SimpleVideoController(
      mediaState = state,
      controllerState = controllerState,
      onGoFullWidth = onGoFullWidth,
      onGoDefaultWidth = onGoDefaultWidth,
      showingFullWidth = showingFullWidth,
      modifier = Modifier
        .fillMaxSize()
        .clip(HedvigTheme.shapes.cornerLarge),
    )
  }
}

@Composable
private fun videoPlayerMediaState(cache: Cache, uri: String): MediaState {
  val context = LocalContext.current
  val exoPlayer = remember(cache, uri, context) {
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
    val cacheDataSourceFactory = CacheDataSource.Factory().setCache(cache)
      .setUpstreamDataSourceFactory(httpDataSourceFactory)
      .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    val mediaSourceFactory = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
    ExoPlayer
      .Builder(context)
      .setMediaSourceFactory(
        mediaSourceFactory,
      )
      .setRenderersFactory(
        DefaultRenderersFactory(context)
          .setEnableDecoderFallback(true),
      )
      .build().apply {
        prepare()
        playWhenReady = false
        repeatMode = Player.REPEAT_MODE_OFF
        setMediaItem(MediaItem.fromUri(uri))
      }
  }
  return rememberMediaState(player = exoPlayer)
}
