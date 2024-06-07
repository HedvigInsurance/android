package com.hedvig.android.feature.changeaddress.destination.enternewaddress

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.changeaddress.DatePickerUiState
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class EnterNewAddressViewModel(initialState: EnterNewAddressUiState) : MoleculeViewModel<EnterNewAddressEvent, EnterNewAddressUiState>(
  initialState = EnterNewAddressUiState(), // todo: parse parameters here! if we need any
  presenter = EnterNewAddressPresenter(),
)

internal class EnterNewAddressPresenter() : MoleculePresenter<EnterNewAddressEvent, EnterNewAddressUiState> {
  @Composable
  override fun MoleculePresenterScope<EnterNewAddressEvent>.present(
    lastState: EnterNewAddressUiState,
  ): EnterNewAddressUiState {
    TODO("Not yet implemented")
  }
}

internal data class EnterNewAddressUiState(
  val isLoading: Boolean = false, // todo: is it button or whole screen?
  val errorMessage: String? = null,
  val street: String? = null,
  val postalCode: String? = null,
  val squareMeters: String? = null,
  val numberInsured: String? = null,
  val datePickerUiState: DatePickerUiState? = null,
  val isEligibleForStudent: Boolean = false,
  val isStudent: Boolean = false,
)

internal sealed interface EnterNewAddressEvent {
  data object DismissErrorDialog : EnterNewAddressEvent

  data class ChangeCoInsured(val coInsured: String) : EnterNewAddressEvent

  data class ChangeSquareMeters(val squareMeters: String) : EnterNewAddressEvent

  data class ChangePostalCode(val postalCode: String) : EnterNewAddressEvent

  data class ChangeStreet(val street: String) : EnterNewAddressEvent

  data class ChangeIsStudent(val isStudent: Boolean) : EnterNewAddressEvent

  data class ChangeMoveDate(val movingDate: LocalDate) : EnterNewAddressEvent

  data object ValidateAddressInput : EnterNewAddressEvent

  data object SubmitNewAddress : EnterNewAddressEvent
}
