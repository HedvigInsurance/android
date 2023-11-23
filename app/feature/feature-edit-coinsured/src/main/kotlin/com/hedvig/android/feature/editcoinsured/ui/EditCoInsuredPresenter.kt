package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.CoInsuredError
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.Member
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal class EditCoInsuredPresenter(
  private val contractId: String,
  private val getCoInsuredUseCaseProvider: GetCoInsuredUseCase,
  private val fetchCoInsuredPersonalInformationUseCaseProvider: FetchCoInsuredPersonalInformationUseCase,
) : MoleculePresenter<EditCoInsuredEvent, EditCoInsuredState> {
  @Composable
  override fun MoleculePresenterScope<EditCoInsuredEvent>.present(lastState: EditCoInsuredState): EditCoInsuredState {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var coInsured by remember {
      val lastCoInsured = lastState.safeCast<EditCoInsuredState.Loaded>()?.listState?.coInsured
      val coInsured = lastCoInsured ?: persistentListOf()
      mutableStateOf(coInsured)
    }
    var coInsuredFromSsn by remember { mutableStateOf<CoInsured?>(null) }
    var coInsuredFromSsnError by remember { mutableStateOf<String?>(null) }
    var member by remember { mutableStateOf(lastState.safeCast<EditCoInsuredState.Loaded>()?.listState?.member) }
    var ssnQuery by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
      if (member != null) {
        return@LaunchedEffect
      }

      isLoading = true
      getCoInsuredUseCaseProvider.invoke(contractId)
        .fold(
          ifLeft = {
            Snapshot.withMutableSnapshot {
              isLoading = false
              errorMessage = when (it) {
                CoInsuredError.ContractNotFound -> "Could not find contract"
                is CoInsuredError.GenericError -> it.message
              }
            }
          },
          ifRight = {
            Snapshot.withMutableSnapshot {
              errorMessage = null
              isLoading = false
              coInsured = it.coInsured
              member = it.member
            }
          },
        )
    }

    CollectEvents { event ->
      when (event) {
        is EditCoInsuredEvent.FetchCoInsuredPersonalInformation -> ssnQuery = event.ssn
        is EditCoInsuredEvent.AddCoInsured -> {}
        is EditCoInsuredEvent.RemoveCoInsured -> {}
        EditCoInsuredEvent.ResetBottomSheetState -> Snapshot.withMutableSnapshot {
          coInsuredFromSsn = null
          coInsuredFromSsnError = null
          ssnQuery = null
        }
      }
    }

    LaunchedEffect(ssnQuery) {
      ssnQuery?.let { ssnQuery ->
        fetchCoInsuredPersonalInformationUseCaseProvider.invoke(ssnQuery)
          .fold(
            ifLeft = { coInsuredFromSsnError = it.message },
            ifRight = {
              Snapshot.withMutableSnapshot {
                coInsuredFromSsn = CoInsured.fromPersonalInformation(it, ssnQuery)
                coInsuredFromSsnError = null
              }
            },
          )
      }
    }

    return if (isLoading) {
      EditCoInsuredState.Loading
    } else if (errorMessage != null) {
      EditCoInsuredState.Error(errorMessage)
    } else if (coInsured.isNotEmpty() || member != null) {
      EditCoInsuredState.Loaded(
        listState = EditCoInsuredState.Loaded.CoInsuredListState(
          coInsured = coInsured,
          member = member,
        ),
        bottomSheetState = EditCoInsuredState.Loaded.BottomSheetState(
          coInsuredFromSsn = coInsuredFromSsn,
          coInsuredFromSsnError = coInsuredFromSsnError,
        ),
      )
    } else {
      EditCoInsuredState.Error("Could not fetch co-insured or member")
    }
  }
}

internal sealed interface EditCoInsuredEvent {
  data class FetchCoInsuredPersonalInformation(val ssn: String) : EditCoInsuredEvent

  data class AddCoInsured(val coInsured: CoInsured) : EditCoInsuredEvent

  data class RemoveCoInsured(val coInsured: CoInsured) : EditCoInsuredEvent

  data object ResetBottomSheetState : EditCoInsuredEvent
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
    )
  }
}
