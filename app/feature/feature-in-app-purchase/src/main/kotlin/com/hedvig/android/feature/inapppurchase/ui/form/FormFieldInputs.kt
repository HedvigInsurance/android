package com.hedvig.android.feature.inapppurchase.ui.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerImmutableState
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFormField
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

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
  errorMessage: String?,
  enabled: Boolean,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigCard {
      RadioGroup(
        options = field.options.map { option ->
          RadioOption(id = RadioOptionId(option.value), text = option.title, label = option.additionalInfo)
        },
        selectedOption = value?.let { RadioOptionId(it) },
        onRadioOptionSelected = { onValueChange(it.id) },
        disabledOptions = field.options.filterNot { it.isSelectable }.map { RadioOptionId(it.value) },
        enabled = enabled,
        style = RadioGroupStyle.Labeled.VerticalWithDivider(field.title),
      )
    }
    if (errorMessage != null) {
      HedvigText(
        text = errorMessage,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.signalRedText,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
      )
    }
  }
}
