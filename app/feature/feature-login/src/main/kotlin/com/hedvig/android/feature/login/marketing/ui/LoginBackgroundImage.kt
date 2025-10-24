package com.hedvig.android.feature.login.marketing.ui

import android.content.Context
import android.graphics.Color
import android.provider.Settings
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import java.io.ByteArrayOutputStream
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.hedvig.android.feature.login.R

@Composable
fun LoginBackgroundImage(painter: Painter = painterResource(R.drawable.login_still_9x16)) {
  Image(
    painter = painter,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize(),
  )
}

private fun Context.areAnimationsEnabled(): Boolean {
  return try {
    Settings.Global.getFloat(
      contentResolver,
      Settings.Global.ANIMATOR_DURATION_SCALE,
    ) != 0f
  } catch (e: Settings.SettingNotFoundException) {
    true
  }
}

@Composable
fun rememberAnimationsEnabled(): Boolean {
  val context = LocalContext.current
  var animationsEnabled by remember {
    mutableStateOf(context.areAnimationsEnabled())
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(context, lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        animationsEnabled = context.areAnimationsEnabled()
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
fun LoginBackgroundVideo(videoResId: Int = R.raw.login_video_compressed) {
  val context = LocalContext.current
  val animationsEnabled = rememberAnimationsEnabled()

  if (!animationsEnabled) {
    LoginBackgroundImage()
    return
  }

  var isVideoReady by remember { mutableStateOf(false) }
  var hasVideoError by remember { mutableStateOf(false) }

  if (hasVideoError) {
    LoginBackgroundImage()
    return
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (!isVideoReady) {
      LoginBackgroundImage()
    }

    val listener = remember {
      object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
          if (playbackState == Player.STATE_READY) {
            isVideoReady = true
          }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
          hasVideoError = true
        }
      }
    }

    val exoPlayer = remember(context, listener, videoResId, animationsEnabled) {
      ExoPlayer.Builder(context).build().apply {
        val videoUri = "android.resource://${context.packageName}/$videoResId".toUri()
        setMediaItem(MediaItem.fromUri(videoUri))
        repeatMode = Player.REPEAT_MODE_ONE
        volume = 0f
        playWhenReady = true
        addListener(listener)
        prepare()
      }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, listener, exoPlayer) {
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
      update = { playerView ->
        playerView.player = exoPlayer
      },
    )
  }
}

/**
 * Video player that supports alpha channel transparency using WebView.
 * Use this for WebM videos with VP9 codec and alpha channel.
 *
 * Note: This embeds the video as base64 data URL since WebView cannot directly
 * access android.resource:// URLs in HTML video elements.
 */
@Composable
fun LoginBackgroundTransparentVideo(videoResId: Int = R.raw.pillow_car) {
  val context = LocalContext.current
  val animationsEnabled = rememberAnimationsEnabled()

  if (!animationsEnabled) {
    LoginBackgroundImage()
    return
  }

  var isVideoReady by remember { mutableStateOf(false) }

  // Convert raw resource to base64 for embedding in HTML
  val videoBase64 = remember(videoResId) {
    try {
      val inputStream = context.resources.openRawResource(videoResId)
      val byteArrayOutputStream = ByteArrayOutputStream()
      val buffer = ByteArray(8192)
      var bytesRead: Int
      while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        byteArrayOutputStream.write(buffer, 0, bytesRead)
      }
      inputStream.close()
      Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
      null
    }
  }

  if (videoBase64 == null) {
    LoginBackgroundImage()
    return
  }

  Box(modifier = Modifier.size(100.dp)) {
    if (!isVideoReady) {
      LoginBackgroundImage()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var webView: WebView? by remember { mutableStateOf(null) }

    AndroidView(
      factory = { ctx ->
        WebView(ctx).apply {
          layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
          )

          // Enable transparency - WebView supports alpha channel in videos!
          setBackgroundColor(Color.TRANSPARENT)
          setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

          // WebView settings
          settings.apply {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            allowContentAccess = true
          }

          webChromeClient = WebChromeClient()

          // Use data URL with base64 encoded video
          val videoDataUrl = "data:video/webm;base64,$videoBase64"

          // Load HTML with transparent video element
          val html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <style>
                * {
                  margin: 0;
                  padding: 0;
                }
                html, body {
                  width: 100%;
                  height: 100%;
                  overflow: hidden;
                  background: transparent;
                }
                video {
                  position: absolute;
                  top: 0;
                  left: 0;
                  width: 100%;
                  height: 100%;
                  object-fit: cover;
                }
              </style>
            </head>
            <body>
              <video id="bgVideo" autoplay loop muted playsinline>
                <source src="$videoDataUrl" type="video/webm">
              </video>
              <script>
                const video = document.getElementById('bgVideo');
                video.addEventListener('loadeddata', () => {
                  console.log('Video loaded');
                });
                video.addEventListener('error', (e) => {
                  console.error('Video error:', e);
                });
              </script>
            </body>
            </html>
          """.trimIndent()

          loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)

          // Mark video as ready after delay for WebView to render
          postDelayed({ isVideoReady = true }, 1000)

          webView = this
        }
      },
      modifier = Modifier.fillMaxSize(),
    )

    DisposableEffect(lifecycleOwner) {
      val observer = LifecycleEventObserver { _, event ->
        when (event) {
          Lifecycle.Event.ON_PAUSE -> webView?.onPause()
          Lifecycle.Event.ON_RESUME -> webView?.onResume()
          else -> {}
        }
      }
      lifecycleOwner.lifecycle.addObserver(observer)

      onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
        webView?.destroy()
      }
    }
  }
}
