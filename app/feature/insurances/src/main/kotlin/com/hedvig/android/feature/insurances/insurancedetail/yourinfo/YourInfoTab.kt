package com.hedvig.android.feature.insurances.insurancedetail.yourinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetails
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
internal fun YourInfoTab(
  coverageItems: ImmutableList<Pair<String, String>>,
  allowEditCoInsured: Boolean,
  cancelInsuranceData: ContractDetails.CancelInsuranceData?,
  onCancelInsuranceClick: (ContractDetails.CancelInsuranceData) -> Unit,
  onEditCoInsuredClick: () -> Unit,
  onChangeAddressClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  var showEditYourInfoBottomSheet by rememberSaveable { mutableStateOf(false) }
  if (showEditYourInfoBottomSheet) {
    val sheetState = rememberModalBottomSheetState(true)
    ModalBottomSheet(
      onDismissRequest = {
        showEditYourInfoBottomSheet = false
      },
      // todo use "https://github.com/c5inco/smoother" for a top only squircle shape here
      sheetState = sheetState,
      tonalElevation = 0.dp,
      windowInsets = WindowInsets(0.dp),
    ) {
      EditInsuranceBottomSheetContent(
        allowEditCoInsured = allowEditCoInsured,
        onEditCoInsuredClick = {
          coroutineScope.launch {
            sheetState.hide()
            showEditYourInfoBottomSheet = false
            onEditCoInsuredClick()
          }
        },
        onChangeAddressClick = {
          coroutineScope.launch {
            sheetState.hide()
            showEditYourInfoBottomSheet = false
            onChangeAddressClick()
          }
        },
        onDismiss = {
          coroutineScope.launch {
            sheetState.hide()
            showEditYourInfoBottomSheet = false
          }
        },
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .padding(bottom = 16.dp)
          .windowInsetsPadding(
            BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
          ),
      )
    }
  }

  Column(modifier) {
    Spacer(Modifier.height(16.dp))
    CoverageRows(coverageItems, Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.CONTRACT_EDIT_INFO_LABEL),
      onClick = { showEditYourInfoBottomSheet = true },
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
      ),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    if (cancelInsuranceData != null) {
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(R.string.TERMINATION_BUTTON),
        onClick = { onCancelInsuranceClick(cancelInsuranceData) },
        colors = ButtonDefaults.textButtonColors(
          contentColor = MaterialTheme.colorScheme.error,
        ),
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CoverageRows(
  coverageRowItems: ImmutableList<Pair<String, String>>,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    coverageRowItems.forEachIndexed { index, (firstText, secondText) ->
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            Text(firstText)
          }
        },
        endSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            Text(
              text = secondText,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.End,
            )
          }
        },
        spaceBetween = 8.dp,
      )
      if (index != coverageRowItems.lastIndex) {
        Divider()
      }
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewYourInfoTab() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      YourInfoTab(
        coverageItems = persistentListOf(
          "Address".repeat(4) to "Bellmansgatan 19A",
          "Postal code" to "118 47".repeat(6),
          "Type" to "Homeowner",
          "Size" to "56 m2",
          "Co-insured".repeat(4) to "You +1".repeat(5),
        ),
        allowEditCoInsured = true,
        cancelInsuranceData = ContractDetails.CancelInsuranceData("1", ""),
        onCancelInsuranceClick = {},
        onEditCoInsuredClick = {},
        onChangeAddressClick = {},
      )
    }
  }
}
