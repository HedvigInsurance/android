package com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.some
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.SelectCoverage
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent.SelectDeductible
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.MutlipleOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.NoOptions
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.DeductibleOptions.OneOption
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest

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
    var tiersInfo: Option<TiersInfo?> by remember {
      mutableStateOf(
        (lastState as? ChoseCoverageLevelAndDeductibleUiState.Content)?.tiersInfo?.some() ?: None,
      )
    }

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
        val initiallySelectedHomeQuote = homeQuotes.firstNotNullOfOrNull { moveHomeQuote ->
          if (moveHomeQuote.defaultChoice) moveHomeQuote else null
        }
        val uniqueCoverageOptions = homeQuotes
          .groupBy { it.tierLevel }
          .map { (_, moveHomeQuotes) ->
            moveHomeQuotes.firstOrNull()
          }
          .filterNotNull()
          .map { moveHomeQuote ->
            CoverageInfo(moveHomeQuote.id, moveHomeQuote.tierName, moveHomeQuote.exposureName, moveHomeQuote.premium)
          }
        val selectedCoverage = initiallySelectedHomeQuote ?: homeQuotes.first()
        tiersInfo = TiersInfo(
          allOptions = homeQuotes,
          coverageOptions = uniqueCoverageOptions,
          selectedCoverage = selectedCoverage,
          selectedDeductible = null,
        ).some()
      }
    }

    return when (val tiersInfoValue = tiersInfo) {
      None -> ChoseCoverageLevelAndDeductibleUiState.Loading
      is Some -> {
        when (val state = tiersInfoValue.value) {
          null -> ChoseCoverageLevelAndDeductibleUiState.MissingOngoingMovingFlow
          else -> ChoseCoverageLevelAndDeductibleUiState.Content(state)
        }
      }
    }
  }
}

sealed interface ChoseCoverageLevelAndDeductibleEvent {
  data class SelectCoverage(val homeQuoteId: String) : ChoseCoverageLevelAndDeductibleEvent

  data class SelectDeductible(val homeQuoteId: String) : ChoseCoverageLevelAndDeductibleEvent
}

internal sealed interface ChoseCoverageLevelAndDeductibleUiState {
  data object MissingOngoingMovingFlow : ChoseCoverageLevelAndDeductibleUiState

  data object Loading : ChoseCoverageLevelAndDeductibleUiState

  data class Content(
    val tiersInfo: TiersInfo,
  ) : ChoseCoverageLevelAndDeductibleUiState {
    val canSubmit = tiersInfo.selectedHomeQuoteId != null
  }
}

data class CoverageInfo(
  val moveHomeQuoteId: String,
  val tierName: String,
  val tierDescription: String,
  val premium: UiMoney,
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
        DeductibleOptions.OneOption(DeductibleOption(selectedCoverage.id, deductible))
      }
    } else {
      val allOptionsWithDeductible = moveHomeQuotes.filter { it.deductible != null }
      when (allOptionsWithDeductible.size) {
        0 -> DeductibleOptions.NoOptions
        1 -> {
          val onlyOption = allOptionsWithDeductible.first()
          DeductibleOptions.OneOption(DeductibleOption(onlyOption.id, onlyOption.deductible!!))
        }

        else -> DeductibleOptions.MutlipleOptions(
          allOptionsWithDeductible.map { moveHomeQuote ->
            DeductibleOption(moveHomeQuote.id, moveHomeQuote.deductible!!)
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
  val deductible: Deductible,
)
