package com.hedvig.android.sample.design.showcase.radio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.showcase.R
import com.hedvig.android.design.system.hedvig.CheckboxGroup
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.IconResource
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface

@Composable
internal fun ShowcaseRadioGroups(modifier: Modifier = Modifier) {
  Column(
    modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      RadioGroupOptions()
      CheckboxGroupOptions()
    }
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.RadioGroupOptions() {
  HedvigText("Sizes")
  var selectedOption by remember { mutableStateOf(RadioOptionId("1")) }
  val defaultOptions = List(2) { index ->
    RadioOption(
      RadioOptionId(index.toString()),
      "Option",
      null,
    )
  }
  for (size in RadioGroupSize.entries) {
    RadioGroup(
      options = defaultOptions,
      selectedOption = selectedOption,
      onRadioOptionSelected = { selectedOption = it },
      size = size,
    )
  }
  HedvigText("Disabled")
  RadioGroup(
    options = defaultOptions,
    selectedOption = selectedOption,
    onRadioOptionSelected = { selectedOption = it },
    enabled = false,
  )
  HedvigText("Labeled")
  RadioGroup(
    options = defaultOptions.map {
      it.copy(label = "Label")
    },
    selectedOption = selectedOption,
    onRadioOptionSelected = { selectedOption = it },
  )
  HedvigText("Icon")
  RadioGroup(
    options = defaultOptions.map {
      it.copy(iconResource = IconResource.Painter(R.drawable.ic_pillow_cat))
    },
    selectedOption = selectedOption,
    onRadioOptionSelected = { selectedOption = it },
  )
  HedvigText("Left Aligned")
  RadioGroup(
    options = defaultOptions,
    selectedOption = selectedOption,
    onRadioOptionSelected = { selectedOption = it },
    style = RadioGroupStyle.LeftAligned,
  )
  HedvigText("Horizontal")
  RadioGroup(
    options = defaultOptions,
    selectedOption = selectedOption,
    onRadioOptionSelected = { selectedOption = it },
    style = RadioGroupStyle.Horizontal,
  )
  HedvigText("Horizontal with label")
  RadioGroup(
    options = defaultOptions,
    selectedOption = selectedOption,
    style = RadioGroupStyle.Labeled.HorizontalFlow("Label"),
    onRadioOptionSelected = { selectedOption = it },
  )
  HedvigText("Vertical with label and divider")
  RadioGroup(
    options = defaultOptions,
    selectedOption = selectedOption,
    style = RadioGroupStyle.Labeled.VerticalWithDivider("Label"),
    onRadioOptionSelected = { selectedOption = it },
  )
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.CheckboxGroupOptions() {
  HedvigText("Sizes")
  val selectedOptions = remember { mutableStateSetOf(RadioOptionId("1")) }
  val onRadioOptionSelected: (RadioOptionId) -> Unit = {
    Snapshot.withMutableSnapshot {
      if (!selectedOptions.add(it)) {
        selectedOptions.remove(it)
      }
    }
  }
  val defaultOptions = List(2) { index ->
    RadioOption(
      RadioOptionId(index.toString()),
      "Option",
      null,
    )
  }
  for (size in RadioGroupSize.entries) {
    CheckboxGroup(
      options = defaultOptions,
      selectedOptions = selectedOptions.toList(),
      onRadioOptionSelected = onRadioOptionSelected,
      size = size,
    )
  }
  HedvigText("Disabled")
  CheckboxGroup(
    options = defaultOptions,
    selectedOptions = selectedOptions.toList(),
    onRadioOptionSelected = onRadioOptionSelected,
    enabled = false,
  )
  HedvigText("Labeled")
  CheckboxGroup(
    options = defaultOptions.map {
      it.copy(label = "Label")
    },
    selectedOptions = selectedOptions.toList(),
    onRadioOptionSelected = onRadioOptionSelected,
  )
  HedvigText("Icon")
  CheckboxGroup(
    options = defaultOptions.map {
      it.copy(iconResource = IconResource.Painter(R.drawable.ic_pillow_cat))
    },
    selectedOptions = selectedOptions.toList(),
    onRadioOptionSelected = onRadioOptionSelected,
  )
  HedvigText("Left Aligned")
  CheckboxGroup(
    options = defaultOptions,
    selectedOptions = selectedOptions.toList(),
    onRadioOptionSelected = onRadioOptionSelected,
    style = RadioGroupStyle.LeftAligned,
  )
  HedvigText("Horizontal")
  CheckboxGroup(
    options = defaultOptions,
    selectedOptions = selectedOptions.toList(),
    onRadioOptionSelected = onRadioOptionSelected,
    style = RadioGroupStyle.Horizontal,
  )
  HedvigText("Horizontal with label")
  CheckboxGroup(
    options = defaultOptions,
    selectedOptions = selectedOptions.toList(),
    style = RadioGroupStyle.Labeled.HorizontalFlow("Label"),
    onRadioOptionSelected = onRadioOptionSelected,
  )
  HedvigText("Vertical with label and divider")
  CheckboxGroup(
    options = defaultOptions,
    selectedOptions = selectedOptions.toList(),
    style = RadioGroupStyle.Labeled.VerticalWithDivider("Label"),
    onRadioOptionSelected = onRadioOptionSelected,
  )
}

@HedvigPreview
@Composable
private fun PreviewShowcaseRadioGroups() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ShowcaseRadioGroups()
    }
  }
}
