package com.hedvig.android.feature.claim.details.ui

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.eygraber.uri.Uri
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.ClaimsServiceUploadFileUseCase
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.core.fileupload.DownloadedFile
import com.hedvig.android.core.fileupload.UploadSuccess
import com.hedvig.android.feature.claim.details.data.ClaimRatingAnswer
import com.hedvig.android.feature.claim.details.data.DismissClaimRatingUseCase
import com.hedvig.android.feature.claim.details.data.Error
import com.hedvig.android.feature.claim.details.data.GetClaimDetailUiStateUseCase
import com.hedvig.android.feature.claim.details.data.SubmitClaimRatingUseCase
import com.hedvig.android.feature.claim.details.ui.ClaimDetailUiState.Content.ClaimOutcome
import com.hedvig.android.feature.claim.details.ui.ClaimDetailUiState.Content.ClaimStatus
import com.hedvig.android.feature.claim.details.ui.rating.RatingButtonState
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent
import com.hedvig.android.feature.claim.details.ui.rating.RatingState
import com.hedvig.android.feature.claim.details.ui.rating.RatingStep
import com.hedvig.android.feature.claim.details.ui.rating.SendStatus
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.molecule.test.test
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test

class ClaimDetailPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private class FakeGetClaimDetailUiStateUseCase : GetClaimDetailUiStateUseCase {
    val emissions = Channel<Either<Error, ClaimDetailUiState.Content>>(Channel.UNLIMITED)

