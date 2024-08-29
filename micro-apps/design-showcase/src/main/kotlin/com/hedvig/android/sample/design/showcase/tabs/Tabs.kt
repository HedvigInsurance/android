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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
  var selectedIndex by remember { mutableStateOf(0) }
  var selectedIndex2 by remember { mutableStateOf(0) }
  var selectedIndex3 by remember { mutableStateOf(0) }
  var selectedIndex4 by remember { mutableStateOf(0) }
  var selectedIndex5 by remember { mutableStateOf(0) }
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
      HedvigText(text = "Small, Filled and Default")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        modifier = Modifier.fillMaxWidth(),
        tabStyle = Filled,
        tabSize = Small,
        tabTitles = titles,
        selectedTabIndex = selectedIndex,
        onTabChosen = {
          selectedIndex = it
        },
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        modifier = Modifier.fillMaxWidth(),
        tabStyle = Default,
        tabSize = Small,
        tabTitles = titles,
        selectedTabIndex = selectedIndex2,
        onTabChosen = {
          selectedIndex2 = it
        },
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Mini Filled")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        modifier = Modifier.fillMaxWidth(),
        tabStyle = Filled,
        tabSize = Mini,
        tabTitles = titles,
        selectedTabIndex = selectedIndex3,
        onTabChosen = {
          selectedIndex3 = it
        },
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Medium Filled")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        modifier = Modifier.fillMaxWidth(),
        tabStyle = Filled,
        tabSize = Medium,
        tabTitles = titles,
        selectedTabIndex = selectedIndex4,
        onTabChosen = {
          selectedIndex4 = it
        },
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Large Filled")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        modifier = Modifier.fillMaxWidth(),
        tabStyle = Filled,
        tabSize = Large,
        tabTitles = titles,
        selectedTabIndex = selectedIndex5,
        onTabChosen = {
          selectedIndex5 = it
        },
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

private val titles = listOf("Overview", "C", "Documents","Something else")
