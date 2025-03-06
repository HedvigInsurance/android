package com.hedvig.android.sample.design.showcase.tabs

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTabRowMaxSixTabs
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize.Large
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize.Medium
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize.Mini
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize.Small
import com.hedvig.android.design.system.hedvig.TabDefaults.TabStyle.Default
import com.hedvig.android.design.system.hedvig.TabDefaults.TabStyle.Filled

@Composable
fun TabsShowcase() {
  Surface(
    modifier = Modifier
      .fillMaxSize(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(
      modifier = Modifier
        .safeContentPadding()
        .padding(horizontal = 16.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HedvigText(text = "1 tab")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = listOf("One title"),
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "3 tabs")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "4 tabs")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles4,
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "5 tabs")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles5,
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "6 tabs")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles6,
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Small, Filled and Default")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Default,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Mini Filled")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        modifier = Modifier.fillMaxWidth(),
        tabSize = Mini,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Medium Filled")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        modifier = Modifier.fillMaxWidth(),
        tabSize = Medium,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Large Filled")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        modifier = Modifier.fillMaxWidth(),
        tabSize = Large,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles6,
        modifier = Modifier.fillMaxWidth(),
        tabSize = Large,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

private val titles2 = listOf("Overview", "Documents")
private val titles3 = listOf("Overview", "C", "Documents")
private val titles4 = listOf("Overview", "C", "Documents", "Documents2")
private val titles5 = listOf("Overview", "C", "Documents", "Documents2", "Something else")
private val titles6 = listOf("Overview", "C", "Documents", "Something else", "Overview", "Overview")
