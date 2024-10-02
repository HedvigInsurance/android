package com.hedvig.android.feature.change.tier.ui.comparison

import androidx.compose.runtime.Composable
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonEvent.ShowTab
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
    CollectEvents { event ->
      when (event) {
        is ShowTab -> TODO()
      }
    }
    // todo!!!!
    return Success
  }
}

internal sealed interface ComparisonState {
  data object Loading : ComparisonState

  data object Success : ComparisonState
  //   val quote: TierDeductibleQuote,
}

internal sealed interface ComparisonEvent {
  data class ShowTab(val index: Int) : ComparisonEvent
}
