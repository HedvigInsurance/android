package com.hedvig.android.shared.foreverui.ui.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.shared.foreverui.ui.data.ForeverData
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepository
import com.hedvig.android.shared.foreverui.ui.ui.ForeverEvent.CloseEditCodeBottomSheet
import com.hedvig.android.shared.foreverui.ui.ui.ForeverEvent.OpenEditCodeBottomSheet
import com.hedvig.android.shared.foreverui.ui.ui.ForeverEvent.RetryLoadReferralData
import com.hedvig.android.shared.foreverui.ui.ui.ForeverEvent.ShowedReferralCodeSubmissionError
import com.hedvig.android.shared.foreverui.ui.ui.ForeverEvent.ShowedReferralCodeSuccessfulChangeMessage
import com.hedvig.android.shared.foreverui.ui.ui.ForeverEvent.SubmitNewReferralCode

class ForeverViewModel(
  foreverRepositoryProvider: Provider<ForeverRepository>,
) : MoleculeViewModel<ForeverEvent, ForeverUiState>(
    ForeverUiState.Loading,
    ForeverPresenter(
      foreverRepositoryProvider = foreverRepositoryProvider,
    ),
  )

internal class ForeverPresenter(
  private val foreverRepositoryProvider: Provider<ForeverRepository>,
) : MoleculePresenter<ForeverEvent, ForeverUiState> {
  @Composable
  override fun MoleculePresenterScope<ForeverEvent>.present(lastState: ForeverUiState): ForeverUiState {
    var isLoadingForeverData by remember { mutableStateOf(lastState.isLoadingForeverData) }
    var foreverDataLoadIteration by remember { mutableIntStateOf(0) }
    var foreverDataErrorMessage by remember { mutableStateOf(lastState.foreverDataErrorMessage) }
    var foreverData by remember { mutableStateOf(lastState.foreverData) }

    var referralCodeToSubmit by remember { mutableStateOf<String?>(null) }
    var referralCodeToSubmitErrorMessage by remember { mutableStateOf<ForeverRepository.ReferralError?>(null) }
    var showReferralCodeToSubmitSuccess by remember { mutableStateOf<Boolean>(false) }
    var showEditReferralCodeBottomSheet by rememberSaveable { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        ShowedReferralCodeSuccessfulChangeMessage -> showReferralCodeToSubmitSuccess = false
        ShowedReferralCodeSubmissionError -> referralCodeToSubmitErrorMessage = null
        RetryLoadReferralData -> foreverDataLoadIteration++
        is SubmitNewReferralCode -> referralCodeToSubmit = event.code

        CloseEditCodeBottomSheet -> showEditReferralCodeBottomSheet = false

        OpenEditCodeBottomSheet -> showEditReferralCodeBottomSheet = true
      }
    }

    LaunchedEffect(foreverDataLoadIteration) {
      isLoadingForeverData = true
      foreverDataErrorMessage = null
      either {
        val referralsData = foreverRepositoryProvider.provide().getReferralsData().bind()
        ForeverData(referralsData = referralsData)
      }.fold(
        ifLeft = { foreverDataErrorMessage = it },
        ifRight = { foreverData = it },
      )
      isLoadingForeverData = false
    }

    LaunchedEffect(referralCodeToSubmit) {
      val codeToSubmit = referralCodeToSubmit ?: return@LaunchedEffect
      foreverRepositoryProvider.provide().updateCode(codeToSubmit).fold(
        ifLeft = {
          referralCodeToSubmit = null
          referralCodeToSubmitErrorMessage = it
        },
        ifRight = {
          showEditReferralCodeBottomSheet = false
          referralCodeToSubmit = null
          showReferralCodeToSubmitSuccess = true
          foreverDataLoadIteration++ // Trigger a refetch of the data to update the campaign code
        },
      )
    }

    return ForeverUiState(
      foreverData = foreverData,
      isLoadingForeverData = isLoadingForeverData,
      foreverDataErrorMessage = foreverDataErrorMessage,
      referralCodeLoading = referralCodeToSubmit != null,
      referralCodeErrorMessage = referralCodeToSubmitErrorMessage,
      showReferralCodeSuccessfullyChangedMessage = showReferralCodeToSubmitSuccess,
      showEditReferralCodeBottomSheet = showEditReferralCodeBottomSheet,
    )
  }
}

sealed interface ForeverEvent {
  data object ShowedReferralCodeSuccessfulChangeMessage : ForeverEvent

  data object ShowedReferralCodeSubmissionError : ForeverEvent

  data class SubmitNewReferralCode(val code: String) : ForeverEvent

  data object RetryLoadReferralData : ForeverEvent

  data object OpenEditCodeBottomSheet : ForeverEvent

  data object CloseEditCodeBottomSheet : ForeverEvent
}

data class ForeverUiState(
  val foreverData: ForeverData?,
  val isLoadingForeverData: Boolean,
  val foreverDataErrorMessage: ErrorMessage?,
  val referralCodeLoading: Boolean,
  val referralCodeErrorMessage: ForeverRepository.ReferralError?,
  val showReferralCodeSuccessfullyChangedMessage: Boolean,
  val showEditReferralCodeBottomSheet: Boolean,
  // = false
) {
  companion object {
    val Loading: ForeverUiState = ForeverUiState(
      foreverData = null,
      isLoadingForeverData = true,
      foreverDataErrorMessage = null,
      referralCodeLoading = false,
      referralCodeErrorMessage = null,
      showReferralCodeSuccessfullyChangedMessage = false,
      showEditReferralCodeBottomSheet = false,
    )
  }
}
