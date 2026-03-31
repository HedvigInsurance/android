package com.hedvig.android.feature.chip.id.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.feature.chip.id.data.GetContractsWithMissingChipIdUseCase
import com.hedvig.android.feature.chip.id.data.PetContractForChipId
import com.hedvig.android.feature.chip.id.data.UpdateChipIdUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class AddChipIdViewModel(
  updateChipIdUseCase: UpdateChipIdUseCase,
  getContractsWithMissingChipIdUseCase: GetContractsWithMissingChipIdUseCase,
  contractId: String,
) : MoleculeViewModel<AddChipIdEvent, AddChipIdUiState>(
  initialState = AddChipIdUiState.Loading,
  presenter = AddChipIdPresenter(
    updateChipIdUseCase = updateChipIdUseCase,
    contractId = contractId,
    getContractsWithMissingChipIdUseCase = getContractsWithMissingChipIdUseCase,
  ),
)

internal class AddChipIdPresenter(
  private val updateChipIdUseCase: UpdateChipIdUseCase,
  private val getContractsWithMissingChipIdUseCase: GetContractsWithMissingChipIdUseCase,
  private val contractId: String,
) : MoleculePresenter<AddChipIdEvent, AddChipIdUiState> {
  @Composable
  override fun MoleculePresenterScope<AddChipIdEvent>.present(lastState: AddChipIdUiState): AddChipIdUiState {
    var chipIdText by remember {
      val lastChipIdState = lastState.content?.chipIdText
      mutableStateOf(lastChipIdState ?: "")
    }
    var currentState by remember { mutableStateOf(lastState) }
    var submittingData by remember { mutableStateOf(false) }
    var showSuccessSnackBar by remember { mutableStateOf(false) }
    var errorType by remember { mutableStateOf<ChipIdErrorType?>(null) }

    var submitIteration by remember { mutableIntStateOf(0) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(chipIdText) {
      errorType = null
    }

    LaunchedEffect(loadIteration) {
      getContractsWithMissingChipIdUseCase.invoke().fold(
        ifLeft = {
          currentState = AddChipIdUiState.Error
        },
        ifRight = {
          val contract = it.firstOrNull { it.id == contractId }
          if (contract == null) {
            currentState = AddChipIdUiState.Error
            return@LaunchedEffect
          }
          currentState = AddChipIdUiState.Content(
            chipIdText = chipIdText,
            contract = contract,
          )
        },
      )
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration == 0) return@LaunchedEffect

      submittingData = true
      errorType = null

      updateChipIdUseCase.invoke(insuranceId = contractId, petId = chipIdText).fold(
        ifLeft = { error ->
          Snapshot.withMutableSnapshot {
            val errorMessage = error.message
            submittingData = false
            errorType =  if (errorMessage==null) ChipIdErrorType.GeneralError
            else ChipIdErrorType.ErrorWithMessage(errorMessage)
          }
        },
        ifRight = {
          Snapshot.withMutableSnapshot {
            showSuccessSnackBar = true
            submittingData = false
          }
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        AddChipIdEvent.RetryLoadData -> {
          loadIteration++
        }

        AddChipIdEvent.SubmitData -> {
          if (!chipIdText.all { it.isDigit() } || chipIdText.length != 15) {
            Snapshot.withMutableSnapshot {
              errorType = ChipIdErrorType.WrongInput
            }
          } else {
            submitIteration++
          }
        }

        AddChipIdEvent.ShowedMessage -> {
          Snapshot.withMutableSnapshot {
            showSuccessSnackBar = false
            errorType = null
          }
        }

        is AddChipIdEvent.UpdateText -> {
          chipIdText = event.newText
        }
      }
    }

    return when (val state = currentState) {
      is AddChipIdUiState.Content -> state.copy(
        chipIdText = chipIdText,
        showSuccessSnackBar = showSuccessSnackBar,
        submittingData = submittingData,
        errorType = errorType,
      )
      AddChipIdUiState.Error, AddChipIdUiState.Loading -> state
    }
  }
}

internal sealed interface AddChipIdUiState {
  val content: Content?
    get() = this as? Content

  data object Loading : AddChipIdUiState

  data object Error : AddChipIdUiState

  data class Content(
    val chipIdText: String,
    val contract: PetContractForChipId,
    val showSuccessSnackBar: Boolean = false,
    val submittingData: Boolean = false,
    val errorType: ChipIdErrorType? = null,
  ) : AddChipIdUiState
}

internal sealed interface ChipIdErrorType {
  data object WrongInput : ChipIdErrorType

  data object GeneralError : ChipIdErrorType

  data class ErrorWithMessage(val message: String) : ChipIdErrorType
}

internal sealed interface AddChipIdEvent {
  data object RetryLoadData : AddChipIdEvent

  data object SubmitData : AddChipIdEvent

  data object ShowedMessage : AddChipIdEvent

  data class UpdateText(val newText: String): AddChipIdEvent
}
