package com.hedvig.android.feature.movingflow.ui.selectcontract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.selectcontract.SelectContractState.NotEmpty
import com.hedvig.android.feature.movingflow.ui.selectcontract.SelectContractState.NotEmpty.Content
import com.hedvig.android.feature.movingflow.ui.selectcontract.SelectContractState.NotEmpty.Redirecting
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import octopus.feature.movingflow.MoveIntentV2CreateMutation
import octopus.feature.movingflow.fragment.MoveIntentFragment

internal class SelectContractViewModel(
  apolloClient: ApolloClient,
  movingFlowRepository: MovingFlowRepository,
) : MoleculeViewModel<SelectContractEvent, SelectContractState>(
    presenter = SelectContractPresenter(apolloClient, movingFlowRepository),
    initialState = SelectContractState.Loading,
  )

internal class SelectContractPresenter(
  private val apolloClient: ApolloClient,
  private val movingFlowRepository: MovingFlowRepository,
) : MoleculePresenter<SelectContractEvent, SelectContractState> {
  @Composable
  override fun MoleculePresenterScope<SelectContractEvent>.present(
    lastState: SelectContractState,
  ): SelectContractState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }
    var submittingAddressId: String? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        SelectContractEvent.ClearNavigation -> {
          val state = currentState as? Content ?: return@CollectEvents
          currentState = state.copy(navigateToHousingType = false)
        }

        is SelectContractEvent.SelectContract -> {
          val state = currentState as? Content ?: return@CollectEvents
          currentState = state.copy(
            selectedAddress = event.contract,
          )
        }

        SelectContractEvent.SubmitContract -> {
          val state = currentState as? Content ?: return@CollectEvents
          val selectedId = state.selectedAddress ?: return@CollectEvents
          submittingAddressId = selectedId.id
        }

        SelectContractEvent.RetryLoadData -> loadIteration++
      }
    }

    LaunchedEffect(submittingAddressId) {
      val id = submittingAddressId
      if (id != null) {
        val state = currentState as? NotEmpty ?: return@LaunchedEffect
        if (state is Content) {
          currentState = state.copy(buttonLoading = true)
        }
        val moveIntent = state.intent
        movingFlowRepository.initiateNewMovingFlow(moveIntent, id)
        submittingAddressId = null
        currentState = when (state) {
          is Content -> state.copy(navigateToHousingType = true, buttonLoading = false)
          is Redirecting -> state.copy(navigateToHousingType = true)
        }
      }
    }

    LaunchedEffect(loadIteration) {
      either {
        val moveIntentCreate = apolloClient
          .mutation(MoveIntentV2CreateMutation())
          .safeExecute()
          .mapLeft(::ErrorMessage)
          .mapLeft { SelectContractState.Error.GenericError(it) }
          .map { it.moveIntentCreate }
          .bind()
        val moveIntent = ensureNotNull(moveIntentCreate.moveIntent) {
          val userError = moveIntentCreate.userError?.message
          if (userError == null) {
            SelectContractState.Error.GenericError(
              ErrorMessage(
                "Unknown MoveIntentV2CreateMutation error",
              ),
            )
          } else {
            SelectContractState.Error.UserPresentable(userError)
          }
        }
        moveIntent
      }.fold(
        ifLeft = { error ->

          currentState = error
        },
        ifRight = { intent ->
          val currentAddressesSize = intent.currentHomeAddresses.size
          when {
            currentAddressesSize > 1 -> {
              currentState = Content(
                intent = intent,
                selectedAddress = null,
                navigateToHousingType = false,
                buttonLoading = false,
              )
            }

            currentAddressesSize == 1 -> {
              Snapshot.withMutableSnapshot {
                submittingAddressId = intent.currentHomeAddresses[0].id
                currentState = Redirecting(
                  intent = intent,
                  selectedAddress = intent.currentHomeAddresses[0],
                  navigateToHousingType = false,
                )
              }
            }

            else -> {
              currentState = SelectContractState.Error.GenericError(
                ErrorMessage("empty current addresses list"),
              )
            }
          }
        },
      )
    }
    return currentState
  }
}

internal sealed interface SelectContractEvent {
  data class SelectContract(val contract: MoveIntentFragment.CurrentHomeAddress) : SelectContractEvent

  data object SubmitContract : SelectContractEvent

  data object ClearNavigation : SelectContractEvent

  data object RetryLoadData : SelectContractEvent
}

internal sealed interface SelectContractState {
  data object Loading : SelectContractState

  sealed interface Error : SelectContractState {
    data class UserPresentable(val message: String) : Error

    data class GenericError(val errorMessage: ErrorMessage) : Error, ErrorMessage by errorMessage
  }

  sealed interface NotEmpty : SelectContractState {
    val intent: MoveIntentFragment
    val selectedAddress: MoveIntentFragment.CurrentHomeAddress?
    val navigateToHousingType: Boolean

    data class Redirecting(
      override val intent: MoveIntentFragment,
      override val selectedAddress: MoveIntentFragment.CurrentHomeAddress,
      override val navigateToHousingType: Boolean,
    ) : NotEmpty

    data class Content(
      override val intent: MoveIntentFragment,
      override val selectedAddress: MoveIntentFragment.CurrentHomeAddress?,
      override val navigateToHousingType: Boolean,
      val buttonLoading: Boolean,
    ) : NotEmpty
  }
}
