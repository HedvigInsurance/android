package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Loop
import kotlinx.coroutines.delay

private class ViewHolder<T> {
  var value: T? = null
}

private enum class HelipadAnimation(val animationName: String) {
  IDLE("Idle"),
  LOADING_INTRO("Loading intro"),
  LOADING("Loading"),
  LOADING_OUTRO("Loading outro"),
}

@Composable
internal actual fun HelipadRiveAnimation(
  modifier: Modifier,
  bottomAnimationFinished: Boolean,
  stepId: String,
) {
  val context = LocalContext.current
  val isDark = isSystemInDarkTheme()
  val resourceName = if (isDark) "hedvig_loader_dark" else "hedvig_loader_light"
  val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)

  if (resourceId == 0) {
    Box(modifier = modifier)
    return
  }

  val riveViewRef = remember { ViewHolder<RiveAnimationView>() }
  val initialAnimationDone = remember { mutableStateOf(false) }
  AndroidView(
    modifier = modifier,
    factory = { ctx ->
      RiveAnimationView(ctx).also { view ->
        view.setRiveResource(
          resId = resourceId,
          animationName = HelipadAnimation.IDLE.animationName,
          autoplay = false,
        )
        riveViewRef.value = view
      }
    },
    onRelease = { view ->
      view.stop()
      riveViewRef.value = null
    },
  )
  LaunchedEffect(bottomAnimationFinished, isDark, stepId) {
    riveViewRef.value?.setRiveResource(
      resId = resourceId,
      animationName = HelipadAnimation.IDLE.animationName,
      autoplay = false,
    )
    if (!bottomAnimationFinished && !initialAnimationDone.value) {
      delay(100L)
      riveViewRef.value?.play(
        animationName = HelipadAnimation.LOADING_INTRO.animationName,
        loop = Loop.ONESHOT,
      )
      delay(1000L)
      riveViewRef.value?.play(
        animationName = HelipadAnimation.LOADING.animationName,
        loop = Loop.LOOP,
      )
      initialAnimationDone.value = true
    } else if (bottomAnimationFinished && initialAnimationDone.value) {
      riveViewRef.value?.stop()
      riveViewRef.value?.play(
        animationName = HelipadAnimation.LOADING_OUTRO.animationName,
        loop = Loop.ONESHOT,
      )
    } else {
      riveViewRef.value?.stop()
      riveViewRef.value?.play(
        animationName = HelipadAnimation.IDLE.animationName,
        loop = Loop.ONESHOT,
      )
      initialAnimationDone.value = false
    }
  }


}

