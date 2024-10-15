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
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.ui.stepcustomize.FailureReason.GENERAL
import com.hedvig.android.feature.change.tier.ui.stepcustomize.FailureReason.QUOTES_ARE_EMPTY
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeDeductibleForChosenTier
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeDeductibleInDialog
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeTier
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeTierInDialog
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ClearNavigateFurtherStep
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ClearNavigateToComparison
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.LaunchComparison
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.Reload
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.SetDeductibleToPreviouslyChosen
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.SetTierToPreviouslyChosen
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.SubmitChosenQuoteToContinue
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Failure
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Loading
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Success
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class SelectCoverageViewModel(
  params: InsuranceCustomizationParameters,
  tierRepository: ChangeTierRepository,
  getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
) : MoleculeViewModel<SelectCoverageEvent, SelectCoverageState>(
    initialState = Loading,
    presenter = SelectCoveragePresenter(
      params = params,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
      tierRepository = tierRepository,
    ),
  )

private class SelectCoveragePresenter(
  private val params: InsuranceCustomizationParameters,
  private val tierRepository: ChangeTierRepository,
  val getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
) : MoleculePresenter<SelectCoverageEvent, SelectCoverageState> {
  @Composable
  override fun MoleculePresenterScope<SelectCoverageEvent>.present(
    lastState: SelectCoverageState,
  ): SelectCoverageState {
    var chosenTier by remember { mutableStateOf(if (lastState is Success) lastState.uiState.chosenTier else null) }
    var chosenQuote by remember { mutableStateOf(if (lastState is Success) lastState.uiState.chosenQuote else null) }
    var chosenTierInDialog by remember {
      mutableStateOf(if (lastState is Success) lastState.uiState.chosenTier else null)
    }
    var chosenQuoteInDialog by remember {
      mutableStateOf(if (lastState is Success) lastState.uiState.chosenQuote else null)
    }
    var quoteToNavigateFurther by remember { mutableStateOf<TierDeductibleQuote?>(null) }
    var quotesToCompare by remember { mutableStateOf<List<TierDeductibleQuote>?>(null) }

    var currentPartialState by remember { mutableStateOf(mapLastStateToPartial(state = lastState)) }

    var currentContractLoadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        is ChangeDeductibleForChosenTier -> {
          chosenQuote = chosenQuoteInDialog
        }

        is ChangeTier -> {
          val state = currentPartialState
          if (state !is PartialUiState.Success) return@CollectEvents
          // set newly chosen tier
          chosenTier = chosenTierInDialog
          // try to pre-choose a quote with the same deductible and newly chosen coverage
          // if there is no such quote, the deductible will not be per-chosen
          val quoteWithNewTierOldDeductible = chosenTier?.let { t ->
            val previouslyChosenDeductible = chosenQuote?.deductible
            if (state.map[t]?.size == 1) {
              state.map[t]?.get(0)
            } else {
              state.map[t]?.firstOrNull { it.deductible == previouslyChosenDeductible }
            }
          }
          chosenQuote = quoteWithNewTierOldDeductible
          chosenQuoteInDialog = quoteWithNewTierOldDeductible
        }

        ClearNavigateFurtherStep -> {
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

        LaunchComparison -> {
          if (currentPartialState !is PartialUiState.Success) return@CollectEvents
          val notFiltered = (currentPartialState as PartialUiState.Success).map.values.flatten()
          logcat { "Mariia: notFiltered: ${notFiltered.map { it.tier.tierName }}" }
          val filtered = notFiltered.distinctBy { it.tier.tierName }
          logcat { "Mariia: filtered: ${filtered.map { it.tier.tierName }}" }
          quotesToCompare =
            filtered
        }

        is ChangeDeductibleInDialog -> {
          chosenQuoteInDialog = event.quote
        }

        is ChangeTierInDialog -> {
          chosenTierInDialog = event.tier
        }

        SetDeductibleToPreviouslyChosen -> {
          chosenQuoteInDialog = chosenQuote
        }
        SetTierToPreviouslyChosen -> {
          chosenTierInDialog = chosenTier
        }

        ClearNavigateToComparison -> {
          quotesToCompare = null
        }
      }
    }

    LaunchedEffect(currentContractLoadIteration) {
      if (lastState !is Success) {
        // todo: added this here, because if we go to comparison,
        //   it could take a long time, and we don't want to lose the member's tier choice if we exceed 15 sec
        getCurrentContractDataUseCase.invoke(params.insuranceId).fold(
          ifLeft = {
            currentPartialState = PartialUiState.Failure(GENERAL)
          },
          ifRight = { currentContractData ->
            val quotesResult: List<TierDeductibleQuote> = tierRepository.getQuotesById(params.quoteIds)
            if (quotesResult.isEmpty()) {
              currentPartialState = PartialUiState.Failure(QUOTES_ARE_EMPTY)
            } else {
              val current: TierDeductibleQuote? =
                if (params.currentTierName != null && params.currentTierLevel != null) {
                  TierDeductibleQuote(
                    id = CURRENT_ID,
                    deductible = currentContractData.deductible,
                    tier = Tier(
                      tierName = params.currentTierName, // todo: HERE edge case. If we have already changed
                      tierLevel = params.currentTierLevel,
                      tierDescription = currentContractData.productVariant.tierDescription,
                      tierDisplayName = currentContractData.productVariant.displayTierName,
                    ),
                    productVariant = currentContractData.productVariant,
                    displayItems = listOf(), // todo: here too!
                    premium = currentContractData.currentDisplayPremium,
                  )
                } else {
                  null
                }
              current?.let {
                tierRepository.addQuotesToDb(listOf(it))
              }
              logcat { "Mariia: current quote: $current" }
              val quotes = buildList {
                addAll(quotesResult)
                current?.let {
                  add(it)
                }
              }
              // pre-choosing current quote
              chosenTier = current?.tier
              chosenTierInDialog = current?.tier
              chosenQuote = current
              chosenQuoteInDialog = current
              currentPartialState = PartialUiState.Success(
                contractData = ContractData(
                  activeDisplayPremium = current?.premium.toString(),
                  contractGroup = current?.productVariant?.contractGroup ?: quotes[0].productVariant.contractGroup,
                  contractDisplayName = current?.productVariant?.displayName ?: quotes[0].productVariant.displayName,
                  contractDisplaySubtitle = currentContractData.currentExposureName,
                ),
                // setting current quote aside for comparison later
                currentActiveQuote = current,
                // adding current tierName and quote to the list, create map
                map = mapQuotesToTiersAndQuotes(quotes),
              )
            }
          },
        )
      }
    }

    val currentPartialStateValue = currentPartialState
    return when (currentPartialStateValue) {
      is PartialUiState.Failure -> Failure(currentPartialStateValue.reason)
      PartialUiState.Loading -> Loading
      is PartialUiState.Success -> {
        val chosenQuoteIndex =
          currentPartialStateValue.map[chosenTier]?.indexOf(chosenQuote).takeIf { it != -1 }
        val chosenTierIndex =
          currentPartialStateValue.map.keys.sortedBy { it.tierLevel }.indexOf(chosenTier)
            .takeIf { it != -1 }
        Success(
          map = currentPartialStateValue.map,
          currentActiveQuote = currentPartialStateValue.currentActiveQuote,
          uiState = SelectCoverageSuccessUiState(
            isCurrentChosen = chosenQuote == currentPartialStateValue.currentActiveQuote,
            chosenQuote = chosenQuote,
            chosenTier = chosenTier,
            tiers = buildListOfTiersAndPremiums(
              map = currentPartialStateValue.map,
              currentDeductible = chosenQuote?.deductible,
            ),
            quotesForChosenTier = currentPartialStateValue.map[chosenTier]!!,
            isTierChoiceEnabled = currentPartialStateValue.map.keys.size > 1,
            contractData = currentPartialStateValue.contractData,
            quoteToNavigateFurther = quoteToNavigateFurther,
            quotesToCompare = quotesToCompare,
            chosenInDialogQuote = chosenQuoteInDialog,
            chosenInDialogTier = chosenTierInDialog,
            chosenTierIndex = chosenTierIndex,
            chosenQuoteIndex = chosenQuoteIndex,
          ),
        )
      }
    }
  }
}

