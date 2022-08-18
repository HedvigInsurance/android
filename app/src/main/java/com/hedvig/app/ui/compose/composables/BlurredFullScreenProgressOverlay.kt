package com.hedvig.app.ui.compose.composables

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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.progressBlue
import com.hedvig.android.core.designsystem.theme.progressYellow
import com.hedvig.app.ui.compose.theme.HedvigTypography
import java.lang.Float.min

@Composable
fun TextProgressOverlay(progressText: String) {
  BlurredFullScreenProgressOverlay {
    Text(
      progressText,
      style = HedvigTypography.h5,
      modifier = Modifier.align(Alignment.Center),
    )
  }
}

@Composable
fun BlurredFullScreenProgressOverlay(content: @Composable BoxScope.() -> Unit) {
  Box(Modifier.fillMaxSize()) {
    AnimatedCircles()
    content()
  }
}

@Composable
private fun AnimatedCircles() {
  val transition = rememberInfiniteTransition()
  val configuration = LocalConfiguration.current

  val translationX by transition.animateValue(
    initialValue = (-200..-100).random().dp,
    targetValue = (40..300).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
  )

  val translationY by transition.animateValue(
    initialValue = (50..500).random().dp,
    targetValue = (-500..-400).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
  )

  val translationX2 by transition.animateValue(
    initialValue = (-200..0).random().dp,
    targetValue = (0..100).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
  )

  val translationY2 by transition.animateValue(
    initialValue = (-200..-50).random().dp,
    targetValue = (50..200).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 15000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
  )

  val radius by transition.animateValue(
    initialValue = (150..200).random().dp,
    targetValue = (220..250).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
  )

  val radius2 by transition.animateValue(
    initialValue = (70..80).random().dp,
    targetValue = (150..300).random().dp,
    typeConverter = Dp.VectorConverter,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 16000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
  )

  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .blur(80.dp, BlurredEdgeTreatment.Rectangle),
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

@Preview
@Composable
fun TextProgressOverlayPreview() {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colors.background,
    ) {
      TextProgressOverlay(progressText = "Calculating price...")
    }
  }
}
