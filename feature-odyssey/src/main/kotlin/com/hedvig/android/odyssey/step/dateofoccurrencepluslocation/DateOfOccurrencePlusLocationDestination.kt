package com.hedvig.android.odyssey.step.dateofoccurrencepluslocation

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
import com.hedvig.android.odyssey.ui.SingleSelectDialog
import hedvig.resources.R
import java.time.LocalDate

@Composable
internal fun DateOfOccurrencePlusLocationDestination(
  imageLoader: ImageLoader,
) {
  DateOfOccurrencePlusLocationScreen(
    imageLoader = imageLoader,
  )
}

@Composable
private fun DateOfOccurrencePlusLocationScreen(
  imageLoader: ImageLoader,
) {
  DateOfOccurrenceAndLocationScreen(
    state = ClaimState(),
    imageLoader = imageLoader,
    onDateOfOccurrence = {},
    onLocation = {},
    locationOptions = emptyList(),
    onNext = {},
  )
}

@Composable
fun DateOfOccurrenceAndLocationScreen(
  state: ClaimState,
  imageLoader: ImageLoader,
  onDateOfOccurrence: (LocalDate) -> Unit,
  locationOptions: List<AutomationClaimDTO2.ClaimLocation>,
  onLocation: (AutomationClaimDTO2.ClaimLocation) -> Unit,
  onNext: () -> Unit,
) {
  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {
    DateOfOccurrenceAndLocation(
      state = state,
      imageLoader = imageLoader,
      onDateOfOccurrence = onDateOfOccurrence,
      locationOptions = locationOptions,
      onLocation = onLocation,
    )
    LargeContainedTextButton(
      onClick = onNext,
      text = stringResource(R.string.general_continue_button),
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

@Composable
fun DateOfOccurrenceAndLocation(
  state: ClaimState,
  imageLoader: ImageLoader,
  onDateOfOccurrence: (LocalDate) -> Unit,
  locationOptions: List<AutomationClaimDTO2.ClaimLocation>,
  onLocation: (AutomationClaimDTO2.ClaimLocation) -> Unit,
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
      title = stringResource(R.string.claims_location_screen_title),
      optionsList = locationOptions,
      onSelected = onLocation,
      getDisplayText = { it.getText() },
      getImageUrl = { null },
      getId = { it.name },
      imageLoader = imageLoader,
    ) { openLocationPickerDialog.value = false }
  }

  Column {
    FormRowButton(
      mainText = stringResource(R.string.claims_incident_screen_date_of_incident),
      secondaryText = state.dateOfOccurrence?.toString() ?: "-",
    ) {
      pickerDialog.show()
    }

    Spacer(modifier = Modifier.padding(top = 4.dp))

    FormRowButton(
      mainText = stringResource(R.string.claims_incident_screen_location),
      secondaryText = state.location.getText(),
    ) {
      openLocationPickerDialog.value = true
    }
  }
}
