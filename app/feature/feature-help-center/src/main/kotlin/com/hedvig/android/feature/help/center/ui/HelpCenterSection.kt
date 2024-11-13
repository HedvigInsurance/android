package com.hedvig.android.feature.help.center.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Medium
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.Surface

@Composable
internal fun HelpCenterSection(
  title: String,
  chipContainerColor: HighlightColor,
  content: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    HighlightLabel(
      labelText = title,
      color = chipContainerColor,
      size = Medium,
    )
    content()
  }
}

@HedvigPreview
@Composable
private fun PreviewHelpCenterSection(
  @PreviewParameter(ColorIndexParameterProvider::class) colorIndex: Int,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HelpCenterSection(
        title = "Common topics",
        chipContainerColor = when (colorIndex) {
          0 -> HighlightColor.Blue(LIGHT)
          1 -> HighlightColor.Yellow(LIGHT)
          2 -> HighlightColor.Amber(LIGHT)
          else -> HighlightColor.Green(LIGHT)
        },
        content = {
          HedvigText("Content")
        },
      )
    }
  }
}

private class ColorIndexParameterProvider : CollectionPreviewParameterProvider<Int>(listOf(0, 1, 2))
