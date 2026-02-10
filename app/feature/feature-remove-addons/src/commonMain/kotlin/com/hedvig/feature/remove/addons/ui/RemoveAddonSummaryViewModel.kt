package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.feature.remove.addons.data.SubmitAddonRemovalUseCase
import kotlinx.datetime.LocalDate

internal class RemoveAddonSummaryViewModel(
  params: CommonSummaryParameters,
  submitAddonRemovalUseCase: SubmitAddonRemovalUseCase,
) : MoleculeViewModel<
  RemoveAddonSummaryEvent, RemoveAddonSummaryState,
  >(
  initialState = RemoveAddonSummaryState.Content(params),
  presenter = RemoveAddonSummaryPresenter(submitAddonRemovalUseCase, params),
)

private class RemoveAddonSummaryPresenter(
  private val submitAddonRemovalUseCase: SubmitAddonRemovalUseCase,
  private val params: CommonSummaryParameters,
) : MoleculePresenter<
  RemoveAddonSummaryEvent, RemoveAddonSummaryState,
  > {
  @Composable
  override fun MoleculePresenterScope<RemoveAddonSummaryEvent>.present(
    lastState: RemoveAddonSummaryState,
  ): RemoveAddonSummaryState {
    var currentState: RemoveAddonSummaryState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (loadIteration > 0) {
        currentState = RemoveAddonSummaryState.Loading()
        submitAddonRemovalUseCase.invoke(
          params.contractId,
          params.addonsToRemove.map {
            it.id
          },
        ).fold(
          ifLeft = {
            currentState = RemoveAddonSummaryState.Content(
              params,
              Unit)
          },
          ifRight = {
            currentState = RemoveAddonSummaryState.Loading(params.activationDate)
          },
        )
      }
    }

    CollectEvents { event: RemoveAddonSummaryEvent ->
      when (event) {
         is RemoveAddonSummaryEvent.Submit -> {
           loadIteration++
         }
        is RemoveAddonSummaryEvent.ReturnToInitialState -> {
          currentState = RemoveAddonSummaryState.Content(params)
        }
        is RemoveAddonSummaryEvent.Retry -> {
          loadIteration++
        }
      }
    }

    return currentState
  }
}

internal sealed interface RemoveAddonSummaryState {
  data class Content(
    val summaryParams: CommonSummaryParameters,
    val navigateToFailure: Unit? = null,
  ) : RemoveAddonSummaryState

  data class Loading(
    val activationDateToNavigateToSuccess: LocalDate? = null,
  ) : RemoveAddonSummaryState
}

internal interface RemoveAddonSummaryEvent {
  data object Retry : RemoveAddonSummaryEvent
  data object ReturnToInitialState : RemoveAddonSummaryEvent
  data object Submit : RemoveAddonSummaryEvent
}
