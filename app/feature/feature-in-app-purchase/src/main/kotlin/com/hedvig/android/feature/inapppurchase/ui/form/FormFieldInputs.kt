package com.hedvig.android.feature.inapppurchase.ui.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DropdownDefaults
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.a11y.accessibilityForDropdown
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerImmutableState
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFormField
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

/**
 * Renders a server-driven field. Values are the strings sent back to the backend: an integer for NUMBER, an ISO
 * date for DATE and the option's value for SINGLE_SELECT.
 */
@Composable
internal fun FormFieldInput(
  state: FormFieldUiState,
  enabled: Boolean,
  onValueChange: (String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (val field = state.field) {
    is InAppPurchaseFormField.Number -> NumberFieldInput(
      field = field,
      value = state.value?.toIntOrNull(),
      errorMessage = state.errorMessage,
      enabled = enabled,
      onValueChange = { onValueChange(it.toString()) },
      modifier = modifier,
    )

    is InAppPurchaseFormField.Date -> DateFieldInput(
      field = field,
      value = state.value?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
      errorMessage = state.errorMessage,
      enabled = enabled,
      onValueChange = { onValueChange(it.toString()) },
      modifier = modifier,
    )

    is InAppPurchaseFormField.SingleSelect -> SingleSelectFieldInput(
      field = field,
      value = state.value,
      selectedOption = state.selectedOption,
      errorMessage = state.errorMessage,
      enabled = enabled,
      onValueChange = onValueChange,
      modifier = modifier,
    )
  }
}

@Composable
private fun NumberFieldInput(
  field: InAppPurchaseFormField.Number,
  value: Int?,
  errorMessage: String?,
  enabled: Boolean,
  onValueChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val current = value ?: field.minValue ?: 0
  HedvigStepper(
    text = current.toString(),
    stepperSize = Medium,
    stepperStyle = Labeled(field.title),
    onMinusClick = { onValueChange(current - 1) },
    onPlusClick = { onValueChange(current + 1) },
    isMinusEnabled = enabled && (field.minValue == null || current > field.minValue),
    isPlusEnabled = enabled && (field.maxValue == null || current < field.maxValue),
    errorText = errorMessage,
    modifier = modifier,
  )
}

@Composable
private fun DateFieldInput(
  field: InAppPurchaseFormField.Date,
  value: LocalDate?,
  errorMessage: String?,
  enabled: Boolean,
  onValueChange: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  val locale = getLocale()
  val selectedDateMillis = value?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
  HedvigDatePicker(
    isVisible = showDatePicker,
    datePickerState = HedvigDatePickerImmutableState(
      selectedDateMillis = selectedDateMillis,
      displayedMonthMillis = selectedDateMillis,
      yearRange = IntRange(
        start = field.minValue?.year ?: value?.year ?: 2000,
        endInclusive = field.maxValue?.year ?: ((value?.year ?: 2000) + 1),
      ),
      minDateInMillis = field.minValue?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds() ?: Long.MIN_VALUE,
      maxDateInMillis = field.maxValue?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds() ?: Long.MAX_VALUE,
      locale = locale,
    ),
    onDismissRequest = { showDatePicker = false },
    onConfirmRequest = { showDatePicker = false },
    onSelectedDateChanged = { millis ->
      if (millis != null) {
        onValueChange(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date)
      }
    },
  )
  val formatter = remember(locale) { HedvigDateTimeFormatterDefaults.dateMonthAndYear(locale) }
  // A read-only text field gives the field its look; the overlay opens the picker.
  Box(modifier) {
    HedvigTextField(
      text = value?.let { formatter.format(it) } ?: "",
      onValueChange = {},
      readOnly = true,
      enabled = enabled,
      labelText = field.title,
      textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
      trailingContent = {},
      errorState = if (errorMessage != null) {
        HedvigTextFieldDefaults.ErrorState.Error.WithMessage(errorMessage)
      } else {
        HedvigTextFieldDefaults.ErrorState.NoError
      },
    )
    Box(
      Modifier
        .matchParentSize()
        .clip(HedvigTheme.shapes.cornerLarge)
        .clickable(enabled = enabled) { showDatePicker = true },
    )
  }
}

@Composable
private fun SingleSelectFieldInput(
  field: InAppPurchaseFormField.SingleSelect,
  value: String?,
  selectedOption: InAppPurchaseFormField.SingleSelect.Option?,
  errorMessage: String?,
  enabled: Boolean,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    val selectedIndex = field.options.indexOfFirst { it.value == value }.takeIf { it >= 0 }
    DropdownWithDialog(
      dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
      style = Label(label = field.title, items = field.options.map { SimpleDropdownItem(it.title) }),
      size = DropdownDefaults.DropdownSize.Medium,
      hintText = field.title,
      chosenItemIndex = selectedIndex,
      isEnabled = enabled,
      hasError = errorMessage != null,
      errorText = errorMessage,
      modifier = Modifier.accessibilityForDropdown(
        labelText = field.title,
        selectedValue = selectedOption?.title ?: field.title,
        isEnabled = enabled,
      ),
    ) { onDismissRequest ->
      SingleSelectDialogContent(
        field = field,
        initiallySelectedValue = value,
        onConfirm = { chosenValue ->
          if (chosenValue != null) onValueChange(chosenValue)
          onDismissRequest()
        },
        onCancel = onDismissRequest,
      )
    }
    val additionalInfo = selectedOption?.additionalInfo
    if (additionalInfo != null) {
      Spacer(Modifier.height(4.dp))
      HedvigNotificationCard(
        message = additionalInfo,
        priority = NotificationDefaults.NotificationPriority.Info,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun SingleSelectDialogContent(
  field: InAppPurchaseFormField.SingleSelect,
  initiallySelectedValue: String?,
  onConfirm: (String?) -> Unit,
  onCancel: () -> Unit,
) {
  var selectedInDialog by remember { mutableStateOf(initiallySelectedValue) }
  Column(Modifier.verticalScroll(rememberScrollState())) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = field.title,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .semantics { heading() },
    )
    Spacer(Modifier.height(24.dp))
    RadioGroup(
      options = field.options.map { option -> RadioOption(id = RadioOptionId(option.value), text = option.title) },
      selectedOption = selectedInDialog?.let { RadioOptionId(it) },
      onRadioOptionSelected = { selectedInDialog = it.id },
      style = RadioGroupStyle.LeftAligned,
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      onClick = { onConfirm(selectedInDialog) },
      enabled = selectedInDialog != null,
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
