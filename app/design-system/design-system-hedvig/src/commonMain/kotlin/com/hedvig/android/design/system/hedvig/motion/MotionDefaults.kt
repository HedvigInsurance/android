package com.hedvig.android.design.system.hedvig.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

object MotionDefaults {
  fun sharedXAxisEnter(density: Density): EnterTransition {
    val offsetPixels = with(density) { SharedAxisDefaults.SharedAxisOffset.dp.roundToPx() }
    return SharedAxisDefaults.sharedXAxisEnterTransition(offsetPixels)
  }

  fun sharedXAxisExit(density: Density): ExitTransition {
    val offsetPixels = with(density) { SharedAxisDefaults.SharedAxisOffset.dp.roundToPx() }
    return SharedAxisDefaults.sharedXAxisExitTransition(-offsetPixels)
  }

  fun sharedXAxisPopEnter(density: Density): EnterTransition {
    val offsetPixels = with(density) { SharedAxisDefaults.SharedAxisOffset.dp.roundToPx() }
    return SharedAxisDefaults.sharedXAxisEnterTransition(-offsetPixels)
  }

  fun sharedXAxisPopExit(density: Density): ExitTransition {
    val offsetPixels = with(density) { SharedAxisDefaults.SharedAxisOffset.dp.roundToPx() }
    return SharedAxisDefaults.sharedXAxisExitTransition(offsetPixels)
  }

  val fadeThroughEnter: EnterTransition = FadeThroughDefaults.fadeThroughEnterTransition

  val fadeThroughExit: ExitTransition = FadeThroughDefaults.fadeThroughExitTransition
}
