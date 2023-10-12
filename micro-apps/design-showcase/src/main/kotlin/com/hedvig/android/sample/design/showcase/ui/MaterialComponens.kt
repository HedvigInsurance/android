package com.hedvig.android.sample.design.showcase.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.sample.design.showcase.ui.colorscheme.HedvigColorScheme
import com.hedvig.android.sample.design.showcase.ui.hedviguikit.HedvigIcons
import com.hedvig.android.sample.design.showcase.ui.hedviguikit.HedvigShapes
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Buttons
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Cards
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Checkbox
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Chips
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3DatePicker
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Divider
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3NavigationBars
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3ProgressBar
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Slider
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Switch
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3Tab
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3TextFields
import com.hedvig.android.sample.design.showcase.ui.m3.components.M3TopAppBars

@Composable
internal fun MaterialComponents(windowSizeClass: WindowSizeClass) {
  ThemeSelection()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeSelection() {
  var showM3: Boolean by rememberSaveable { mutableStateOf(false) }
  var showColorSchemes: Boolean by rememberSaveable { mutableStateOf(false) }
  var showHedvigUiKit: Boolean by rememberSaveable { mutableStateOf(true) }
  var showIcons: Boolean by rememberSaveable { mutableStateOf(false) }
  var showShapes: Boolean by rememberSaveable { mutableStateOf(false) }
  when {
    showM3 -> {
      BackHandler { showM3 = false }
      M3()
    }
    showColorSchemes -> {
      BackHandler { showColorSchemes = false }
      HedvigColorScheme()
    }
    showHedvigUiKit -> {
      BackHandler { showHedvigUiKit = false }
      HedvigUiKit()
    }
    showIcons -> {
      BackHandler { showIcons = false }
      HedvigIcons()
    }
    showShapes -> {
      BackHandler { showShapes = false }
      HedvigShapes()
    }
    else -> {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        Surface {
          FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { showM3 = true }) {
              Text("M3")
            }
            Button(onClick = { showColorSchemes = true }) {
              Text("ColorScheme")
            }
            Button(onClick = { showHedvigUiKit = true }) {
              Text("Hedvig UI Kit")
            }
            Button(onClick = { showIcons = true }) {
              Text("New design Icons")
            }
            Button(onClick = { showShapes = true }) {
              Text("Hedvig squircle shapes")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun M3() {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = rememberLazyListState(),
  ) {
    LightAndDarkItem { M3Buttons() }
    LightAndDarkItem { M3TextFields() }
    LightAndDarkItem { M3Chips() }
    LightAndDarkItem { M3Checkbox() }
    LightAndDarkItem { M3Slider() }
    LightAndDarkItem { M3ProgressBar() }
    LightAndDarkItem { M3Divider() }
    LightAndDarkItem { M3Cards() }
    LightAndDarkItem { M3TopAppBars() }
    LightAndDarkItem { M3NavigationBars() }
    LightAndDarkItem { M3Tab() }
    LightAndDarkItem { M3Switch() }
    LightAndDarkItem { M3DatePicker() }
  }
}

@Suppress("FunctionName")
private fun LazyListScope.LightAndDarkItem(content: @Composable () -> Unit) {
  item {
    Row(Modifier.fillMaxWidth()) {
      Box(Modifier.weight(1f)) {
        HedvigTheme(darkTheme = false) {
          Surface(Modifier.fillMaxWidth()) {
            content()
          }
        }
      }
      Box(Modifier.weight(1f)) {
        HedvigTheme(darkTheme = true) {
          Surface(Modifier.fillMaxWidth()) {
            content()
          }
        }
      }
    }
  }
}
