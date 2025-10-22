package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun HedvigStepProgress(steps: List<StepProgressItem>, numberOfActivatedSteps: Int, modifier: Modifier = Modifier) {
  val animationColors = hedvigStepProgressColors
  val activatedStepsProgress = remember(steps, numberOfActivatedSteps) { Animatable(0f) }
  val pulsatingStepProgress = remember(steps, numberOfActivatedSteps) { Animatable(0f) }
  LaunchedEffect(steps, numberOfActivatedSteps) {
    val activatedStepsAnimationSpec = tween<Float>(
      durationMillis = 1000 * numberOfActivatedSteps,
      delayMillis = 1000,
      easing = LinearEasing,
    )
    activatedStepsProgress.animateTo(1f, activatedStepsAnimationSpec)
    repeat(3) {
      pulsatingStepProgress.animateTo(1f, tween(2000))
      delay(200)
      pulsatingStepProgress.animateTo(0f, tween(1000))
    }
  }
  val inactiveColor = animationColors.inactive
  val activatedColor = animationColors.activated
  Row(
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    modifier = modifier
      .fillMaxWidth()
      .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
      .drawWithContent {
        drawContent()
        val oneStepWidth = this.size.width / steps.size
        val progressBarHeightPx = ProgressBarHeight.toPx()
        drawRect(
          color = activatedColor,
          size = Size(
            width = (oneStepWidth * numberOfActivatedSteps) * activatedStepsProgress.value,
            height = progressBarHeightPx,
          ),
          blendMode = BlendMode.SrcIn,
        )
        if (numberOfActivatedSteps < steps.size) {
          drawRect(
            color = activatedColor.copy(alpha = pulsatingStepProgress.value),
            topLeft = Offset(oneStepWidth * numberOfActivatedSteps, 0f),
            size = Size(
              width = oneStepWidth,
              height = progressBarHeightPx,
            ),
            blendMode = BlendMode.SrcAtop,
          )
        }
      },
  ) {
    for (step in steps) {
      ProgressStep(
        step = step,
        colors = animationColors,
        modifier = Modifier
          .weight(1f)
          .semantics(true) {},
      )
    }
  }
}

@Composable
private fun ProgressStep(step: StepProgressItem, colors: HedvigStepProgressColors, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier
        .height(ProgressBarHeight)
        .fillMaxWidth()
        .background(colors.inactive, HedvigTheme.shapes.cornerLarge),
    )
    if (step.title != null) {
      HedvigText(
        step.title,
        style = HedvigTheme.typography.label,
      )
    }
    if (step.subtitle != null) {
      HedvigText(
        step.subtitle,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
  }
}

private val ProgressBarHeight = 8.dp

private data class HedvigStepProgressColors(
  val activated: Color,
  val inactive: Color,
)

private val hedvigStepProgressColors: HedvigStepProgressColors
  @Composable get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      HedvigStepProgressColors(
        activated = signalGreenElement,
        inactive = surfaceSecondary,
      )
    }
  }

data class StepProgressItem(
  val title: String?,
  val subtitle: String?,
)

@Preview
@Composable
private fun PreviewCheckboxStyles() {
  HedvigTheme(darkTheme = false) {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
      modifier = Modifier,
    ) {
      Column(Modifier.fillMaxWidth()) {
        HedvigStepProgress(
          steps = previewMockSteps,
          numberOfActivatedSteps = 2,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

private val previewMockSteps = listOf(
  StepProgressItem(
    "1 insurance",
    "No discount",
  ),
  StepProgressItem(
    "2 insurances",
    "15% discount",
  ),
  StepProgressItem(
    "3 or more",
    "15% discount",
  ),
)
