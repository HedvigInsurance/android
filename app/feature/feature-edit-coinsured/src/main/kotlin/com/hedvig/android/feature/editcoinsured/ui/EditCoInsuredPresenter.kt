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
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal class EditCoInsuredPresenter(
  private val contractId: String,
  private val getCoInsuredUseCaseProvider: Provider<GetCoInsuredUseCase>,
) : MoleculePresenter<EditCoInsuredEvent, EditCoInsuredState> {
  @Composable
  override fun MoleculePresenterScope<EditCoInsuredEvent>.present(lastState: EditCoInsuredState): EditCoInsuredState {
    var errorMessage by remember { mutableStateOf(lastState.errorMessage) }
    var isLoading by remember { mutableStateOf(lastState.isLoading) }
    var coInsured by remember { mutableStateOf(lastState.coInsured) }
    LaunchedEffect(Unit) {
      isLoading = true
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
          }
        },
      )
    }
    return EditCoInsuredState(
      isLoading = isLoading,
      errorMessage = errorMessage,
      coInsured = coInsured,
    )
  }
}

internal sealed interface EditCoInsuredEvent

internal data class EditCoInsuredState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val coInsured: ImmutableList<CoInsured> = persistentListOf(),
)
