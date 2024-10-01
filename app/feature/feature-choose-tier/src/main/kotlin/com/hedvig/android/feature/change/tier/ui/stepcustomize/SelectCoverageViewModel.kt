package com.hedvig.android.feature.change.tier.ui.stepcustomize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeDeductibleForChosenTier
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeTier
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ClearNavigationStep
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.Reload
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.SubmitChosenQuoteToContinue
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Failure
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Loading
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Success
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class SelectCoverageViewModel(
  insuranceId: String,
  intent: ChangeTierDeductibleIntent,
  getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
) : MoleculeViewModel<SelectCoverageEvent, SelectCoverageState>(
    initialState = Loading,
    presenter = SelectCoveragePresenter(
      insuranceId = insuranceId,
      intent = intent,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    ),
  )

private class SelectCoveragePresenter(
  val insuranceId: String,
  val intent: ChangeTierDeductibleIntent,
  val getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
) : MoleculePresenter<SelectCoverageEvent, SelectCoverageState> {
  @Composable
  override fun MoleculePresenterScope<SelectCoverageEvent>.present(
    lastState: SelectCoverageState,
  ): SelectCoverageState {
    var chosenTier by remember { mutableStateOf(if (lastState is Success) lastState.uiState.chosenTier else null) }
    var chosenQuote by remember { mutableStateOf(if (lastState is Success) lastState.uiState.chosenQuote else null) }
    var quoteToNavigateFurther by remember { mutableStateOf<TierDeductibleQuote?>(null) }

    var currentPartialState by remember { mutableStateOf(mapLastStateToPartial(state = lastState)) }

    var currentContractLoadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        is ChangeDeductibleForChosenTier -> {
          chosenQuote = event.quote
        }

        is ChangeTier -> {
          val state = currentPartialState
          if (state !is PartialUiState.Success) return@CollectEvents
          // set newly chosen tier
          chosenTier = event.tier
          // try to pre-choose a quote with the same deductible and newly chosen coverage
          // if there is no such quote, the deductible will not be per-chosen
          val currentlyChosenDeductible = chosenQuote?.deductible
          val quoteWithNewTierOldDeductible =
            state.map[event.tier]!!.firstOrNull { it.deductible == currentlyChosenDeductible }
          chosenQuote = quoteWithNewTierOldDeductible
        }

        ClearNavigationStep -> {
          quoteToNavigateFurther = null
        }

        SubmitChosenQuoteToContinue -> {
          val state = currentPartialState
          if (state !is PartialUiState.Success) return@CollectEvents
          if (chosenQuote != state.currentActiveQuote) {
            quoteToNavigateFurther = chosenQuote
          }
        }

        Reload -> currentContractLoadIteration++
      }
    }

    LaunchedEffect(currentContractLoadIteration) {
      getCurrentContractDataUseCase.invoke(insuranceId).fold(
        ifLeft = {
          currentPartialState = PartialUiState.Failure
        },
        ifRight = { currentContractData ->
          val current = TierDeductibleQuote(
            id = CURRENT_ID,
            deductible = currentContractData.deductible,
            tier = currentContractData.tier,
            productVariant = currentContractData.productVariant,
            displayItems = listOf(),
            premium = currentContractData.currentDisplayPremium,
          )
          // pre-choosing current quote
          chosenTier = current.tier
          chosenQuote = current
          currentPartialState = PartialUiState.Success(
            contractData = ContractData(
              activeDisplayPremium = current.premium.toString(),
              contractGroup = current.productVariant.contractGroup,
              contractDisplayName = current.productVariant.displayName,
              contractDisplaySubtitle = currentContractData.currentExposureName,
            ),
            // setting current quote aside for comparison later
            currentActiveQuote = current,
            // adding current tierName and quote to the list, create map
            map = mapQuotesToTiersAndQuotes(intent.quotes + listOf(current)),
          )
        },
      )
    }
    return when (currentPartialState) {
      PartialUiState.Failure -> Failure
      PartialUiState.Loading -> Loading
      is PartialUiState.Success -> Success(
        map = (currentPartialState as PartialUiState.Success).map,
        currentActiveQuote = (currentPartialState as PartialUiState.Success).currentActiveQuote,
        uiState = SelectCoverageSuccessUiState(
          isCurrentChosen = chosenQuote == (currentPartialState as PartialUiState.Success).currentActiveQuote,
          chosenQuote = chosenQuote,
          chosenTier = chosenTier,
          tiers = buildListOfTiersAndPremiums(
            map = (currentPartialState as PartialUiState.Success).map,
            currentDeductible = chosenQuote?.deductible,
          ),
          quotesForChosenTier = (currentPartialState as PartialUiState.Success).map[chosenTier]!!,
          isTierChoiceEnabled = (currentPartialState as PartialUiState.Success).map.keys.size > 1,
          contractData = (currentPartialState as PartialUiState.Success).contractData,
        ),
      )
    }
  }
}

