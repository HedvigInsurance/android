package com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.some
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.ClearNavigateToComparison
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.LaunchComparison
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.NavigatedToSummary
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.SelectCoverage
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.SelectDeductible
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.SubmitSelectedHomeQuoteId
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.MutlipleOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.NoOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.OneOption
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

internal class ChoseCoverageLevelAndDeductibleViewModel(
  movingFlowRepository: MovingFlowRepository,
) : MoleculeViewModel<ChoseCoverageLevelAndDeductibleEvent, ChoseCoverageLevelAndDeductibleUiState>(
  ChoseCoverageLevelAndDeductibleUiState.Loading,
  ChoseCoverageLevelAndDeductiblePresenter(movingFlowRepository),
)

private class ChoseCoverageLevelAndDeductiblePresenter(
  private val movingFlowRepository: MovingFlowRepository,
) : MoleculePresenter<ChoseCoverageLevelAndDeductibleEvent, ChoseCoverageLevelAndDeductibleUiState> {
  @Composable
  override fun MoleculePresenterScope<ChoseCoverageLevelAndDeductibleEvent>.present(
    lastState: ChoseCoverageLevelAndDeductibleUiState,
  ): ChoseCoverageLevelAndDeductibleUiState {
    logcat { "Stelios: lastState:$lastState" }
    var tiersInfo: Option<TiersInfo?> by remember {
      mutableStateOf(
        (lastState as? ChoseCoverageLevelAndDeductibleUiState.Content)?.tiersInfo?.some() ?: None,
      )
    }
    var submittingSelectedHomeQuoteId: String? by remember { mutableStateOf(null) }
    var navigateToSummaryScreenWithHomeQuoteId: String? by remember { mutableStateOf(null) }
    var comparisonParameters: ComparisonParameters? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        is SelectCoverage -> {
          val currentContent = tiersInfo.getOrNull() ?: return@CollectEvents
          val newSelectedCoverage =
            currentContent.allOptions.firstOrNull { it.id == event.homeQuoteId } ?: return@CollectEvents
          if (newSelectedCoverage == currentContent.selectedCoverage) return@CollectEvents
          tiersInfo = currentContent.copy(
            selectedCoverage = newSelectedCoverage,
            selectedDeductible = null,
          ).some()
        }

        is SelectDeductible -> {
          val currentContent = tiersInfo.getOrNull() ?: return@CollectEvents
          val newSelectedDeductible =
            currentContent.allOptions.firstOrNull { it.id == event.homeQuoteId } ?: return@CollectEvents
          tiersInfo = currentContent.copy(selectedDeductible = newSelectedDeductible).some()
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
      movingFlowRepository
        .movingFlowState()
        .run {
          if (lastState is ChoseCoverageLevelAndDeductibleUiState.Content) {
            // if we had some temporary input not persisted to disk when we last left this screen, we want that to still
            //  be presented. Otherwise the disk value, which is the initial state will override any uncomitted input
            this.drop(1)
          } else {
            this
          }
        }
        .collectLatest { movingFlowState ->
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
                moveHomeQuote.id,
                moveHomeQuote.tierDisplayName,
                moveHomeQuote.tierDescription,
                minPriceMoveQuote?.premium ?: moveHomeQuote.premium,
              )
            }
            .toList()
          val selectedCoverage = initiallySelectedHomeQuote ?: homeQuotes.first()
          tiersInfo = TiersInfo(
            allOptions = homeQuotes,
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
        when (val state = tiersInfoValue.value) {
          null -> ChoseCoverageLevelAndDeductibleUiState.MissingOngoingMovingFlow
          else -> ChoseCoverageLevelAndDeductibleUiState.Content(
            tiersInfo = state,
            navigateToSummaryScreenWithHomeQuoteId = navigateToSummaryScreenWithHomeQuoteId,
            isSubmitting = submittingSelectedHomeQuoteId != null,
            comparisonParameters = comparisonParameters,
          )
        }
      }
    }
  }
}

sealed interface ChoseCoverageLevelAndDeductibleEvent {
  data class SelectCoverage(val homeQuoteId: String) : ChoseCoverageLevelAndDeductibleEvent

  data class SelectDeductible(val homeQuoteId: String) : ChoseCoverageLevelAndDeductibleEvent

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
    val comparisonParameters: ComparisonParameters?,
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
)

internal data class TiersInfo(
  val allOptions: List<MoveHomeQuote>,
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
        DeductibleOptions.NoOptions
      } else {
        DeductibleOptions.OneOption(DeductibleOption(selectedCoverage.id, selectedCoverage.premium, deductible))
      }
    } else {
      val allOptionsWithDeductible = moveHomeQuotes.filter { it.deductible != null }
      when (allOptionsWithDeductible.size) {
        0 -> DeductibleOptions.NoOptions
        1 -> {
          val onlyOption = allOptionsWithDeductible.first()
          DeductibleOptions.OneOption(DeductibleOption(onlyOption.id, onlyOption.premium, onlyOption.deductible!!))
        }

        else -> DeductibleOptions.MutlipleOptions(
          allOptionsWithDeductible.map { moveHomeQuote ->
            DeductibleOption(moveHomeQuote.id, moveHomeQuote.premium, moveHomeQuote.deductible!!)
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
)
