package com.hedvig.android.sample.design.showcase.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTabRow
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize.Large
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize.Medium
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize.Mini
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize.Small
import com.hedvig.android.design.system.hedvig.TabDefaults.TabStyle.Default
import com.hedvig.android.design.system.hedvig.TabDefaults.TabStyle.Filled

@Composable
fun TabsShowcase() {
  Surface(
    modifier = Modifier.fillMaxSize(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(
      modifier = Modifier
        .safeContentPadding()
        .padding(horizontal = 16.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      for (size in listOf(Mini, Small, Medium, Large)) {
        HedvigText(text = size.toString())
        AllStyles(size)
      }
      for (tabNumbers in 1..7) {
        HedvigText(text = "$tabNumbers tabs")
        HedvigTabRow(
          tabTitles = titles[tabNumbers - 1],
          modifier = Modifier.fillMaxWidth(),
          tabSize = Small,
          tabStyle = Filled,
        )
      }
    }
  }
}

@Composable
private fun AllStyles(size: TabSize) {
  for (style in listOf(Filled, Default)) {
    HedvigTabRow(
      tabTitles = titles[2],
      modifier = Modifier.fillMaxWidth(),
      tabSize = size,
      tabStyle = style,
    )
  }
}

private val titles = listOf(
  listOf("Overview"),
  listOf("Overview", "Documents"),
  listOf("Overview", "C", "Documents"),
  listOf("Overview", "C", "Documents", "Documents2"),
  listOf("Overview", "C", "Documents", "Documents2", "Something else"),
  listOf("Overview", "C", "Documents", "Something else", "Overview", "Overview"),
  listOf("Overview", "C", "Documents", "Something else", "Overview", "Overview", "More Overview"),
)

@HedvigPreview
@Composable
private fun PreviewTabsShowcase() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TabsShowcase()
    }
  }
}
