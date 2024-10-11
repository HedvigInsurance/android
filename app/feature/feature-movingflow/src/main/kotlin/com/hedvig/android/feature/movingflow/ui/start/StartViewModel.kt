package com.hedvig.android.feature.movingflow.ui.start

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.movingflow.storage.MovingFlowStorage
import com.hedvig.android.feature.movingflow.ui.start.StartUiState.StartError
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import hedvig.resources.R
import octopus.feature.movingflow.MoveIntentV2CreateMutation

internal class StartViewModel(
  apolloClient: ApolloClient,
  movingFlowStorage: MovingFlowStorage,
) : MoleculeViewModel<StartEvent, StartUiState>(
    StartUiState.Content(HousingType.entries.first(), null, null),
    StartPresenter(apolloClient, movingFlowStorage),
  )

private class StartPresenter(
  private val apolloClient: ApolloClient,
  private val movingFlowStorage: MovingFlowStorage,
) : MoleculePresenter<StartEvent, StartUiState> {
  @Composable
  override fun MoleculePresenterScope<StartEvent>.present(lastState: StartUiState): StartUiState {
    var selectedHousingType by remember {
      mutableStateOf((lastState as? StartUiState.Content)?.housingType ?: HousingType.entries.first())
    }
    var submittingHousingType: HousingType? by remember { mutableStateOf(null) }
    var error: StartError? by remember { mutableStateOf(null) }
    var moveIntentId: String? by remember { mutableStateOf(null) }

    if (submittingHousingType != null) {
      LaunchedEffect(submittingHousingType) {
        either {
          val moveIntentCreate = apolloClient
            .mutation(MoveIntentV2CreateMutation())
            .safeExecute()
            .mapLeft(::ErrorMessage)
            .mapLeft { StartError.GenericError(it) }
            .map { it.moveIntentCreate }
            .bind()
          val moveIntent = ensureNotNull(moveIntentCreate.moveIntent) {
            val userError = moveIntentCreate.userError?.message
            if (userError == null) {
              StartUiState.StartError.GenericError(ErrorMessage("Unknown MoveIntentV2CreateMutation error"))
            } else {
              StartUiState.StartError.UserPresentable(userError)
            }
          }
          movingFlowStorage.initiateNewMovingFlow(moveIntent)
          moveIntent.saveToDb()
          moveIntent.id
        }.fold(
          ifLeft = {
            error = it
          },
          ifRight = { id ->
            moveIntentId = id
          },
        )
      }
    }
    if (errorMessage != null) {
      return StartUiState.Error(errorMessage?.message)
    }
    return StartUiState.Content(selectedHousingType, submittingHousingType, moveIntentId)
  }
}

internal sealed interface StartEvent {
  data class SelectHousingType(val housingType: HousingType) : StartEvent
}

internal sealed interface StartUiState {
  sealed interface StartError : StartUiState {
    data class UserPresentable(val message: String) : StartError

    data class GenericError(val errorMessage: ErrorMessage) : StartError, ErrorMessage by errorMessage
  }

  data class Content(
    val housingType: HousingType,
    val submittingHousingType: HousingType?,
    val initiatedMovingFlowId: String?,
  ) : StartUiState
}

internal enum class HousingType {
  ApartmentRent,
  ApartmentOwn,
  Villa,
}

internal fun HousingType.stringResource() = when (this) {
  HousingType.ApartmentRent -> R.string.CHANGE_ADDRESS_APARTMENT_RENT_LABEL
  HousingType.ApartmentOwn -> R.string.CHANGE_ADDRESS_APARTMENT_OWN_LABEL
  HousingType.Villa -> R.string.CHANGE_ADDRESS_VILLA_LABEL
}
