package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.runtime.Composable
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.ExpandCard
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.Reload
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.ScrollToDetails
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.SubmitQuote
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Loading
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class SummaryViewModel(
  quoteId: String,
  tierRepository: ChangeTierRepository,
) : MoleculeViewModel<SummaryEvent, SummaryState>(
    initialState = Loading,
    presenter = SummaryPresenter(
      quoteId = quoteId,
      tierRepository = tierRepository,
    ),
  )

private class SummaryPresenter(
  private val quoteId: String,
  private val tierRepository: ChangeTierRepository,
) : MoleculePresenter<SummaryEvent, SummaryState> {
  @Composable
  override fun MoleculePresenterScope<SummaryEvent>.present(lastState: SummaryState): SummaryState {
    CollectEvents { event ->
      when (event) {
        ExpandCard -> TODO()
        Reload -> TODO()
        ScrollToDetails -> TODO()
        SubmitQuote -> TODO()
      }
    }
    TODO("Not yet implemented")
  }
}

internal sealed interface SummaryState {
  data object Loading : SummaryState

  data class Success(
    val quote: TierDeductibleQuote,
  ) : SummaryState

  data object Failure : SummaryState
}

internal sealed interface SummaryEvent {
  data object SubmitQuote : SummaryEvent

  data object ScrollToDetails : SummaryEvent

  data object ExpandCard : SummaryEvent

  data object Reload : SummaryEvent
}
