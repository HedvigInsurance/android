package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.Member
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import octopus.type.CurrencyCode

@Composable
internal fun EditCoInsuredAddOrRemoveDestination(
  viewModel: EditCoInsuredViewModel,
  navigateToSuccessScreen: (LocalDate) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  EditCoInsuredScreen(
    navigateUp = navigateUp,
    uiState = uiState,
    onSave = {
      viewModel.emit(EditCoInsuredEvent.OnBottomSheetContinue)
    },
    onSsnChanged = {
      viewModel.emit(EditCoInsuredEvent.OnSsnChanged(it))
    },
    onRemoveCoInsured = {
      viewModel.emit(EditCoInsuredEvent.RemoveCoInsured(it))
    },
    onRemoveCoInsuredClicked = {
      viewModel.emit(EditCoInsuredEvent.OnRemoveCoInsuredClicked(it))
    },
    onAddCoInsuredClicked = {
      viewModel.emit(EditCoInsuredEvent.OnAddCoInsuredClicked)
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
    onResetRemoveBottomSheetState = {
      viewModel.emit(EditCoInsuredEvent.ResetRemoveBottomSheetState)
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
  onSave: () -> Unit,
  onSsnChanged: (String) -> Unit,
  onRemoveCoInsured: (CoInsured) -> Unit,
  onRemoveCoInsuredClicked: (CoInsured) -> Unit,
  onAddCoInsuredClicked: () -> Unit,
  onCommitChanges: () -> Unit,
  onCompleted: (LocalDate) -> Unit,
  onDismissError: () -> Unit,
  onResetAddBottomSheetState: () -> Unit,
  onResetRemoveBottomSheetState: () -> Unit,
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
            windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top),
          ) {
            AddCoInsuredBottomSheetContent(
              bottomSheetState = uiState.addBottomSheetState,
              onContinue = onSave,
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

        if (uiState.removeBottomSheetState.show && uiState.removeBottomSheetState.coInsured != null) {
          val sheetState = rememberModalBottomSheetState(true)
          ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = onResetRemoveBottomSheetState,
            shape = MaterialTheme.shapes.squircleLargeTop,
            sheetState = sheetState,
            tonalElevation = 0.dp,
            windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top),
          ) {
            RemoveCoInsuredBottomSheetContent(
              onRemove = { onRemoveCoInsured(it) },
              onDismiss = {
                coroutineScope.launch {
                  sheetState.hide()
                }.invokeOnCompletion {
                  onResetRemoveBottomSheetState()
                }
              },
              isLoading = uiState.removeBottomSheetState.isLoading,
              coInsured = uiState.removeBottomSheetState.coInsured,
              errorMessage = uiState.removeBottomSheetState.errorMessage,
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
            onRemove = onRemoveCoInsuredClicked,
            onEdit = {},
            allowEdit = false,
          )

          Spacer(Modifier.height(8.dp))
          if (uiState.listState.noCoInsuredHaveMissingInfo()) {
            HedvigSecondaryContainedButton(
              text = stringResource(id = R.string.CONTRACT_ADD_COINSURED),
              onClick = onAddCoInsuredClicked,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }
        }

        Column {
          if (uiState.listState.priceInfo != null && uiState.listState.hasMadeChanges()) {
            Spacer(Modifier.height(8.dp))
            PriceInfo(uiState.listState.priceInfo)
            HedvigContainedButton(
              text = stringResource(id = R.string.CONTRACT_ADD_COINSURED_CONFIRM_CHANGES),
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
          Spacer(
            Modifier.padding(
              WindowInsets.safeDrawing
                .only(WindowInsetsSides.Bottom).asPaddingValues(),
            ),
          )
        }
      }

      EditCoInsuredState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
    }
  }
}

@Composable
private fun PriceInfo(priceInfo: EditCoInsuredState.Loaded.PriceInfo) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()

  Column(
    modifier = Modifier
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = { Text(text = stringResource(id = R.string.CONTRACT_ADD_COINSURED_TOTAL)) },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End) {
          Text(
            text = stringResource(
              id = R.string.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL,
              priceInfo.previousPrice.toString(),
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = stringResource(id = R.string.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL, priceInfo.newPrice.toString()))
        }
      },
      spaceBetween = 8.dp,
    )
    Text(
      text = stringResource(
        id = R.string.CONTRACT_ADD_COINSURED_STARTS_FROM,
        dateTimeFormatter.format(priceInfo.validFrom.toJavaLocalDate()),
      ),
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.End,
      modifier = Modifier.fillMaxWidth(),
    )
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
            ),
            member = Member(
              firstName = "Member",
              lastName = "Membersson",
              ssn = "197312331093",
            ),
            priceInfo = EditCoInsuredState.Loaded.PriceInfo(
              previousPrice = UiMoney(100.0, CurrencyCode.SEK),
              newPrice = UiMoney(200.0, CurrencyCode.SEK),
              validFrom = LocalDate.fromEpochDays(400),
            ),
            allCoInsured = persistentListOf(),
          ),
          addBottomSheetState = EditCoInsuredState.Loaded.AddBottomSheetState(
            isLoading = false,
          ),
          removeBottomSheetState = EditCoInsuredState.Loaded.RemoveBottomSheetState(),
        ),
        onSave = {},
        onSsnChanged = {},
        onRemoveCoInsured = {},
        onRemoveCoInsuredClicked = {},
        onAddCoInsuredClicked = {},
        onCommitChanges = {},
        onCompleted = {},
        onDismissError = {},
        onResetAddBottomSheetState = {},
        onResetRemoveBottomSheetState = {},
        onFirstNameChanged = {},
        onLastNameChanged = {},
        onBirthDateChanged = {},
        onManualInputSwitchChanged = {},
        onCoInsuredSelected = {},
        onAddNewCoInsured = {},
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
            allCoInsured = persistentListOf(),
          ),
          addBottomSheetState = EditCoInsuredState.Loaded.AddBottomSheetState(
            isLoading = false,
          ),
          removeBottomSheetState = EditCoInsuredState.Loaded.RemoveBottomSheetState(),
        ),
        onSave = {},
        onSsnChanged = {},
        onRemoveCoInsured = {},
        onRemoveCoInsuredClicked = {},
        onAddCoInsuredClicked = {},
        onCommitChanges = {},
        onCompleted = {},
        onDismissError = {},
        onResetAddBottomSheetState = {},
        onResetRemoveBottomSheetState = {},
        onFirstNameChanged = {},
        onLastNameChanged = {},
        onBirthDateChanged = {},
        onManualInputSwitchChanged = {},
        onCoInsuredSelected = {},
        onAddNewCoInsured = {},
      )
    }
  }
}
