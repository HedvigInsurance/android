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
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ClearNavigationStep
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.LaunchComparison
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.Reload
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
          val locallyChosen = chosenTierInDialog
          locallyChosen?.let { local ->
            chosenTier = local
            // try to pre-choose a quote with the same deductible and newly chosen coverage
            // if there is no such quote, the deductible will not be per-chosen
            val previouslyChosenDeductible = chosenQuote?.deductible
            val quoteWithNewTierOldDeductible =
              state.map[local]!!.firstOrNull { it.deductible == previouslyChosenDeductible }
            chosenQuote = quoteWithNewTierOldDeductible
          }
        }

        ClearNavigationStep -> {
          quoteToNavigateFurther = null
          quotesToCompare = null
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
          val flattened = (currentPartialState as PartialUiState.Success).map.values.flatten().distinctBy {
            it.tier
          }
          quotesToCompare =
            flattened // we don't show deductible in the comparison table, so we send only one quote of each Tier for comparison
        }

        is ChangeDeductibleInDialog -> {
          chosenQuoteInDialog = event.quote
        }
        is ChangeTierInDialog -> {
          chosenTierInDialog = event.tier
        }
      }
    }

    LaunchedEffect(currentContractLoadIteration) {
      getCurrentContractDataUseCase.invoke(params.insuranceId).fold(
        ifLeft = {
          currentPartialState = PartialUiState.Failure(GENERAL)
        },
        ifRight = { currentContractData ->
          val quotesResult: List<TierDeductibleQuote> = tierRepository.getQuotesById(params.quoteIds)
          logcat { "Mariia: got this quotes from repo: $quotesResult" }
          if (quotesResult.isEmpty()) {
            currentPartialState = PartialUiState.Failure(QUOTES_ARE_EMPTY)
          } else {
            logcat { "Mariia: got this quotes: $quotesResult" }
            val current: TierDeductibleQuote? =
              if (params.currentTierName != null && params.currentTierLevel != null) {
                val info = quotesResult.firstOrNull { it.tier.tierLevel == 1 }
                logcat {
                  "Mariia: got this tierLevel for current: ${params.currentTierLevel} and corresponding quote: $info"
                }
                TierDeductibleQuote(
                  id = CURRENT_ID,
                  deductible = currentContractData.deductible,
                  tier = Tier(
                    tierName = params.currentTierName,
                    tierLevel = params.currentTierLevel,
                    info = currentContractData.productVariant.tierNameLong,
                  ),
                  productVariant = currentContractData.productVariant,
                  displayItems = listOf(),
                  premium = currentContractData.currentDisplayPremium,
                )
              } else {
                null
              }
            logcat { "Mariia: got this current: $current" }
            val quotes = buildList {
              addAll(quotesResult)
              current?.let {
                add(it)
              }
            }
            // pre-choosing current quote
            chosenTier = current?.tier
            chosenQuote = current
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
    return when (currentPartialState) {
      is PartialUiState.Failure -> Failure((currentPartialState as PartialUiState.Failure).reason)
      PartialUiState.Loading -> Loading
      is PartialUiState.Success -> {
        val currentlyChosenQuote = chosenQuote
        logcat { "mariia: currentlyChosenQuote is $currentlyChosenQuote" }
        Success(
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
            quoteToNavigateFurther = quoteToNavigateFurther,
            quotesToCompare = quotesToCompare,
            chosenInDialogQuote = chosenQuoteInDialog,
            chosenInDialogTier = chosenTierInDialog,
          ),
        )
      }
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

  data object ChangeDeductibleForChosenTier : SelectCoverageEvent

  data object ChangeTier : SelectCoverageEvent

  data class ChangeDeductibleInDialog(val quote: TierDeductibleQuote) : SelectCoverageEvent

  data class ChangeTierInDialog(val tier: Tier) : SelectCoverageEvent

  data object LaunchComparison : SelectCoverageEvent

  data object ClearNavigationStep : SelectCoverageEvent

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
  val chosenQuote: TierDeductibleQuote?,
  val chosenInDialogTier: Tier?,
  val chosenInDialogQuote: TierDeductibleQuote?,
  val isCurrentChosen: Boolean,
  val isTierChoiceEnabled: Boolean,
  val quoteToNavigateFurther: TierDeductibleQuote? = null,
  val quotesToCompare: List<TierDeductibleQuote>? = null,
  val tiers: List<Pair<Tier, String>>, // sorted list of tiers with corresponding premiums (depending on selected deductible)
  val quotesForChosenTier: List<TierDeductibleQuote>,
)

internal data class ContractData(
  val contractGroup: ContractGroup,
  val contractDisplayName: String,
  val contractDisplaySubtitle: String,
  val activeDisplayPremium: String?,
)

private const val CURRENT_ID = "current"
