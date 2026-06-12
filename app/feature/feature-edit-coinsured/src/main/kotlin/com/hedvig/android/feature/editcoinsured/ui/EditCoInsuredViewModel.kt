package com.hedvig.android.feature.editcoinsured.ui

import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel
internal class EditCoInsuredViewModel(
  @Assisted contractId: String,
  @Assisted type: CoInsuredFlowType,
  getCoInsuredUseCaseProvider: GetCoInsuredUseCase,
  fetchCoInsuredPersonalInformationUseCaseProvider: FetchCoInsuredPersonalInformationUseCase,
  createMidtermChangeUseCase: CreateMidtermChangeUseCase,
  commitMidtermChangeUseCase: CommitMidtermChangeUseCase,
  backstack: Backstack,
) : MoleculeViewModel<EditCoInsuredEvent, EditCoInsuredState>(
    EditCoInsuredState.Loading,
    EditCoInsuredPresenter(
      contractId = contractId,
      type = type,
      getCoInsuredUseCase = getCoInsuredUseCaseProvider,
      fetchCoInsuredPersonalInformationUseCase = fetchCoInsuredPersonalInformationUseCaseProvider,
      createMidtermChangeUseCase = createMidtermChangeUseCase,
      commitMidtermChangeUseCase = commitMidtermChangeUseCase,
      backstack = backstack,
    ),
  )
