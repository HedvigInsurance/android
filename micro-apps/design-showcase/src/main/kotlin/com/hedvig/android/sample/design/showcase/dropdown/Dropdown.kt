package com.hedvig.android.sample.design.showcase.dropdown

import androidx.compose.animation.AnimatedVisibility
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
import com.hedvig.android.design.showcase.R
import com.hedvig.android.design.system.hedvig.DropdownDefaults
import com.hedvig.android.design.system.hedvig.DropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.IconResource
import com.hedvig.android.design.system.hedvig.Surface

@Composable
fun DropdownShowcase() {
  Surface(
    modifier = Modifier
      .fillMaxSize(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    var chosenIndex by remember { mutableStateOf<Int?>(null) }
    var error by remember { mutableStateOf(false) }
    var chosenIndex2 by remember { mutableStateOf<Int?>(null) }
    var error2 by remember { mutableStateOf(false) }
    var chosenIndex3 by remember { mutableStateOf<Int?>(null) }
    var error3 by remember { mutableStateOf(false) }
    var showAllGood by remember { mutableStateOf(false) }
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
          simpleItems,
        ),
        chosenItemIndex = chosenIndex,
        hintText = "Select...",
        onItemChosen = {
          chosenIndex = it
        },
        hasError = error,
        size = DropdownDefaults.DropdownSize.Large,
        errorText = "You must select something",
        onSelectorClick = {
          error = false
        },
      )
      Spacer(Modifier.height(16.dp))
      DropdownWithDialog(
        style = DropdownDefaults.DropdownStyle.Label(
          simpleItems,
          "Label",
        ),
        chosenItemIndex = chosenIndex2,
        hintText = "Select...",
        onItemChosen = {
          chosenIndex2 = it
        },
        hasError = error2,
        size = DropdownDefaults.DropdownSize.Medium,
        errorText = "You must select something",
        onSelectorClick = {
          error2 = false
        },
      )
      Spacer(Modifier.height(16.dp))
      DropdownWithDialog(
        style = DropdownDefaults.DropdownStyle.Icon(
          iconItems,
          IconResource.Painter(R.drawable.ic_pillow_cat),
        ),
        chosenItemIndex = chosenIndex3,
        hintText = "Select...",
        onItemChosen = {
          chosenIndex3 = it
        },
        hasError = error3,
        size = DropdownDefaults.DropdownSize.Small,
        errorText = "You must select something",
        onSelectorClick = {
          error3 = false
        },
      )
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = "Check selection",
        enabled = true,
        onClick = {
          error = chosenIndex == null
          error2 = chosenIndex2 == null
          error3 = chosenIndex3 == null
          showAllGood = !error2 && !error && !error3
        },
      )
      Spacer(Modifier.height(8.dp))
      AnimatedVisibility(showAllGood) {
        HedvigText("All good!")
      }
    }
  }
}

private val simpleItems = listOf(
  DropdownItem.SimpleDropdownItem("Home"),
  DropdownItem.SimpleDropdownItem("Cat"),
  DropdownItem.SimpleDropdownItem("UFO"),
  DropdownItem.SimpleDropdownItem("Canary"),
)
private val iconItems = listOf(
  DropdownItem.DropdownItemWithIcon("Home", IconResource.Painter(R.drawable.ic_pillow_cat)),
  DropdownItem.DropdownItemWithIcon("Cat", IconResource.Painter(R.drawable.ic_pillow_homeowner)),
  DropdownItem.DropdownItemWithIcon("UFO", IconResource.Painter(R.drawable.ic_pillow_dog)),
  DropdownItem.DropdownItemWithIcon("Canary", IconResource.Painter(R.drawable.ic_pillow_home)),
)
