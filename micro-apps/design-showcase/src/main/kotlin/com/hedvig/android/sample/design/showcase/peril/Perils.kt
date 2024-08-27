package com.hedvig.android.sample.design.showcase.peril

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PerilData
import com.hedvig.android.design.system.hedvig.PerilDefaults.PerilSize
import com.hedvig.android.design.system.hedvig.PerilList
import com.hedvig.android.design.system.hedvig.Surface

@Composable
fun PerilsShowcase() {
  Surface(
    modifier = Modifier
      .fillMaxSize()
      .safeContentPadding(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(
      Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
    ) {
      Spacer(Modifier.height(32.dp))
      PerilList(
        previewPerils,
        PerilSize.Small,
      )
      Spacer(Modifier.height(32.dp))
      PerilList(
        previewPerils,
        PerilSize.Large,
      )
    }
  }
}

private val previewPerils: List<PerilData> = List(4) { index ->
  PerilData(
    title = "Eldsvåda $index",
    description = "description description description description " +
      "description description description descri" +
      "ption description description description description$index",
    covered = listOf("Covered#$index", "Also covered#$index"),
    colorCode = "#FFD0ECFB",
    // colorCode = null
    // colorCode = "#FFD0ECFBsfawefawesfaw"
  )
}
