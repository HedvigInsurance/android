package com.hedvig.android.feature.editcoinsured.ui

import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class EditCoInsuredViewModel(
  contractId: String,
  getCoInsuredUseCaseProvider: GetCoInsuredUseCase,
  fetchCoInsuredPersonalInformationUseCaseProvider: FetchCoInsuredPersonalInformationUseCase,
  createMidtermChangeUseCase: CreateMidtermChangeUseCase,
  commitMidtermChangeUseCase: CommitMidtermChangeUseCase,
) : MoleculeViewModel<EditCoInsuredEvent, EditCoInsuredState>(
    EditCoInsuredState.Loading,
    EditCoInsuredPresenter(
      contractId = contractId,
      getCoInsuredUseCase = getCoInsuredUseCaseProvider,
      fetchCoInsuredPersonalInformationUseCase = fetchCoInsuredPersonalInformationUseCaseProvider,
      createMidtermChangeUseCase = createMidtermChangeUseCase,
      commitMidtermChangeUseCase = commitMidtermChangeUseCase,
    ),
  )
