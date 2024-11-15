package com.hedvig.android.shared.foreverui.ui.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.raise.either
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.shared.foreverui.ui.data.ForeverData
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepository
import com.hedvig.android.shared.foreverui.ui.ui.ForeverEvent.CloseEditCodeBottomSheet
import com.hedvig.android.shared.foreverui.ui.ui.ForeverEvent.OpenEditCodeBottomSheet
import com.hedvig.android.shared.foreverui.ui.ui.ForeverEvent.RetryLoadReferralData
import com.hedvig.android.shared.foreverui.ui.ui.ForeverEvent.ShowedReferralCodeSubmissionError
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
    var currentState by remember { mutableStateOf(lastState) }
    var foreverDataLoadIteration by remember { mutableIntStateOf(0) }
    var referralCodeToSubmit by remember { mutableStateOf<String?>(null) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        ShowedReferralCodeSubmissionError -> {
          val state = currentState as? ForeverUiState.Success ?: return@CollectEvents
          currentState = state.copy(referralCodeErrorMessage = null)
        }

        RetryLoadReferralData -> foreverDataLoadIteration++

        is SubmitNewReferralCode -> {
          referralCodeToSubmit = event.code
        }

        CloseEditCodeBottomSheet -> {
          val state = currentState as? ForeverUiState.Success ?: return@CollectEvents
          currentState = state.copy(showEditReferralCodeBottomSheet = false)
        }

        OpenEditCodeBottomSheet -> {
          val state = currentState as? ForeverUiState.Success ?: return@CollectEvents
          currentState = state.copy(showEditReferralCodeBottomSheet = true)
        }

        ForeverEvent.ShowedReferralCodeSuccessfulChangeMessage -> {
          showSuccessSnackbar = false
        }
      }
    }

    LaunchedEffect(foreverDataLoadIteration) {
      val state = currentState
      currentState = if (state !is ForeverUiState.Success) {
        ForeverUiState.Loading
      } else {
        state.copy(reloading = true)
      }
      either {
        val referralsData = foreverRepositoryProvider.provide().getReferralsData().bind()
        ForeverData(referralsData = referralsData)
      }.fold(
        ifLeft = {
          logcat(priority = LogPriority.INFO) { "Tried to load ForeverData but got error: $it" }
          currentState = ForeverUiState.Error
        },
        ifRight = {
          currentState = ForeverUiState.Success(
            foreverData = it,
            referralCodeLoading = false,
            referralCodeErrorMessage = null,
            showEditReferralCodeBottomSheet = false,
            reloading = false,
            showReferralCodeSuccessfullyChangedMessage = showSuccessSnackbar,
          )
        },
      )
    }

    LaunchedEffect(referralCodeToSubmit) {
      val codeToSubmit = referralCodeToSubmit ?: return@LaunchedEffect
      val state = currentState as? ForeverUiState.Success ?: return@LaunchedEffect
      currentState = state.copy(referralCodeLoading = true)
      foreverRepositoryProvider.provide().updateCode(codeToSubmit).fold(
        ifLeft = {
          referralCodeToSubmit = null
          currentState = state.copy(
            referralCodeErrorMessage = it,
            referralCodeLoading = false,
            showReferralCodeSuccessfullyChangedMessage = false,
          )
        },
        ifRight = {
          referralCodeToSubmit = null
          showSuccessSnackbar = true
          currentState = state.copy(
            referralCodeLoading = false,
            showEditReferralCodeBottomSheet = false,
          )
          foreverDataLoadIteration++ // Trigger a refetch of the data to update the campaign code
        },
      )
    }
    val state = currentState
    return when (state) {
      ForeverUiState.Error, ForeverUiState.Loading -> state
      is ForeverUiState.Success -> state.copy(showReferralCodeSuccessfullyChangedMessage = showSuccessSnackbar)
    }
  }
}

sealed interface ForeverEvent {
  data object ShowedReferralCodeSubmissionError : ForeverEvent

  data class SubmitNewReferralCode(val code: String) : ForeverEvent

  data object RetryLoadReferralData : ForeverEvent

  data object OpenEditCodeBottomSheet : ForeverEvent

  data object CloseEditCodeBottomSheet : ForeverEvent

  data object ShowedReferralCodeSuccessfulChangeMessage : ForeverEvent
}

sealed interface ForeverUiState {
  data object Loading : ForeverUiState

  data object Error : ForeverUiState

  data class Success(
    val foreverData: ForeverData?,
    val referralCodeLoading: Boolean,
    val reloading: Boolean,
    val referralCodeErrorMessage: ForeverRepository.ReferralError?,
    val showEditReferralCodeBottomSheet: Boolean,
    val showReferralCodeSuccessfullyChangedMessage: Boolean,
  ) : ForeverUiState
}
