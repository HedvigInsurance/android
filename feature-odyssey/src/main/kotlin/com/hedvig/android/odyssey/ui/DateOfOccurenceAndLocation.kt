package com.hedvig.android.odyssey.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.FormRowButton
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.odyssey.model.Claim
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun DateOfOccurrenceAndLocation(
  state: ClaimState,
  onDateOfOccurrence: (LocalDate) -> Unit,
  onLocation: (AutomationClaimDTO2.ClaimLocation) -> Unit,
  onNext: suspend () -> Unit,
) {
  val coroutineScope = rememberCoroutineScope()
  val openLocationPickerDialog = remember { mutableStateOf(false) }

  val now = LocalDate.now()
  val pickerDialog = DatePickerDialog(
    LocalContext.current,
    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
      onDateOfOccurrence(LocalDate.of(year, month, dayOfMonth))
    },
    now.year,
    now.monthValue,
    now.dayOfMonth,
  )

  if (openLocationPickerDialog.value) {
    SingleSelectDialog(
      title = "Select location",
      optionsList = listOf(
        AutomationClaimDTO2.ClaimLocation.AT_HOME,
        AutomationClaimDTO2.ClaimLocation.ABROAD,
        AutomationClaimDTO2.ClaimLocation.IN_HOME_COUNTRY,
      ),
      onSelected = onLocation,
      getDisplayText = { it.getText() },
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
        secondaryText = state.dateOfOccurrence?.toString() ?: "-",
      ) {
        pickerDialog.show()
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = "Location",
        secondaryText = state.location.getText(),
      ) {
        openLocationPickerDialog.value = true
      }
    }

    LargeContainedTextButton(
      onClick = {
        coroutineScope.launch {
          onNext()
        }
      },
      text = "Next",
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}
