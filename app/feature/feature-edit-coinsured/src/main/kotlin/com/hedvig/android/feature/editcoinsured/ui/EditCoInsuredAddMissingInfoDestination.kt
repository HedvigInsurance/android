package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.Member
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.InfoFromSsn
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.ManualInfo
import hedvig.resources.R
import kotlinx.coroutines.flow.drop
import kotlinx.datetime.LocalDate

@Composable
internal fun EditCoInsuredAddMissingInfoDestination(
  viewModel: EditCoInsuredViewModel,
  navigateToSuccessScreen: (LocalDate) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  EditCoInsuredScreen(
    navigateUp = navigateUp,
    uiState = uiState,
    onCoInsuredClicked = {
      viewModel.emit(EditCoInsuredEvent.OnEditCoInsuredClicked(it))
    },
    onSsnChanged = {
      viewModel.emit(EditCoInsuredEvent.OnSsnChanged(it))
    },
    onBottomSheetContinue = {
      viewModel.emit(EditCoInsuredEvent.OnBottomSheetContinue)
    },
    onCommitChanges = {
      viewModel.emit(EditCoInsuredEvent.OnCommitChanges)
    },
    onCompleted = {
      navigateToSuccessScreen(it)
    },
    onDismissError = {
      viewModel.emit(EditCoInsuredEvent.OnDismissError)
    },
    onResetAddBottomSheetState = {
      viewModel.emit(EditCoInsuredEvent.ResetAddBottomSheetState)
    },
    onFirstNameChanged = {
      viewModel.emit(EditCoInsuredEvent.OnFirstNameChanged(it))
    },
    onLastNameChanged = {
      viewModel.emit(EditCoInsuredEvent.OnLastNameChanged(it))
    },
    onBirthDateChanged = {
      viewModel.emit(EditCoInsuredEvent.OnBirthDateChanged(it))
    },
    onManualInputSwitchChanged = {
      viewModel.emit(EditCoInsuredEvent.OnManualInputSwitchChanged(it))
    },
    onAddNewCoInsured = {
      viewModel.emit(EditCoInsuredEvent.OnAddNewCoInsured)
    },
    onCoInsuredSelected = {
      viewModel.emit(EditCoInsuredEvent.OnCoInsuredSelected(it))
    },
  )
}

