package com.hedvig.android.feature.addon.purchase.ui.summary

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class AddonSummaryViewModel(summaryParameters: SummaryParameters) : MoleculeViewModel<AddonSummaryEvent, AddonSummaryState>(
  initialState = AddonSummaryState.Loading,
  presenter = AddonSummaryPresenter(summaryParameters),
)

internal class AddonSummaryPresenter(private val summaryParameters: SummaryParameters) :
  MoleculePresenter<AddonSummaryEvent, AddonSummaryState> {
  @Composable
  override fun MoleculePresenterScope<AddonSummaryEvent>.present(lastState: AddonSummaryState): AddonSummaryState {
    CollectEvents { event ->
      when (event) {
        AddonSummaryEvent.Reload -> TODO()
      }
    }

    TODO("Not yet implemented")
  }
}

internal sealed interface AddonSummaryState {
  data object Loading : AddonSummaryState

  data object Success : AddonSummaryState

  data object Failure : AddonSummaryState
}

internal sealed interface AddonSummaryEvent {
  data object Reload : AddonSummaryEvent
}
