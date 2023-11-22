package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.CoInsuredError
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.Member
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

internal class EditCoInsuredPresenter(
  private val contractId: String,
  private val getCoInsuredUseCaseProvider: Provider<GetCoInsuredUseCase>,
  private val fetchCoInsuredPersonalInformationUseCaseProvider: Provider<FetchCoInsuredPersonalInformationUseCase>,
) : MoleculePresenter<EditCoInsuredEvent, EditCoInsuredState> {
  @Composable
  override fun MoleculePresenterScope<EditCoInsuredEvent>.present(lastState: EditCoInsuredState): EditCoInsuredState {
    var errorMessage by remember { mutableStateOf(lastState.errorMessage) }
    var isLoading by remember { mutableStateOf(lastState.isLoading) }
    var coInsured by remember { mutableStateOf(lastState.coInsured) }
    var coInsuredFromSsn by remember { mutableStateOf(lastState.coInsuredFromSsn) }
    var member by remember { mutableStateOf(lastState.member) }

    LaunchedEffect(Unit) {
      Snapshot.withMutableSnapshot {
        isLoading = true
      }
      getCoInsuredUseCaseProvider.provide().invoke(contractId).fold(
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
        is EditCoInsuredEvent.FetchCoInsuredPersonalInformation -> {
          launch {
            fetchCoInsuredPersonalInformationUseCaseProvider.provide()
              .invoke(event.ssn)
              .fold(
                ifLeft = { errorMessage = it.message },
                ifRight = { coInsuredFromSsn = it },
              )
          }
        }

        is EditCoInsuredEvent.AddCoInsured -> {}
        is EditCoInsuredEvent.RemoveCoInsured -> {}
      }
    }
    return EditCoInsuredState(
      isLoading = isLoading,
      errorMessage = errorMessage,
      coInsured = coInsured,
      coInsuredFromSsn = coInsuredFromSsn,
      member = member,
    )
  }
}

internal sealed interface EditCoInsuredEvent {
  data class FetchCoInsuredPersonalInformation(val ssn: String) : EditCoInsuredEvent
  data class AddCoInsured(val coInsured: CoInsured) : EditCoInsuredEvent
  data class RemoveCoInsured(val coInsured: CoInsured) : EditCoInsuredEvent
}

internal data class EditCoInsuredState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val coInsured: ImmutableList<CoInsured> = persistentListOf(),
  val member: Member? = null,
  val isLoadingPersonalInfo: Boolean = false,
  val coInsuredFromSsn: CoInsured? = null,
)
