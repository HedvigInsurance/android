package com.hedvig.android.sample.design.showcase.materialBottomSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken

@Composable
internal fun MaterialExperiment() {
  var showBottomSheetOne by remember { mutableStateOf(false) }
  if (showBottomSheetOne) {
    LongSheetWithEditTextDefaultDescriptions({
      showBottomSheetOne = false
    })
  }
  var showBottomSheetTwo by remember { mutableStateOf(false) }
  if (showBottomSheetTwo) {
    LongSheetWithTextAndButtonsCustomDescription({
      showBottomSheetTwo = false
    },
      )
  }
  Column(
    Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.SpaceAround,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    HedvigButton(
      onClick = {
        showBottomSheetOne = true
      },
      enabled = true,
      text = "Open bottom sheet with edit text",
      modifier = Modifier.fillMaxWidth(),
    )
    HedvigButton(
      onClick = {
        showBottomSheetTwo = true
      },
      enabled = true,
      text = "Open bottom sheet with text and buttons",
      modifier = Modifier.fillMaxWidth(),
    )
    HedvigText("Quite long text that we don't want to be read by TalkBack " +
      "if the bottom sheet is expanded.")
  }
}

@Composable
private fun LongSheetWithEditTextDefaultDescriptions(onDismissRequest: () -> Unit) {
  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
  ) {
    Column(
      Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically
            ) {
            HedvigText("Yellow daffodils")
          }
        },
        endSlot = {
          HedvigButton(
            onClick = {},
            enabled = true,
            text = "See price",
            modifier = Modifier.fillMaxWidth(),
          )
        },
        spaceBetween = 8.dp,
      )
      EditTextForSheet()
      HedvigText(LOREM_IPSUM)
    }
  }
}

@Composable
private fun LongSheetWithTextAndButtonsCustomDescription(
  onDismissRequest: () -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState()) {
  val description = when (sheetState.currentValue) {
    SheetValue.Hidden -> "modal sheet hidden"
    SheetValue.Expanded -> "modal sheet expanded"
    SheetValue.PartiallyExpanded -> "modal sheet partiallyExpanded"
  }
  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    modifier = Modifier.semantics(
      mergeDescendants = true
    ){
      contentDescription = description
    }
  ) {
    Column(
      Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            HedvigText("Blue daffodils")
          }
        },
        endSlot = {
          HedvigButton(
            onClick = {},
            enabled = true,
            text = "See price",
            modifier = Modifier.fillMaxWidth(),
          )
        },
        spaceBetween = 8.dp,
      )
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            HedvigText("Space daffodils")
          }
        },
        endSlot = {
          HedvigButton(
            onClick = {},
            enabled = true,
            text = "See price",
            modifier = Modifier.fillMaxWidth(),
          )
        },
        spaceBetween = 8.dp,
      )
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            HedvigText("Alien daffodils")
          }
        },
        endSlot = {
          HedvigButton(
            onClick = {},
            enabled = true,
            text = "See price",
            modifier = Modifier.fillMaxWidth(),
          )
        },
        spaceBetween = 8.dp,
      )
      HedvigText(LOREM_IPSUM)
      EditTextForSheet()
    }
  }
}

@Composable
private fun EditTextForSheet() {
  var value by remember { mutableStateOf<String?>(null) }
  HedvigTextField(
    text = value ?: "",
    onValueChange = {
      value = it
    },
    labelText = "Label",
    textFieldSize = TextFieldSize.Medium,
  )
}


private val LOREM_IPSUM =
  """
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer sodales
laoreet commodo. Phasellus a purus eu risus elementum consequat. Aenean eu
elit ut nunc convallis laoreet non ut libero. Suspendisse interdum placerat
risus vel ornare. Donec vehicula, turpis sed consectetur ullamcorper, ante
nunc egestas quam, ultricies adipiscing velit enim at nunc. Aenean id diam
neque. Praesent ut lacus sed justo viverra fermentum et ut sem. Fusce
convallis gravida lacinia. Integer semper dolor ut elit sagittis lacinia.
Praesent sodales scelerisque eros at rhoncus. Duis posuere sapien vel ipsum
ornare interdum at eu quam. Vestibulum vel massa erat. Aenean quis sagittis
purus. Phasellus arcu purus, rutrum id consectetur non, bibendum at nibh.

Duis nec erat dolor. Nulla vitae consectetur ligula. Quisque nec mi est. Ut
quam ante, rutrum at pellentesque gravida, pretium in dui. Cras eget sapien
velit. Suspendisse ut sem nec tellus vehicula eleifend sit amet quis velit.
Phasellus quis suscipit nisi. Nam elementum malesuada tincidunt. Curabitur
iaculis pretium eros, malesuada faucibus leo eleifend a. Curabitur congue
orci in neque euismod a blandit libero vehicula.
"""
