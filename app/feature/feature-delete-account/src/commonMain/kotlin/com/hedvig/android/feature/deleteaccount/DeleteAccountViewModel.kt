package com.hedvig.android.feature.chat

import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.deleteaccount.DeleteAccountEvent
import com.hedvig.android.feature.deleteaccount.DeleteAccountPresenter
import com.hedvig.android.feature.deleteaccount.DeleteAccountUiState
import com.hedvig.android.feature.deleteaccount.data.DeleteAccountStateUseCase
import com.hedvig.android.feature.deleteaccount.data.RequestAccountDeletionUseCase
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class DeleteAccountViewModel(
  private val requestAccountDeletionUseCase: RequestAccountDeletionUseCase,
  private val deleteAccountStateUseCase: DeleteAccountStateUseCase,
  backstack: Backstack,
) : MoleculeViewModel<DeleteAccountEvent, DeleteAccountUiState>(
    DeleteAccountUiState.Loading,
    DeleteAccountPresenter(
      requestAccountDeletionUseCase = requestAccountDeletionUseCase,
      deleteAccountStateUseCase = deleteAccountStateUseCase,
      backstack = backstack,
    ),
  )