@Composable
private fun buildListOfTiersAndPremiums(
  map: SnapshotStateMap<Tier, List<TierDeductibleQuote>>,
  currentDeductible: Deductible?,
): List<Pair<Tier, UiMoney>> {
  return buildList {
    map.keys.forEach { tier ->
      // trying to show premium for same deductible in different tier-coverage,
      // but if this doesn't work, the lowest for this coverage
      val premium = map[tier]!!.firstOrNull {
        it.deductible == currentDeductible
      }?.premium
        ?: map[tier]!!.minBy { it.tier.tierLevel }.premium
      add(tier to premium)
    }
  }.sortedBy { pair ->
    pair.first.tierLevel
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
        it.deductible?.deductibleAmount?.amount ?: it.premium.amount
      }
    }
  val result = mutableStateMapOf(*grouped.toTypedArray())
  return result
}

internal sealed interface SelectCoverageEvent {
  data object SubmitChosenQuoteToContinue : SelectCoverageEvent

  data object ChangeDeductibleForChosenTier : SelectCoverageEvent

  data object SetTierToPreviouslyChosen : SelectCoverageEvent

  data object SetDeductibleToPreviouslyChosen : SelectCoverageEvent

  data object ChangeTier : SelectCoverageEvent

  data class ChangeDeductibleInDialog(val quote: TierDeductibleQuote) : SelectCoverageEvent

