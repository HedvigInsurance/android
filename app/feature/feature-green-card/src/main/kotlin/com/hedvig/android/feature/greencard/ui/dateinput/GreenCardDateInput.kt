package com.hedvig.android.feature.greencard.ui.dateinput

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.a11y.accessibilityForDatePickerField
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.api.HedvigDisplayMode
import com.hedvig.android.design.system.hedvig.clearFocusOnControlTap
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.greencard.data.GreenCardContractSpecification
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import hedvig.resources.travel_certificate_start_date_title
import java.util.Locale
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GreenCardDateInputDestination(viewModel: GreenCardDateInputViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  GreenCardDateInputScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    reload = { viewModel.emit(GreenCardDateInputEvent.RetryLoadData) },
    onDateChanged = { viewModel.emit(GreenCardDateInputEvent.ClearDateError) },
    submit = { viewModel.emit(GreenCardDateInputEvent.Submit) },
  )
}

@Composable
private fun GreenCardDateInputScreen(
  uiState: GreenCardDateInputUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  onDateChanged: () -> Unit,
  submit: () -> Unit,
) {
  when (uiState) {
    GreenCardDateInputUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    GreenCardDateInputUiState.Failure -> {
      HedvigScaffold(navigateUp = navigateUp) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    is GreenCardDateInputUiState.Success -> {
      val form = uiState.form
      HedvigScaffold(navigateUp = navigateUp) {
        Spacer(Modifier.height(8.dp))
        FlowHeading(
          // TODO: Add "When are you travelling?" / "När reser du?" to Lokalise
          "When are you travelling?",
          form.specification.registrationNumber,
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        DateField(
          label = stringResource(Res.string.travel_certificate_start_date_title),
          datePickerState = form.startDatePickerState,
          onDateChanged = onDateChanged,
          errorText = when (uiState.dateError) {
            // TODO: Add "Select a departure date" / "Välj ett avresedatum" to Lokalise
            DateError.MissingStartDate -> "Select a departure date"

            else -> null
          },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(4.dp))
        DateField(
          // TODO: Add "Return date" / "Hemresedatum" to Lokalise
          label = "Return date",
          datePickerState = form.endDatePickerState,
          onDateChanged = onDateChanged,
          errorText = when (uiState.dateError) {
            // TODO: Add "Select a return date" / "Välj ett hemresedatum" to Lokalise
            DateError.MissingEndDate -> "Select a return date"

            // TODO: Add "The return date can't be before the departure date" / "Hemresedatumet kan inte vara före avresedatumet" to Lokalise
            DateError.EndBeforeStart -> "The return date can't be before the departure date"

            // TODO: Add "Pick an earlier return date" / "Välj ett tidigare hemresedatum" to Lokalise
            DateError.EndTooLate -> "Pick an earlier return date"

            else -> null
          },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(Res.string.general_continue_button),
          onClick = submit,
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun DateField(
  label: String,
  datePickerState: HedvigDatePickerState,
  onDateChanged: () -> Unit,
  errorText: String?,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val locale = getLocale()
  val dateFormatter = remember(locale) { HedvigDateTimeFormatterDefaults.dateMonthAndYear(locale) }
  if (showDatePicker) {
    HedvigDatePicker(
      datePickerState = datePickerState,
      onDismissRequest = { showDatePicker = false },
      onConfirmRequest = {
        showDatePicker = false
        onDateChanged()
      },
    )
  }
  val text = datePickerState.selectedDate()?.let { dateFormatter.format(it) }.orEmpty()
  val onClick = {
    focusManager.clearFocus()
    showDatePicker = true
  }
  Box(
    modifier.accessibilityForDatePickerField(
      labelText = label,
      selectedDate = text,
      isEnabled = true,
      onClick = onClick,
    ),
  ) {
    HedvigTextField(
      text = text,
      onValueChange = {},
      readOnly = true,
      enabled = true,
      labelText = label,
      textFieldSize = Medium,
      errorState = if (errorText != null) ErrorState.Error.WithMessage(errorText) else ErrorState.NoError,
      trailingContent = {},
    )
    Box(
      Modifier
        .matchParentSize()
        .clip(HedvigTheme.shapes.cornerLarge)
        .clearFocusOnControlTap()
        .clickable(onClick = onClick),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewGreenCardDateInput() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      GreenCardDateInputScreen(
        uiState = GreenCardDateInputUiState.Success(
          form = GreenCardForm(
            specification = GreenCardContractSpecification(
              contractId = "id",
              registrationNumber = "ABC 123",
              minStartDate = LocalDate(2026, 11, 18),
              maxEndDate = LocalDate(2027, 2, 18),
              maxPeriodDays = 90,
            ),
            startDatePickerState = HedvigDatePickerState(
              Locale.ENGLISH,
              null,
              null,
              2026..2027,
              HedvigDisplayMode.Picker,
            ),
            endDatePickerState = HedvigDatePickerState(
              Locale.ENGLISH,
              null,
              null,
              2026..2027,
              HedvigDisplayMode.Picker,
            ),
          ),
          dateError = DateError.MissingEndDate,
        ),
        navigateUp = {},
        reload = {},
        onDateChanged = {},
        submit = {},
      )
    }
  }
}
