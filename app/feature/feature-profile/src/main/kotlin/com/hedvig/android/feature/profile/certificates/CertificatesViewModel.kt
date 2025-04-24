package com.hedvig.android.feature.profile.certificates

import com.hedvig.android.molecule.android.MoleculeViewModel

internal class CertificatesViewModel: MoleculeViewModel<CertificatesEvent,CertificatesState>(
  initialState = TODO(),
  presenter = TODO()
)

internal sealed interface CertificatesState {
  data object Loading: CertificatesState
  data object Failure: CertificatesState
  data class Success(
    val isTravelCertificateAvailable: Boolean,
    val isInsuranceEvidenceAvailable: Boolean,
  ): CertificatesState
}
internal sealed interface CertificatesEvent {
  data object Retry: CertificatesEvent
}
