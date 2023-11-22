package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.Member
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun EditCoInsuredDestination(
  contractId: String,
  allowEdit: Boolean,
  navigateUp: () -> Unit,
) {
  val viewModel: EditCoInsuredViewModel = koinViewModel { parametersOf(contractId) }
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  EditCoInsuredScreen(
    navigateUp = navigateUp,
    allowEdit = allowEdit,
    uiState = uiState,
    onSave = {
      viewModel.emit(EditCoInsuredEvent.AddCoInsured(it))
    },
    onFetchInfo = {
      viewModel.emit(EditCoInsuredEvent.FetchCoInsuredPersonalInformation(it))
    },
  )
}

@Composable
private fun EditCoInsuredScreen(
  navigateUp: () -> Unit,
  allowEdit: Boolean,
  uiState: EditCoInsuredState,
  onSave: (CoInsured) -> Unit,
  onFetchInfo: (ssn: String) -> Unit,
) {
  val coroutineScope = rememberCoroutineScope()
  var showAddCoInsuredBottomSheet by rememberSaveable { mutableStateOf(false) }
  if (showAddCoInsuredBottomSheet) {
    val sheetState = rememberModalBottomSheetState(true)
    ModalBottomSheet(
      containerColor = MaterialTheme.colorScheme.background,
      onDismissRequest = {
        showAddCoInsuredBottomSheet = false
      },
      shape = MaterialTheme.shapes.squircleLargeTop,
      sheetState = sheetState,
      tonalElevation = 0.dp,
      windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top),
    ) {
      AddCoInsuredBottomSheetContent(
        onSave = onSave,
        onFetchInfo = onFetchInfo,
        onDismiss = {
          coroutineScope.launch {
            sheetState.hide()
          }.invokeOnCompletion {
            showAddCoInsuredBottomSheet = false
          }
        },
        isLoading = uiState.isLoadingPersonalInfo,
        coInsured = uiState.coInsuredFromSsn,
      )
    }
  }

  Column(Modifier.fillMaxSize()) {
    TopAppBarWithBack(
      title = stringResource(id = R.string.COINSURED_EDIT_TITLE),
      onClick = navigateUp,
    )
    CoInsuredList(uiState, allowEdit)
    if (!allowEdit) {
      Spacer(Modifier.height(8.dp))
      HedvigContainedButton(
        text = stringResource(id = R.string.CONTRACT_ADD_COINSURED),
        onClick = {
          showAddCoInsuredBottomSheet = true
        },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}

@Composable
private fun CoInsuredList(uiState: EditCoInsuredState, allowEdit: Boolean) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  Column {
    uiState.member?.let {
      InsuredRow(
        displayName = it.displayName,
        details = it.ssn,
        hasMissingInfo = false,
        allowEdit = false,
        isMember = true,
        onRemove = { },
        onEdit = { },
      )
    }
    Divider()
    uiState.coInsured.forEachIndexed { index, coInsured ->
      InsuredRow(
        displayName = coInsured.displayName,
        details = coInsured.details(dateTimeFormatter),
        hasMissingInfo = coInsured.hasMissingInfo,
        isMember = false,
        allowEdit = allowEdit,
        onRemove = { },
        onEdit = { },
      )
      if (index < uiState.coInsured.size - 1) {
        Divider()
      }
    }
  }
}

@Composable
@HedvigPreview
private fun EditCoInsuredScreenEditablePreview() {
  HedvigTheme {
    Surface {
      EditCoInsuredScreen(
        navigateUp = { },
        allowEdit = true,
        uiState = EditCoInsuredState(
          isLoading = false,
          errorMessage = null,
          coInsured = persistentListOf(
            CoInsured(
              "Test",
              "Testersson",
              LocalDate.fromEpochDays(300),
              "19910113-1093",
              hasMissingInfo = false,
            ),
            CoInsured(
              null,
              null,
              null,
              null,
              hasMissingInfo = true,
            ),
          ),
          member = Member(
            firstName = "Member",
            lastName = "Membersson",
            ssn = "197312331093",
          ),
          coInsuredFromSsn = null,
          isLoadingPersonalInfo = false,
        ),
        onSave = {},
        onFetchInfo = {},
      )
    }
  }
}

@Composable
@HedvigPreview
private fun EditCoInsuredScreenNonEditablePreview() {
  HedvigTheme {
    Surface {
      EditCoInsuredScreen(
        navigateUp = { },
        allowEdit = false,
        uiState = EditCoInsuredState(
          isLoading = false,
          errorMessage = null,
          coInsured = persistentListOf(
            CoInsured(
              "Test",
              "Testersson",
              LocalDate.fromEpochDays(300),
              "19910113-1093",
              hasMissingInfo = false,
            ),
            CoInsured(
              null,
              null,
              null,
              null,
              hasMissingInfo = true,
            ),
          ),
          member = Member(
            firstName = "Member",
            lastName = "Membersson",
            ssn = "197312331093",
          ),
          coInsuredFromSsn = null,
          isLoadingPersonalInfo = false,
        ),
        onSave = {},
        onFetchInfo = {},
      )
    }
  }
}
