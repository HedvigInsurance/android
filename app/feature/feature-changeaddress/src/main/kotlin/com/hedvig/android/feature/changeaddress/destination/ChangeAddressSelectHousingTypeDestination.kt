package com.hedvig.android.feature.changeaddress.destination

import HousingType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.changeaddress.ChangeAddressUiState
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.ui.AddressInfoCard
import displayNameResource

@Composable
internal fun ChangeAddressSelectHousingTypeDestination(
  viewModel: ChangeAddressViewModel,
  navigateUp: () -> Unit,
  onHousingTypeSubmitted: () -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChangeAddressSelectHousingTypeScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onHousingTypeSelected = viewModel::onHousingTypeSelected,
    onHousingTypeSubmitted = onHousingTypeSubmitted,
    onHousingTypeErrorDialogDismissed = viewModel::onHousingTypeErrorDialogDismissed,
    onValidateHousingType = viewModel::onValidateHousingType,
  )
}

@Composable
private fun ChangeAddressSelectHousingTypeScreen(
  uiState: ChangeAddressUiState,
  navigateUp: () -> Unit,
  onHousingTypeSelected: (HousingType) -> Unit,
  onHousingTypeSubmitted: () -> Unit,
  onHousingTypeErrorDialogDismissed: () -> Unit,
  onValidateHousingType: () -> Unit,
) {
  uiState.housingType.errorMessageRes?.let {
    ErrorDialog(
      title = stringResource(hedvig.resources.R.string.general_error),
      message = stringResource(it),
      onDismiss = { onHousingTypeErrorDialogDismissed() },
    )
  }
  HedvigScaffold(
    navigateUp = navigateUp,
  ) {
    Spacer(modifier = Modifier.height(48.dp))
    Text(
      text = stringResource(hedvig.resources.R.string.CHANGE_ADDRESS_SELECT_HOUSING_TYPE_TITLE),
      style = MaterialTheme.typography.headlineMedium,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.weight(1f))
    RadioButton(HousingType.APARTMENT_OWN, uiState.housingType.input, onHousingTypeSelected)
    Spacer(modifier = Modifier.height(8.dp))
    RadioButton(HousingType.APARTMENT_RENT, uiState.housingType.input, onHousingTypeSelected)
    Spacer(modifier = Modifier.height(8.dp))
    RadioButton(HousingType.VILLA, uiState.housingType.input, onHousingTypeSelected)
    Spacer(modifier = Modifier.height(16.dp))
    AddressInfoCard(
      text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_COVERAGE_INFO_TEXT),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
    HedvigContainedButton(
      stringResource(id = hedvig.resources.R.string.general_continue_button),
      onClick = {
        onValidateHousingType()
        if (uiState.isHousingTypeValid) {
          onHousingTypeSubmitted()
        }
      },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun RadioButton(
  housingType: HousingType,
  selectedHousingType: HousingType?,
  selectHousingType: (HousingType) -> Unit,
) {
  HedvigCard(
    onClick = { selectHousingType(housingType) },
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp, horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(housingType.displayNameResource()),
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.weight(1f),
      )
      RadioButton(
        selected = selectedHousingType == housingType,
        onClick = {
          selectHousingType(housingType)
        },
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChangeAddressSelectHousingTypeScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChangeAddressSelectHousingTypeScreen(
        ChangeAddressUiState(),
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
