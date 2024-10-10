package com.hedvig.android.feature.change.tier.ui.comparison

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Loading
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Success
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class ComparisonViewModel(
  quoteIds: List<String>,
  tierRepository: ChangeTierRepository,
) : MoleculeViewModel<ComparisonEvent, ComparisonState>(
    initialState = Loading,
    presenter = ComparisonPresenter(
      quoteIds = quoteIds,
      tierRepository = tierRepository,
    ),
  )

private class ComparisonPresenter(
  private val quoteIds: List<String>,
  private val tierRepository: ChangeTierRepository,
) : MoleculePresenter<ComparisonEvent, ComparisonState> {
  @Composable
  override fun MoleculePresenterScope<ComparisonEvent>.present(lastState: ComparisonState): ComparisonState {
    var currentState by remember { mutableStateOf(lastState) }

    LaunchedEffect(Unit) {
      // TODO: add error state!!! and either!
      val result = tierRepository.getQuotesById(quoteIds).sortedBy { it.tier.tierLevel }
      currentState = Success(result)
    }

    return currentState
  }
}

internal sealed interface ComparisonState {
  data object Loading : ComparisonState

  data class Success(val quotes: List<TierDeductibleQuote>) : ComparisonState
}

internal sealed interface ComparisonEvent