  data class ChangeTierInDialog(val tier: Tier) : SelectCoverageEvent

  data object LaunchComparison : SelectCoverageEvent

  data object ClearNavigateFurtherStep : SelectCoverageEvent

  data object ClearNavigateToComparison: SelectCoverageEvent

  data object Reload : SelectCoverageEvent
}

private fun mapLastStateToPartial(state: SelectCoverageState): PartialUiState {
  return when (state) {
    Loading -> PartialUiState.Loading
    is Failure -> PartialUiState.Failure(state.reason)
    is Success -> PartialUiState.Success(
      contractData = state.uiState.contractData,
      currentActiveQuote = state.currentActiveQuote,
      map = state.map,
    )
  }
}

private sealed interface PartialUiState {
  data object Loading : PartialUiState

  data class Failure(val reason: FailureReason) : PartialUiState

  data class Success(
    val contractData: ContractData,
    val currentActiveQuote: TierDeductibleQuote?,
    val map: SnapshotStateMap<Tier, List<TierDeductibleQuote>>,
  ) : PartialUiState
}

internal sealed interface SelectCoverageState {
  data object Loading : SelectCoverageState

  data class Success(
    val uiState: SelectCoverageSuccessUiState,
    val currentActiveQuote: TierDeductibleQuote?,
    val map: SnapshotStateMap<Tier, List<TierDeductibleQuote>>,
  ) : SelectCoverageState

  data class Failure(val reason: FailureReason) : SelectCoverageState
}

internal enum class FailureReason {
  GENERAL,
  QUOTES_ARE_EMPTY,
}

internal data class SelectCoverageSuccessUiState(
  val contractData: ContractData,
  val chosenTier: Tier?,
  val chosenTierIndex: Int?,
  val chosenQuote: TierDeductibleQuote?,
  val chosenQuoteIndex: Int?,
  val chosenInDialogTier: Tier?,
  val chosenInDialogQuote: TierDeductibleQuote?,
  val isCurrentChosen: Boolean,
  val isTierChoiceEnabled: Boolean,
  val quoteToNavigateFurther: TierDeductibleQuote? = null,
  val quotesToCompare: List<TierDeductibleQuote>? = null,
  val tiers: List<Pair<Tier, UiMoney>>, // sorted list of tiers with corresponding premiums (depending on selected deductible)
  val quotesForChosenTier: List<TierDeductibleQuote>,
)

internal data class ContractData(
  val contractGroup: ContractGroup,
  val contractDisplayName: String,
  val contractDisplaySubtitle: String,
  val activeDisplayPremium: String?,
)

private const val CURRENT_ID = "current"
