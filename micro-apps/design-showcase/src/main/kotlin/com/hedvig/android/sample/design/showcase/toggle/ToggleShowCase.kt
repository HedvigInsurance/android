package com.hedvig.android.sample.design.showcase.toggle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigToggle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ToggleDefaults
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDefaultStyleSize.Medium
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDetailedStyleSize.Large
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDetailedStyleSize.Small
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle.Default
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle.Detailed

@Composable
fun ToggleShowcase() {
  Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
    var enabled by remember { mutableStateOf(false) }
    var enabled2 by remember { mutableStateOf(true) }
    var enabled3 by remember { mutableStateOf(false) }
    var enabled4 by remember { mutableStateOf(false) }
    var enabled5 by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 16.dp)) {
      Spacer(Modifier.height(64.dp))
      HedvigToggle(
        turnedOn = enabled,
        onClick = { enabled = !enabled },
        labelText = "Large",
        enabled = true,
        toggleStyle = Default(ToggleDefaults.ToggleDefaultStyleSize.Large),
      )
      Spacer(Modifier.height(8.dp))
      Row(Modifier.fillMaxWidth()) {
        HedvigToggle(
          turnedOn = enabled3,
          onClick = { enabled3 = !enabled3 },
          labelText = "Medium",
          toggleStyle = Default(Medium),
          enabled = true,
          modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        HedvigToggle(
          turnedOn = enabled4,
          onClick = { enabled4 = !enabled4 },
          labelText = "Small",
          toggleStyle = Default(ToggleDefaults.ToggleDefaultStyleSize.Small),
          enabled = true,
          modifier = Modifier.weight(1f),
        )
      }
      Spacer(Modifier.height(8.dp))
      Row {
        HedvigToggle(
          turnedOn = enabled2,
          onClick = { enabled2 = !enabled2 },
          labelText = "Large",
          modifier = Modifier.weight(1f),
          enabled = true,
          toggleStyle = Detailed(
            size = Large,
            descriptionText = "Long long long description Long long ",
          ),
        )
        Spacer(Modifier.width(8.dp))
        HedvigToggle(
          turnedOn = enabled5,
          onClick = { enabled5 = !enabled5 },
          labelText = "Small",
          modifier = Modifier.weight(1f),
          enabled = true,
          toggleStyle = Detailed(
            size = Small,
            descriptionText = "Long long long description Long long ",
          ),
        )
      }

      Spacer(Modifier.height(8.dp))
    }
  }
}
