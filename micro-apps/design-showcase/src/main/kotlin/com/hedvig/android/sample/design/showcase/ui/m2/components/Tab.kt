package com.hedvig.android.sample.design.showcase.ui.m2.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun M2Tab() {
  var selectedIndex by remember { mutableStateOf(0) }
  val tabs = listOf("Accounts", "Cards", "Funds")
  Column {
    Spacer(Modifier.size(16.dp))
    M2OnSurfaceText(
      text = "Tab",
      style = MaterialTheme.typography.h5,
    )
    Spacer(Modifier.size(16.dp))
    TabRow(selectedTabIndex = selectedIndex) {
      tabs.forEachIndexed { index, title ->
        Tab(
          text = { Text(title) },
          selected = index == selectedIndex,
          onClick = { selectedIndex = index },
        )
      }
    }
  }
}
