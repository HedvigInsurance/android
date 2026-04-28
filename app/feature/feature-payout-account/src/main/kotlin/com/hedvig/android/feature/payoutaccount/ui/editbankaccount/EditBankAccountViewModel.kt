package com.hedvig.android.feature.payoutaccount.ui.editbankaccount

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.then
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.hedvig.android.feature.payoutaccount.data.SetupNordeaPayoutUseCase
import com.hedvig.android.feature.payoutaccount.data.bankNameForClearingNumber
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class EditBankAccountViewModel(
  setupNordeaPayoutUseCase: SetupNordeaPayoutUseCase,
) : MoleculeViewModel<EditBankAccountEvent, EditBankAccountUiState>(
  EditBankAccountUiState(
    accountNumberState = TextFieldState(),
    bankName = null,
    isLoading = false,
    errorMessage = null,
    showSuccessSnackBar = false,
  ),
  EditBankAccountPresenter(setupNordeaPayoutUseCase),
)

internal sealed interface EditBankAccountEvent {
  data object Save : EditBankAccountEvent

  data object ShowedSnackBar : EditBankAccountEvent
}

internal data class EditBankAccountUiState(
  val accountNumberState: TextFieldState,
  val bankName: String?,
  val isLoading: Boolean,
  val errorMessage: String?,
  val showSuccessSnackBar: Boolean,
) {
  val canSave: Boolean
    get() = !isLoading && accountNumberState.text.length in 10..17

  // Combined clearing and account number: 10-17 digits
  val accountNumberInputTransformation: InputTransformation = InputTransformation.maxLength(17).digitsOnly()
}

internal class EditBankAccountPresenter(
  private val setupNordeaPayoutUseCase: SetupNordeaPayoutUseCase,
) : MoleculePresenter<EditBankAccountEvent, EditBankAccountUiState> {
  @Composable
  override fun MoleculePresenterScope<EditBankAccountEvent>.present(
    lastState: EditBankAccountUiState,
  ): EditBankAccountUiState {
    val accountNumberState = remember { lastState.accountNumberState }
    val bankName = bankNameForClearingNumber(accountNumberState.text.toString().take(4))
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessSnackBar by remember { mutableStateOf(false) }
    var saveIteration by remember { mutableStateOf<String?>(null) }

    val currentSave = saveIteration
    if (currentSave != null) {
      LaunchedEffect(currentSave) {
        isLoading = true
        errorMessage = null
        setupNordeaPayoutUseCase.invoke(currentSave).fold(
          ifLeft = {
            isLoading = false
            errorMessage = it.message ?: ""
            saveIteration = null
          },
          ifRight = {
            isLoading = false
            showSuccessSnackBar = true
            saveIteration = null
          },
        )
      }
    }

    LaunchedEffect(accountNumberState) {
      snapshotFlow { accountNumberState.text }.collect {
        errorMessage = null
      }
    }

    CollectEvents { event ->
      when (event) {
        EditBankAccountEvent.Save -> {
          if (!isLoading) {
            saveIteration = accountNumberState.text.toString()
          }
        }

        EditBankAccountEvent.ShowedSnackBar -> {
          showSuccessSnackBar = false
        }
      }
    }

    return EditBankAccountUiState(
      accountNumberState = accountNumberState,
      bankName = bankName,
      isLoading = isLoading,
      errorMessage = errorMessage,
      showSuccessSnackBar = showSuccessSnackBar,
    )
  }
}

@Stable
private fun InputTransformation.digitsOnly(): InputTransformation = this.then(DigitsOnlyTransformation)

private data object DigitsOnlyTransformation : InputTransformation {
  override fun TextFieldBuffer.transformInput() {
    val current = toString()
    val filtered = current.filter { it.isDigit() }
    if (filtered.length != current.length) {
      replace(0, current.length, filtered)
      placeCursorAtEnd()
    }
  }

  override fun toString(): String = "InputTransformation.DigitsOnly"
}
