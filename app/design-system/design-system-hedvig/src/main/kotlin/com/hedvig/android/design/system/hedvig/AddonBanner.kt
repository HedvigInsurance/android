package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import hedvig.resources.R

@Composable
fun FeatureAddonBanner(
  title: String,
  description: String,
  buttonText: String,
  labels: List<String>,
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val containerColor = HedvigTheme.colorScheme.surfacePrimary
  val borderColor = HedvigTheme.colorScheme.surfacePrimary
  Surface(
    modifier = modifier.semantics(mergeDescendants = true) {},
    shape = HedvigTheme.shapes.cornerLarge,
    color = containerColor,
    border = borderColor,
  ) {
    Column(
      Modifier.padding(
        vertical = 16.dp,
        horizontal = 12.dp,
      ),
    ) {
      Row {
        HedvigText(title)
        Spacer(Modifier.width(8.dp))
        LabelRow(labels)
      }
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = description,
        color = HedvigTheme.colorScheme.textSecondary,
        style = HedvigTheme.typography.label.copy(
          lineBreak = LineBreak.Heading,
        ),
      )
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        text = buttonText,
        enabled = true,
        onClick = onButtonClick,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        buttonSize = ButtonDefaults.ButtonSize.Small,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabelRow(labels: List<String>, modifier: Modifier = Modifier) {
  FlowRow(
    horizontalArrangement = Arrangement.End,
    verticalArrangement = Arrangement.spacedBy(4.dp),
    modifier = modifier.fillMaxWidth(),
  ) {
    labels.forEachIndexed { index, label ->
      HighlightLabel(
        labelText = label,
        size = HighlightLabelDefaults.HighLightSize.Small,
        color = HighlightLabelDefaults.HighlightColor.Grey(HighlightLabelDefaults.HighlightShade.DARK),
      )
      if (index != labels.lastIndex) {
        Spacer(Modifier.width(8.dp))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAddonBanner() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      FeatureAddonBanner(
        title = "Travel Plus",
        description = "Extended travel insurance with extra coverage for your travels",
        labels = listOf("Popular", "60 days"),
        buttonText = stringResource(R.string.ADDON_FLOW_SEE_PRICE_BUTTON),
        onButtonClick = {},
      )
    }
  }
}
