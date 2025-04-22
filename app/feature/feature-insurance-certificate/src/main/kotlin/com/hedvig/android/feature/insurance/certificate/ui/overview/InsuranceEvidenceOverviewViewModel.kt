package com.hedvig.android.feature.insurance.certificate.ui.overview

import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewState.Loading
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class InsuranceEvidenceOverviewViewModel(
) : MoleculeViewModel
<InsuranceEvidenceOverviewEvent, InsuranceEvidenceOverviewState>(
  initialState = Loading,
  presenter = TODO(),
) {
}

internal sealed interface InsuranceEvidenceOverviewState {
  data object Loading : InsuranceEvidenceOverviewState
}

internal sealed interface InsuranceEvidenceOverviewEvent