private fun buildListOfTiersAndPremiums(
  map: SnapshotStateMap<Tier, List<TierDeductibleQuote>>,
  currentDeductible: Deductible?,
): List<Pair<Tier, String>> {
  return buildList {
    map.keys.forEach { tier ->
      // trying to show premium for same deductible in different tier-coverage,
      // but if this doesn't work, the lowest for this coverage
      val premium = map[tier]!!.firstOrNull {
        it.deductible == currentDeductible
      }?.premium ?: map[tier]!!.minBy { it.tier.tierLevel }.premium
      add(tier to premium.toString())
    }
  }
}

private fun mapQuotesToTiersAndQuotes(
  quotes: List<TierDeductibleQuote>,
): SnapshotStateMap<Tier, List<TierDeductibleQuote>> {
  val grouped = quotes
    .groupBy {
      it.tier
    }
    .map { entry ->
      entry.key to entry.value.sortedBy {
        it.premium.amount
      }
    }
  val result = mutableStateMapOf(*grouped.toTypedArray())
  return result
}

internal sealed interface SelectCoverageEvent {
  data object SubmitChosenQuoteToContinue : SelectCoverageEvent

  data class ChangeDeductibleForChosenTier(val quote: TierDeductibleQuote) : SelectCoverageEvent

  data class ChangeTier(val tier: Tier) : SelectCoverageEvent

  data object ClearNavigationStep : SelectCoverageEvent

  data object Reload : SelectCoverageEvent
}

private fun mapLastStateToPartial(state: SelectCoverageState): PartialUiState {
  return when (state) {
    Loading -> PartialUiState.Loading
    Failure -> PartialUiState.Failure
    is Success -> PartialUiState.Success(
      contractData = state.uiState.contractData,
      currentActiveQuote = state.currentActiveQuote,
      map = state.map,
    )
  }
}

private sealed interface PartialUiState {
  data object Loading : PartialUiState

  data object Failure : PartialUiState

  data class Success(
    val contractData: ContractData,
    val currentActiveQuote: TierDeductibleQuote,
    val map: SnapshotStateMap<Tier, List<TierDeductibleQuote>>,
  ) : PartialUiState
}

internal sealed interface SelectCoverageState {
  data object Loading : SelectCoverageState

  data class Success(
    val uiState: SelectCoverageSuccessUiState,
    val currentActiveQuote: TierDeductibleQuote,
    val map: SnapshotStateMap<Tier, List<TierDeductibleQuote>>,
  ) : SelectCoverageState

  data object Failure : SelectCoverageState
}

internal data class SelectCoverageSuccessUiState(
  val contractData: ContractData,
  val chosenTier: Tier?,
  val chosenQuote: TierDeductibleQuote?,
  val isCurrentChosen: Boolean,
  val isTierChoiceEnabled: Boolean,
  val quoteToNavigateFurther: TierDeductibleQuote? = null,
  val tiers: List<Pair<Tier, String>>, // sorted list of tiers with corresponding premiums (depending on selected deductible)
  val quotesForChosenTier: List<TierDeductibleQuote>,
)

internal data class ContractData(
  val contractGroup: ContractGroup,
  val contractDisplayName: String,
  val contractDisplaySubtitle: String,
  val activeDisplayPremium: String,
)

private const val CURRENT_ID = "current"
