package com.hedvig.android.sample.design.showcase.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ClickableItem
import com.hedvig.android.design.system.hedvig.ClickableList
import com.hedvig.android.design.system.hedvig.ClickableListDefaults
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@Composable
fun ClickableListShowcase() {
  var chosenItem by remember { mutableStateOf<ClickableItem?>(null) }
  val entries = listOf(
    ClickableItem("1 cat"),
    ClickableItem("2 cats"),
    ClickableItem("3 cats"),
    ClickableItem("4 cats"),
  )
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
      Spacer(Modifier.height(16.dp))
      HedvigText("You've chosen: ${chosenItem?.text ?: "nothing"}")
      Spacer(Modifier.height(16.dp))
      HedvigText("Style: Filled, size: Small", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
      Spacer(Modifier.height(16.dp))
      ClickableList(
        entries,
        onItemClick = {
          chosenItem = entries[it]
        },
        size = ClickableListDefaults.Size.Small,
        style = ClickableListDefaults.Style.Filled,
      )
      Spacer(Modifier.height(48.dp))
      HedvigText("Style: Default, size: Large", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
      Spacer(Modifier.height(16.dp))
      ClickableList(
        entries,
        onItemClick = {
          chosenItem = entries[it]
        },
        size = ClickableListDefaults.Size.Large,
        style = ClickableListDefaults.Style.Default,
      )
    }
  }
}
