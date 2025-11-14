package com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.some
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.movingflow.data.AddonId
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveMtaQuote
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.AlterAddon
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.ClearNavigateToComparison
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.LaunchComparison
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.NavigatedToSummary
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.SelectCoverage
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.SelectDeductible
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.SubmitSelectedHomeQuoteId
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.MutlipleOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.NoOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.OneOption
import com.hedvig.android.feature.movingflow.ui.summary.GetMoveIntentCostUseCase
import com.hedvig.android.feature.movingflow.ui.summary.MoveIntentCost
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.tiersandaddons.CostBreakdownEntry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class ChoseCoverageLevelAndDeductibleViewModel(
  intentId: String,
  movingFlowRepository: MovingFlowRepository,
  getMoveIntentCostUseCase: GetMoveIntentCostUseCase,
) : MoleculeViewModel<ChoseCoverageLevelAndDeductibleEvent, ChoseCoverageLevelAndDeductibleUiState>(
    ChoseCoverageLevelAndDeductibleUiState.Loading,
    ChoseCoverageLevelAndDeductiblePresenter(intentId, movingFlowRepository, getMoveIntentCostUseCase),
  )

private class ChoseCoverageLevelAndDeductiblePresenter(
  private val intentId: String,
  private val movingFlowRepository: MovingFlowRepository,
  private val getMoveIntentCostUseCase: GetMoveIntentCostUseCase,
) : MoleculePresenter<ChoseCoverageLevelAndDeductibleEvent, ChoseCoverageLevelAndDeductibleUiState> {
  @Composable
  override fun MoleculePresenterScope<ChoseCoverageLevelAndDeductibleEvent>.present(
    lastState: ChoseCoverageLevelAndDeductibleUiState,
  ): ChoseCoverageLevelAndDeductibleUiState {
    var tiersInfo: Option<TiersInfo?> by remember {
      mutableStateOf(
        (lastState as? ChoseCoverageLevelAndDeductibleUiState.Content)?.tiersInfo?.some() ?: None,
      )
    }
    var submittingSelectedHomeQuoteId: String? by remember { mutableStateOf(null) }
    var navigateToSummaryScreenWithHomeQuoteId: String? by remember { mutableStateOf(null) }
    var comparisonParameters: ComparisonParameters? by remember { mutableStateOf(null) }
    val moveIntentCost: MoveIntentCost? by produceState(null, tiersInfo.getOrNull()?.selectedCoverage) {
      val selectedCoverage = tiersInfo.getOrNull()?.selectedCoverage ?: return@produceState
      getMoveIntentCostUseCase.invoke(
        intentId,
        selectedCoverage.id,
        selectedCoverage.relatedAddonQuotes.filter { !it.isExcludedByUser }.map { it.addonId.id },
      ).collectLatest { result ->
        val moveIntentCost = result.getOrNull()
        value = moveIntentCost
      }
    }
    val costBreakdown: List<CostBreakdownEntry>? = tiersInfo.map { tiersInfo ->
      val selectedCoverage = tiersInfo?.selectedCoverage ?: return@map null
      buildList<CostBreakdownEntry> {
        add(
          CostBreakdownEntry(
            selectedCoverage.productVariant.displayName,
            selectedCoverage.premium,
          ),
        )
        addAll(
          selectedCoverage.includedRelatedAddonQuotes.map {
            CostBreakdownEntry(
              it.addonVariant.displayName,
              it.premium,
            )
          },
        )
        addAll(
          moveIntentCost?.quoteCosts?.firstOrNull { it.id == selectedCoverage.id }?.let {
            it.discounts.map {
              CostBreakdownEntry(
                it.displayName,
                it.displayValue,
              )
            }
          }.orEmpty(),
        )
      }
    }.getOrNull()

    CollectEvents { event ->
      when (event) {
        is SelectCoverage -> {
          val currentContent = tiersInfo.getOrNull() ?: return@CollectEvents
          val currentDeductible = currentContent.selectedDeductible?.deductible
          val newlySelectedQuote = currentContent.allOptions.firstOrNull { it.id == event.homeQuoteId }
          val newlySelectedTier = newlySelectedQuote?.tierName
          val quoteWithSameDeductibleAndNewTier = currentContent.allOptions.firstOrNull {
            it.tierName == newlySelectedTier && it.deductible == currentDeductible
          }
          val newSelectedCoverage = quoteWithSameDeductibleAndNewTier ?: newlySelectedQuote ?: return@CollectEvents
          if (newSelectedCoverage == currentContent.selectedCoverage) return@CollectEvents
          launch {
            movingFlowRepository.updatePreselectedHomeQuoteId(newSelectedCoverage.id)
          }
        }

        is SelectDeductible -> {
          val currentContent = tiersInfo.getOrNull() ?: return@CollectEvents
          val newSelectedDeductible =
            currentContent.allOptions.firstOrNull { it.id == event.homeQuoteId } ?: return@CollectEvents
          launch {
            movingFlowRepository.updatePreselectedHomeQuoteId(newSelectedDeductible.id)
          }
        }

        is AlterAddon -> {
          launch {
            movingFlowRepository.changeHomeAddonExclusion(event.addonId, event.exclude)
          }
        }

        is SubmitSelectedHomeQuoteId -> {
          submittingSelectedHomeQuoteId = event.homeQuoteId
        }

        NavigatedToSummary -> navigateToSummaryScreenWithHomeQuoteId = null

        ClearNavigateToComparison -> comparisonParameters = null

        LaunchComparison -> {
          val currentContent = tiersInfo.getOrNull() ?: return@CollectEvents
          val filtered = currentContent.allOptions.distinctBy { it.tierName }
          val selected = filtered.firstOrNull { it.tierName == currentContent.selectedCoverage.tierName }
          comparisonParameters = ComparisonParameters(
            termsIds = filtered.map { it.productVariant.termsVersion },
            selectedTermsVersion = selected?.productVariant?.termsVersion,
          )
        }
      }
    }

    LaunchedEffect(Unit) {
      movingFlowRepository.movingFlowState().collectLatest { movingFlowState ->
        if (movingFlowState?.movingFlowQuotes == null) {
          tiersInfo = null.some()
          return@collectLatest
        }
        val homeQuotes = movingFlowState.movingFlowQuotes.homeQuotes
        if (homeQuotes.isEmpty()) {
          tiersInfo = null.some()
          return@collectLatest
        }
        val initiallySelectedHomeQuote = run {
          val previouslySelected = homeQuotes.firstNotNullOfOrNull { moveHomeQuote ->
            val wasPreviouslySelectedInTheFlow = moveHomeQuote.id == movingFlowState.lastSelectedHomeQuoteId
            if (wasPreviouslySelectedInTheFlow) moveHomeQuote else null
          }
          if (previouslySelected != null) return@run previouslySelected
          homeQuotes.firstNotNullOfOrNull { moveHomeQuote ->
            if (moveHomeQuote.defaultChoice) moveHomeQuote else null
          }
        }
        val uniqueCoverageOptions = homeQuotes
          .groupBy { it.tierName }
          .mapNotNull { (_, moveHomeQuotes) ->
            val alreadySelectedCoverage = moveHomeQuotes.firstOrNull { it.id == initiallySelectedHomeQuote?.id }
            val minPriceMoveQuote = moveHomeQuotes.minByOrNull { it.premium.amount }
            val moveHomeQuote = alreadySelectedCoverage ?: minPriceMoveQuote
            if (moveHomeQuote == null) return@mapNotNull null
            CoverageInfo(
              moveHomeQuoteId = moveHomeQuote.id,
              tierName = moveHomeQuote.tierDisplayName,
              tierDescription = moveHomeQuote.tierDescription,
              minimumPremiumForCoverage = minPriceMoveQuote?.premium
                ?: moveHomeQuote.premium,
            )
          }
          .toList()
        val selectedCoverage = initiallySelectedHomeQuote ?: homeQuotes.first()
        tiersInfo = TiersInfo(
          allOptions = homeQuotes,
          mtaQuotes = movingFlowState.movingFlowQuotes.mtaQuotes,
          coverageOptions = uniqueCoverageOptions,
          selectedCoverage = selectedCoverage,
          selectedDeductible = selectedCoverage,
        ).some()
      }
    }

    LaunchedEffect(submittingSelectedHomeQuoteId) {
      val submittingSelectedHomeQuoteIdValue = submittingSelectedHomeQuoteId ?: return@LaunchedEffect
      movingFlowRepository.updatePreselectedHomeQuoteId(submittingSelectedHomeQuoteIdValue)
      Snapshot.withMutableSnapshot {
        submittingSelectedHomeQuoteId = null
        navigateToSummaryScreenWithHomeQuoteId = submittingSelectedHomeQuoteIdValue
      }
    }

    return when (val tiersInfoValue = tiersInfo) {
      None -> ChoseCoverageLevelAndDeductibleUiState.Loading
      is Some -> {
        when (val info = tiersInfoValue.value) {
          null -> ChoseCoverageLevelAndDeductibleUiState.MissingOngoingMovingFlow
          else -> {
            val selectedQuoteCost = moveIntentCost?.quoteCosts?.firstOrNull {
              it.id == info.selectedCoverage.id
            }
            ChoseCoverageLevelAndDeductibleUiState.Content(
              tiersInfo = info,
              costBreakdown = costBreakdown,
              navigateToSummaryScreenWithHomeQuoteId = navigateToSummaryScreenWithHomeQuoteId,
              premium = selectedQuoteCost?.monthlyNet,
              grossPremium = selectedQuoteCost?.monthlyGross,
              isSubmitting = submittingSelectedHomeQuoteId != null,
              comparisonParameters = comparisonParameters,
            )
          }
        }
      }
    }
  }
}

