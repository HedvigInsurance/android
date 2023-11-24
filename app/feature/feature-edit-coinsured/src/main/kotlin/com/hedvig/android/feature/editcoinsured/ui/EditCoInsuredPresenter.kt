package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.raise.either
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.CoInsuredError
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.Member
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.AddCoInsured
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.FetchCoInsuredPersonalInformation
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.OnAddCoInsuredClicked
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.OnDismissError
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.OnRemoveCoInsuredClicked
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.RemoveCoInsured
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.ResetAddBottomSheetState
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.ResetRemoveBottomSheetState
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Error
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loading
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
      val lastListState = lastState.safeCast<Loaded>()?.listState
      mutableStateOf(lastListState ?: Loaded.CoInsuredListState())
    }
    var addBottomSheetState by remember {
      val lastBottomSheetState = lastState.safeCast<Loaded>()?.addBottomSheetState
      mutableStateOf(lastBottomSheetState ?: Loaded.AddBottomSheetState())
    }
    var removeBottomSheetState by remember {
      val lastBottomSheetState = lastState.safeCast<Loaded>()?.removeBottomSheetState
      mutableStateOf(lastBottomSheetState ?: Loaded.RemoveBottomSheetState())
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
          listState = Loaded.CoInsuredListState(
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
        is FetchCoInsuredPersonalInformation -> ssnQuery = event.ssn
        is AddCoInsured -> addedCoInsured = event.coInsured
        is RemoveCoInsured -> removedCoInsured = event.coInsured
        is OnRemoveCoInsuredClicked -> removeBottomSheetState = Loaded.RemoveBottomSheetState(
          show = true,
          coInsured = event.coInsured,
        )

        OnAddCoInsuredClicked -> addBottomSheetState = Loaded.AddBottomSheetState(show = true)
        ResetAddBottomSheetState -> addBottomSheetState = Loaded.AddBottomSheetState()
        ResetRemoveBottomSheetState -> removeBottomSheetState = Loaded.RemoveBottomSheetState()
        OnDismissError -> errorMessage = null
      }
    }

    LaunchedEffect(ssnQuery) {
      ssnQuery?.let { ssnQuery ->
        either {
          val result = fetchCoInsuredPersonalInformationUseCase.invoke(ssnQuery).bind()
          addBottomSheetState = addBottomSheetState.copy(
            coInsured = CoInsured.fromPersonalInformation(result, ssnQuery),
            errorMessage = null,
          )
        }.onLeft {
          addBottomSheetState = addBottomSheetState.copy(errorMessage = it.message)
        }
      }
    }

    LaunchedEffect(addedCoInsured) {
      addedCoInsured?.let { coInsured ->
        addBottomSheetState = addBottomSheetState.copy(isLoading = true)
        val updatedCoInsured = (listState.coInsured + coInsured).toImmutableList()

        createMidtermChangeUseCase
          .invoke(contractId, updatedCoInsured)
          .fold(
            ifLeft = {
              addBottomSheetState = addBottomSheetState.copy(
                errorMessage = it.message,
                coInsured = null,
                isLoading = false,
              )
              addedCoInsured = null
            },
            ifRight = {
              Snapshot.withMutableSnapshot {
                ssnQuery = null
                addedCoInsured = null
                listState = listState.copy(coInsured = it.coInsured)
                addBottomSheetState = Loaded.AddBottomSheetState(show = false)
              }
            },
          )
      }
    }

    LaunchedEffect(removedCoInsured) {
      removedCoInsured?.let { coInsured ->
        removeBottomSheetState = removeBottomSheetState.copy(isLoading = true)
        val updatedCoInsured = listState.coInsured.filterNot { it.id == coInsured.id }.toImmutableList()

        createMidtermChangeUseCase
          .invoke(contractId, updatedCoInsured)
          .fold(
            ifLeft = {
              removeBottomSheetState = Loaded.RemoveBottomSheetState(
                errorMessage = it.message,
                isLoading = false,
              )
              removedCoInsured = null
            },
            ifRight = {
              listState = listState.copy(coInsured = it.coInsured)
              removedCoInsured = null
              removeBottomSheetState = Loaded.RemoveBottomSheetState(show = false)
            },
          )
      }
    }

    return if (isLoading) {
      Loading
    } else if (errorMessage != null) {
      Error(errorMessage)
    } else if (listState.member != null) {
      Loaded(
        listState = listState,
        addBottomSheetState = addBottomSheetState,
        removeBottomSheetState = removeBottomSheetState,
      )
    } else {
      Error("Could not fetch member")
    }
  }
}

internal sealed interface EditCoInsuredEvent {
  data class FetchCoInsuredPersonalInformation(val ssn: String) : EditCoInsuredEvent
  data class AddCoInsured(val coInsured: CoInsured) : EditCoInsuredEvent
  data class RemoveCoInsured(val coInsured: CoInsured) : EditCoInsuredEvent
  data object OnDismissError : EditCoInsuredEvent
  data object ResetAddBottomSheetState : EditCoInsuredEvent
  data object OnAddCoInsuredClicked : EditCoInsuredEvent
  data object ResetRemoveBottomSheetState : EditCoInsuredEvent
  data class OnRemoveCoInsuredClicked(val coInsured: CoInsured) : EditCoInsuredEvent
}

internal sealed interface EditCoInsuredState {
  data object Loading : EditCoInsuredState

  data class Error(val message: String?) : EditCoInsuredState

  data class Loaded(
    val listState: CoInsuredListState,
    val addBottomSheetState: AddBottomSheetState,
    val removeBottomSheetState: RemoveBottomSheetState,
  ) : EditCoInsuredState {
    data class CoInsuredListState(
      val coInsured: ImmutableList<CoInsured> = persistentListOf(),
      val member: Member? = null,
    )

    data class AddBottomSheetState(
      val coInsured: CoInsured? = null,
      val errorMessage: String? = null,
      val isLoading: Boolean = false,
      val show: Boolean = false,
    )

    data class RemoveBottomSheetState(
      val coInsured: CoInsured? = null,
      val errorMessage: String? = null,
      val isLoading: Boolean = false,
      val show: Boolean = false,
    )

  }
}
