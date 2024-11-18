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
import androidx.compose.runtime.mutableIntStateOf
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
  var selectedIndex00 by remember { mutableIntStateOf(0) }
  var selectedIndex01 by remember { mutableIntStateOf(0) }
  var selectedIndex02 by remember { mutableIntStateOf(0) }
  var selectedIndex03 by remember { mutableIntStateOf(0) }
  var selectedIndex04 by remember { mutableIntStateOf(0) }
  var selectedIndex by remember { mutableIntStateOf(0) }
  var selectedIndex2 by remember { mutableIntStateOf(0) }
  var selectedIndex3 by remember { mutableIntStateOf(0) }
  var selectedIndex4 by remember { mutableIntStateOf(0) }
  var selectedIndex5 by remember { mutableIntStateOf(0) }
  var selectedIndex6 by remember { mutableIntStateOf(0) }
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
        selectedTabIndex = selectedIndex00,
        onTabChosen = {
          selectedIndex00 = it
        },
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "3 tabs")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        selectedTabIndex = selectedIndex01,
        onTabChosen = {
          selectedIndex01 = it
        },
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "4 tabs")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles4,
        selectedTabIndex = selectedIndex02,
        onTabChosen = {
          selectedIndex02 = it
        },
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "5 tabs")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles5,
        selectedTabIndex = selectedIndex03,
        onTabChosen = {
          selectedIndex03 = it
        },
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "6 tabs")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles6,
        selectedTabIndex = selectedIndex04,
        onTabChosen = {
          selectedIndex04 = it
        },
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Small, Filled and Default")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        selectedTabIndex = selectedIndex,
        onTabChosen = {
          selectedIndex = it
        },
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        selectedTabIndex = selectedIndex2,
        onTabChosen = {
          selectedIndex2 = it
        },
        modifier = Modifier.fillMaxWidth(),
        tabSize = Small,
        tabStyle = Default,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Mini Filled")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        selectedTabIndex = selectedIndex3,
        onTabChosen = {
          selectedIndex3 = it
        },
        modifier = Modifier.fillMaxWidth(),
        tabSize = Mini,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Medium Filled")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        selectedTabIndex = selectedIndex4,
        onTabChosen = {
          selectedIndex4 = it
        },
        modifier = Modifier.fillMaxWidth(),
        tabSize = Medium,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(text = "Large Filled")
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles3,
        selectedTabIndex = selectedIndex5,
        onTabChosen = {
          selectedIndex5 = it
        },
        modifier = Modifier.fillMaxWidth(),
        tabSize = Large,
        tabStyle = Filled,
      )
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTabRowMaxSixTabs(
        tabTitles = titles6,
        selectedTabIndex = selectedIndex6,
        onTabChosen = {
          selectedIndex6 = it
        },
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