internal sealed interface ChoseCoverageLevelAndDeductibleEvent {
  data class SelectCoverage(val homeQuoteId: String) : ChoseCoverageLevelAndDeductibleEvent

  data class SelectDeductible(val homeQuoteId: String) : ChoseCoverageLevelAndDeductibleEvent

  data class AlterAddon(val addonId: AddonId, val exclude: Boolean) : ChoseCoverageLevelAndDeductibleEvent

  data class SubmitSelectedHomeQuoteId(val homeQuoteId: String) : ChoseCoverageLevelAndDeductibleEvent

  data object NavigatedToSummary : ChoseCoverageLevelAndDeductibleEvent

  data object LaunchComparison : ChoseCoverageLevelAndDeductibleEvent

  data object ClearNavigateToComparison : ChoseCoverageLevelAndDeductibleEvent
}

internal sealed interface ChoseCoverageLevelAndDeductibleUiState {
  data object MissingOngoingMovingFlow : ChoseCoverageLevelAndDeductibleUiState

  data object Loading : ChoseCoverageLevelAndDeductibleUiState

  data class Content(
    val tiersInfo: TiersInfo,
    val costBreakdown: List<CostBreakdownEntry>?,
    val comparisonParameters: ComparisonParameters?,
    val premium: UiMoney?,
    val grossPremium: UiMoney?,
    val navigateToSummaryScreenWithHomeQuoteId: String?,
    val isSubmitting: Boolean,
  ) : ChoseCoverageLevelAndDeductibleUiState {
    val canSubmit = tiersInfo.selectedHomeQuoteId != null && !isSubmitting
  }
}

