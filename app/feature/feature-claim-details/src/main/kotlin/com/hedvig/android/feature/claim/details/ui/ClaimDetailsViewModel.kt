package com.hedvig.android.feature.claim.details.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.fx.coroutines.parMap
import com.hedvig.android.audio.player.SignedAudioUrl
import com.hedvig.android.feature.claim.details.data.GetClaimDetailUiStateUseCase
import com.hedvig.android.feature.claim.details.data.UploadFileUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow

internal class ClaimDetailsViewModel(
  claimId: String,
  getClaimDetailUiStateUseCase: GetClaimDetailUiStateUseCase,
  uploadFileUseCase: UploadFileUseCase,
) : MoleculeViewModel<ClaimDetailsEvent, ClaimDetailUiState>(
    ClaimDetailUiState.Loading,
    ClaimDetailPresenter(claimId, getClaimDetailUiStateUseCase, uploadFileUseCase),
  )

private class ClaimDetailPresenter(
  private val claimId: String,
  private val getClaimDetailUiStateUseCase: GetClaimDetailUiStateUseCase,
  private val uploadFileUseCase: UploadFileUseCase,
) : MoleculePresenter<ClaimDetailsEvent, ClaimDetailUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimDetailsEvent>.present(lastState: ClaimDetailUiState): ClaimDetailUiState {
    var isLoading: Boolean by remember { mutableStateOf(lastState as? ClaimDetailUiState.Loading != null) }
    var hasError: Boolean by remember { mutableStateOf(lastState as? ClaimDetailUiState.Error != null) }
    var content: ClaimDetailUiState.Content? by remember { mutableStateOf(lastState as? ClaimDetailUiState.Content) }
    var loadIteration by remember { mutableIntStateOf(0) }
    val mediaToSend = remember { Channel<Uri>(Channel.UNLIMITED) }

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

    LaunchedEffect(mediaToSend) {
      mediaToSend.receiveAsFlow().parMap { uri: Uri ->
        content?.uploadUri?.let { uploadUri ->
          content = content?.copy(
            isUploadingFile = true,
            uploadError = null,
          )
          uploadFileUseCase.invoke(uploadUri, uri).fold(
            ifLeft = {
              content = content?.copy(
                isUploadingFile = false,
                uploadError = it.message,
              )
            },
            ifRight = {
              loadIteration++
            },
          )
        }
      }.collect()
    }

    CollectEvents { event ->
      when (event) {
        ClaimDetailsEvent.Retry -> loadIteration++
        is ClaimDetailsEvent.UploadFile -> mediaToSend.trySend(event.uri)
        ClaimDetailsEvent.DismissUploadError -> content = content?.copy(uploadError = null)
      }
    }

    if (hasError) {
      return ClaimDetailUiState.Error
    }
    return content ?: ClaimDetailUiState.Loading
  }
}

internal sealed interface ClaimDetailsEvent {
  data object Retry : ClaimDetailsEvent

  data object DismissUploadError : ClaimDetailsEvent

  data class UploadFile(val uri: Uri) : ClaimDetailsEvent
}

internal sealed interface ClaimDetailUiState {
  data object Loading : ClaimDetailUiState

  data object Error : ClaimDetailUiState

  data class Content(
    val claimId: String,
    val submittedContent: SubmittedContent?,
    val files: List<ClaimFile>,
    val claimStatusCardUiState: ClaimStatusCardUiState,
    val claimStatus: ClaimStatus,
    val claimOutcome: ClaimOutcome,
    val uploadUri: String,
    val isUploadingFile: Boolean,
    val uploadError: String?,
  ) : ClaimDetailUiState {
    sealed interface SubmittedContent {
      data class Audio(val signedAudioURL: SignedAudioUrl) : SubmittedContent

      data class FreeText(val text: String) : SubmittedContent
    }

    data class ClaimFile(
      val id: String,
      val name: String,
      val mimeType: String,
      val url: String,
      val thumbnailUrl: String?,
    )

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
