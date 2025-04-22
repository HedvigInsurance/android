package com.hedvig.android.feature.insurance.certificate.ui.email

import com.hedvig.android.feature.insurance.certificate.data.GetInsuranceEvidenceUseCase
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputState.Loading
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class InsuranceEvidenceEmailInputViewModel(
  getInsuranceEvidenceUseCase: GetInsuranceEvidenceUseCase
): MoleculeViewModel
<InsuranceEvidenceEmailInputEvent,InsuranceEvidenceEmailInputState>(
  initialState = Loading,
  presenter = TODO()
) {
}

internal sealed interface InsuranceEvidenceEmailInputState {
  data object Loading: InsuranceEvidenceEmailInputState
}
internal sealed interface InsuranceEvidenceEmailInputEvent