data class CoverageInfo(
  val moveHomeQuoteId: String,
  val tierName: String,
  val tierDescription: String?,
  val minimumPremiumForCoverage: UiMoney,
) {
  val id = "$moveHomeQuoteId$tierName$tierDescription$minimumPremiumForCoverage"
}

internal data class TiersInfo(
  val allOptions: List<MoveHomeQuote>,
  val mtaQuotes: List<MoveMtaQuote>,
  val coverageOptions: List<CoverageInfo>,
  val selectedCoverage: MoveHomeQuote,
  val selectedDeductible: MoveHomeQuote?,
) {
  val deductibleOptions: DeductibleOptions = allOptions.filter { homeQuote ->
    homeQuote.tierLevel == selectedCoverage.tierLevel
  }.let { moveHomeQuotes ->
    if (moveHomeQuotes.size <= 1) {
      val deductible = selectedCoverage.deductible
      if (deductible == null) {
        NoOptions
      } else {
        OneOption(selectedCoverage.toDeductibleOption(deductible))
      }
    } else {
      val allOptionsWithDeductible = moveHomeQuotes.filter { it.deductible != null }
      when (allOptionsWithDeductible.size) {
        0 -> NoOptions
        1 -> {
          val onlyOption = allOptionsWithDeductible.first()
          OneOption(onlyOption.toDeductibleOption(onlyOption.deductible!!))
        }

        else -> MutlipleOptions(
          allOptionsWithDeductible.map { moveHomeQuote ->
            moveHomeQuote.toDeductibleOption(moveHomeQuote.deductible!!)
          },
        )
      }
    }
  }

  val selectedHomeQuoteId: String? = when (deductibleOptions) {
    NoOptions -> selectedCoverage.id
    is OneOption -> selectedDeductible?.id
    is MutlipleOptions -> selectedDeductible?.id
  }
}

private fun MoveHomeQuote.toDeductibleOption(deductible: Deductible): DeductibleOption {
  return DeductibleOption(id, premium, deductible)
}

internal sealed interface DeductibleOptions {
  data object NoOptions : DeductibleOptions

  data class OneOption(val deductibleOption: DeductibleOption) : DeductibleOptions

  data class MutlipleOptions(
    val deductibleOptions: List<DeductibleOption>,
  ) : DeductibleOptions
}

internal data class DeductibleOption(
  val homeQuoteId: String,
  val homeQuotePremium: UiMoney,
  val deductible: Deductible,
) {
  val id = "$homeQuoteId$homeQuotePremium$deductible"
}
