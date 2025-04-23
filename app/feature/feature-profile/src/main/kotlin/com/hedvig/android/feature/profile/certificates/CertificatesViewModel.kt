package com.hedvig.android.feature.profile.certificates

import com.hedvig.android.molecule.android.MoleculeViewModel

internal class CertificatesViewModel: MoleculeViewModel<CertificatesEvent,CertificatesState>(
  initialState = TODO(),
  presenter = TODO()
)

internal sealed interface CertificatesState
internal sealed interface CertificatesEvent
