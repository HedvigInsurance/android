package com.hedvig.android.feature.chip.id.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.text.TextRange
import com.hedvig.android.feature.chip.id.data.UpdateChipIdUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class AddChipIdViewModel(
  private val updateChipIdUseCase: UpdateChipIdUseCase,
  insuranceId: String,
) : MoleculeViewModel<AddChipIdEvent, AddChipIdUiState>(
  initialState = AddChipIdUiState.Loading,
  presenter = AddChipIdPresenter(
    updateChipIdUseCase = updateChipIdUseCase,
    insuranceId = insuranceId,
  ),
)

internal class AddChipIdPresenter(
  private val updateChipIdUseCase: UpdateChipIdUseCase,
  private val insuranceId: String,
) : MoleculePresenter<AddChipIdEvent, AddChipIdUiState> {
  @Composable
  override fun MoleculePresenterScope<AddChipIdEvent>.present(
    lastState: AddChipIdUiState,
  ): AddChipIdUiState {
    val chipIdState = remember {
      val lastChipIdState = lastState.content?.chipIdState
      TextFieldState(lastChipIdState?.text?.toString() ?: "", lastChipIdState?.selection ?: TextRange(0))
    }
    var submittingData by remember { mutableStateOf(false) }
    var showSuccessSnackBar by remember { mutableStateOf(false) }
    var errorType by remember { mutableStateOf<ChipIdErrorType?>(null) }

    var submitIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(submitIteration) {
      if (submitIteration == 0) return@LaunchedEffect

      submittingData = true
      errorType = null

      updateChipIdUseCase.invoke(insuranceId = insuranceId, petId = chipIdState.text.toString()).fold(
        ifLeft = { error ->
          Snapshot.withMutableSnapshot {
            submittingData = false
            errorType = ChipIdErrorType.GeneralError
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
          // Retry by resetting state
          chipIdState.setTextAndPlaceCursorAtEnd("")
        }

        AddChipIdEvent.SubmitData -> {
          if (!chipIdState.text.toString().all { it.isDigit() } || chipIdState.text.toString().length != 15) {
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
      }
    }

    return AddChipIdUiState.Content(
      chipIdState = chipIdState,
      showSuccessSnackBar = showSuccessSnackBar,
      submittingData = submittingData,
      errorType = errorType,
    )
  }
}

internal sealed interface AddChipIdUiState {
  val content: Content?
    get() = this as? Content

  data object Loading : AddChipIdUiState

  data object Error : AddChipIdUiState

  data class Content(
    val chipIdState: TextFieldState,
    val showSuccessSnackBar: Boolean = false,
    val submittingData: Boolean = false,
    val errorType: ChipIdErrorType? = null,
  ) : AddChipIdUiState {
    val isChipIdValid: Boolean
      get() = chipIdState.text.toString().length == 15 && chipIdState.text.toString().all { it.isDigit() }
  }
}

internal sealed interface ChipIdErrorType {
  data object WrongInput : ChipIdErrorType
  data object GeneralError : ChipIdErrorType
}

internal sealed interface AddChipIdEvent {
  data object RetryLoadData : AddChipIdEvent
  data object SubmitData : AddChipIdEvent
  data object ShowedMessage : AddChipIdEvent
}
