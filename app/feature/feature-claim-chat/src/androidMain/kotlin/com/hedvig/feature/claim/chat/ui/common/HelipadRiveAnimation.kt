package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Loop
import kotlinx.coroutines.delay

@Composable
internal actual fun HelipadRiveAnimation(
  modifier: Modifier,
  bottomAnimationFinished: Boolean,
) {
  val context = LocalContext.current
  val isDark = isSystemInDarkTheme()
  val resourceName = if (isDark) "hedvig_loader_dark" else "hedvig_loader_light"
  val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)

  if (resourceId == 0) {
    Box(modifier = modifier)
    return
  }

  val riveViewRef = remember { mutableStateOf<RiveAnimationView?>(null) }
  val initialAnimationDone = remember { mutableStateOf(false) }

  AndroidView(
    modifier = modifier,
    factory = { ctx ->
      RiveAnimationView(ctx).also { view ->
        view.setRiveResource(
          resId = resourceId,
          animationName = "Idle",
          autoplay = false,
        )
        riveViewRef.value = view
      }
    },
  )

  LaunchedEffect(bottomAnimationFinished) {
    if (!bottomAnimationFinished && !initialAnimationDone.value) {
      delay(100L)
      riveViewRef.value?.play(
        animationName = "Loading intro", loop = Loop.ONESHOT,
      )
      delay(1000L)
      riveViewRef.value?.play(animationName = "Loading", loop = Loop.LOOP)
      initialAnimationDone.value = true
    } else if (bottomAnimationFinished && initialAnimationDone.value) {
      riveViewRef.value?.stop()
      riveViewRef.value?.play(
        animationName = "Loading outro", loop = Loop.ONESHOT,
      )
    } else {
      riveViewRef.value?.stop()
      riveViewRef.value?.play(
        animationName = "Idle", loop = Loop.ONESHOT,
      )
      initialAnimationDone.value = false
    }
  }
}
