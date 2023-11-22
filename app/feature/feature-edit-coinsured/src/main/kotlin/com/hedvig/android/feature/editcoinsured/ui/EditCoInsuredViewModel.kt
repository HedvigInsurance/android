package com.hedvig.android.feature.editcoinsured.ui

import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class EditCoInsuredViewModel(
  contractId: String,
  getCoInsuredUseCaseProvider: Provider<GetCoInsuredUseCase>,
  fetchCoInsuredPersonalInformationUseCaseProvider: Provider<FetchCoInsuredPersonalInformationUseCase>,
) : MoleculeViewModel<EditCoInsuredEvent, EditCoInsuredState>(
  EditCoInsuredState(),
  EditCoInsuredPresenter(
    contractId = contractId,
    getCoInsuredUseCaseProvider = getCoInsuredUseCaseProvider,
    fetchCoInsuredPersonalInformationUseCaseProvider = fetchCoInsuredPersonalInformationUseCaseProvider,
  ),
)
