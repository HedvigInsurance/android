package com.hedvig.android.sample.design.showcase.dropdown

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.DropdownDefaults
import com.hedvig.android.design.system.hedvig.DropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@Composable
fun DropdownShowcase() {
  Surface(
    modifier = Modifier
      .fillMaxSize(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    var chosenIndex by remember { mutableStateOf<Int?>(null) }
    Column(
      modifier = Modifier
        .safeContentPadding()
        .fillMaxSize()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(16.dp))
      DropdownWithDialog(
        style = DropdownDefaults.DropdownStyle.Default(
          simpleItems
        ),
        chosenItemIndex = chosenIndex,
        hintText = "Select...",
        onItemChosen = {
          chosenIndex = it
        },
        size = DropdownDefaults.DropdownSize.Small
      )
    }
  }
}

private val simpleItems = listOf(
  DropdownItem.SimpleDropdownItem("Home"),
  DropdownItem.SimpleDropdownItem("Cat"),
  DropdownItem.SimpleDropdownItem("UFO"),
  DropdownItem.SimpleDropdownItem("Canary")
)
