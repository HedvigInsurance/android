package com.hedvig.android.core.designsystem.material3.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

object MotionDefaults {
  val fadeThroughEnter: EnterTransition = FadeThroughDefaults.fadeThroughEnterTransition

  val fadeThroughExit: ExitTransition = FadeThroughDefaults.fadeThroughExitTransition
}
