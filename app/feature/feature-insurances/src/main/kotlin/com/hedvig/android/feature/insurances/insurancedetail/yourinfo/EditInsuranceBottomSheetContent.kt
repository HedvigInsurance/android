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
import androidx.compose.ui.res.stringResource
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
import hedvig.resources.R
import kotlin.collections.buildList

@Composable
internal fun EditInsuranceBottomSheetContent(
  allowEditCoInsured: Boolean,
  allowChangeTier: Boolean,
  allowTerminatingInsurance: Boolean,
  onEditCoInsuredClick: () -> Unit,
  onChangeTierClick: () -> Unit,
  onCancelInsuranceClick: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedItemId: String? by rememberSaveable { mutableStateOf(null) }
  val options = buildList {
    if (allowChangeTier) {
      add(
        RadioOption(
          RadioOptionId("0"),
          stringResource(R.string.insurance_details_change_coverage),
          stringResource(R.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE),
        ),
      )
    }
    if (allowEditCoInsured) {
      add(
        RadioOption(
          RadioOptionId("1"),
          stringResource(R.string.CONTRACT_EDIT_COINSURED),
          stringResource(R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE),
        ),
      )
    }
    if (allowTerminatingInsurance) {
      add(
        RadioOption(
          RadioOptionId("2"),
          stringResource(R.string.HC_QUICK_ACTIONS_CANCELLATION_TITLE),
          stringResource(R.string.HC_QUICK_ACTIONS_CANCELLATION_SUBTITLE),
        ),
      )
    }
  }
  Column(
    modifier = modifier,
  ) {
    HedvigText(
      text = stringResource(id = R.string.CONTRACT_CHANGE_INFORMATION_TITLE),
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
      text = stringResource(id = R.string.general_continue_button),
      enabled = selectedItemId != null,
      onClick = dropUnlessResumed {
        when (selectedItemId) {
          "0" if allowChangeTier -> onChangeTierClick()
          "1" if allowEditCoInsured -> onEditCoInsuredClick()
          "2" if allowTerminatingInsurance -> onCancelInsuranceClick()
          else -> {}
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
        allowTerminatingInsurance = true,
        allowEditCoInsured = true,
        allowChangeTier = true,
        onChangeTierClick = {},
        onEditCoInsuredClick = {},
        onDismiss = {},
        modifier = Modifier.padding(horizontal = 16.dp),
        onCancelInsuranceClick = {},
      )
    }
  }
}
