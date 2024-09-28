package com.hedvig.android.feature.change.tier.ui.stepcustomize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.change.tier.data.ChangeTierDeductibleIntent
import com.hedvig.android.feature.change.tier.data.Deductible
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.data.Tier
import com.hedvig.android.feature.change.tier.data.TierDeductibleQuote
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeDeductibleForChosenTier
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeTier
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ClearNavigationStep
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.Reload
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.SubmitChosenQuoteToContinue
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageUiState.Failure
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageUiState.Loading
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageUiState.Success
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class SelectCoverageViewModel(
  insuranceId: String,
  intent: ChangeTierDeductibleIntent,
  getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
) : MoleculeViewModel<SelectCoverageEvent, SelectCoverageUiState>(
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
) : MoleculePresenter<SelectCoverageEvent, SelectCoverageUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectCoverageEvent>.present(
    lastState: SelectCoverageUiState,
  ): SelectCoverageUiState {

    val pairs = mapQuotesToTiersAndDeductibles(intent.quotes)
    var chosenQuote by remember { mutableStateOf<TierDeductibleQuote?>(null) }
    var currentState by remember { mutableStateOf(lastState) }
    var currentContractLoadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        is ChangeDeductibleForChosenTier -> TODO()
        is ChangeTier -> TODO()
        ClearNavigationStep -> TODO()
        SubmitChosenQuoteToContinue -> TODO()
        Reload -> currentContractLoadIteration++
      }
    }

    LaunchedEffect(currentContractLoadIteration) {
      getCurrentContractDataUseCase.invoke(insuranceId).fold(
        ifLeft = {
          currentState = Failure
        },
        ifRight = { currentContractData ->
          val currentPairIndex = pairs.indexOfFirst {
            it.first.tierName == currentContractData.tier.tierName &&
              it.second.contains(currentContractData.deductible)
          }
          val currentDeductibleIndex = pairs.first{
            it.first.tierName == currentContractData.tier.tierName &&
              it.second.contains(currentContractData.deductible)
          }.second.indexOf(currentContractData.deductible)
          TODO()
//          currentState = Success(
//
//          )
        },
      )
    }
    return currentState
  }
}

private fun mapQuotesToTiersAndDeductibles(quotes: List<TierDeductibleQuote>)
  :  List<Pair<Tier, List<Deductible>>> {
  val grouped = quotes.groupBy {
    it.tier
  }.map { entry ->
    entry.key to entry.value.map { quote ->
      quote.deductible
    }
  }
  return grouped
}

private fun mapTierAndDeductibleToQuote(pair: Pair<Tier, Deductible>, list: List<TierDeductibleQuote>)
  : TierDeductibleQuote? {
  return list.firstOrNull {
    it.tier == pair.first && it.deductible == pair.second
  }
}


internal sealed interface SelectCoverageEvent {
  data object SubmitChosenQuoteToContinue : SelectCoverageEvent
  data class ChangeDeductibleForChosenTier(val deductible: Deductible) : SelectCoverageEvent
  data class ChangeTier(val tier: Tier) : SelectCoverageEvent
  data object ClearNavigationStep : SelectCoverageEvent
  data object Reload : SelectCoverageEvent
}

internal interface SelectCoverageUiState {
  data object Loading : SelectCoverageUiState

  data class Success(
    val data: CustomizeContractData,
    val chosenPairIndex: Int?,
    val chosenDeductibleIndex: Int?,
    val newDisplayPremium: String,
    val isCurrentChosen: Boolean,
    val isTierChoiceEnabled: Boolean,
    val quoteToNavigateFurther: TierDeductibleQuote? = null,
  ) : SelectCoverageUiState

  data object Failure : SelectCoverageUiState
}

internal data class CustomizeContractData(
  val contractGroup: ContractGroup,
  val displayName: String,
  val displaySubtitle: String,
  val currentDisplayPremium: String,
  val tierAndDeductiblesData: List<Pair<Tier, List<Deductible>>>
)