    override fun invoke(claimId: String): Flow<Either<Error, ClaimDetailUiState.Content>> = emissions.receiveAsFlow()
  }

  private class FakeClaimRatingUseCases : SubmitClaimRatingUseCase, DismissClaimRatingUseCase {
    val recorded = mutableListOf<Pair<String, ClaimRatingAnswer>>()
    val submitResults = Channel<Either<ErrorMessage, Unit>>(Channel.UNLIMITED)

    override suspend fun invoke(claimId: String, rating: Int, comment: String?): Either<ErrorMessage, Unit> {
      recorded.add(claimId to ClaimRatingAnswer.Rated(rating, comment))
      return submitResults.receive()
    }

    override suspend fun invoke(claimId: String): Either<ErrorMessage, Unit> {
      recorded.add(claimId to ClaimRatingAnswer.Dismissed)
      return Unit.right()
    }
  }

  private val getClaimDetailUiStateUseCase = FakeGetClaimDetailUiStateUseCase()
  private val recorder = FakeClaimRatingUseCases()
  private val presenter = ClaimDetailPresenter(
    claimId = "claim-1",
    getClaimDetailUiStateUseCase = getClaimDetailUiStateUseCase,
    claimsServiceUploadFileUseCase = object : ClaimsServiceUploadFileUseCase {
      override suspend fun invoke(url: String, uri: Uri): Either<ErrorMessage, UploadSuccess> = error("Not used")

      override suspend fun invoke(url: String, uris: List<Uri>): Either<ErrorMessage, UploadSuccess> = error("Not used")
    },
    downloadPdfUseCase = object : DownloadPdfUseCase {
      override suspend fun invoke(url: String): Either<ErrorMessage, DownloadedFile> = error("Not used")
    },
    submitClaimRatingUseCase = recorder,
    dismissClaimRatingUseCase = recorder,
  )

  @Test
  fun `a claim that is not closed has no rating button`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      assertThat(awaitContent(content(status = ClaimStatus.IN_PROGRESS)).ratingButton).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a closed partner claim has no rating button`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      assertThat(awaitContent(content(isPartnerClaim = true)).ratingButton).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a closed claim not yet rated has an enabled rating button`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      assertThat(awaitContent(content()).ratingButton).isEqualTo(RatingButtonState.Enabled)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a closed claim already rated has a submitted rating button`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      assertThat(awaitContent(content(hasSubmittedRating = true)).ratingButton).isEqualTo(RatingButtonState.Submitted)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `sending a rating shows a spinner until the backend confirms, then thanks and marks the button submitted`() =
    runTest {
      presenter.test(ClaimDetailUiState.Loading) {
        awaitContent(content())

        assertThat(sendRatingEvent(RatingEvent.OpenRequested).rating).isEqualTo(RatingState.Open(RatingStep.Stars))
        assertThat(sendRatingEvent(RatingEvent.StarSelected(4)).rating)
          .isEqualTo(RatingState.Open(RatingStep.Comment(4)))
        val whileSending = sendRatingEvent(RatingEvent.Send("Bra"))

        assertThat(whileSending.rating).isEqualTo(RatingState.Open(RatingStep.Comment(4, SendStatus.Sending)))
        assertThat(whileSending.ratingButton).isEqualTo(RatingButtonState.Enabled)
        assertThat(recorder.recorded).containsExactly("claim-1" to ClaimRatingAnswer.Rated(4, "Bra"))

        recorder.submitResults.send(Unit.right())
        val afterSend = awaitContentItem()

        assertThat(afterSend.rating).isEqualTo(RatingState.Open(RatingStep.ThankYou(4)))
        assertThat(afterSend.ratingButton).isEqualTo(RatingButtonState.Submitted)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `a failed send shows the error, keeps the button enabled and can be sent again`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      awaitContent(content())
      sendRatingEvent(RatingEvent.OpenRequested)
      sendRatingEvent(RatingEvent.StarSelected(2))
      sendRatingEvent(RatingEvent.Send("Slow"))

      recorder.submitResults.send(ErrorMessage().left())
      val afterFailure = awaitContentItem()

      assertThat(afterFailure.rating).isEqualTo(RatingState.Open(RatingStep.Comment(2, SendStatus.Failed)))
      assertThat(afterFailure.ratingButton).isEqualTo(RatingButtonState.Enabled)

      assertThat(sendRatingEvent(RatingEvent.Send("Slow")).rating)
        .isEqualTo(RatingState.Open(RatingStep.Comment(2, SendStatus.Sending)))
      recorder.submitResults.send(Unit.right())

      assertThat(awaitContentItem().rating).isEqualTo(RatingState.Open(RatingStep.ThankYou(2)))
      assertThat(recorder.recorded).containsExactly(
        "claim-1" to ClaimRatingAnswer.Rated(2, "Slow"),
        "claim-1" to ClaimRatingAnswer.Rated(2, "Slow"),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `closing the sheet while sending records no dismissal and a later success marks the button submitted`() =
    runTest {
      presenter.test(ClaimDetailUiState.Loading) {
        awaitContent(content())
        sendRatingEvent(RatingEvent.OpenRequested)
        sendRatingEvent(RatingEvent.StarSelected(5))
        sendRatingEvent(RatingEvent.Send(""))

        assertThat(sendRatingEvent(RatingEvent.SheetDismissed).rating).isEqualTo(RatingState.Closed)
        recorder.submitResults.send(Unit.right())

        val afterSuccess = awaitContentItem()
        assertThat(afterSuccess.rating).isEqualTo(RatingState.Closed)
        assertThat(afterSuccess.ratingButton).isEqualTo(RatingButtonState.Submitted)
        assertThat(recorder.recorded).containsExactly("claim-1" to ClaimRatingAnswer.Rated(5, null))
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `a send that was in flight when the presenter stopped can be sent again`() = runTest {
    val sending = content().copy(rating = RatingState.Open(RatingStep.Comment(3, SendStatus.Sending)))
    presenter.test(sending) {
      assertThat(awaitContentItem().rating).isEqualTo(RatingState.Open(RatingStep.Comment(3)))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `the button stays submitted when a refresh still says the claim is not rated`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      awaitContent(content())
      sendRatingEvent(RatingEvent.OpenRequested)
      sendRatingEvent(RatingEvent.StarSelected(2))
      sendRatingEvent(RatingEvent.Send(""))
      recorder.submitResults.send(Unit.right())
      awaitContentItem()

      getClaimDetailUiStateUseCase.emissions.send(content(hasUnreadMessages = true).right())

      assertThat(awaitContentItem().ratingButton).isEqualTo(RatingButtonState.Submitted)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `dismissing the stars step records a dismissal and keeps the button enabled`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      awaitContent(content())
      sendRatingEvent(RatingEvent.OpenRequested)
      val afterDismiss = sendRatingEvent(RatingEvent.SheetDismissed)

      assertThat(afterDismiss.rating).isEqualTo(RatingState.Closed)
      assertThat(afterDismiss.ratingButton).isEqualTo(RatingButtonState.Enabled)
      assertThat(recorder.recorded).containsExactly("claim-1" to ClaimRatingAnswer.Dismissed)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `the rating opens again after the stars step sheet is closed`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      awaitContent(content())
      sendRatingEvent(RatingEvent.OpenRequested)
      sendRatingEvent(RatingEvent.SheetDismissed)

      assertThat(sendRatingEvent(RatingEvent.OpenRequested).rating).isEqualTo(RatingState.Open(RatingStep.Stars))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `the rating opens again after skipping the stars step and the sheet reporting its close`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      awaitContent(content())
      sendRatingEvent(RatingEvent.OpenRequested)
      sendRatingEvent(RatingEvent.Skip)
      sendEvent(ClaimDetailsEvent.Rating(RatingEvent.SheetDismissed))

      val reopened = sendRatingEvent(RatingEvent.OpenRequested)

      assertThat(reopened.rating).isEqualTo(RatingState.Open(RatingStep.Stars))
      assertThat(reopened.ratingButton).isEqualTo(RatingButtonState.Enabled)
      assertThat(recorder.recorded).containsExactly("claim-1" to ClaimRatingAnswer.Dismissed)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `closing the comment step after picking a star records a dismissal, keeps the button enabled and reopens fresh`() =
    runTest {
      presenter.test(ClaimDetailUiState.Loading) {
        awaitContent(content())
        sendRatingEvent(RatingEvent.OpenRequested)
        sendRatingEvent(RatingEvent.StarSelected(4))
        val afterClose = sendRatingEvent(RatingEvent.SheetDismissed)

        assertThat(afterClose.rating).isEqualTo(RatingState.Closed)
        assertThat(afterClose.ratingButton).isEqualTo(RatingButtonState.Enabled)
        assertThat(recorder.recorded).containsExactly("claim-1" to ClaimRatingAnswer.Dismissed)
        assertThat(sendRatingEvent(RatingEvent.OpenRequested).rating).isEqualTo(RatingState.Open(RatingStep.Stars))
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `skipping the comment step after typing a comment records a dismissal and keeps the button enabled`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      awaitContent(content())
      sendRatingEvent(RatingEvent.OpenRequested)
      sendRatingEvent(RatingEvent.StarSelected(3))
      val afterSkip = sendRatingEvent(RatingEvent.Skip)

      assertThat(afterSkip.rating).isEqualTo(RatingState.Closed)
      assertThat(afterSkip.ratingButton).isEqualTo(RatingButtonState.Enabled)
      assertThat(recorder.recorded).containsExactly("claim-1" to ClaimRatingAnswer.Dismissed)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `the open sheet survives a second claim emission`() = runTest {
    presenter.test(ClaimDetailUiState.Loading) {
      awaitContent(content())
      sendRatingEvent(RatingEvent.OpenRequested)
      sendRatingEvent(RatingEvent.StarSelected(5))

      getClaimDetailUiStateUseCase.emissions.send(content(hasUnreadMessages = true).right())
      val afterPoll = awaitContentItem()

      assertThat(afterPoll.hasUnreadMessages).isEqualTo(true)
      assertThat(afterPoll.rating).isEqualTo(RatingState.Open(RatingStep.Comment(5)))
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun MoleculePresenterTestContext<ClaimDetailsEvent, ClaimDetailUiState>.awaitContent(
    content: ClaimDetailUiState.Content,
  ): ClaimDetailUiState.Content {
    assertThat(awaitItem()).isEqualTo(ClaimDetailUiState.Loading)
    getClaimDetailUiStateUseCase.emissions.send(content.right())
    return awaitContentItem()
  }

  private suspend fun MoleculePresenterTestContext<ClaimDetailsEvent, ClaimDetailUiState>.sendRatingEvent(
    event: RatingEvent,
  ): ClaimDetailUiState.Content {
    sendEvent(ClaimDetailsEvent.Rating(event))
    return awaitContentItem()
  }

  private suspend fun MoleculePresenterTestContext<ClaimDetailsEvent, ClaimDetailUiState>.awaitContentItem():
    ClaimDetailUiState.Content {
    val state = awaitItem()
    assertThat(state).isInstanceOf<ClaimDetailUiState.Content>()
    return state as ClaimDetailUiState.Content
  }

  private fun content(
    status: ClaimStatus = ClaimStatus.CLOSED,
    isPartnerClaim: Boolean = false,
    hasSubmittedRating: Boolean = false,
    hasUnreadMessages: Boolean = false,
  ) = ClaimDetailUiState.Content(
    claimId = "claim-1",
    conversationId = null,
    hasUnreadMessages = hasUnreadMessages,
    submittedContent = null,
    files = emptyList(),
    claimStatusCardUiState = ClaimStatusCardUiState(
      id = "claim-1",
      claimType = null,
      insuranceDisplayName = null,
      submittedDate = null,
      pillTypes = emptyList(),
      claimProgressItemsUiState = emptyList(),
    ),
    claimStatus = status,
    claimOutcome = ClaimOutcome.PAID,
    uploadUri = "",
    isUploadingFile = false,
    uploadError = null,
    claimType = null,
    submittedAt = LocalDateTime(2026, 1, 1, 12, 0),
    insuranceDisplayName = null,
    termsConditionsUrl = null,
    savedFileUri = null,
    downloadError = null,
    isLoadingPdf = null,
    appealInstructionsUrl = null,
    isUploadingFilesEnabled = false,
    infoText = null,
    displayItems = emptyList(),
    externalId = null,
    handlerEmail = null,
    isPartnerClaim = isPartnerClaim,
    hasSubmittedRating = hasSubmittedRating,
  )
}
