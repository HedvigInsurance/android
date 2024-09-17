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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.Member
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.InfoFromSsn
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.ManualInfo
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

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
        Column(
          Modifier
            .fillMaxSize()
            .padding(
              WindowInsets
                .safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues(),
            )
            .nestedScroll(remember { object : NestedScrollConnection {} })
            .verticalScroll(state = rememberScrollState()),
        ) {
          LaunchedEffect(uiState.contractUpdateDate) {
            if (uiState.contractUpdateDate != null) {
              onCompleted(uiState.contractUpdateDate)
            }
          }
          HedvigBottomSheet(
            bottomButtonText = stringResource(id = R.string.general_cancel_button),
            isVisible = uiState.addBottomSheetState.show,
            onVisibleChange = { isVisible ->
              if (!isVisible) {
                onResetAddBottomSheetState()
              }
            },
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
            )
          }

          HedvigBottomSheet(
            bottomButtonText = stringResource(R.string.general_cancel_button),
            isVisible = uiState.removeBottomSheetState.show && uiState.removeBottomSheetState.coInsured != null,
            onVisibleChange = { isVisible ->
              if (!isVisible) {
                onResetRemoveBottomSheetState()
              }
            },
          ) {
            if (uiState.removeBottomSheetState.coInsured != null) {
              RemoveCoInsuredBottomSheetContent(
                onRemove = { onRemoveCoInsured(it) },
                isLoading = uiState.removeBottomSheetState.isLoading,
                coInsured = uiState.removeBottomSheetState.coInsured,
                errorMessage = uiState.removeBottomSheetState.errorMessage,
              )
            }
          }
          CoInsuredList(
            uiState = uiState.listState,
            onRemove = onRemoveCoInsuredClicked,
            onEdit = {},
            allowEdit = false,
            modifier = Modifier.padding(horizontal = 16.dp),
          )

          Spacer(Modifier.height(8.dp))
          if (uiState.listState.noCoInsuredHaveMissingInfo()) {
            HedvigSecondaryContainedButton(
              text = stringResource(id = R.string.CONTRACT_ADD_COINSURED),
              onClick = onAddCoInsuredClicked,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }

          Spacer(Modifier.weight(1f))
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
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
            manualInfo = ManualInfo(),
            infoFromSsn = InfoFromSsn(),
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
@HedvigMultiScreenPreview
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
            manualInfo = ManualInfo(),
            infoFromSsn = InfoFromSsn(),
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
