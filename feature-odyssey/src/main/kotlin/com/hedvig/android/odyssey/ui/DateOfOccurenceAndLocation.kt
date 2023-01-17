package com.hedvig.android.odyssey.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.FormRowButton
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.odyssey.ClaimsFlowViewModel
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import java.time.LocalDate

@Composable
fun DateOfOccurrenceAndLocation(viewModel: ClaimsFlowViewModel) {
  val viewState by viewModel.viewState.collectAsState()
  val openLocationPickerDialog = remember { mutableStateOf(false) }

  val now = LocalDate.now()
  val pickerDialog = DatePickerDialog(
    LocalContext.current,
    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
      viewModel.onDateOfOccurrence(LocalDate.of(year, month, dayOfMonth))
    },
    now.year,
    now.monthValue,
    now.dayOfMonth,
  )

  val locationValues = viewState.claim?.inputs
    ?.filterIsInstance<Input.Location>()
    ?.firstOrNull()
    ?.locationOptions
    ?: emptyList()

  if (openLocationPickerDialog.value) {
    SingleSelectDialog(
      title = "Select location",
      optionsList = locationValues,
      onSelected = viewModel::onLocation,
      getDisplayText = { location: AutomationClaimDTO2.ClaimLocation -> location.getText() },
    ) { openLocationPickerDialog.value = false }
  }

  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {

    Spacer(modifier = Modifier.padding(top = 20.dp))

    Column {
      FormRowButton(
        mainText = "Date of incident",
        secondaryText = viewState.claim?.state?.dateOfOccurrence?.toString() ?: now.toString(),
      ) {
        pickerDialog.show()
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = "Location",
        secondaryText = viewState.claim?.state?.location?.getText() ?: "-",
      ) {
        openLocationPickerDialog.value = true
      }
    }

    LargeContainedTextButton(
      onClick = viewModel::onNext,
      text = "Next",
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}
