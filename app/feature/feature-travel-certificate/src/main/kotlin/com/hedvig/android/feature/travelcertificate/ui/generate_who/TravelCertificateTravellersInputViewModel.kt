package com.hedvig.android.feature.travelcertificate.ui.generate_who

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.ui.generate_when.TravelCertificatePrimaryInput
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class TravelCertificateTravellersInputViewModel(
  primaryInput: TravelCertificatePrimaryInput,
  createTravelCertificateUseCase: CreateTravelCertificateUseCase,
):  MoleculeViewModel<TravelCertificateTravellersInputEvent,TravelCertificateTravellersInputUiState>(
  initialState = TravelCertificateTravellersInputUiState.Loading,
  presenter = TravelCertificateTravellersInputPresenter(
    primaryInput,
    createTravelCertificateUseCase,
  )
)

internal class TravelCertificateTravellersInputPresenter(
  private val primaryInput: TravelCertificatePrimaryInput,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
) : MoleculePresenter<TravelCertificateTravellersInputEvent, TravelCertificateTravellersInputUiState> {
  @Composable
  override fun MoleculePresenterScope<TravelCertificateTravellersInputEvent>.present(lastState: TravelCertificateTravellersInputUiState): TravelCertificateTravellersInputUiState {
    TODO("Not yet implemented")
  }

}


internal sealed interface TravelCertificateTravellersInputUiState {

  data object Loading: TravelCertificateTravellersInputUiState
  //todo
}

internal sealed interface TravelCertificateTravellersInputEvent {
 //todo
}
