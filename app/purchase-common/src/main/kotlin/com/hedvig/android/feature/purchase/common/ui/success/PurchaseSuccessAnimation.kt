package com.hedvig.android.feature.purchase.common.ui.success

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.view.TextureView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.purchase.common.R
import kotlinx.coroutines.delay

private enum class Phase { Video, Message, Content }

private const val VIDEO_FADE_OUT_MS = 300
private const val MESSAGE_FADE_IN_MS = 500
private const val MESSAGE_HOLD_MS = 1000L
private const val OVERLAY_EXIT_MS = 500
private const val CONTENT_ENTER_MS = 500

@Composable
fun PurchaseSuccessAnimation(message: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  val animationsEnabled = rememberAnimationsEnabled()
  var phase by rememberSaveable {
    mutableStateOf(if (animationsEnabled) Phase.Video else Phase.Content)
  }

  LaunchedEffect(phase) {
    if (phase == Phase.Message) {
      delay(MESSAGE_HOLD_MS)
      phase = Phase.Content
    }
  }

  Box(modifier.fillMaxSize()) {
    AnimatedVisibility(
      visible = phase == Phase.Content,
      enter = fadeIn(tween(CONTENT_ENTER_MS)) +
        slideInVertically(tween(CONTENT_ENTER_MS)) { it / 12 },
    ) {
      content()
    }

    AnimatedVisibility(
      visible = phase != Phase.Content,
      exit = fadeOut(tween(OVERLAY_EXIT_MS)) +
        slideOutVertically(tween(OVERLAY_EXIT_MS)) { -it / 12 },
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(HedvigTheme.colorScheme.backgroundPrimary),
      ) {
        AnimatedContent(
          modifier = Modifier.fillMaxSize(),
          targetState = phase,
          transitionSpec = {
            fadeIn(tween(MESSAGE_FADE_IN_MS)) togetherWith fadeOut(tween(VIDEO_FADE_OUT_MS))
          },
          label = "PurchaseSuccessAnimation.phase",
        ) { currentPhase ->
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (currentPhase) {
              Phase.Video -> SuccessVideo(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                onEnded = { phase = Phase.Message },
                onError = { phase = Phase.Content },
              )
              Phase.Message -> HedvigText(
                text = message,
                style = HedvigTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
              )
              Phase.Content -> Unit
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SuccessVideo(onEnded: () -> Unit, onError: () -> Unit, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val currentOnEnded by rememberUpdatedState(onEnded)
  val currentOnError by rememberUpdatedState(onError)

  val listener = remember {
    object : Player.Listener {
      override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) {
          currentOnEnded()
        }
      }

      override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
        currentOnError()
      }
    }
  }

  val exoPlayer = remember(context) {
    ExoPlayer.Builder(context).build().apply {
      val uri = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .path(R.raw.purchase_success_animation.toString())
        .build()
      setMediaItem(MediaItem.fromUri(uri))
      repeatMode = Player.REPEAT_MODE_OFF
      volume = 0f
      playWhenReady = true
      addListener(listener)
      prepare()
    }
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner, exoPlayer) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_START -> exoPlayer.play()
        Lifecycle.Event.ON_STOP -> exoPlayer.pause()
        else -> {}
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      exoPlayer.removeListener(listener)
      exoPlayer.release()
    }
  }

  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { ctx ->
      TextureView(ctx).apply { isOpaque = false }
    },
    update = { textureView -> exoPlayer.setVideoTextureView(textureView) },
  )
}

@Composable
private fun rememberAnimationsEnabled(): Boolean {
  val context = LocalContext.current
  return remember(context) { context.animationsEnabled() }
}

private fun Context.animationsEnabled(): Boolean {
  return try {
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE) != 0f
  } catch (e: Settings.SettingNotFoundException) {
    true
  }
}
