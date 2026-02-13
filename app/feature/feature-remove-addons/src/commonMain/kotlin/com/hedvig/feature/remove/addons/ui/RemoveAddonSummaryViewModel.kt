package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.feature.remove.addons.data.GetAddonRemovalCostBreakdownUseCase
import com.hedvig.feature.remove.addons.data.GetInsurancesWithRemovableAddonsUseCase
import com.hedvig.feature.remove.addons.data.SubmitAddonRemovalUseCase
import com.hedvig.ui.tiersandaddons.QuoteCostBreakdown
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate

internal class RemoveAddonSummaryViewModel(
  params: CommonSummaryParameters,
  submitAddonRemovalUseCase: SubmitAddonRemovalUseCase,
  getAddonRemovalCostBreakdownUseCase: GetAddonRemovalCostBreakdownUseCase,
  getInsurancesWithRemovableAddonsUseCase: GetInsurancesWithRemovableAddonsUseCase,
) : MoleculeViewModel<
    RemoveAddonSummaryEvent,
    RemoveAddonSummaryState,
  >(
    initialState = RemoveAddonSummaryState.Loading(),
    presenter = RemoveAddonSummaryPresenter(
      submitAddonRemovalUseCase = submitAddonRemovalUseCase,
      params = params,
      getAddonRemovalCostBreakdownUseCase = getAddonRemovalCostBreakdownUseCase,
      getInsurancesWithRemovableAddonsUseCase = getInsurancesWithRemovableAddonsUseCase,
    ),
  )

private class RemoveAddonSummaryPresenter(
  private val submitAddonRemovalUseCase: SubmitAddonRemovalUseCase,
  private val params: CommonSummaryParameters,
  private val getAddonRemovalCostBreakdownUseCase: GetAddonRemovalCostBreakdownUseCase,
  private val getInsurancesWithRemovableAddonsUseCase: GetInsurancesWithRemovableAddonsUseCase,
) : MoleculePresenter<
    RemoveAddonSummaryEvent,
    RemoveAddonSummaryState,
  > {
  @Composable
  override fun MoleculePresenterScope<RemoveAddonSummaryEvent>.present(
    lastState: RemoveAddonSummaryState,
  ): RemoveAddonSummaryState {
    var currentState: RemoveAddonSummaryState by remember { mutableStateOf(lastState) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var activationDateForNavigation by remember { mutableStateOf<LocalDate?>(null) }
    var failureForNavigation by remember { mutableStateOf<Unit?>(null) }

    LaunchedEffect(loadIteration) {
      val exposureName = getInsurancesWithRemovableAddonsUseCase
        .invoke()
        .getOrNull()
        ?.firstOrNull { it.id == params.contractId }
        ?.contractExposure
      if (exposureName == null) {
        currentState = RemoveAddonSummaryState.Failure
        return@LaunchedEffect
      }
      getAddonRemovalCostBreakdownUseCase.invoke(
        contractId = params.contractId,
        addonsToRemove = params.addonsToRemove,
        addonsLeft = params.existingAddons.filter { !params.addonsToRemove.contains(it) },
        baseCost = params.baseCost,
        insuranceDisplayName = params.productVariant.displayName,
      ).fold(
        ifLeft = {
          currentState = RemoveAddonSummaryState.Failure
        },
        ifRight = { result ->
          currentState = RemoveAddonSummaryState.Content(
            summaryParams = params,
            costBreakdown = result,
            exposureName = exposureName,
            navigateToFailure = null,
          )
        },
      )
    }

    LaunchedEffect(submitIteration) {
      val state = currentState as? RemoveAddonSummaryState.Content ?: return@LaunchedEffect
      if (submitIteration > 0) {
        currentState = RemoveAddonSummaryState.Loading()
        submitAddonRemovalUseCase.invoke(
          params.contractId,
          params.addonsToRemove.map {
            it.id
          },
        ).fold(
          ifLeft = {
            failureForNavigation = Unit
            currentState = state
          },
          ifRight = {
            activationDateForNavigation = params.activationDate
          },
        )
      }
    }

    CollectEvents { event: RemoveAddonSummaryEvent ->
      when (event) {
        is RemoveAddonSummaryEvent.Submit -> {
          submitIteration++
        }

        is RemoveAddonSummaryEvent.ReturnToInitialState -> {
          failureForNavigation = null
          activationDateForNavigation = null
        }

        is RemoveAddonSummaryEvent.Retry -> {
          loadIteration++
        }
      }
    }

    return when (val state = currentState) {
      is RemoveAddonSummaryState.Content -> state.copy(
        navigateToFailure = failureForNavigation,
      )

      is RemoveAddonSummaryState.Loading -> state.copy(activationDateToNavigateToSuccess = activationDateForNavigation)

      RemoveAddonSummaryState.Failure -> state
    }
  }
}

internal sealed interface RemoveAddonSummaryState {
  data class Content(
    val summaryParams: CommonSummaryParameters,
    val costBreakdown: QuoteCostBreakdown,
    val exposureName: String,
    val navigateToFailure: Unit? = null,
  ) : RemoveAddonSummaryState

  data class Loading(
    val activationDateToNavigateToSuccess: LocalDate? = null,
  ) : RemoveAddonSummaryState

  data object Failure : RemoveAddonSummaryState
}

internal interface RemoveAddonSummaryEvent {
  data object Retry : RemoveAddonSummaryEvent

  data object ReturnToInitialState : RemoveAddonSummaryEvent

  data object Submit : RemoveAddonSummaryEvent
}
