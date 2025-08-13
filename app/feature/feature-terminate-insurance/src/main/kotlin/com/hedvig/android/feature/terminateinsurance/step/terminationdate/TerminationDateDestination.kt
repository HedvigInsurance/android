package com.hedvig.android.feature.terminateinsurance.step.terminationdate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxSize.Small
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerImmutableState
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardDate
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardInsurance
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R
import java.util.Locale
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TerminationDateDestination(
  viewModel: TerminationDateViewModel,
  onContinue: (LocalDate) -> Unit,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TerminationDateScreen(
    uiState = uiState,
    submit = {
      uiState.datePickerState.selectedDateMillis?.let {
        val date = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
        onContinue(date)
      }
    },
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
    onCheckedChange = {
      viewModel.changeCheckBoxState()
    },
    changeSelectedDate = { long ->
      viewModel.changeSelectedDate(long)
    },
  )
}

@Composable
private fun TerminationDateScreen(
  uiState: TerminateInsuranceUiState,
  submit: () -> Unit,
  navigateUp: () -> Unit,
  changeSelectedDate: (Long?) -> Unit,
  closeTerminationFlow: () -> Unit,
  onCheckedChange: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) { title ->
    FlowHeading(
      title,
      stringResource(id = R.string.TERMINATION_DATE_TEXT),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    TerminationInfoCardInsurance(
      displayName = uiState.displayName,
      exposureName = uiState.exposureName,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(4.dp))
    DateButton(
      datePickerState = uiState.datePickerState,
      modifier = Modifier.padding(horizontal = 16.dp),
      onSelectedDateChange = { long ->
        changeSelectedDate(long)
      },
    )
    if (uiState.datePickerState.selectedDateMillis != null) {
      Spacer(modifier = Modifier.height(4.dp))
      ImportantInfoCheckBox(
        uiState.isCheckBoxChecked,
        onCheckedChange,
        Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))

    HedvigButton(
      text = stringResource(id = R.string.general_continue_button),
      onClick = submit,
      enabled = uiState.canSubmit,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )

    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun ImportantInfoCheckBox(isChecked: Boolean, onCheckedChange: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        HedvigText(
          text = stringResource(id = R.string.TERMINATION_FLOW_IMPORTANT_INFORMATION_TITLE),
          style = HedvigTheme.typography.headlineSmall,
        )
        HedvigText(
          text = stringResource(id = R.string.TERMINATION_FLOW_IMPORTANT_INFORMATION_TEXT),
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
        Spacer(Modifier.height(16.dp))
        HedvigTheme(darkTheme = false) {
          Checkbox(
            checkboxSize = Small,
            optionText = stringResource(R.string.TERMINATION_FLOW_I_UNDERSTAND_TEXT),
            chosenState = if (isChecked) Chosen else NotChosen,
            onClick = onCheckedChange,
            containerColor = HedvigTheme.colorScheme.fillNegative,
          )
        }
      }
    }
  }
}

@Composable
private fun DateButton(
  datePickerState: HedvigDatePickerImmutableState,
  onSelectedDateChange: (Long?) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  HedvigDatePicker(
    datePickerState = datePickerState,
    isVisible = showDatePicker,
    onDismissRequest = { showDatePicker = false },
    onConfirmRequest = { showDatePicker = false },
    onSelectedDateChanged = onSelectedDateChange,
  )
  TerminationInfoCardDate(
    dateValue = datePickerState.selectedDateMillis?.let {
      Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
    }?.toString(),
    onClick = { showDatePicker = true },
    isLocked = false,
    modifier = modifier,
  )
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewTerminationDateScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationDateScreen(
        TerminateInsuranceUiState(
          HedvigDatePickerImmutableState(
            null,
            null,
            2015..2023,
            locale = Locale.ROOT,
            maxDateInMillis = 2222L,
            minDateInMillis = 1111L,
          ),
          false,
          "Bullegatan 34",
          "Homeowner insurance",
          true,
        ),
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewImportantInfoCheckBox() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ImportantInfoCheckBox(
        true,
        {},
      )
    }
  }
}
