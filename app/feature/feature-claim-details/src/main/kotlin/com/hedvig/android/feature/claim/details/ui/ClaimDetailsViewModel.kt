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
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.core.fileupload.UploadFileUseCase
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.feature.claim.details.data.GetClaimDetailUiStateUseCase
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.audio.player.data.SignedAudioUrl
import java.io.File
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

internal class ClaimDetailsViewModel(
  claimId: String,
  getClaimDetailUiStateUseCase: GetClaimDetailUiStateUseCase,
  uploadFileUseCase: UploadFileUseCase,
  downloadPdfUseCase: DownloadPdfUseCase,
) : MoleculeViewModel<ClaimDetailsEvent, ClaimDetailUiState>(
    ClaimDetailUiState.Loading,
    ClaimDetailPresenter(claimId, getClaimDetailUiStateUseCase, uploadFileUseCase, downloadPdfUseCase),
  )

private class ClaimDetailPresenter(
  private val claimId: String,
  private val getClaimDetailUiStateUseCase: GetClaimDetailUiStateUseCase,
  private val uploadFileUseCase: UploadFileUseCase,
  private val downloadPdfUseCase: DownloadPdfUseCase,
) : MoleculePresenter<ClaimDetailsEvent, ClaimDetailUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimDetailsEvent>.present(lastState: ClaimDetailUiState): ClaimDetailUiState {
    var isLoading: Boolean by remember { mutableStateOf(lastState as? ClaimDetailUiState.Loading != null) }
    var hasError: Boolean by remember { mutableStateOf(lastState as? ClaimDetailUiState.Error != null) }
    var content: ClaimDetailUiState.Content? by remember { mutableStateOf(lastState as? ClaimDetailUiState.Content) }
    var loadIteration by remember { mutableIntStateOf(0) }
    val mediaToSend = remember { Channel<Uri>(Channel.UNLIMITED) }

    var downloadingUrl by remember {
      mutableStateOf<String?>(null)
    }
    LaunchedEffect(loadIteration) {
      isLoading = true
      hasError = false
      getClaimDetailUiStateUseCase.invoke(claimId).collect { result ->
        isLoading = false
        result.fold(
          ifLeft = {
            if (content !is ClaimDetailUiState.Content) {
              hasError = true
              content = null
            }
          },
          ifRight = { claimDetailUiState: ClaimDetailUiState.Content ->
            hasError = false
            content = claimDetailUiState
          },
        )
      }
    }

    LaunchedEffect(downloadingUrl) {
      val downloadingUrlValue = downloadingUrl ?: return@LaunchedEffect
      logcat(LogPriority.INFO) { "Downloading terms and conditions with url:$downloadingUrl" }
      downloadPdfUseCase.invoke(downloadingUrlValue)
        .fold(
          ifLeft = { errorMessage ->
            logcat(LogPriority.ERROR) { "Downloading terms and conditions failed:$errorMessage" }
            content = content?.copy(downloadError = true, isLoadingPdf = false)
            downloadingUrl = null
          },
          ifRight = { uri ->
            logcat(
              LogPriority.INFO,
            ) { "Downloading terms and conditions succeeded. Result uri:${uri.absolutePath}" }
            content = content?.copy(downloadError = null, savedFileUri = uri, isLoadingPdf = false)
            downloadingUrl = null
          },
        )
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
        ClaimDetailsEvent.DismissUploadError -> content = content?.copy(uploadError = null)
        is ClaimDetailsEvent.DownloadPdf -> {
          content = content?.copy(isLoadingPdf = true)
          downloadingUrl = event.url
        }
        ClaimDetailsEvent.DismissDownloadError -> content = content?.copy(downloadError = null)
        ClaimDetailsEvent.HandledSharingPdfFile -> content = content?.copy(downloadError = null, savedFileUri = null)
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

  data object DismissDownloadError : ClaimDetailsEvent

  data class DownloadPdf(val url: String) : ClaimDetailsEvent

  data object HandledSharingPdfFile : ClaimDetailsEvent
}

internal sealed interface ClaimDetailUiState {
  data object Loading : ClaimDetailUiState

  data object Error : ClaimDetailUiState

  data class Content(
    val claimId: String,
    val conversationId: String?,
    val hasUnreadMessages: Boolean,
    val submittedContent: SubmittedContent?,
    val files: List<UiFile>,
    val claimStatusCardUiState: ClaimStatusCardUiState,
    val claimStatus: ClaimStatus,
    val claimOutcome: ClaimOutcome,
    val uploadUri: String,
    val isUploadingFile: Boolean,
    val uploadError: String?,
    val claimType: String?,
    val incidentDate: LocalDate?,
    val submittedAt: LocalDateTime,
    val insuranceDisplayName: String?,
    val termsConditionsUrl: String?,
    val savedFileUri: File?,
    val downloadError: Boolean?,
    val isLoadingPdf: Boolean,
    val appealInstructionsUrl: String?,
    val isUploadingFilesEnabled: Boolean
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
