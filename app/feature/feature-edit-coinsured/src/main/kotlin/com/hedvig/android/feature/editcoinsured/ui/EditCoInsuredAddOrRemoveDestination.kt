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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigBottomSheetPriceBreakdown
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.Member
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.InfoFromSsn
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.ManualInfo
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.RemoveBottomSheetContentState
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
          modifier = Modifier.fillMaxWidth(),
        )
      }

      is EditCoInsuredState.Loaded -> {
        Column(
          Modifier
            .fillMaxSize()
            .padding(
              WindowInsets
                .safeDrawing
                .only(WindowInsetsSides.Horizontal)
                .asPaddingValues(),
            )
            .nestedScroll(remember { object : NestedScrollConnection {} })
            .verticalScroll(state = rememberScrollState()),
        ) {
          LaunchedEffect(uiState.contractUpdateDate) {
            if (uiState.contractUpdateDate != null) {
              onCompleted(uiState.contractUpdateDate)
            }
          }
          val addHedvigBottomSheetState =
            rememberHedvigBottomSheetState<EditCoInsuredState.Loaded.AddBottomSheetContentState>()
          DismissSheetOnSuccessfulInfoChangeEffect(
            sheetState = addHedvigBottomSheetState,
            infoSuccessfullyChanged = uiState.finishedAdding,
          )
          ClearBottomSheetContentStateOnSheetDismissedEffect(
            sheetState = addHedvigBottomSheetState,
            clearBottomSheetState = onResetAddBottomSheetState,
          )
          HedvigBottomSheet(
            hedvigBottomSheetState = addHedvigBottomSheetState,
          ) {
            AddCoInsuredBottomSheetContent(
              bottomSheetState = uiState.addBottomSheetContentState,
              onContinue = onSave,
              onDismiss = {
                addHedvigBottomSheetState.dismiss()
                onResetAddBottomSheetState()
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
          val removeHedvigBottomSheetState = rememberHedvigBottomSheetState<RemoveBottomSheetContentState>()
          DismissRemoveCoinsuredSheetOnSuccessfulRemoveEffect(removeHedvigBottomSheetState, uiState.finishedRemoving)
          ClearRemoveBottomSheetContentStateOnSheetDismissedEffect(
            removeHedvigBottomSheetState,
            onResetRemoveBottomSheetState,
          )
          HedvigBottomSheet(
            removeHedvigBottomSheetState,
          ) {
            if (uiState.removeBottomSheetContentState.coInsured != null) {
              RemoveCoInsuredBottomSheetContent(
                onDismiss = {
                  removeHedvigBottomSheetState.dismiss()
                  onResetRemoveBottomSheetState()
                },
                onRemove = { onRemoveCoInsured(it) },
                isLoading = uiState.removeBottomSheetContentState.isLoading,
                coInsured = uiState.removeBottomSheetContentState.coInsured,
                errorMessage = uiState.removeBottomSheetContentState.errorMessage,
              )
            }
          }
          val costBreakdownBottomSheetState = rememberHedvigBottomSheetState<Unit>()
          val mockGross = UiMoney(200.0, UiCurrencyCode.SEK) // todo: REMOVE MOCK
          val mockNet = UiMoney(130.0, UiCurrencyCode.SEK) // todo: REMOVE MOCK
          val mockDisplayItems = listOf(
            "Homeowner Insurance Max" to "179 kr/mo",
            "Extended travel 60 days" to "21 kr/mo",
            "15% bundle discount" to "70 kr/mo",
          ) // todo: REMOVE MOCK
          HedvigBottomSheetPriceBreakdown(
            costBreakdownBottomSheetState,
            displayItems = mockDisplayItems,
            totalNet = mockNet,
            totalGross = mockGross,
          )
          CoInsuredList(
            uiState = uiState.listState,
            onRemove = { insured ->
              onRemoveCoInsuredClicked(insured)
              removeHedvigBottomSheetState.show(uiState.removeBottomSheetContentState)
            },
            onEdit = {},
            allowEdit = false,
          )

          Spacer(Modifier.height(8.dp))
          if (uiState.listState.noCoInsuredHaveMissingInfo()) {
            HedvigButton(
              text = stringResource(id = R.string.CONTRACT_ADD_COINSURED),
              onClick = {
                addHedvigBottomSheetState.show(uiState.addBottomSheetContentState)
                onAddCoInsuredClicked()
              },
              enabled = true,
              buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            )
          }

          Spacer(Modifier.weight(1f))
          Column {
            if (uiState.listState.priceInfo != null && uiState.listState.hasMadeChanges()) {
              Spacer(Modifier.height(16.dp))
              PriceInfo(uiState.listState.priceInfo, costBreakdownBottomSheetState)
              HedvigButton(
                text = stringResource(id = R.string.CONTRACT_ADD_COINSURED_CONFIRM_CHANGES),
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
private fun PriceInfo(
  priceInfo: EditCoInsuredState.Loaded.PriceInfo,
  costBreakdownBottomSheetState: HedvigBottomSheetState<Unit>,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()

  Column(
    modifier = Modifier
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          HedvigText(text = stringResource(id = R.string.PRICE_PREVIOUS_PRICE))
        }
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End) {
          HedvigText(
            modifier = Modifier
              .padding(vertical = 16.dp),
            text = stringResource(
              id = R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              priceInfo.previousPrice.toString(),
            ),
          )
        }
      },
      spaceBetween = 8.dp,
      modifier = Modifier
        .horizontalDivider(DividerPosition.Bottom),
    )

    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          HedvigText(text = stringResource(id = R.string.PRICE_NEW_PRICE))
          if (
            true // todo: REMOVE MOCK
            //  grossPrice!=netPrice
          ) {
            IconButton(
              onClick = {
                costBreakdownBottomSheetState.show(Unit)
              },
            ) {
              Icon(
                HedvigIcons.InfoFilled,
                contentDescription = EmptyContentDescription,
                tint = HedvigTheme.colorScheme.fillSecondary,
              )
            }
          }
        }
      },
      endSlot = {
        Column(horizontalAlignment = Alignment.End) {
          HedvigText(
            modifier = Modifier
              .padding(top = 16.dp),
            text = stringResource(
              id = R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              priceInfo.newPrice.toString(),
            ),
          )
          HedvigText(
            text = stringResource(
              id = R.string.SUMMARY_TOTAL_PRICE_SUBTITLE,
              dateTimeFormatter.format(priceInfo.validFrom.toJavaLocalDate()),
            ),
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      spaceBetween = 8.dp,
    )
  }
}

@Composable
@HedvigPreview
private fun EditCoInsuredScreenErrorPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      EditCoInsuredScreen(
        navigateUp = { },
        uiState = EditCoInsuredState.Error("Something happened"),
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
private fun EditCoInsuredScreenEditablePreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
          addBottomSheetContentState = EditCoInsuredState.Loaded.AddBottomSheetContentState(
            isLoading = false,
            manualInfo = ManualInfo(),
            infoFromSsn = InfoFromSsn(),
          ),
          removeBottomSheetContentState = EditCoInsuredState.Loaded.RemoveBottomSheetContentState(),
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
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
            manualInfo = ManualInfo(),
            infoFromSsn = InfoFromSsn(),
          ),
          removeBottomSheetContentState = EditCoInsuredState.Loaded.RemoveBottomSheetContentState(),
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
