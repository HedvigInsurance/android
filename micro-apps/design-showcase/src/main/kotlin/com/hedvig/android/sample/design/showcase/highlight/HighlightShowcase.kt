package com.hedvig.android.sample.design.showcase.highlight

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Large
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Medium
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Small
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Amber
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Blue
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Green
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Grey
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Pink
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Purple
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Red
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Teal
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Yellow
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.DARK
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.Surface

@Composable
fun HighlightShowcase() {
  Surface(
    modifier = Modifier
      .fillMaxSize(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(
      modifier = Modifier
        .safeContentPadding()
        .fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(16.dp))
      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
      ) {
        items(itemsList) { item ->
          HighlightLabel(
            modifier = Modifier.padding(2.dp),
            labelText = "Label",
            color = item.color,
            size = item.size,
          )
        }
      }
    }
  }
}

private data class HighLightShowcaseItem(
  val size: HighLightSize,
  val color: HighlightColor,
)

private val itemsList = listOf(
  HighLightShowcaseItem(Large, Blue(LIGHT)),
  HighLightShowcaseItem(Large, Blue(MEDIUM)),
  HighLightShowcaseItem(Large, Blue(DARK)),
  HighLightShowcaseItem(Medium, Blue(LIGHT)),
  HighLightShowcaseItem(Medium, Blue(MEDIUM)),
  HighLightShowcaseItem(Medium, Blue(DARK)),
  HighLightShowcaseItem(Small, Blue(LIGHT)),
  HighLightShowcaseItem(Small, Blue(MEDIUM)),
  HighLightShowcaseItem(Small, Blue(DARK)),
  HighLightShowcaseItem(Large, Teal(LIGHT)),
  HighLightShowcaseItem(Large, Teal(MEDIUM)),
  HighLightShowcaseItem(Large, Teal(DARK)),
  HighLightShowcaseItem(Medium, Teal(LIGHT)),
  HighLightShowcaseItem(Medium, Teal(MEDIUM)),
  HighLightShowcaseItem(Medium, Teal(DARK)),
  HighLightShowcaseItem(Small, Teal(LIGHT)),
  HighLightShowcaseItem(Small, Teal(MEDIUM)),
  HighLightShowcaseItem(Small, Teal(DARK)),
  HighLightShowcaseItem(Large, Purple(LIGHT)),
  HighLightShowcaseItem(Large, Purple(MEDIUM)),
  HighLightShowcaseItem(Large, Purple(DARK)),
  HighLightShowcaseItem(Medium, Purple(LIGHT)),
  HighLightShowcaseItem(Medium, Purple(MEDIUM)),
  HighLightShowcaseItem(Medium, Purple(DARK)),
  HighLightShowcaseItem(Small, Purple(LIGHT)),
  HighLightShowcaseItem(Small, Purple(MEDIUM)),
  HighLightShowcaseItem(Small, Purple(DARK)),
  HighLightShowcaseItem(Large, Green(LIGHT)),
  HighLightShowcaseItem(Large, Green(MEDIUM)),
  HighLightShowcaseItem(Large, Green(DARK)),
  HighLightShowcaseItem(Medium, Green(LIGHT)),
  HighLightShowcaseItem(Medium, Green(MEDIUM)),
  HighLightShowcaseItem(Medium, Green(DARK)),
  HighLightShowcaseItem(Small, Green(LIGHT)),
  HighLightShowcaseItem(Small, Green(MEDIUM)),
  HighLightShowcaseItem(Small, Green(DARK)),
  HighLightShowcaseItem(Large, Yellow(LIGHT)),
  HighLightShowcaseItem(Large, Yellow(MEDIUM)),
  HighLightShowcaseItem(Large, Yellow(DARK)),
  HighLightShowcaseItem(Medium, Yellow(LIGHT)),
  HighLightShowcaseItem(Medium, Yellow(MEDIUM)),
  HighLightShowcaseItem(Medium, Yellow(DARK)),
  HighLightShowcaseItem(Small, Yellow(LIGHT)),
  HighLightShowcaseItem(Small, Yellow(MEDIUM)),
  HighLightShowcaseItem(Small, Yellow(DARK)),
  HighLightShowcaseItem(Large, Amber(LIGHT)),
  HighLightShowcaseItem(Large, Amber(MEDIUM)),
  HighLightShowcaseItem(Large, Amber(DARK)),
  HighLightShowcaseItem(Medium, Amber(LIGHT)),
  HighLightShowcaseItem(Medium, Amber(MEDIUM)),
  HighLightShowcaseItem(Medium, Amber(DARK)),
  HighLightShowcaseItem(Small, Amber(LIGHT)),
  HighLightShowcaseItem(Small, Amber(MEDIUM)),
  HighLightShowcaseItem(Small, Amber(DARK)),
  HighLightShowcaseItem(Large, Red(LIGHT)),
  HighLightShowcaseItem(Large, Red(MEDIUM)),
  HighLightShowcaseItem(Large, Red(DARK)),
  HighLightShowcaseItem(Medium, Red(LIGHT)),
  HighLightShowcaseItem(Medium, Red(MEDIUM)),
  HighLightShowcaseItem(Medium, Red(DARK)),
  HighLightShowcaseItem(Small, Red(LIGHT)),
  HighLightShowcaseItem(Small, Red(MEDIUM)),
  HighLightShowcaseItem(Small, Red(DARK)),
  HighLightShowcaseItem(Large, Pink(LIGHT)),
  HighLightShowcaseItem(Large, Pink(MEDIUM)),
  HighLightShowcaseItem(Large, Pink(DARK)),
  HighLightShowcaseItem(Medium, Pink(LIGHT)),
  HighLightShowcaseItem(Medium, Pink(MEDIUM)),
  HighLightShowcaseItem(Medium, Pink(DARK)),
  HighLightShowcaseItem(Small, Pink(LIGHT)),
  HighLightShowcaseItem(Small, Pink(MEDIUM)),
  HighLightShowcaseItem(Small, Pink(DARK)),
  HighLightShowcaseItem(Large, Grey(LIGHT)),
  HighLightShowcaseItem(Large, Grey(MEDIUM)),
  HighLightShowcaseItem(Large, Grey(DARK)),
  HighLightShowcaseItem(Medium, Grey(LIGHT)),
  HighLightShowcaseItem(Medium, Grey(MEDIUM)),
  HighLightShowcaseItem(Medium, Grey(DARK)),
  HighLightShowcaseItem(Small, Grey(LIGHT)),
  HighLightShowcaseItem(Small, Grey(MEDIUM)),
  HighLightShowcaseItem(Small, Grey(DARK)),
)
