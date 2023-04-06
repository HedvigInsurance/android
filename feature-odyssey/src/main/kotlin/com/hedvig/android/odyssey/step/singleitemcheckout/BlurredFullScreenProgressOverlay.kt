package com.hedvig.android.odyssey.step.singleitemcheckout

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import java.lang.Float.min

private val progressBlue = Color(0xffC3CBD6)
private val progressYellow = Color(0xffEDCDAB)

@Composable
internal fun BlurredFullScreenProgressOverlay(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Surface(modifier.fillMaxSize()) {
    Box {
      AnimatedCircles()
      content()
    }
  }
}

@Composable
private fun AnimatedCircles() {
  val transition = rememberInfiniteTransition("Ball transition")
  val configuration = LocalConfiguration.current

  val translationX by transition.animateValue(
    initialValue = (-200..-100).random().dp,
    targetValue = (40..300).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "ball#1_x_translation",
  )

  val translationY by transition.animateValue(
    initialValue = (50..500).random().dp,
    targetValue = (-500..-400).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "ball#1_y_translation",
  )

  val translationX2 by transition.animateValue(
    initialValue = (-200..0).random().dp,
    targetValue = (0..100).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "ball#2_x_translation",
  )

  val translationY2 by transition.animateValue(
    initialValue = (-200..-50).random().dp,
    targetValue = (50..200).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 15000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "ball#2_y_translation",
  )

  val radius by transition.animateValue(
    initialValue = (150..200).random().dp,
    targetValue = (220..250).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "ball#1_radius",
  )

  val radius2 by transition.animateValue(
    initialValue = (70..80).random().dp,
    targetValue = (150..300).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 16000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "ball#2_radius",
  )

  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .blur(80.dp),
  ) {
    val screenHeight = configuration.screenHeightDp.dp.toPx()
    val screenWidth = configuration.screenWidthDp.dp.toPx()
    translate(
      left = min(translationX.toPx(), screenWidth),
      top = min(translationY.toPx(), screenHeight),
    ) {
      drawCircle(progressBlue, radius = radius.toPx())
    }

    translate(
      left = min(translationX2.toPx(), screenWidth),
      top = min(translationY2.toPx(), screenHeight),
    ) {
      drawCircle(progressYellow, radius = radius2.toPx())
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewBlurredFullScreenProgressOverlay() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      BlurredFullScreenProgressOverlay {
        Text(
          text = "Calculating price...",
          modifier = Modifier.align(Alignment.Center),
        )
      }
    }
  }
}
