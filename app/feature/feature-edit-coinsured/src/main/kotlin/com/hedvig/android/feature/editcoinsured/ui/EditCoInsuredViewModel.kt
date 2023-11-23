package com.hedvig.android.feature.editcoinsured.ui

import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class EditCoInsuredViewModel(
  contractId: String,
  getCoInsuredUseCase: GetCoInsuredUseCase,
  fetchCoInsuredPersonalInformationUseCase: FetchCoInsuredPersonalInformationUseCase,
) : MoleculeViewModel<EditCoInsuredEvent, EditCoInsuredState>(
    EditCoInsuredState.Loading,
    EditCoInsuredPresenter(
      contractId = contractId,
      getCoInsuredUseCaseProvider = getCoInsuredUseCase,
      fetchCoInsuredPersonalInformationUseCaseProvider = fetchCoInsuredPersonalInformationUseCase,
    ),
  )
