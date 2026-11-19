package com.hedvig.android.feature.inapppurchase.ui.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.CheckboxGroup
import com.hedvig.android.design.system.hedvig.DropdownDefaults
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.a11y.accessibilityForDropdown
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFormField
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFormField.SingleSelect.Option
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchableSelectFieldInput(
  field: InAppPurchaseFormField.SearchableSelect,
  values: List<String>,
  errorMessage: String?,
  enabled: Boolean,
  onValuesChange: (List<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectedTitles = field.options.filter { it.value in values }.joinToString { it.title }
  DropdownWithDialog(
    dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    style = Label(label = field.title, items = listOf(SimpleDropdownItem(selectedTitles))),
    size = DropdownDefaults.DropdownSize.Medium,
    hintText = field.title,
    chosenItemIndex = if (selectedTitles.isEmpty()) null else 0,
    isEnabled = enabled,
    hasError = errorMessage != null,
    errorText = errorMessage,
    modifier = modifier.accessibilityForDropdown(
      labelText = field.title,
      selectedValue = selectedTitles.ifEmpty { field.title },
      isEnabled = enabled,
    ),
  ) { onDismissRequest ->
    SearchableSelectDialogContent(
      field = field,
      initiallySelected = values,
      onConfirm = { chosen ->
        onValuesChange(chosen)
        onDismissRequest()
      },
      onCancel = onDismissRequest,
    )
  }
}

@Composable
private fun SearchableSelectDialogContent(
  field: InAppPurchaseFormField.SearchableSelect,
  initiallySelected: List<String>,
  onConfirm: (List<String>) -> Unit,
  onCancel: () -> Unit,
) {
  var selected by remember { mutableStateOf(initiallySelected) }
  val queryState = rememberTextFieldState()
  val shownOptions = filterOptions(field.options, queryState.text.toString())
  Column {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = field.title,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .semantics { heading() },
    )
    Spacer(Modifier.height(16.dp))
    HedvigTextField(
      state = queryState,
      // TODO: Add "Search" / "Sök" to Lokalise
      labelText = "Search",
      textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Column(
      Modifier
        .heightIn(max = 360.dp)
        .verticalScroll(rememberScrollState()),
    ) {
      CheckboxGroup(
        options = shownOptions.map { RadioOption(id = RadioOptionId(it.value), text = it.title) },
        selectedOptions = selected.map { RadioOptionId(it) },
        onRadioOptionSelected = { selected = toggle(selected, it.id, field.maxSelections) },
        style = RadioGroupStyle.LeftAligned,
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      onClick = { onConfirm(selected) },
      enabled = selected.isNotEmpty(),
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
      onClick = onCancel,
      buttonSize = ButtonDefaults.ButtonSize.Large,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

internal fun filterOptions(options: List<Option>, query: String): List<Option> {
  val trimmed = query.trim()
  if (trimmed.isEmpty()) return options
  return options.filter { it.title.contains(trimmed, ignoreCase = true) }
}

/**
 * Selecting a chosen [value] removes it. A new value is added while under [maxSelections], or replaces the current one
 * when only one may be chosen.
 */
internal fun toggle(selected: List<String>, value: String, maxSelections: Int): List<String> = when {
  value in selected -> selected - value
  maxSelections == 1 -> listOf(value)
  selected.size < maxSelections -> selected + value
  else -> selected
}
