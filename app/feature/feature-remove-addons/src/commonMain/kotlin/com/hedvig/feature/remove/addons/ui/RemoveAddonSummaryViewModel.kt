package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.datetime.LocalDate

internal class RemoveAddonSummaryViewModel(
  params: CommonSummaryParameters
) : MoleculeViewModel<
  RemoveAddonSummaryEvent, RemoveAddonSummaryState,
  >(
  initialState = RemoveAddonSummaryState.Loading,
  presenter = RemoveAddonSummaryPresenter(params),
)

private class RemoveAddonSummaryPresenter(
  private val params: CommonSummaryParameters
) : MoleculePresenter<
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
  data class Content(
    val activationDate: LocalDate,
    val navigateToFailure: Unit? = null
  ) : RemoveAddonSummaryState

  data class Loading(
    val activationDateToNavigateToSuccess: LocalDate? = null
  ) : RemoveAddonSummaryState
}

internal interface RemoveAddonSummaryEvent {
  data object Retry : RemoveAddonSummaryEvent
  data object ReturnToInitialState : RemoveAddonSummaryEvent
  data object Submit : RemoveAddonSummaryEvent
}
