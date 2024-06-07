package com.hedvig.android.feature.changeaddress.destination.offer

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.data.SuccessfulMove
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class ChangeAddressOfferViewModel() : MoleculeViewModel<ChangeAddressOfferEvent, ChangeAddressOfferUiState>(
  initialState = ChangeAddressOfferUiState(), // todo: parameters?
  presenter = ChangeAddressOfferPresenter(),
)

internal class ChangeAddressOfferPresenter : MoleculePresenter<ChangeAddressOfferEvent, ChangeAddressOfferUiState> {
  @Composable
  override fun MoleculePresenterScope<ChangeAddressOfferEvent>.present(
    lastState: ChangeAddressOfferUiState,
  ): ChangeAddressOfferUiState {
    TODO("Not yet implemented")
  }
}

internal data class ChangeAddressOfferUiState(
  val quotes: List<MoveQuote> = emptyList(),
  val successfulMoveResult: SuccessfulMove? = null,
  val errorMessage: String? = null,
  val movingDate: LocalDate? = null, // todo: validated input??
  val moveIntentId: MoveIntentId? = null,
  val isLoading: Boolean = false, // todo: button here
)

internal sealed interface ChangeAddressOfferEvent {
  data class ConfirmMove(val id: MoveIntentId) : ChangeAddressOfferEvent

  data class ExpandQuote(val moveQuote: MoveQuote) : ChangeAddressOfferEvent

  data object DismissErrorDialog : ChangeAddressOfferEvent
}
