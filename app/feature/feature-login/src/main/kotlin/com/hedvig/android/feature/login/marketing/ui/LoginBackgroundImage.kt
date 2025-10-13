package com.hedvig.android.feature.login.marketing.ui

import android.provider.Settings
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.hedvig.android.feature.login.R
import androidx.core.net.toUri

@Composable
fun LoginBackgroundImage(painter: Painter = painterResource(R.drawable.login_still_9x16)) {
  Image(
    painter = painter,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize(),
  )
}

@Composable
fun rememberAnimationsEnabled(): Boolean {
  val context = LocalContext.current
  var animationsEnabled by remember {
    mutableStateOf(
      Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
      ) != 0f,
    )
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        val animationDurationScale = Settings.Global.getFloat(
          context.contentResolver,
          Settings.Global.ANIMATOR_DURATION_SCALE,
          1f,
        )
        animationsEnabled = animationDurationScale != 0f
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  return animationsEnabled
}

@Composable
fun LoginBackgroundVideo(videoResId: Int = R.raw.login_video) {
  val context = LocalContext.current
  val animationsEnabled = rememberAnimationsEnabled()

  if (!animationsEnabled) {
    LoginBackgroundImage()
  } else {
    Box(modifier = Modifier.fillMaxSize()) {
      // Show static image as fallback/placeholder
      LoginBackgroundImage()

      val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
          val videoUri = "android.resource://${context.packageName}/$videoResId".toUri()
          setMediaItem(MediaItem.fromUri(videoUri))
          repeatMode = Player.REPEAT_MODE_ONE
          volume = 0f
          playWhenReady = true
          prepare()
        }
      }

      val lifecycleOwner = LocalLifecycleOwner.current
      DisposableEffect(lifecycleOwner) {
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
          exoPlayer.release()
        }
      }

      AndroidView(
        factory = { ctx ->
          PlayerView(ctx).apply {
            player = exoPlayer
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            layoutParams = FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT,
            )
          }
        },
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}
