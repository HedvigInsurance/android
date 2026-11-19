package com.hedvig.android.feature.claim.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.fx.coroutines.parMap
import com.eygraber.uri.Uri
import com.hedvig.android.audio.player.data.SignedAudioUrl
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.fileupload.ClaimsServiceUploadFileUseCase
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.core.fileupload.DownloadedFile
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.feature.claim.details.data.ClaimRatingAnswer
import com.hedvig.android.feature.claim.details.data.DismissClaimRatingUseCase
import com.hedvig.android.feature.claim.details.data.GetClaimDetailUiStateUseCase
import com.hedvig.android.feature.claim.details.data.SubmitClaimRatingUseCase
import com.hedvig.android.feature.claim.details.ui.ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN
import com.hedvig.android.feature.claim.details.ui.ClaimDetailUiState.Content.ClaimStatus.CLOSED
import com.hedvig.android.feature.claim.details.ui.rating.RatingButtonState
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent
import com.hedvig.android.feature.claim.details.ui.rating.RatingState
import com.hedvig.android.feature.claim.details.ui.rating.reduceRating
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import java.io.File
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ClaimDetailsViewModel(
  @Assisted claimId: String,
  @Assisted openRatingOnLoad: Boolean,
  getClaimDetailUiStateUseCase: GetClaimDetailUiStateUseCase,
  claimsServiceUploadFileUseCase: ClaimsServiceUploadFileUseCase,
  downloadPdfUseCase: DownloadPdfUseCase,
  submitClaimRatingUseCase: SubmitClaimRatingUseCase,
  dismissClaimRatingUseCase: DismissClaimRatingUseCase,
) : MoleculeViewModel<ClaimDetailsEvent, ClaimDetailUiState>(
    ClaimDetailUiState.Loading,
    ClaimDetailPresenter(
      claimId,
      openRatingOnLoad,
      getClaimDetailUiStateUseCase,
      claimsServiceUploadFileUseCase,
      downloadPdfUseCase,
      submitClaimRatingUseCase,
      dismissClaimRatingUseCase,
    ),
  )

