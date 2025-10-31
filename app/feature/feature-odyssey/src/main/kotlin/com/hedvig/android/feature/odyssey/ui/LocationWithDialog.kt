package com.hedvig.android.feature.odyssey.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.data.claimflow.LocationOption
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.SingleSelectDialog
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.R

@Composable
internal fun LocationWithDialog(
  locationOptions: List<LocationOption>,
  selectedLocation: LocationOption?,
  selectLocationOption: (LocationOption) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  var showLocationPickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showLocationPickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.claims_incident_screen_location),
      options = locationOptions.map { locationOption ->
        RadioOption(
          id = RadioOptionId(locationOption.displayName),
          text = locationOption.displayName,
        )
      },
      selectedOption = selectedLocation?.displayName?.let { RadioOptionId(it) },
      onRadioOptionSelected = { id ->
        selectLocationOption(locationOptions.first { it.displayName == id.id })
      },
      onDismissRequest = { showLocationPickerDialog = false },
    )
  }

  HedvigBigCard(
    onClick = { showLocationPickerDialog = true },
    labelText = stringResource(R.string.claims_location_screen_title),
    inputText = selectedLocation?.displayName,
    enabled = enabled,
    modifier = modifier,
  )
}

@HedvigPreview
@Composable
private fun PreviewLocationWithDialog(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasSelectedLocation: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      LocationWithDialog(
        emptyList(),
        if (hasSelectedLocation) LocationOption("", "Stockholm") else null,
        {},
        false,
      )
    }
  }
}
