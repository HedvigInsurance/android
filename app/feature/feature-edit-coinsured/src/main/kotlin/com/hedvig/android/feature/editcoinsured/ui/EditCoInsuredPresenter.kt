package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.CoInsuredError
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeResult
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.Member
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal class EditCoInsuredPresenter(
  private val contractId: String,
  private val getCoInsuredUseCase: GetCoInsuredUseCase,
  private val fetchCoInsuredPersonalInformationUseCase: FetchCoInsuredPersonalInformationUseCase,
  private val createMidtermChangeUseCase: CreateMidtermChangeUseCase,
) : MoleculePresenter<EditCoInsuredEvent, EditCoInsuredState> {
  @Composable
  override fun MoleculePresenterScope<EditCoInsuredEvent>.present(lastState: EditCoInsuredState): EditCoInsuredState {
    var listState by remember {
      val lastListState = lastState.safeCast<EditCoInsuredState.Loaded>()?.listState
      mutableStateOf(lastListState ?: EditCoInsuredState.Loaded.CoInsuredListState())
    }
    var bottomSheetState by remember {
      val lastBottomSheetState = lastState.safeCast<EditCoInsuredState.Loaded>()?.bottomSheetState
      mutableStateOf(lastBottomSheetState ?: EditCoInsuredState.Loaded.BottomSheetState())
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var ssnQuery by remember { mutableStateOf<String?>(null) }
    var addedCoInsured by remember { mutableStateOf<CoInsured?>(null) }
    var removedCoInsured by remember { mutableStateOf<CoInsured?>(null) }

    LaunchedEffect(Unit) {
      if (listState.member != null) {
        return@LaunchedEffect
      }
      isLoading = true
      either {
        val result = getCoInsuredUseCase.invoke(contractId).bind()
        Snapshot.withMutableSnapshot {
          errorMessage = null
          isLoading = false
          listState = EditCoInsuredState.Loaded.CoInsuredListState(
            coInsured = result.coInsured,
            member = result.member,
          )
        }
      }.onLeft {
        Snapshot.withMutableSnapshot {
          isLoading = false
          errorMessage = when (it) {
            CoInsuredError.ContractNotFound -> "Could not find contract"
            is CoInsuredError.GenericError -> it.message
          }
        }
      }
    }

    CollectEvents { event ->
      when (event) {
        is EditCoInsuredEvent.FetchCoInsuredPersonalInformation -> ssnQuery = event.ssn
        is EditCoInsuredEvent.AddCoInsured -> addedCoInsured = event.coInsured
        is EditCoInsuredEvent.RemoveCoInsured -> removedCoInsured = event.coInsured
        EditCoInsuredEvent.OnDismissError -> Snapshot.withMutableSnapshot { errorMessage = null }
        EditCoInsuredEvent.OnAddCoInsuredClicked -> bottomSheetState = bottomSheetState.copy(show = true)
        EditCoInsuredEvent.ResetBottomSheetState -> bottomSheetState = bottomSheetState.copy(
          coInsuredFromSsn = null,
          coInsuredFromSsnError = null,
          show = false,
        )
      }
    }

    LaunchedEffect(ssnQuery) {
      ssnQuery?.let { ssnQuery ->
        either {
          val result = fetchCoInsuredPersonalInformationUseCase.invoke(ssnQuery).bind()
          bottomSheetState = bottomSheetState.copy(
            coInsuredFromSsn = CoInsured.fromPersonalInformation(result, ssnQuery),
            coInsuredFromSsnError = null,
          )
        }.onLeft {
          bottomSheetState = bottomSheetState.copy(coInsuredFromSsnError = it.message)
        }
      }
    }

    fun onEditSuccess(result: CreateMidtermChangeResult) {
      Snapshot.withMutableSnapshot {
        ssnQuery = null
        listState = listState.copy(
          coInsured = result.coInsured,
        )
        bottomSheetState = bottomSheetState.copy(
          isLoadingPersonalInfo = false,
          coInsuredFromSsn = null,
          coInsuredFromSsnError = null,
          show = false,
        )
      }
    }

    fun onEditError(errorMessage: ErrorMessage) {
      bottomSheetState = bottomSheetState.copy(
        isLoadingPersonalInfo = false,
        coInsuredFromSsn = null,
        coInsuredFromSsnError = errorMessage.message,
      )
    }

    LaunchedEffect(addedCoInsured) {
      addedCoInsured?.let { addedCoInsured ->
        bottomSheetState = bottomSheetState.copy(isLoadingPersonalInfo = true)
        val updatedCoInsured = (listState.coInsured + addedCoInsured).toImmutableList()

        createMidtermChangeUseCase
          .invoke(contractId, updatedCoInsured)
          .fold(
            ifLeft = ::onEditError,
            ifRight = ::onEditSuccess,
          )
      }
    }

    LaunchedEffect(removedCoInsured) {
      removedCoInsured?.let { removedCoInsured ->
        bottomSheetState = bottomSheetState.copy(isLoadingPersonalInfo = true)
        val updatedCoInsured = listState.coInsured.filterNot { it.id == removedCoInsured.id }.toImmutableList()

        createMidtermChangeUseCase
          .invoke(contractId, updatedCoInsured)
          .fold(
            ifLeft = ::onEditError,
            ifRight = ::onEditSuccess,
          )
      }
    }

    return if (isLoading) {
      EditCoInsuredState.Loading
    } else if (errorMessage != null) {
      EditCoInsuredState.Error(errorMessage)
    } else if (listState.member != null) {
      EditCoInsuredState.Loaded(
        listState = listState,
        bottomSheetState = bottomSheetState,
      )
    } else {
      EditCoInsuredState.Error("Could not fetch member")
    }
  }
}

internal sealed interface EditCoInsuredEvent {
  data class FetchCoInsuredPersonalInformation(val ssn: String) : EditCoInsuredEvent
  data class AddCoInsured(val coInsured: CoInsured) : EditCoInsuredEvent
  data class RemoveCoInsured(val coInsured: CoInsured) : EditCoInsuredEvent
  data object OnDismissError : EditCoInsuredEvent
  data object ResetBottomSheetState : EditCoInsuredEvent
  data object OnAddCoInsuredClicked : EditCoInsuredEvent
}

internal sealed interface EditCoInsuredState {
  data object Loading : EditCoInsuredState

  data class Error(val message: String?) : EditCoInsuredState

  data class Loaded(
    val listState: CoInsuredListState,
    val bottomSheetState: BottomSheetState,
  ) : EditCoInsuredState {
    data class CoInsuredListState(
      val coInsured: ImmutableList<CoInsured> = persistentListOf(),
      val member: Member? = null,
    )

    data class BottomSheetState(
      val coInsuredFromSsn: CoInsured? = null,
      val coInsuredFromSsnError: String? = null,
      val isLoadingPersonalInfo: Boolean = false,
      val show: Boolean = false,
    )
  }
}
