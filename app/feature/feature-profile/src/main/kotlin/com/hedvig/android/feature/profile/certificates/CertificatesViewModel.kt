package com.hedvig.android.feature.profile.certificates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.profile.data.CheckGreenCardDestinationAvailabilityUseCase
import com.hedvig.android.feature.profile.data.CheckInsuranceEvidenceAvailabilityUseCase
import com.hedvig.android.feature.profile.data.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class CertificatesViewModel(
  checkTravelCertificateDestinationAvailabilityUseCase: CheckTravelCertificateDestinationAvailabilityUseCase,
  checkInsuranceEvidenceAvailabilityUseCase: CheckInsuranceEvidenceAvailabilityUseCase,
  checkGreenCardDestinationAvailabilityUseCase: CheckGreenCardDestinationAvailabilityUseCase,
) : MoleculeViewModel<CertificatesEvent, CertificatesState>(
    initialState = CertificatesState.Loading,
    presenter = CertificatesPresenter(
      checkTravelCertificateDestinationAvailabilityUseCase,
      checkInsuranceEvidenceAvailabilityUseCase,
      checkGreenCardDestinationAvailabilityUseCase,
    ),
  )

private class CertificatesPresenter(
  private val checkTravelCertificateDestinationAvailabilityUseCase:
    CheckTravelCertificateDestinationAvailabilityUseCase,
  private val checkInsuranceEvidenceAvailabilityUseCase: CheckInsuranceEvidenceAvailabilityUseCase,
  private val checkGreenCardDestinationAvailabilityUseCase: CheckGreenCardDestinationAvailabilityUseCase,
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
        flow { emit(checkGreenCardDestinationAvailabilityUseCase.invoke()) },
      ) { evidence, travel, greenCard ->
        Triple(evidence, travel, greenCard)
      }.collectLatest { (evidence, travel, greenCard) ->
        if (evidence.isLeft() && travel.isLeft() && greenCard.isLeft()) {
          logcat { "CertificatesPresenter: no certificates available" }
          currentState = CertificatesState.Failure
        } else {
          currentState = CertificatesState.Success(
            isTravelCertificateAvailable = travel.isRight(),
            isInsuranceEvidenceAvailable = evidence.isRight(),
            isGreenCardAvailable = greenCard.isRight(),
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
    val isGreenCardAvailable: Boolean,
  ) : CertificatesState
}

internal sealed interface CertificatesEvent {
  data object Retry : CertificatesEvent
}
