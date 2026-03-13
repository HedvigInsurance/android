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
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.feature.remove.addons.data.CurrentlyActiveAddon
import com.hedvig.feature.remove.addons.data.StartAddonRemovalResponse
import com.hedvig.feature.remove.addons.data.StartAddonRemovalUseCase
import kotlinx.datetime.LocalDate

internal class SelectAddonToRemoveViewModel(
  startAddonRemovalUseCase: StartAddonRemovalUseCase,
  contractId: ContractId,
  preselectedAddonVariant: AddonVariant?,
) : MoleculeViewModel<SelectAddonToRemoveEvent, SelectAddonToRemoveState>(
    initialState = SelectAddonToRemoveState.Loading(),
    presenter = SelectAddonToRemovePresenter(startAddonRemovalUseCase, contractId, preselectedAddonVariant),
  )

private class SelectAddonToRemovePresenter(
  private val startAddonRemovalUseCase: StartAddonRemovalUseCase,
  private val contractId: ContractId,
  private val preselectedAddonVariant: AddonVariant?,
) : MoleculePresenter<SelectAddonToRemoveEvent, SelectAddonToRemoveState> {
  @Composable
  override fun MoleculePresenterScope<SelectAddonToRemoveEvent>.present(
    lastState: SelectAddonToRemoveState,
  ): SelectAddonToRemoveState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var errorMessage: String? by remember { mutableStateOf((lastState as? SelectAddonToRemoveState.Error)?.message) }
    var isLoading: Boolean by remember { mutableStateOf((lastState as? SelectAddonToRemoveState.Loading) != null) }
    var response: StartAddonRemovalResponse? by remember {
      mutableStateOf((lastState as? SelectAddonToRemoveState.Success)?.addonOffer)
    }
    val selectedToggleableOptions = remember {
      val addonsChosenForRemoval = (lastState as? SelectAddonToRemoveState.Success)?.addonsChosenForRemoval.orEmpty()
      mutableStateListOf(*addonsChosenForRemoval.toTypedArray())
    }
    var paramsToNavigateToSummary by remember { mutableStateOf<CommonSummaryParameters?>(null) }

    LaunchedEffect(loadIteration) {
      if (loadIteration != 0) {
        isLoading = true
      }
      startAddonRemovalUseCase.invoke(contractId).fold(
        ifLeft = {
          response = null
          errorMessage = it.message
          selectedToggleableOptions.clear()
        },
        ifRight = { result ->
          val addonsChosenForRemoval = preselectedAddonVariant?.let { addonVariant ->
            listOfNotNull(
              result.existingAddonsToRemove.firstOrNull { it.displayTitle == addonVariant.displayName },
            )
          } ?: if (result.existingAddonsToRemove.size == 1) result.existingAddonsToRemove else emptyList()

          val summaryParams = if (result.existingAddonsToRemove.size == 1) {
            CommonSummaryParameters(
              contractId = contractId,
              addonsToRemove = result.existingAddonsToRemove,
              activationDate = result.activationDate,
              baseCost = result.baseCost,
              currentTotalCost = result.currentTotalCost,
              productVariant = result.productVariant,
              existingAddons = result.existingAddonsToRemove,
            )
          } else {
            null
          }
          if (summaryParams != null) {
            isLoading = true
            paramsToNavigateToSummary = summaryParams
          } else {
            Snapshot.withMutableSnapshot {
              response = result
              errorMessage = null
              selectedToggleableOptions.clear()
              selectedToggleableOptions.addAll(addonsChosenForRemoval)
              isLoading = false
            }
          }
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
          val responseValue = response ?: return@CollectEvents
          val summaryParams = CommonSummaryParameters(
            contractId = contractId,
            addonsToRemove = selectedToggleableOptions,
            activationDate = responseValue.activationDate,
            baseCost = responseValue.baseCost,
            currentTotalCost = responseValue.currentTotalCost,
            productVariant = responseValue.productVariant,
            existingAddons = responseValue.existingAddonsToRemove,
          )
          paramsToNavigateToSummary = summaryParams
        }

        is SelectAddonToRemoveEvent.ToggleOption -> {
          if (selectedToggleableOptions.contains(event.option)) {
            selectedToggleableOptions.remove(event.option)
          } else {
            selectedToggleableOptions.add(event.option)
          }
        }
      }
    }

    val responseValue = response
    return when {
      errorMessage != null -> SelectAddonToRemoveState.Error(errorMessage)

      isLoading -> SelectAddonToRemoveState.Loading(paramsToNavigateToSummary = paramsToNavigateToSummary)

      responseValue != null -> SelectAddonToRemoveState.Success(
        addonOffer = responseValue,
        addonsChosenForRemoval = selectedToggleableOptions,
        paramsToNavigateToSummary = paramsToNavigateToSummary,
      )

      else -> SelectAddonToRemoveState.Error(null)
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

  data class Loading(
    val paramsToNavigateToSummary: CommonSummaryParameters? = null,
  ) : SelectAddonToRemoveState
}

internal sealed interface SelectAddonToRemoveEvent {
  data object Retry : SelectAddonToRemoveEvent

  data object ClearNavigation : SelectAddonToRemoveEvent

  data object Submit : SelectAddonToRemoveEvent

  data class ToggleOption(val option: CurrentlyActiveAddon) : SelectAddonToRemoveEvent
}

internal data class CommonSummaryParameters(
  val contractId: ContractId,
  val addonsToRemove: List<CurrentlyActiveAddon>,
  val activationDate: LocalDate,
  val baseCost: ItemCost,
  val currentTotalCost: ItemCost,
  val productVariant: ProductVariant,
  val existingAddons: List<CurrentlyActiveAddon>,
)
