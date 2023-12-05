package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
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
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.Member
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

@Composable
internal fun EditCoInsuredAddMissingInfoDestination(viewModel: EditCoInsuredViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  EditCoInsuredScreen(
    navigateUp = navigateUp,
    uiState = uiState,
    onCoInsuredClicked = {
      viewModel.emit(EditCoInsuredEvent.OnEditCoInsuredClicked(it))
    },
    onSave = {
      viewModel.emit(EditCoInsuredEvent.OnCoInsuredAddedFromBottomSheet)
    },
    onSsnChanged = {
      viewModel.emit(EditCoInsuredEvent.OnSsnChanged(it))
    },
    onFetchInfo = {
      viewModel.emit(EditCoInsuredEvent.FetchCoInsuredPersonalInformation)
    },
    onDismissError = {
      viewModel.emit(EditCoInsuredEvent.OnDismissError)
    },
    onResetAddBottomSheetState = {
      viewModel.emit(EditCoInsuredEvent.ResetAddBottomSheetState)
    },
    onCommitChanges = {
      viewModel.emit(EditCoInsuredEvent.OnCommitChanges)
    },
    onCompleted = {
      navigateUp()
    },
  )
}

@Composable
private fun EditCoInsuredScreen(
  navigateUp: () -> Unit,
  uiState: EditCoInsuredState,
  onCoInsuredClicked: (CoInsured) -> Unit,
  onSsnChanged: (String) -> Unit,
  onSave: () -> Unit,
  onFetchInfo: () -> Unit,
  onCommitChanges: () -> Unit,
  onCompleted: (LocalDate) -> Unit,
  onDismissError: () -> Unit,
  onResetAddBottomSheetState: () -> Unit,
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
            windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top),
          ) {
            AddCoInsuredBottomSheetContent(
              onSave = onSave,
              onFetchInfo = onFetchInfo,
              onSsnChanged = onSsnChanged,
              onDismiss = {
                coroutineScope.launch {
                  sheetState.hide()
                }.invokeOnCompletion {
                  onResetAddBottomSheetState()
                }
              },
              displayName = uiState.addBottomSheetState.displayName,
              ssn = uiState.addBottomSheetState.ssn,
              birthDate = uiState.addBottomSheetState.birthDate,
              isLoading = uiState.addBottomSheetState.isLoading,
              errorMessage = uiState.addBottomSheetState.errorMessage,
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
              isLoading = uiState.listState.isCommittingUpdate,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }

          Spacer(Modifier.height(8.dp))
          HedvigTextButton(
            onClick = navigateUp,
            text = stringResource(id = R.string.general_cancel_button),
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.height(16.dp))
        }
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
            priceInfo = EditCoInsuredState.Loaded.PriceInfo(
              previousPrice = UiMoney(100.0, CurrencyCode.SEK),
              newPrice = UiMoney(200.0, CurrencyCode.SEK),
              validFrom = LocalDate.fromEpochDays(400),
            ),
            originalCoInsured = persistentListOf(
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
            updatedCoInsured = persistentListOf(
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
          ),
          addBottomSheetState = EditCoInsuredState.Loaded.AddBottomSheetState(
            isLoading = false,
          ),
          removeBottomSheetState = EditCoInsuredState.Loaded.RemoveBottomSheetState(),
        ),
        onSave = {},
        onFetchInfo = {},
        onDismissError = {},
        onResetAddBottomSheetState = {},
        onCommitChanges = {},
        onCompleted = {},
        onCoInsuredClicked = {},
        onSsnChanged = {},
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
            originalCoInsured = persistentListOf(
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
          ),
          addBottomSheetState = EditCoInsuredState.Loaded.AddBottomSheetState(
            isLoading = false,
          ),
          removeBottomSheetState = EditCoInsuredState.Loaded.RemoveBottomSheetState(),
        ),
        onSave = {},
        onFetchInfo = {},
        onDismissError = {},
        onResetAddBottomSheetState = {},
        onCommitChanges = {},
        onCompleted = {},
        onCoInsuredClicked = {},
        onSsnChanged = {},
      )
    }
  }
}
