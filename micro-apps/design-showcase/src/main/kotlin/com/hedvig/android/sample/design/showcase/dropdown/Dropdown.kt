package com.hedvig.android.sample.design.showcase.dropdown

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
    var showAllGood by remember { mutableStateOf(false) }
    val listOfCombo = buildList {
      for (style in styles) {
        for (size in sizes) {
          add(Pair(style, size))
        }
      }
    }
    val hasErrorMap = remember {
      mutableStateMapOf(*listOfCombo.indices.map { it to false }.toTypedArray())
    }
    val chosenMap = remember {
      mutableStateMapOf<Int, Int?>(*listOfCombo.indices.map { it to null }.toTypedArray())
    }
    Column(
      modifier = Modifier
        .safeContentPadding()
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Spacer(Modifier.height(16.dp))
      listOfCombo.forEachIndexed { index: Int, combo ->
        DropdownWithDialog(
          style = combo.first,
          chosenItemIndex = chosenMap[index],
          hintText = "Select...",
          onItemChosen = {
            chosenMap[index] = it
          },
          hasError = hasErrorMap[index] ?: false,
          size = combo.second,
          errorText = "You must select something",
          onSelectorClick = {
            hasErrorMap[index] = false
          },
        )
      }
      AnimatedVisibility(showAllGood) {
        HedvigText("All good!")
      }
      HedvigButton(
        text = "Check selection",
        enabled = true,
        onClick = {
          val indicesWithNoChoice = chosenMap.filter {
            it.value == null
          }.keys
          indicesWithNoChoice.forEach {
            hasErrorMap[it] = true
          }
          showAllGood = !chosenMap.containsValue(null)
        },
      )
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

private val styles = listOf(
  DropdownDefaults.DropdownStyle.Default(simpleItems),
  DropdownDefaults.DropdownStyle.Label(
    simpleItems,
    "Label",
  ),
  DropdownDefaults.DropdownStyle.Icon(
    iconItems,
    IconResource.Painter(R.drawable.ic_pillow_cat),
  ),
)

private val sizes = listOf(
  DropdownDefaults.DropdownSize.Small,
  DropdownDefaults.DropdownSize.Medium,
  DropdownDefaults.DropdownSize.Large,
)
