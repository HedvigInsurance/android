package com.hedvig.android.feature.insurances.insurancedetail.yourinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.SelectableHedvigCard
import hedvig.resources.R

@Composable
internal fun EditInsuranceBottomSheetContent(
  allowChangeAddress: Boolean,
  allowEditCoInsured: Boolean,
  allowChangeTier: Boolean,
  onEditCoInsuredClick: () -> Unit,
  onChangeAddressClick: () -> Unit,
  onChangeTierClick: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedItemIndex by rememberSaveable { mutableIntStateOf(-1) }
  Column(
    modifier = modifier,
  ) {
    Text(
      text = stringResource(id = R.string.CONTRACT_CHANGE_INFORMATION_TITLE),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
    )
    Spacer(modifier = Modifier.height(24.dp))
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      if (allowChangeAddress) {
        SelectableHedvigCard(
          text = stringResource(R.string.insurance_details_change_address_button),
          isSelected = selectedItemIndex == 0,
          onClick = {
            selectedItemIndex = if (selectedItemIndex == 0) {
              -1
            } else {
              0
            }
          },
        )
      }
      if (allowEditCoInsured) {
        SelectableHedvigCard(
          text = stringResource(R.string.CONTRACT_EDIT_COINSURED),
          isSelected = selectedItemIndex == 1,
          onClick = {
            selectedItemIndex = if (selectedItemIndex == 1) {
              -1
            } else {
              1
            }
          },
        )
      }
      if (allowChangeTier) {
        SelectableHedvigCard(
          text = stringResource(R.string.insurance_details_change_coverage),
          isSelected = selectedItemIndex == 2,
          onClick = {
            selectedItemIndex = if (selectedItemIndex == 2) {
              -1
            } else {
              2
            }
          },
        )
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(id = R.string.general_continue_button),
      enabled = selectedItemIndex > -1,
      onClick = {
        if (selectedItemIndex == 0) {
          onChangeAddressClick()
        } else if (selectedItemIndex == 1 && allowEditCoInsured) {
          onEditCoInsuredClick()
        } else if (selectedItemIndex == 2 && allowChangeTier) {
          onChangeTierClick()
        }
      },
    )
    Spacer(modifier = Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_cancel_button),
      onClick = onDismiss,
    )
    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
@HedvigPreview
private fun PreviewEditInsuranceBottomSheetContent() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      EditInsuranceBottomSheetContent(
        allowChangeAddress = true,
        allowEditCoInsured = true,
        allowChangeTier = true,
        onChangeTierClick = {},
        onEditCoInsuredClick = {},
        onChangeAddressClick = {},
        onDismiss = {},
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}
