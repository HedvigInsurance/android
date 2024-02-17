package com.hedvig.android.feature.chat

import com.hedvig.android.feature.deleteaccount.DeleteAccountEvent
import com.hedvig.android.feature.deleteaccount.DeleteAccountPresenter
import com.hedvig.android.feature.deleteaccount.DeleteAccountUiState
import com.hedvig.android.feature.deleteaccount.data.DeleteAccountStateUseCase
import com.hedvig.android.feature.deleteaccount.data.RequestAccountDeletionUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel

internal class DeleteAccountViewModel(
  private val requestAccountDeletionUseCase: RequestAccountDeletionUseCase,
  private val deleteAccountStateUseCase: DeleteAccountStateUseCase,
) : MoleculeViewModel<DeleteAccountEvent, DeleteAccountUiState>(
    DeleteAccountUiState.Loading,
    DeleteAccountPresenter(
      requestAccountDeletionUseCase = requestAccountDeletionUseCase,
      deleteAccountStateUseCase = deleteAccountStateUseCase,
    ),
  )
