package com.hedvig.android.feature.insurances.insurancedetail.yourinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioOptionRightAligned
import com.hedvig.android.design.system.hedvig.Surface
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
    HedvigText(
      text = stringResource(id = R.string.CONTRACT_CHANGE_INFORMATION_TITLE),
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
        RadioOptionRightAligned(
          chosenState = if (selectedItemIndex == 0) Chosen else NotChosen,
          onClick = { selectedItemIndex = 0 },
          optionContent = {
            Column {
              HedvigText(
                text = stringResource(R.string.insurance_details_change_address_button),
              )
              HedvigText(
                text = stringResource(R.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE),
                color = HedvigTheme.colorScheme.textSecondary,
                style = HedvigTheme.typography.label,
              )
            }
          },
        )
      }
      if (allowEditCoInsured) {
        RadioOptionRightAligned(
          optionContent = {
            Column {
              HedvigText(
                text = stringResource(R.string.CONTRACT_EDIT_COINSURED),
              )
              HedvigText(
                text = stringResource(R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE),
                color = HedvigTheme.colorScheme.textSecondary,
                style = HedvigTheme.typography.label,
              )
            }
          },
          chosenState = if (selectedItemIndex == 1) Chosen else NotChosen,
          onClick = { selectedItemIndex = 1 },
        )
      }
      if (allowChangeTier) {
        RadioOptionRightAligned(
          optionContent = {
            Column {
              HedvigText(
                text = stringResource(R.string.insurance_details_change_coverage),
              )
              HedvigText(
                text = stringResource(R.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE),
                color = HedvigTheme.colorScheme.textSecondary,
                style = HedvigTheme.typography.label,
              )
            }
          },
          chosenState = if (selectedItemIndex == 2) Chosen else NotChosen,
          onClick = { selectedItemIndex = 2 },
        )
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(id = R.string.general_continue_button),
      enabled = selectedItemIndex != -1,
      onClick = dropUnlessResumed {
        if (selectedItemIndex == 0 && allowChangeAddress) {
          onChangeAddressClick()
        } else if (selectedItemIndex == 1 && allowEditCoInsured) {
          onEditCoInsuredClick()
        } else if (selectedItemIndex == 2 && allowChangeTier) {
          onChangeTierClick()
        }
      },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.general_cancel_button),
      buttonSize = Large,
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
@HedvigPreview
private fun PreviewEditInsuranceBottomSheetContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
