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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun M3Cards() {
  Column {
    Spacer(Modifier.size(16.dp))
    M3OnSurfaceText(
      text = "Cards",
      style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.size(16.dp))
    Card(
      onClick = {},
      modifier = Modifier.size(width = 180.dp, height = 100.dp),
    ) {
      Box(Modifier.fillMaxSize()) {
        Text("Card", Modifier.align(Alignment.Center))
      }
    }
    Spacer(Modifier.size(16.dp))
    ElevatedCard(
      onClick = {},
      modifier = Modifier.size(width = 180.dp, height = 100.dp),
    ) {
      Box(Modifier.fillMaxSize()) {
        Text("Elevated card", Modifier.align(Alignment.Center))
      }
    }
    Spacer(Modifier.size(16.dp))
    OutlinedCard(
      onClick = {},
      modifier = Modifier.size(width = 180.dp, height = 100.dp),
    ) {
      Box(Modifier.fillMaxSize()) {
        Text("Outlined card", Modifier.align(Alignment.Center))
      }
    }
    Spacer(Modifier.size(16.dp))
    M3OnSurfaceText(
      text = "Higher elevated cards",
      style = MaterialTheme.typography.labelLarge,
    )
    Spacer(Modifier.size(4.dp))
    Card(
      onClick = {},
      modifier = Modifier.size(width = 180.dp, height = 100.dp),
      elevation = CardDefaults.cardElevation(32.dp),
    ) {
      Box(Modifier.fillMaxSize()) {
        Text("Card", Modifier.align(Alignment.Center))
      }
    }
    Spacer(Modifier.size(16.dp))
    ElevatedCard(
      onClick = {},
      modifier = Modifier.size(width = 180.dp, height = 100.dp),
      elevation = CardDefaults.elevatedCardElevation(32.dp),
    ) {
      Box(Modifier.fillMaxSize()) {
        Text("Elevated card 32.dp", Modifier.align(Alignment.Center))
      }
    }
    Spacer(Modifier.size(16.dp))
    OutlinedCard(
      onClick = {},
      modifier = Modifier.size(width = 180.dp, height = 100.dp),
      elevation = CardDefaults.outlinedCardElevation(32.dp),
    ) {
      Box(Modifier.fillMaxSize()) {
        Text("Outlined card 32.dp", Modifier.align(Alignment.Center))
      }
    }
  }
}
