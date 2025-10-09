package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.tokens.TweenAnimationTokens.FastAnimationTokens.durationMillis

@Composable
fun HedvigStepProgress(steps: List<StepProgressItem>, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    steps.forEachIndexed { index, step ->
      ProgressStep(
        step,
        modifier = Modifier
          .weight(1f)
          .semantics(true) {},
      )
      if (index != steps.lastIndex) {
        Spacer(Modifier.width(4.dp))
      }
    }
  }
}

@Composable
fun ProgressStep(step: StepProgressItem, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val infiniteTransition = rememberInfiniteTransition()
    val animationColors = hedvigStepProgressColors
    val widthFraction = infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = progressAnimationSpec(0f, 1f),
    )
    val animatedStepColor = infiniteTransition.animateColor(
      animationColors.activated,
      animationColors.inactive,
      animationSpec = progressAnimationSpec(
        animationColors.inactive, animationColors.activated
      ),
    )

    val color = if (step.activated) {
      hedvigStepProgressColors.activated
    } else {
      hedvigStepProgressColors.inactive
    }
    Box(
      modifier = Modifier
        .height(8.dp)
        .fillMaxWidth(),
    ) {
      Surface(
        modifier = Modifier
          .height(8.dp)
          .fillMaxWidth(),
        color = color,
        shape = HedvigTheme.shapes.cornerLarge,
      ) {}
      if (step.animate) {
        Surface(
          modifier = Modifier
            .height(8.dp)
            .fillMaxWidth(widthFraction.value),
          color = animatedStepColor.value,
          shape = HedvigTheme.shapes.cornerLarge,
        ) {}
      }
    }

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

@Composable
private fun <T> progressAnimationSpec(
  startValue: T,
  endValue: T,
): InfiniteRepeatableSpec<T> = infiniteRepeatable(
  keyframes {
    durationMillis = 2000
    startValue at 0 using LinearEasing
    endValue at 1200
    endValue at 1800
  },
  RepeatMode.Restart,
)

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
  val activated: Boolean,
  val animate: Boolean = false,
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
          previewMockSteps,
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
    true,
  ),
  StepProgressItem(
    "2 insurances",
    "15% discount",
    true,
  ),
  StepProgressItem(
    "3 or more",
    "15% discount",
    false,
  ),
)
