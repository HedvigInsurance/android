package com.hedvig.android.feature.terminateinsurance.step.terminationdate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.RoundedCornerCheckBox
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardDate
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardInsurance
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R
import kotlinx.datetime.Instant
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
  )
}

@Composable
private fun TerminationDateScreen(
  uiState: TerminateInsuranceUiState,
  submit: () -> Unit,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onCheckedChange: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) {
    Text(
      style = MaterialTheme.typography.headlineSmall.copy(
        lineBreak = LineBreak.Heading,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      ),
      text = stringResource(id = R.string.TERMINATION_DATE_TEXT),
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
    Spacer(modifier = Modifier.height(8.dp))
    DateButton(
      datePickerState = uiState.datePickerState,
      modifier = Modifier,
    )
    if (uiState.datePickerState.selectedDateMillis != null) {
      Spacer(modifier = Modifier.height(8.dp))
      ImportantInfoCheckBox(
        uiState.isCheckBoxChecked,
        onCheckedChange,
        Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(id = R.string.TERMINATION_FLOW_CANCEL_INSURANCE_BUTTON),
      onClick = submit,
      enabled = uiState.canSubmit,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun ImportantInfoCheckBox(isChecked: Boolean, onCheckedChange: () -> Unit, modifier: Modifier = Modifier) {
  HedvigCard(
    onClick = null,
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(id = R.string.TERMINATION_FLOW_IMPORTANT_INFORMATION_TITLE),
          style = MaterialTheme.typography.bodyLarge,
        )
        Text(
          text = stringResource(id = R.string.TERMINATION_FLOW_IMPORTANT_INFORMATION_TEXT),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          onClick = {
            onCheckedChange()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            // todo: not really sure what to do here, the white color is not in the palette.
            //  this element stays the same in light/dark themes
          ),
        ) {
          HorizontalItemsWithMaximumSpaceTaken(
            startSlot = {
              Text(
                textAlign = TextAlign.Start,
                text = stringResource(R.string.TERMINATION_FLOW_I_UNDERSTAND_TEXT),
                style = MaterialTheme.typography.bodyLarge,
              )
            },
            endSlot = {
              Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                RoundedCornerCheckBox(
                  isChecked = isChecked,
                  onCheckedChange = {
                    onCheckedChange()
                  },
                  checkMarkColor = Color.White,
                  // todo: same here with color
                  checkColor = MaterialTheme.colorScheme.typeElement,
                )
              }
            },
          )
        }
      }
    }
  }
}

@Composable
private fun DateButton(datePickerState: DatePickerState, modifier: Modifier = Modifier) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          shape = MaterialTheme.shapes.medium,
          onClick = { showDatePicker = false },
          enabled = datePickerState.selectedDateMillis != null,
        ) {
          Text(text = stringResource(R.string.general_save_button))
        }
      },
    ) {
      HedvigDatePicker(
        datePickerState = datePickerState,
        colors = DatePickerDefaults.colors(
          selectedDayContainerColor = MaterialTheme.colorScheme.typeElement,
          selectedDayContentColor = Color.White,
          // todo: same here with color
          todayDateBorderColor = MaterialTheme.colorScheme.typeElement,
        ),
      )
    }
  }
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
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationDateScreen(
        TerminateInsuranceUiState(
          rememberDatePickerState(),
          false,
          "Bullegatan 34",
          "Homeowner insurance",
          true,
        ),
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
    Surface(color = MaterialTheme.colorScheme.background) {
      ImportantInfoCheckBox(
        true,
        {},
      )
    }
  }
}
