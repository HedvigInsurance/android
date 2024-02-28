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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ArrowBack

@Composable
internal fun M3TopAppBars() {
  Column {
    Spacer(Modifier.size(16.dp))
    M3OnSurfaceText(
      text = "Top app bars",
      style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.size(16.dp))
    M3OnSurfaceText(
      text = "Not Scrolled State",
      style = MaterialTheme.typography.labelLarge,
    )
    Spacer(Modifier.size(4.dp))
    TopAppBar(
      title = { Text("Small top app bar") },
      navigationIcon = { NavigationIcon() },
    )
    Spacer(modifier = Modifier.size(16.dp))
    M3OnSurfaceText(
      text = "Scrolled State",
      style = MaterialTheme.typography.labelLarge,
    )
    Spacer(Modifier.size(4.dp))
    // Hacky scroll state to show how the toolbar will look if its scrolled state
    val scrollState = TopAppBarState(-1f, 0f, -2f)
    TopAppBar(
      title = { Text("Small top app bar") },
      navigationIcon = { NavigationIcon() },
      scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(scrollState),
    )
  }
}

@Composable
private fun NavigationIcon() {
  IconButton(onClick = {}) {
    Icon(
      imageVector = Icons.Hedvig.ArrowBack,
      contentDescription = null,
    )
  }
}
