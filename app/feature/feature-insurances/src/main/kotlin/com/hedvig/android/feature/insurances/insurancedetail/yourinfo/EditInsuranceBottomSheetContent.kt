package com.hedvig.android.feature.insurances.insurancedetail.yourinfo

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.CONTRACT_CHANGE_INFORMATION_TITLE
import hedvig.resources.CONTRACT_EDIT_COINSURED
import hedvig.resources.EDIT_COOWNER_SUBTITLE
import hedvig.resources.EDIT_COOWNER_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_CANCELLATION_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_CANCELLATION_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_REMOVE_ADDON_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE
import hedvig.resources.REMOVE_ADDON_BUTTON_TITLE
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import hedvig.resources.insurance_details_change_coverage
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EditInsuranceBottomSheetContent(
  allowEditCoInsured: Boolean,
  allowEditCoOwners: Boolean,
  allowChangeTier: Boolean,
  allowTerminatingInsurance: Boolean,
  allowRemovingAddon: Boolean,
  onEditCoInsuredClick: () -> Unit,
  onEditCoOwnersClick: () -> Unit,
  onChangeTierClick: () -> Unit,
  onCancelInsuranceClick: () -> Unit,
  onRemoveAddonClick: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedItemId: String? by rememberSaveable { mutableStateOf(null) }
  val options = buildList {
    if (allowChangeTier) {
      add(
        RadioOption(
          RadioOptionId("0"),
          stringResource(Res.string.insurance_details_change_coverage),
          stringResource(Res.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE),
        ),
      )
    }
    if (allowEditCoInsured) {
      add(
        RadioOption(
          RadioOptionId("1"),
          stringResource(Res.string.CONTRACT_EDIT_COINSURED),
          stringResource(Res.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE),
        ),
      )
    }
    if (allowEditCoOwners) {
      add(
        RadioOption(
          RadioOptionId("2"),
          stringResource(Res.string.EDIT_COOWNER_TITLE),
          stringResource(Res.string.EDIT_COOWNER_SUBTITLE),
        ),
      )
    }
    if (allowTerminatingInsurance) {
      add(
        RadioOption(
          RadioOptionId("3"),
          stringResource(Res.string.HC_QUICK_ACTIONS_CANCELLATION_TITLE),
          stringResource(Res.string.HC_QUICK_ACTIONS_CANCELLATION_SUBTITLE),
        ),
      )
    }
    if (allowRemovingAddon) {
      add(
        RadioOption(
          RadioOptionId("4"),
          stringResource(Res.string.REMOVE_ADDON_BUTTON_TITLE),
          stringResource(Res.string.HC_QUICK_ACTIONS_REMOVE_ADDON_SUBTITLE),
        ),
      )
    }
  }
  Column(
    modifier = modifier,
  ) {
    HedvigText(
      text = stringResource(Res.string.CONTRACT_CHANGE_INFORMATION_TITLE),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .semantics { heading() },
    )
    Spacer(modifier = Modifier.height(24.dp))
    RadioGroup(
      options = options,
      selectedOption = selectedItemId?.let { RadioOptionId(it) },
      onRadioOptionSelected = { selectedItemId = it.id },
    )
    Spacer(modifier = Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      enabled = selectedItemId != null,
      onClick = dropUnlessResumed {
        when (selectedItemId) {
          "0" if allowChangeTier -> {
            onChangeTierClick()
          }

          "1" if allowEditCoInsured -> {
            onEditCoInsuredClick()
          }

          "2" if allowEditCoOwners -> {
            onEditCoOwnersClick()
          }

          "3" if allowTerminatingInsurance -> {
            onCancelInsuranceClick()
          }

          "4" if allowRemovingAddon -> {
            onRemoveAddonClick()
          }

          else -> {}
        }
      },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
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
        allowTerminatingInsurance = true,
        allowEditCoInsured = true,
        allowEditCoOwners = true,
        allowChangeTier = true,
        allowRemovingAddon = true,
        onChangeTierClick = {},
        onEditCoInsuredClick = {},
        onEditCoOwnersClick = {},
        onDismiss = {},
        onCancelInsuranceClick = {},
        onRemoveAddonClick = {},
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}