@Composable
private fun EditCoInsuredScreen(
  navigateUp: () -> Unit,
  uiState: EditCoInsuredState,
  onCoInsuredClicked: (CoInsured) -> Unit,
  onSsnChanged: (String) -> Unit,
  onBottomSheetContinue: () -> Unit,
  onCommitChanges: () -> Unit,
  onCompleted: (LocalDate) -> Unit,
  onDismissError: () -> Unit,
  onResetAddBottomSheetState: () -> Unit,
  onFirstNameChanged: (String) -> Unit,
  onLastNameChanged: (String) -> Unit,
  onBirthDateChanged: (LocalDate) -> Unit,
  onManualInputSwitchChanged: (Boolean) -> Unit,
  onAddNewCoInsured: () -> Unit,
  onCoInsuredSelected: (CoInsured) -> Unit,
) {
  Column(Modifier.fillMaxSize()) {
    TopAppBar(
      title = stringResource(id = R.string.COINSURED_EDIT_TITLE),
      actionType = TopAppBarActionType.BACK,
      onActionClick = navigateUp,
    )

    when (uiState) {
      is EditCoInsuredState.Error -> {
        ErrorDialog(
          title = stringResource(id = R.string.general_error),
          message = uiState.message,
          onDismiss = onDismissError,
        )
      }

      is EditCoInsuredState.Loaded -> {
        LaunchedEffect(uiState.contractUpdateDate) {
          if (uiState.contractUpdateDate != null) {
            onCompleted(uiState.contractUpdateDate)
          }
        }
        val hedvigBottomSheetState =
          rememberHedvigBottomSheetState<EditCoInsuredState.Loaded.AddBottomSheetContentState>()
        DismissSheetOnSuccessfulInfoChangeEffect(hedvigBottomSheetState, uiState.finishedAdding)
        ClearBottomSheetContentStateOnSheetDismissedEffect(hedvigBottomSheetState, onResetAddBottomSheetState)
        HedvigBottomSheet(
          hedvigBottomSheetState = hedvigBottomSheetState,
        ) {
          AddCoInsuredBottomSheetContent(
            bottomSheetState = uiState.addBottomSheetContentState,
            onContinue = onBottomSheetContinue,
            onDismiss = {
              hedvigBottomSheetState.dismiss()
            },
            onSsnChanged = onSsnChanged,
            onFirstNameChanged = onFirstNameChanged,
            onLastNameChanged = onLastNameChanged,
            onBirthDateChanged = onBirthDateChanged,
            onManualInputSwitchChanged = onManualInputSwitchChanged,
            onAddNewCoInsured = onAddNewCoInsured,
            onCoInsuredSelected = onCoInsuredSelected,
          )
        }

        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(
              WindowInsets
                .safeDrawing
                .only(WindowInsetsSides.Horizontal)
                .asPaddingValues(),
            )
            .verticalScroll(state = rememberScrollState()),
        ) {
          CoInsuredList(
            uiState = uiState.listState,
            onRemove = {},
            onEdit = { insured ->
              hedvigBottomSheetState.show(uiState.addBottomSheetContentState)
              onCoInsuredClicked(insured)
            },
            allowEdit = true,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(8.dp))

          if (uiState.listState.priceInfo != null && uiState.listState.hasMadeChanges()) {
            HedvigNotificationCard(
              message = stringResource(
                id = R.string.CONTRACT_ADD_COINSURED_REVIEW_INFO,
              ),
              priority = NotificationDefaults.NotificationPriority.Attention,
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            )
          }

          Spacer(Modifier.weight(1f))
          if (uiState.listState.priceInfo != null && uiState.listState.hasMadeChanges()) {
            Spacer(Modifier.height(8.dp))
            HedvigButton(
              text = stringResource(id = R.string.GENERAL_SAVE_CHANGES_BUTTON),
              onClick = onCommitChanges,
              enabled = true,
              isLoading = uiState.listState.isCommittingUpdate,
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            )
          }
          Spacer(Modifier.height(8.dp))
          HedvigTextButton(
            onClick = navigateUp,
            text = stringResource(R.string.general_cancel_button),
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .fillMaxWidth(),
          )
          Spacer(Modifier.height(16.dp))
          Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
        }
      }

      EditCoInsuredState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
    }
  }
}

@Composable
internal fun DismissSheetOnSuccessfulInfoChangeEffect(
  sheetState: HedvigBottomSheetState<EditCoInsuredState.Loaded.AddBottomSheetContentState>,
  infoSuccessfullyChanged: Boolean,
) {
  val updatedInfoSuccessfullyChanged by rememberUpdatedState(infoSuccessfullyChanged)
  LaunchedEffect(sheetState) {
    snapshotFlow { updatedInfoSuccessfullyChanged }
      .drop(1)
      .collect {
        if (it) {
          sheetState.dismiss()
        }
      }
  }
}

@Composable
internal fun DismissRemoveCoinsuredSheetOnSuccessfulRemoveEffect(
  sheetState: HedvigBottomSheetState<EditCoInsuredState.Loaded.RemoveBottomSheetContentState>,
  coInsuredSuccessfullyRemoved: Boolean,
) {
  val updatedCoInsuredSuccessfullyRemoved by rememberUpdatedState(coInsuredSuccessfullyRemoved)
  LaunchedEffect(sheetState) {
    snapshotFlow { updatedCoInsuredSuccessfullyRemoved }
      .drop(1)
      .collect {
        if (it) {
          sheetState.dismiss()
        }
      }
  }
}

@Composable
internal fun ClearRemoveBottomSheetContentStateOnSheetDismissedEffect(
  sheetState: HedvigBottomSheetState<EditCoInsuredState.Loaded.RemoveBottomSheetContentState>,
  clearBottomSheetState: () -> Unit,
) {
  val updatedClearBottomSheetState by rememberUpdatedState(clearBottomSheetState)
  LaunchedEffect(sheetState) {
    snapshotFlow { sheetState.isVisible }
      .drop(1)
      .collect {
        if (!it) {
          updatedClearBottomSheetState()
        }
      }
  }
}

