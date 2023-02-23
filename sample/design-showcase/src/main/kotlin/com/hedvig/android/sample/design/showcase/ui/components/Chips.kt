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

package com.hedvig.android.sample.design.showcase.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun M3Chips() {
  Column {
    Spacer(Modifier.size(16.dp))
    M3OnSurfaceText(
      text = "Chips",
      style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.size(16.dp))
    var selectedId by remember { mutableStateOf(1) }
    FilterChip(
      label = { Text("Android") },
      onClick = { selectedId = 1 },
      selected = selectedId == 1,
    )
    Spacer(modifier = Modifier.size(4.dp))
    FilterChip(
      label = { Text("Material") },
      onClick = { selectedId = 2 },
      selected = selectedId == 2,
    )
    Spacer(modifier = Modifier.size(4.dp))
    FilterChip(
      label = { Text("Compose") },
      onClick = { selectedId = 3 },
      selected = selectedId == 3,
    )
  }
}
