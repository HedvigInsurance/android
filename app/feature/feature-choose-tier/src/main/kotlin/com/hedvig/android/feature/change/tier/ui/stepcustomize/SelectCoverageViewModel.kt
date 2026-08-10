package com.hedvig.android.feature.change.tier.ui.stepcustomize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.navigation.ComparisonKey
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.navigation.SummaryKey
import com.hedvig.android.feature.change.tier.navigation.SummaryParameters
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeDeductibleForChosenTier
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeDeductibleInDialog
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeTier
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.ChangeTierInDialog
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.LaunchComparison
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.Reload
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.SetDeductibleToPreviouslyChosen
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.SetTierToPreviouslyChosen
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageEvent.SubmitChosenQuoteToContinue
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Failure
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Loading
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageState.Success
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SelectCoverageViewModel(
  @Assisted params: InsuranceCustomizationParameters,
  tierRepository: ChangeTierRepository,
  getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
  backstack: Backstack,
) : MoleculeViewModel<SelectCoverageEvent, SelectCoverageState>(
    initialState = Loading,
    presenter = SelectCoveragePresenter(
      params = params,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
      tierRepository = tierRepository,
      backstack = backstack,
    ),
  )

internal class SelectCoveragePresenter(
  private val params: InsuranceCustomizationParameters,
  private val tierRepository: ChangeTierRepository,
  val getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
  private val backstack: Backstack,
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
          chosenTier = chosenTierInDialog
          val listOfQuotes = state.map[chosenTierInDialog]
          val quoteToChoose = if (listOfQuotes?.size == 1) listOfQuotes[0] else null
          chosenQuote = quoteToChoose
          chosenQuoteInDialog = quoteToChoose
        }

        SubmitChosenQuoteToContinue -> {
          val state = currentPartialState
          if (state !is PartialUiState.Success) return@CollectEvents
          val quoteToContinue = chosenQuote
          if (quoteToContinue != null && quoteToContinue != state.currentActiveQuote) {
            backstack.add(
              SummaryKey(
                SummaryParameters(
                  quoteIdToSubmit = quoteToContinue.id,
                  activationDate = params.activationDate,
                  insuranceId = params.insuranceId,
                ),
              ),
            )
          }
        }

        Reload -> {
          currentContractLoadIteration++
        }

        LaunchComparison -> {
          if (currentPartialState !is PartialUiState.Success) return@CollectEvents
          val notFiltered = (currentPartialState as PartialUiState.Success).map.values.flatten()
          val filtered = notFiltered.distinctBy { it.tier.tierName }
          backstack.add(
            ComparisonKey(
              ComparisonParameters(
                termsIds = filtered.map { it.productVariant.termsVersion },
                selectedTermsVersion = filtered.firstOrNull {
                  it.tier.tierName == chosenTier?.tierName
                }?.productVariant?.termsVersion,
              ),
            ),
          )
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
      }
    }

    LaunchedEffect(currentContractLoadIteration) {
      if (lastState !is Success) {
        getCurrentContractDataUseCase.invoke(params.insuranceId).fold(
          ifLeft = {
            currentPartialState = PartialUiState.Failure
          },
          ifRight = { currentContractData ->
            val quotesResult = tierRepository.getQuotesById(params.quoteIds)
            if (quotesResult.isEmpty()) {
              currentPartialState = PartialUiState.Failure
            } else {
              val currentFromDb = quotesResult.firstOrNull { it.id == tierRepository.getCurrentQuoteId() }
              if (currentFromDb == null) {
                logcat { "Select coveragePresenter: tried to get current quoteToChange from DB but found none" }
                currentPartialState = PartialUiState.Failure
              } else {
                val current: TierDeductibleQuote = currentFromDb
                // pre-choosing current quote
                chosenTier = current.tier
                chosenTierInDialog = current.tier
                chosenQuote = current
                chosenQuoteInDialog = current
                currentPartialState = PartialUiState.Success(
                  contractData = ContractData(
                    activeDisplayPremium = current.newTotalCost.monthlyNet,
                    contractGroup = current.productVariant.contractGroup,
                    contractDisplayName = current.productVariant.displayName,
                    contractDisplaySubtitle = currentContractData.currentExposureName,
                  ),
                  // setting current quote aside for comparison later
                  currentActiveQuote = current,
                  // adding current tierName and quote to the list, create map
                  map = mapQuotesToTiersAndQuotes(quotesResult),
                )
              }
            }
          },
        )
      }
    }

    return when (val currentPartialStateValue = currentPartialState) {
      is PartialUiState.Failure -> {
        Failure
      }

      PartialUiState.Loading -> {
        Loading
      }

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
            ),
            quotesForChosenTier = currentPartialStateValue.map[chosenTier]!!,
            isTierChoiceEnabled = currentPartialStateValue.map.keys.size > 1,
            isPaymentProtection =
              currentPartialStateValue.contractData.contractGroup == ContractGroup.PAYMENT_PROTECTION,
            contractData = currentPartialStateValue.contractData,
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
private fun buildListOfTiersAndPremiums(map: Map<Tier, List<TierDeductibleQuote>>): List<Pair<Tier, UiMoney>> {
  return buildList {
    map.keys.forEach { tier ->
      // show the lowest premium for this coverage (with From... added later)
      val premium = map[tier]!!.minBy { it.newTotalCost.monthlyNet.amount }.newTotalCost.monthlyNet
      add(tier to premium)
    }
  }.sortedBy { pair ->
    pair.first.tierLevel
  }
}

private fun mapQuotesToTiersAndQuotes(quotes: List<TierDeductibleQuote>): Map<Tier, List<TierDeductibleQuote>> {
  val grouped = quotes
    .groupBy {
      it.tier
    }
    .map { entry ->
      entry.key to entry.value.sortedBy {
        it.deductible?.deductibleAmount?.amount ?: it.newTotalCost.monthlyNet.amount
      }
    }
  val result = mapOf(*grouped.toTypedArray())
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

  data object Reload : SelectCoverageEvent
}

private fun mapLastStateToPartial(state: SelectCoverageState): PartialUiState {
  return when (state) {
    Loading -> PartialUiState.Loading

    is Failure -> PartialUiState.Failure

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
    val currentActiveQuote: TierDeductibleQuote?,
    val map: Map<Tier, List<TierDeductibleQuote>>,
  ) : PartialUiState
}

internal sealed interface SelectCoverageState {
  data object Loading : SelectCoverageState

  data class Success(
    val uiState: SelectCoverageSuccessUiState,
    val currentActiveQuote: TierDeductibleQuote?,
    val map: Map<Tier, List<TierDeductibleQuote>>,
  ) : SelectCoverageState

  data object Failure : SelectCoverageState
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
  // Payment protection has no real tiers nor comparable coverage: it reuses the tier flow only to pick an
  // insured amount. Used to hide the compare/show-coverage button and to swap the deductible-mentioning subtitle.
  val isPaymentProtection: Boolean,
  // sorted list of tiers with corresponding premiums (depending on selected deductible)
  val tiers: List<Pair<Tier, UiMoney>>,
  val quotesForChosenTier: List<TierDeductibleQuote>,
)

internal data class ContractData(
  val contractGroup: ContractGroup,
  val contractDisplayName: String,
  val contractDisplaySubtitle: String,
  val activeDisplayPremium: UiMoney?,
)