@Composable
internal fun ClearBottomSheetContentStateOnSheetDismissedEffect(
  sheetState: HedvigBottomSheetState<EditCoInsuredState.Loaded.AddBottomSheetContentState>,
  clearBottomSheetState: () -> Unit,
) {
  val updatedClearBottomSheetState by rememberUpdatedState(clearBottomSheetState)
  LaunchedEffect(sheetState) {
    snapshotFlow { sheetState.isVisible }
      .drop(1)
      .collect {
        if (!it) {
          updatedClearBottomSheetState()
        }
      }
  }
}

@Composable
@HedvigPreview
private fun EditCoInsuredScreenErrorPreview() {
  HedvigTheme {
    Surface {
      EditCoInsuredScreen(
        navigateUp = { },
        uiState = EditCoInsuredState.Error("Something"),
        onCoInsuredClicked = {},
        onSsnChanged = {},
        onBottomSheetContinue = {},
        onCommitChanges = {},
        onCompleted = {},
        onDismissError = {},
        onResetAddBottomSheetState = {},
        onFirstNameChanged = {},
        onLastNameChanged = {},
        onBirthDateChanged = {},
        onManualInputSwitchChanged = {},
        onAddNewCoInsured = {},
        onCoInsuredSelected = {},
      )
    }
  }
}

@Composable
@HedvigMultiScreenPreview
private fun EditCoInsuredScreenEditablePreview() {
  HedvigTheme {
    Surface {
      EditCoInsuredScreen(
        navigateUp = { },
        uiState = EditCoInsuredState.Loaded(
          listState = EditCoInsuredState.Loaded.CoInsuredListState(
            originalCoInsured = listOf(
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
            updatedCoInsured = listOf(
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
            priceInfo = EditCoInsuredState.Loaded.PriceInfo(
              previousPrice = UiMoney(100.0, UiCurrencyCode.SEK),
              newPrice = UiMoney(200.0, UiCurrencyCode.SEK),
              validFrom = LocalDate.fromEpochDays(400),
            ),
            allCoInsured = listOf(),
          ),
          addBottomSheetContentState = EditCoInsuredState.Loaded.AddBottomSheetContentState(
            isLoading = false,
            manualInfo = ManualInfo(),
            infoFromSsn = InfoFromSsn(),
          ),
          removeBottomSheetContentState = EditCoInsuredState.Loaded.RemoveBottomSheetContentState(),
        ),
        onCoInsuredClicked = {},
        onSsnChanged = {},
        onBottomSheetContinue = {},
        onCommitChanges = {},
        onCompleted = {},
        onDismissError = {},
        onResetAddBottomSheetState = {},
        onFirstNameChanged = {},
        onLastNameChanged = {},
        onBirthDateChanged = {},
        onManualInputSwitchChanged = {},
        onAddNewCoInsured = {},
        onCoInsuredSelected = {},
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
        uiState = EditCoInsuredState.Loaded(
          listState = EditCoInsuredState.Loaded.CoInsuredListState(
            originalCoInsured = listOf(
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
            allCoInsured = listOf(),
          ),
          addBottomSheetContentState = EditCoInsuredState.Loaded.AddBottomSheetContentState(
            isLoading = false,
            infoFromSsn = InfoFromSsn(),
            manualInfo = ManualInfo(),
          ),
          removeBottomSheetContentState = EditCoInsuredState.Loaded.RemoveBottomSheetContentState(),
        ),
        onCoInsuredClicked = {},
        onSsnChanged = {},
        onBottomSheetContinue = {},
        onCommitChanges = {},
        onCompleted = {},
        onDismissError = {},
        onResetAddBottomSheetState = {},
        onFirstNameChanged = {},
        onLastNameChanged = {},
        onBirthDateChanged = {},
        onManualInputSwitchChanged = {},
        onAddNewCoInsured = {},
        onCoInsuredSelected = {},
      )
    }
  }
}
