package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.feature.remove.addons.data.CurrentlyActiveAddon
import com.hedvig.feature.remove.addons.data.StartAddonRemovalResponse
import com.hedvig.feature.remove.addons.data.StartAddonRemovalUseCase
import kotlinx.datetime.LocalDate

internal class SelectAddonToRemoveViewModel(
  startAddonRemovalUseCase: StartAddonRemovalUseCase,
  contractAndAddonId: Pair<String, String?>,
) : MoleculeViewModel<
    SelectAddonToRemoveEvent,
    SelectAddonToRemoveState,
  >(
    initialState = SelectAddonToRemoveState.Loading,
    presenter = SelectAddonToRemovePresenter(startAddonRemovalUseCase, contractAndAddonId),
  )

private class SelectAddonToRemovePresenter(
  private val startAddonRemovalUseCase: StartAddonRemovalUseCase,
  private val contractAndAddonId: Pair<String, String?>,
) : MoleculePresenter<
    SelectAddonToRemoveEvent,
    SelectAddonToRemoveState,
  > {
  @Composable
  override fun MoleculePresenterScope<SelectAddonToRemoveEvent>.present(
    lastState: SelectAddonToRemoveState,
  ): SelectAddonToRemoveState {
    var currentState: SelectAddonToRemoveState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var paramsToNavigateToSummary by remember { mutableStateOf<CommonSummaryParameters?>(null) }
    val selectedToggleableOptions = remember {
      mutableStateListOf(
        *(
          (lastState as? SelectAddonToRemoveState.Success)
            ?.addonsChosenForRemoval ?: emptyList()
        ).toTypedArray(),
      )
    }

    LaunchedEffect(loadIteration) {
      startAddonRemovalUseCase.invoke(contractAndAddonId.first).fold(
        ifLeft = {
          currentState = SelectAddonToRemoveState.Error(it.message)
        },
        ifRight = { result ->
          currentState = SelectAddonToRemoveState.Success(
            addonOffer = result,
            addonsChosenForRemoval = contractAndAddonId.second?.let { preselectedId ->
              val preselected = result.existingAddonsToRemove.firstOrNull { it.id == preselectedId }
              preselected?.let {
                listOf(preselected)
              }
            } ?: emptyList(),
          )
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        SelectAddonToRemoveEvent.Retry -> {
          loadIteration++
        }

        SelectAddonToRemoveEvent.ClearNavigation -> {
          paramsToNavigateToSummary = null
        }

        SelectAddonToRemoveEvent.Submit -> {
          val state = currentState as? SelectAddonToRemoveState.Success ?: return@CollectEvents
          val summaryParams = CommonSummaryParameters(
            addonsToRemove = selectedToggleableOptions,
            activationDate = state.addonOffer.activationDate,
            baseCost = state.addonOffer.baseCost,
            currentTotalCost = state.addonOffer.currentTotalCost,
            contractId = contractAndAddonId.first,
            productVariant = state.addonOffer.productVariant,
            existingAddons = state.addonOffer.existingAddonsToRemove,
          )
          paramsToNavigateToSummary = summaryParams
        }

        is SelectAddonToRemoveEvent.ToggleOption -> {
          Snapshot.withMutableSnapshot {
            if (selectedToggleableOptions.contains(event.option)) {
              selectedToggleableOptions.remove(event.option)
            } else {
              selectedToggleableOptions.add(event.option)
            }
          }
        }
      }
    }

    return when (val state = currentState) {
      is SelectAddonToRemoveState.Error,
      SelectAddonToRemoveState.Loading,
      -> state

      is SelectAddonToRemoveState.Success -> state.copy(
        addonsChosenForRemoval = selectedToggleableOptions,
        paramsToNavigateToSummary = paramsToNavigateToSummary,
      )
    }
  }
}

internal sealed interface SelectAddonToRemoveState {
  data class Success(
    val addonOffer: StartAddonRemovalResponse,
    val addonsChosenForRemoval: List<CurrentlyActiveAddon>,
    val paramsToNavigateToSummary: CommonSummaryParameters? = null,
  ) : SelectAddonToRemoveState

  data class Error(val message: String?) : SelectAddonToRemoveState

  data object Loading : SelectAddonToRemoveState
}

internal sealed interface SelectAddonToRemoveEvent {
  data object Retry : SelectAddonToRemoveEvent

  data object ClearNavigation : SelectAddonToRemoveEvent

  data object Submit : SelectAddonToRemoveEvent

  data class ToggleOption(val option: CurrentlyActiveAddon) : SelectAddonToRemoveEvent
}

internal data class CommonSummaryParameters(
  val contractId: String,
  val addonsToRemove: List<CurrentlyActiveAddon>,
  val activationDate: LocalDate,
  val baseCost: ItemCost,
  val currentTotalCost: ItemCost,
  val productVariant: ProductVariant,
  val existingAddons: List<CurrentlyActiveAddon>,
)
