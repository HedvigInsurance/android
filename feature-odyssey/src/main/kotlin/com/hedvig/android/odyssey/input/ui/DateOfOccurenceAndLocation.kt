package com.hedvig.android.odyssey.input.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.FormRowButton
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import java.time.LocalDate

@Composable
fun DateOfOccurrenceAndLocation(
  state: ClaimState,
  imageLoader: ImageLoader,
  onDateOfOccurrence: (LocalDate) -> Unit,
  locationOptions: List<AutomationClaimDTO2.ClaimLocation>,
  onLocation: (AutomationClaimDTO2.ClaimLocation) -> Unit,
  onNext: () -> Unit,
) {
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
      title = stringResource(hedvig.resources.R.string.claims_location_screen_title),
      optionsList = locationOptions,
      onSelected = onLocation,
      getDisplayText = { it.getText() },
      getImageUrl = { null },
      getId = { it.name },
      imageLoader = imageLoader,
    ) { openLocationPickerDialog.value = false }
  }

  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {
    Column {
      Spacer(modifier = Modifier.padding(top = 20.dp))

      FormRowButton(
        mainText = stringResource(hedvig.resources.R.string.claims_incident_screen_date_of_incident),
        secondaryText = state.dateOfOccurrence?.toString() ?: "-",
      ) {
        pickerDialog.show()
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = stringResource(hedvig.resources.R.string.claims_incident_screen_location),
        secondaryText = state.location.getText(),
      ) {
        openLocationPickerDialog.value = true
      }
    }

    LargeContainedTextButton(
      onClick = onNext,
      text = stringResource(hedvig.resources.R.string.general_continue_button),
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}