internal class ClaimDetailPresenter(
  private val claimId: String,
  private val openRatingOnLoad: Boolean,
  private val getClaimDetailUiStateUseCase: GetClaimDetailUiStateUseCase,
  private val claimsServiceUploadFileUseCase: ClaimsServiceUploadFileUseCase,
  private val downloadPdfUseCase: DownloadPdfUseCase,
  private val submitClaimRatingUseCase: SubmitClaimRatingUseCase,
  private val dismissClaimRatingUseCase: DismissClaimRatingUseCase,
) : MoleculePresenter<ClaimDetailsEvent, ClaimDetailUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimDetailsEvent>.present(lastState: ClaimDetailUiState): ClaimDetailUiState {
    var isLoading: Boolean by remember { mutableStateOf(lastState as? ClaimDetailUiState.Loading != null) }
    var hasError: Boolean by remember { mutableStateOf(lastState as? ClaimDetailUiState.Error != null) }
    var content: ClaimDetailUiState.Content? by remember { mutableStateOf(lastState as? ClaimDetailUiState.Content) }
    var rating: RatingState by remember {
      mutableStateOf((lastState as? ClaimDetailUiState.Content)?.rating ?: RatingState.Closed)
    }
    var ratingSubmittedLocally by remember {
      mutableStateOf((lastState as? ClaimDetailUiState.Content)?.ratingButton == RatingButtonState.Submitted)
    }
    var ratingAutoOpenPending by remember {
      mutableStateOf(openRatingOnLoad && lastState !is ClaimDetailUiState.Content)
    }
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
            content = content?.copy(downloadError = true, isLoadingPdf = null)
            downloadingUrl = null
          },
          ifRight = { downloadedFile ->
            logcat(
              LogPriority.INFO,
            ) { "Downloading terms and conditions succeeded. Result path:${downloadedFile.path}" }
            content = content?.copy(downloadError = null, savedFileUri = downloadedFile, isLoadingPdf = null)
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
          claimsServiceUploadFileUseCase.invoke(uploadUri, uri).fold(
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
        ClaimDetailsEvent.Retry -> {
          loadIteration++
        }

        ClaimDetailsEvent.DismissUploadError -> {
          content = content?.copy(uploadError = null)
        }

        is ClaimDetailsEvent.DownloadPdf -> {
          content = content?.copy(isLoadingPdf = event.url)
          downloadingUrl = event.url
        }

        ClaimDetailsEvent.DismissDownloadError -> {
          content = content?.copy(downloadError = null)
        }

        ClaimDetailsEvent.HandledSharingPdfFile -> {
          content = content?.copy(downloadError = null, savedFileUri = null)
        }

        is ClaimDetailsEvent.Rating -> {
          val transition = reduceRating(rating, event.event)
          rating = transition.state
          val answer = transition.answer ?: return@CollectEvents
          if (answer is ClaimRatingAnswer.Rated) {
            ratingSubmittedLocally = true
          }
          launch {
            when (answer) {
              is ClaimRatingAnswer.Rated -> submitClaimRatingUseCase.invoke(claimId, answer.rating, answer.comment)
              ClaimRatingAnswer.Dismissed -> dismissClaimRatingUseCase.invoke(claimId)
            }.onLeft { logcat(LogPriority.WARN) { "Failed to record rating answer for claim $claimId: $it" } }
          }
        }
      }
    }

    val ratingButton = content?.ratingButton(ratingSubmittedLocally)
    LaunchedEffect(ratingAutoOpenPending, ratingButton) {
      if (!ratingAutoOpenPending || ratingButton == null) return@LaunchedEffect
      ratingAutoOpenPending = false
      if (ratingButton == RatingButtonState.Enabled) {
        rating = reduceRating(rating, RatingEvent.OpenRequested).state
      }
    }

    if (hasError) {
      return ClaimDetailUiState.Error
    }
    return content?.let {
      it.copy(rating = rating, ratingButton = ratingButton)
    } ?: ClaimDetailUiState.Loading
  }
}

internal sealed interface ClaimDetailsEvent {
  data object Retry : ClaimDetailsEvent

  data object DismissUploadError : ClaimDetailsEvent

  data object DismissDownloadError : ClaimDetailsEvent

  data class DownloadPdf(val url: String) : ClaimDetailsEvent

  data object HandledSharingPdfFile : ClaimDetailsEvent

  data class Rating(val event: RatingEvent) : ClaimDetailsEvent
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
    val submittedAt: LocalDateTime,
    val insuranceDisplayName: String?,
    val termsConditionsUrl: String?,
    val savedFileUri: DownloadedFile?,
    val downloadError: Boolean?,
    val isLoadingPdf: String?,
    val appealInstructionsUrl: String?,
    val isUploadingFilesEnabled: Boolean,
    val infoText: String?,
    val displayItems: List<DisplayItem>,
    val externalId: String?,
    val handlerEmail: String?,
    val isPartnerClaim: Boolean,
    val hasSubmittedRating: Boolean,
    val rating: RatingState = RatingState.Closed,
    /** Null when the claim cannot be rated in the app. */
    val ratingButton: RatingButtonState? = null,
  ) : ClaimDetailUiState {
    val claimIsInUndeterminedState: Boolean = claimStatus == CLOSED && claimOutcome == UNKNOWN

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
      UNRESPONSIVE,
    }

    companion object
  }
}

private fun ClaimDetailUiState.Content.ratingButton(ratingSubmittedLocally: Boolean): RatingButtonState? = when {
  claimStatus != CLOSED || isPartnerClaim -> null
  hasSubmittedRating || ratingSubmittedLocally -> RatingButtonState.Submitted
  else -> RatingButtonState.Enabled
}
