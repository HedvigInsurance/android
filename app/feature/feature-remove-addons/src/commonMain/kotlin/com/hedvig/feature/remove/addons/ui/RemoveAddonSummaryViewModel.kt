package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.feature.remove.addons.RemoveAddonSubmitFailureKey
import com.hedvig.feature.remove.addons.RemoveAddonSubmitSuccessKey
import com.hedvig.feature.remove.addons.SummaryParameters
import com.hedvig.feature.remove.addons.data.GetAddonRemovalCostBreakdownUseCase
import com.hedvig.feature.remove.addons.data.GetInsurancesWithRemovableAddonsUseCase
import com.hedvig.feature.remove.addons.data.SubmitAddonRemovalUseCase
import com.hedvig.feature.remove.addons.navigateExitingRemoveAddonFlow
import com.hedvig.ui.tiersandaddons.QuoteCostBreakdown
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class RemoveAddonSummaryViewModel(
  @Assisted params: SummaryParameters,
  submitAddonRemovalUseCase: SubmitAddonRemovalUseCase,
  getAddonRemovalCostBreakdownUseCase: GetAddonRemovalCostBreakdownUseCase,
  getInsurancesWithRemovableAddonsUseCase: GetInsurancesWithRemovableAddonsUseCase,
  backstack: Backstack,
) : MoleculeViewModel<
    RemoveAddonSummaryEvent,
    RemoveAddonSummaryState,
  >(
    initialState = RemoveAddonSummaryState.Loading,
    presenter = RemoveAddonSummaryPresenter(
      submitAddonRemovalUseCase = submitAddonRemovalUseCase,
      params = params,
      getAddonRemovalCostBreakdownUseCase = getAddonRemovalCostBreakdownUseCase,
      getInsurancesWithRemovableAddonsUseCase = getInsurancesWithRemovableAddonsUseCase,
      backstack = backstack,
    ),
  )

private class RemoveAddonSummaryPresenter(
  private val submitAddonRemovalUseCase: SubmitAddonRemovalUseCase,
  private val params: SummaryParameters,
  private val getAddonRemovalCostBreakdownUseCase: GetAddonRemovalCostBreakdownUseCase,
  private val getInsurancesWithRemovableAddonsUseCase: GetInsurancesWithRemovableAddonsUseCase,
  private val backstack: Backstack,
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

    LaunchedEffect(loadIteration) {
      val exposureName = getInsurancesWithRemovableAddonsUseCase
        .invoke()
        .getOrNull()
        ?.firstOrNull { it.contractId == params.contractId }
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
          )
        },
      )
    }

    LaunchedEffect(submitIteration) {
      val state = currentState as? RemoveAddonSummaryState.Content ?: return@LaunchedEffect
      if (submitIteration > 0) {
        currentState = RemoveAddonSummaryState.Loading
        submitAddonRemovalUseCase.invoke(
          params.contractId,
          params.addonsToRemove.map {
            it.id
          },
        ).fold(
          ifLeft = {
            currentState = state
            backstack.add(RemoveAddonSubmitFailureKey)
          },
          ifRight = {
            backstack.navigateExitingRemoveAddonFlow(RemoveAddonSubmitSuccessKey(params.activationDate))
          },
        )
      }
    }

    CollectEvents { event: RemoveAddonSummaryEvent ->
      when (event) {
        is RemoveAddonSummaryEvent.Submit -> {
          submitIteration++
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
    val summaryParams: SummaryParameters,
    val costBreakdown: QuoteCostBreakdown,
    val exposureName: String,
  ) : RemoveAddonSummaryState

  data object Loading : RemoveAddonSummaryState

  data object Failure : RemoveAddonSummaryState
}

internal interface RemoveAddonSummaryEvent {
  data object Retry : RemoveAddonSummaryEvent

  data object Submit : RemoveAddonSummaryEvent
}
