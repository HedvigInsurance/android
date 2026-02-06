package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class RemoveAddonSummaryViewModel() : MoleculeViewModel<
  RemoveAddonSummaryEvent, RemoveAddonSummaryState,
  >(
  initialState = RemoveAddonSummaryState.Loading,
  presenter = RemoveAddonSummaryPresenter(),
)

private class RemoveAddonSummaryPresenter : MoleculePresenter<
  RemoveAddonSummaryEvent, RemoveAddonSummaryState,
  > {
  @Composable
  override fun MoleculePresenterScope<RemoveAddonSummaryEvent>.present(
    lastState: RemoveAddonSummaryState,
  ): RemoveAddonSummaryState {
    TODO("Not yet implemented")
  }
}

internal sealed interface RemoveAddonSummaryState {
  data object Success : RemoveAddonSummaryState

  data object Error : RemoveAddonSummaryState

  data object Loading : RemoveAddonSummaryState
}

internal interface RemoveAddonSummaryEvent {
  data object Retry : RemoveAddonSummaryEvent
}
