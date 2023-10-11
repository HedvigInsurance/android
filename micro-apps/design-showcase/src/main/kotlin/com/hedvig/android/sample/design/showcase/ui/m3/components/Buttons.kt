/**
 * MIT License
 *
 * Copyright (c) 2023 Odin Asbjørnsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.hedvig.android.sample.design.showcase.ui.m3.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigOutlinedButton

@Composable
internal fun M3Buttons() {
  Column {
    val enabled by remember { mutableStateOf(true) }
    Spacer(Modifier.size(16.dp))
    M3OnSurfaceText(
      text = "Buttons",
      style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.size(16.dp))
    HedvigContainedButton({}, Modifier.padding(horizontal = 8.dp)) {
      Text("HedvigContainedButton")
    }
    Spacer(Modifier.size(16.dp))
    HedvigOutlinedButton({}, Modifier.padding(horizontal = 8.dp)) {
      Text("Hedvig LargeOutlinedButton")
    }
    Spacer(Modifier.size(16.dp))

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
