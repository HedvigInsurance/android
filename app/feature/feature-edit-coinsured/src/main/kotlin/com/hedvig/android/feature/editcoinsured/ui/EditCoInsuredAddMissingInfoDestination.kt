package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.infocard.VectorWarningCard
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.Member
import hedvig.resources.R
import kotlinx.coroutines.launch
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
    TopAppBarWithBack(
      title = stringResource(id = R.string.COINSURED_EDIT_TITLE),
      onClick = navigateUp,
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
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(uiState.contractUpdateDate) {
          if (uiState.contractUpdateDate != null) {
            onCompleted(uiState.contractUpdateDate)
          }
        }

        if (uiState.addBottomSheetState.show) {
          val sheetState = rememberModalBottomSheetState(true)
          ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = onResetAddBottomSheetState,
            shape = MaterialTheme.shapes.squircleLargeTop,
            sheetState = sheetState,
            tonalElevation = 0.dp,
            contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top) },
          ) {
            AddCoInsuredBottomSheetContent(
              bottomSheetState = uiState.addBottomSheetState,
              onContinue = onBottomSheetContinue,
              onSsnChanged = onSsnChanged,
              onFirstNameChanged = onFirstNameChanged,
              onLastNameChanged = onLastNameChanged,
              onBirthDateChanged = onBirthDateChanged,
              onManualInputSwitchChanged = onManualInputSwitchChanged,
              onAddNewCoInsured = onAddNewCoInsured,
              onCoInsuredSelected = onCoInsuredSelected,
              onDismiss = {
                coroutineScope.launch {
                  sheetState.hide()
                }.invokeOnCompletion {
                  onResetAddBottomSheetState()
                }
              },
            )
          }
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(state = rememberScrollState()),
        ) {
          CoInsuredList(
            uiState = uiState.listState,
            onRemove = {},
            onEdit = onCoInsuredClicked,
            allowEdit = true,
          )
          Spacer(Modifier.height(8.dp))

          if (uiState.listState.priceInfo != null && uiState.listState.hasMadeChanges()) {
            VectorWarningCard(
              text = stringResource(
                id = R.string.CONTRACT_ADD_COINSURED_REVIEW_INFO,
              ),
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }
        }

        Column {
          if (uiState.listState.priceInfo != null && uiState.listState.hasMadeChanges()) {
            Spacer(Modifier.height(8.dp))
            HedvigContainedButton(
              text = stringResource(id = R.string.GENERAL_SAVE_CHANGES_BUTTON),
              onClick = onCommitChanges,
              enabled = true,
              isLoading = uiState.listState.isCommittingUpdate,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }

          Spacer(Modifier.height(8.dp))
          HedvigTextButton(
            onClick = navigateUp,
            text = stringResource(id = R.string.general_cancel_button),
            modifier = Modifier.padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }

      EditCoInsuredState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
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
          addBottomSheetState = EditCoInsuredState.Loaded.AddBottomSheetState(
            isLoading = false,
          ),
          removeBottomSheetState = EditCoInsuredState.Loaded.RemoveBottomSheetState(),
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
          addBottomSheetState = EditCoInsuredState.Loaded.AddBottomSheetState(
            isLoading = false,
          ),
          removeBottomSheetState = EditCoInsuredState.Loaded.RemoveBottomSheetState(),
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
