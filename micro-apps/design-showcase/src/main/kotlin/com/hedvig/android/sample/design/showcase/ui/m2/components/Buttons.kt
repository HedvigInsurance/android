package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.core.designsystem.component.button.LargeTextButton

@Composable
internal fun M2Buttons() {
  Column {
    val enabled by remember { mutableStateOf(true) }
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Buttons",
      style = MaterialTheme.typography.h5,
    )
    Spacer(Modifier.size(16.dp))
    LargeContainedButton({}, Modifier.padding(horizontal = 8.dp)) {
      Text("Hedvig LargeTextButton")
    }
    Spacer(Modifier.size(16.dp))
    LargeOutlinedButton({}, Modifier.padding(horizontal = 8.dp)) {
      Text("Hedvig LargeTextButton")
    }
    Spacer(Modifier.size(16.dp))
    LargeTextButton({}, Modifier.padding(horizontal = 8.dp)) {
      Text("Hedvig LargeTextButton")
    }

    Spacer(Modifier.size(16.dp))
    Button(
      onClick = {},
      enabled = enabled,
    ) {
      Text("Primary button")
    }
    Spacer(Modifier.size(16.dp))
    OutlinedButton(
      onClick = {},
      enabled = enabled,
    ) {
      Text("Secondary button")
    }
    Spacer(Modifier.size(16.dp))
    TextButton(
      onClick = {},
      enabled = enabled,
    ) {
      Text("Text button")
    }
  }
}
