package com.hedvig.android.feature.terminateinsurance.step.terminationdate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.datepicker.HedvigDatePicker
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.terminateinsurance.ui.TerminationOverviewScreenScaffold
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
  )
}

@Composable
private fun TerminationDateScreen(uiState: TerminateInsuranceUiState, submit: () -> Unit, navigateUp: () -> Unit) {
  TerminationOverviewScreenScaffold(
    navigateUp = navigateUp,
    topAppBarText = "",
  ) {
    Text(
      text = stringResource(id = R.string.TERMINATION_FLOW_CANCELLATION_TITLE),
      fontSize = MaterialTheme.typography.headlineSmall.fontSize,
      fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
      fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Text(
      text = stringResource(id = R.string.TERMINATION_DATE_TEXT),
      fontSize = MaterialTheme.typography.headlineSmall.fontSize,
      fontStyle = MaterialTheme.typography.headlineSmall.fontStyle,
      fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigCard(
      onClick = null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .heightIn(72.dp)
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = uiState.displayName,
            style = MaterialTheme.typography.bodyLarge,
          )
          Text(
            text = uiState.exposureName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    DateButton(
      datePickerState = uiState.datePickerState,
      modifier = Modifier,
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.general_continue_button),
      onClick = submit,
      enabled = uiState.canSubmit,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun DateButton(datePickerState: DatePickerState, modifier: Modifier = Modifier) {
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  if (showDatePicker) {
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        HedvigContainedSmallButton(
          text = stringResource(R.string.general_save_button),
          onClick = { showDatePicker = false },
          enabled = datePickerState.selectedDateMillis != null,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
      },
    ) {
      HedvigDatePicker(datePickerState = datePickerState)
    }
  }

  Column(modifier) {
    HedvigCard(
      onClick = { showDatePicker = true },
      colors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(Modifier.weight(1f)) {
          Text(
            text = "Termination date", // todo: real copy here
            style = MaterialTheme.typography.bodyLarge,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = datePickerState.selectedDateMillis?.let {
              Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
            }?.toString() ?: "Select date...", // todo: real copy here
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewTerminationDateScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationDateScreen(
        TerminateInsuranceUiState(rememberDatePickerState(), false, "Bullegatan 34", "Homeowner insurance"),
        {},
        {},
      )
    }
  }
}
