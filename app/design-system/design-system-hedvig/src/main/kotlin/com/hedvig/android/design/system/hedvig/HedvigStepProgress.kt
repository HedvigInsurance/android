package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HedvigStepProgress(
  steps: List<StepProgressItem>,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    steps.forEachIndexed { index, step ->
      ProgressStep(
        step,
        modifier = Modifier.weight(1f),
      )
      if (index != steps.lastIndex) {
        Spacer(Modifier.width(4.dp))
      }
    }
  }
}

@Composable
fun ProgressStep(
  step: StepProgressItem,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Surface(
      modifier = Modifier
        .height(8.dp)
        .fillMaxWidth(),
      color = if (step.activated) hedvigStepProgressColors.activated
      else hedvigStepProgressColors.inactive,
      shape = HedvigTheme.shapes.cornerLarge,
    ) {}
    if (step.title!=null) {
      HedvigText(
        step.title,
        style = HedvigTheme.typography.label,
      )
    }
    if (step.subtitle!=null) {
      HedvigText(
        step.subtitle,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
  }
}

private data class HedvigStepProgressColors(
  val activated: Color,
  val inactive: Color,
)


private val hedvigStepProgressColors: HedvigStepProgressColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
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
  val animate: Boolean = false
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
