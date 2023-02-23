@file:OptIn(ExperimentalMaterial3Api::class)

package com.hedvig.android.sample.design.showcase.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.android.core.designsystem.material3.HedvigMaterial3Theme
import com.hedvig.android.sample.design.showcase.ui.components.M3Buttons
import com.hedvig.android.sample.design.showcase.ui.components.M3Cards
import com.hedvig.android.sample.design.showcase.ui.components.M3Checkbox
import com.hedvig.android.sample.design.showcase.ui.components.M3Chips
import com.hedvig.android.sample.design.showcase.ui.components.M3DatePicker
import com.hedvig.android.sample.design.showcase.ui.components.M3Divider
import com.hedvig.android.sample.design.showcase.ui.components.M3NavigationBars
import com.hedvig.android.sample.design.showcase.ui.components.M3ProgressBar
import com.hedvig.android.sample.design.showcase.ui.components.M3Slider
import com.hedvig.android.sample.design.showcase.ui.components.M3Switch
import com.hedvig.android.sample.design.showcase.ui.components.M3Tab
import com.hedvig.android.sample.design.showcase.ui.components.M3TextFields
import com.hedvig.android.sample.design.showcase.ui.components.M3TopAppBars

@Composable
fun MaterialComponents() {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = rememberLazyListState(),
  ) {
    LightAndDarkItem { M3DatePicker() }
    LightAndDarkItem { M3Buttons() }
    LightAndDarkItem { M3TextFields() }
    LightAndDarkItem { M3Chips() }
    LightAndDarkItem { M3Switch() }
    LightAndDarkItem { M3Checkbox() }
    LightAndDarkItem { M3Slider() }
    LightAndDarkItem { M3ProgressBar() }
    LightAndDarkItem { M3Divider() }
    LightAndDarkItem { M3Cards() }
    LightAndDarkItem { M3TopAppBars() }
    LightAndDarkItem { M3NavigationBars() }
    LightAndDarkItem { M3Tab() }
  }
}

@Suppress("FunctionName")
fun LazyListScope.LightAndDarkItem(content: @Composable () -> Unit) {
  item {
    Row {
      Surface(Modifier.weight(1f)) {
        content()
      }
      HedvigMaterial3Theme(true) {
        Surface(Modifier.weight(1f)) {
          content()
        }
      }
    }
  }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MaterialComponentsPreview() {
  HedvigMaterial3Theme {
    Surface {
      MaterialComponents()
    }
  }
}
