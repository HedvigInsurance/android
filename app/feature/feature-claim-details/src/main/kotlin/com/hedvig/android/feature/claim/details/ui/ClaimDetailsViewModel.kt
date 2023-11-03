package com.hedvig.android.feature.claim.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.audio.player.SignedAudioUrl
import com.hedvig.android.feature.claim.details.data.GetClaimDetailUiStateUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState

internal class ClaimDetailsViewModel(
  private val claimId: String,
  private val getClaimDetailUiStateUseCase: GetClaimDetailUiStateUseCase,
) : MoleculeViewModel<ClaimDetailsEvent, ClaimDetailUiState>(
  ClaimDetailUiState.Loading,
  ClaimDetailPresenter(claimId, getClaimDetailUiStateUseCase),
)

private class ClaimDetailPresenter(
  private val claimId: String,
  private val getClaimDetailUiStateUseCase: GetClaimDetailUiStateUseCase,
) : MoleculePresenter<ClaimDetailsEvent, ClaimDetailUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimDetailsEvent>.present(lastState: ClaimDetailUiState): ClaimDetailUiState {
    var isLoading: Boolean by remember { mutableStateOf(lastState as? ClaimDetailUiState.Loading != null) }
    var hasError: Boolean by remember { mutableStateOf(lastState as? ClaimDetailUiState.Error != null) }
    var content: ClaimDetailUiState.Content? by remember { mutableStateOf(lastState as? ClaimDetailUiState.Content) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      isLoading = true
      hasError = false
      val forceNetworkFetch = loadIteration != 0
      getClaimDetailUiStateUseCase.invoke(claimId, forceNetworkFetch).collect { result ->
        isLoading = false
        result.fold(
          ifLeft = {
            hasError = true
            content = null
          },
          ifRight = { claimDetailUiState: ClaimDetailUiState.Content ->
            hasError = false
            content = claimDetailUiState
          },
        )
      }
    }

    CollectEvents { event ->
      when (event) {
        ClaimDetailsEvent.Retry -> loadIteration++
      }
    }

    if (hasError) {
      return ClaimDetailUiState.Error
    }
    val contentValue = content
    return if (contentValue == null) {
      ClaimDetailUiState.Loading
    } else {
      contentValue
    }
  }
}

internal sealed interface ClaimDetailsEvent {
  data object Retry : ClaimDetailsEvent
}

internal sealed interface ClaimDetailUiState {
  data object Loading : ClaimDetailUiState
  data object Error : ClaimDetailUiState
  data class Content(
    val claimId: String,
    val submittedContent: SubmittedContent?,
    val claimStatusCardUiState: ClaimStatusCardUiState,
    val claimStatus: ClaimStatus,
    val claimOutcome: ClaimOutcome,
  ) : ClaimDetailUiState {
    sealed interface SubmittedContent {
      data class Audio(val signedAudioURL: SignedAudioUrl) : SubmittedContent
      data class FreeText(val text: String) : SubmittedContent
    }

    enum class ClaimStatus {
      CREATED,
      IN_PROGRESS,
      CLOSED,
      REOPENED,
      UNKNOWN,
    }

    enum class ClaimOutcome {
      PAID,
      NOT_COMPENSATED,
      NOT_COVERED,
      UNKNOWN,
    }

    companion object
  }
}
