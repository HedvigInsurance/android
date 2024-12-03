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
import com.hedvig.android.data.claimflow.LocalContractContractOption
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.SingleSelectDialog
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.R

@Composable
internal fun ContractOptionWithDialog(
  locationOptions: List<LocalContractContractOption>,
  selectedLocation: LocalContractContractOption,
  selectLocationOption: (LocalContractContractOption) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  var showLocationPickerDialog by rememberSaveable { mutableStateOf(false) }
  if (showLocationPickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.claims_incident_screen_location),
      optionsList = locationOptions.map { option ->
        RadioOptionData(
          id = option.id,
          optionText = option.displayName,
          chosenState = if (selectedLocation.id == option.id) Chosen else NotChosen,
        )
      },
      onSelected = { radioOptionData ->
        selectLocationOption(locationOptions.first { it.id == radioOptionData.id })
      },
      onDismissRequest = { showLocationPickerDialog = false },
    )
  }

  HedvigBigCard(
    onClick = { showLocationPickerDialog = true },
    labelText = stringResource(R.string.CLAIM_TRIAGING_ABOUT_TITILE),
    inputText = selectedLocation.displayName,
    enabled = enabled,
    modifier = modifier,
  )
}

@HedvigPreview
@Composable
private fun PreviewContractOptionWithDialog(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) isEnabled: Boolean,
) {
  val locationOptions = List(4) {
    LocalContractContractOption(it.toString(), "Option#$it")
  }
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ContractOptionWithDialog(
        locationOptions,
        locationOptions[0],
        {},
        isEnabled,
      )
    }
  }
}
