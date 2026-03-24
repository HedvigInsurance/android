package com.hedvig.android.feature.editcoinsured.ui

import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class EditCoInsuredViewModel(
  contractId: String,
  type: CoInsuredFlowType,
  getCoInsuredUseCaseProvider: GetCoInsuredUseCase,
  fetchCoInsuredPersonalInformationUseCaseProvider: FetchCoInsuredPersonalInformationUseCase,
  createMidtermChangeUseCase: CreateMidtermChangeUseCase,
  commitMidtermChangeUseCase: CommitMidtermChangeUseCase,
) : MoleculeViewModel<EditCoInsuredEvent, EditCoInsuredState>(
    EditCoInsuredState.Loading,
    EditCoInsuredPresenter(
      contractId = contractId,
      type = type,
      getCoInsuredUseCase = getCoInsuredUseCaseProvider,
      fetchCoInsuredPersonalInformationUseCase = fetchCoInsuredPersonalInformationUseCaseProvider,
      createMidtermChangeUseCase = createMidtermChangeUseCase,
      commitMidtermChangeUseCase = commitMidtermChangeUseCase,
    ),
  )
