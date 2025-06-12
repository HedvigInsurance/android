package com.hedvig.android.feature.profile.certificates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.profile.data.CheckInsuranceEvidenceAvailabilityUseCase
import com.hedvig.android.feature.profile.data.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

internal class CertificatesViewModel(
  checkTravelCertificateDestinationAvailabilityUseCase: CheckTravelCertificateDestinationAvailabilityUseCase,
  checkInsuranceEvidenceAvailabilityUseCase: CheckInsuranceEvidenceAvailabilityUseCase,
) : MoleculeViewModel<CertificatesEvent, CertificatesState>(
    initialState = CertificatesState.Loading,
    presenter = CertificatesPresenter(
      checkTravelCertificateDestinationAvailabilityUseCase,
      checkInsuranceEvidenceAvailabilityUseCase,
    ),
  )

private class CertificatesPresenter(
  private val checkTravelCertificateDestinationAvailabilityUseCase:
    CheckTravelCertificateDestinationAvailabilityUseCase,
  private val checkInsuranceEvidenceAvailabilityUseCase: CheckInsuranceEvidenceAvailabilityUseCase,
) : MoleculePresenter<CertificatesEvent, CertificatesState> {
  @Composable
  override fun MoleculePresenterScope<CertificatesEvent>.present(lastState: CertificatesState): CertificatesState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        CertificatesEvent.Retry -> dataLoadIteration++
      }
    }

    LaunchedEffect(dataLoadIteration) {
      currentState = CertificatesState.Loading
      combine(
        flow { emit(checkInsuranceEvidenceAvailabilityUseCase.invoke()) },
        flow { emit(checkTravelCertificateDestinationAvailabilityUseCase.invoke()) },
      ) { evidence, travel ->
        (evidence to travel)
      }.collectLatest { (insuranceEvidenceAvailability, travelCertificateDestinationAvailability) ->
        if (insuranceEvidenceAvailability.isLeft() && travelCertificateDestinationAvailability.isLeft()) {
          logcat { "CertificatesPresenter: both InsuranceEvidence and TravelCertificateDestination not available" }
          currentState = CertificatesState.Failure
        } else {
          val evidenceAvailable = travelCertificateDestinationAvailability.getOrNull()
          val travelCertificateDestinationAvailable = travelCertificateDestinationAvailability.getOrNull()
          currentState = CertificatesState.Success(
            isTravelCertificateAvailable = travelCertificateDestinationAvailable != null,
            isInsuranceEvidenceAvailable = evidenceAvailable != null,
          )
        }
      }
    }
    return currentState
  }
}

internal sealed interface CertificatesState {
  data object Loading : CertificatesState

  data object Failure : CertificatesState

  data class Success(
    val isTravelCertificateAvailable: Boolean,
    val isInsuranceEvidenceAvailable: Boolean,
  ) : CertificatesState
}

internal sealed interface CertificatesEvent {
  data object Retry : CertificatesEvent
}
