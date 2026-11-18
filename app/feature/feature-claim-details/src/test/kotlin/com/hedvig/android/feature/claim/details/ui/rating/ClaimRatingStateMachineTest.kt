package com.hedvig.android.feature.claim.details.ui.rating

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.data.cross.sell.after.claim.closed.ClosedClaimInfo
import com.hedvig.android.feature.claim.details.data.ClaimClosedAnswer
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.CommentChanged
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.ExitRequested
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.Finish
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.Send
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.SheetDismissed
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.Skip
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.StarSelected
import com.hedvig.android.feature.claim.details.ui.rating.RatingState.Asking
import com.hedvig.android.feature.claim.details.ui.rating.RatingState.Idle
import com.hedvig.android.feature.claim.details.ui.rating.RatingState.Leaving
import com.hedvig.android.feature.claim.details.ui.rating.RatingStep.Comment
import com.hedvig.android.feature.claim.details.ui.rating.RatingStep.Stars
import com.hedvig.android.feature.claim.details.ui.rating.RatingStep.ThankYou
import org.junit.Test

class ClaimRatingStateMachineTest {
  private val claim = ClosedClaimInfo("claim-1", "CLOSED", "THEFT", "SE_APARTMENT_RENT", "contract-1")
  private val eligible = ClosedClaimFlow(claim, askForRating = true)
  private val alreadyRated = ClosedClaimFlow(claim, askForRating = false)

  @Test
  fun `exit without a closed claim flow leaves immediately with no answer`() {
    assertThat(reduceRating(Idle, ExitRequested(ExitVia.SystemBack), null))
      .isEqualTo(RatingTransition(Leaving(ExitVia.SystemBack), null))
  }

  @Test
  fun `exit on a closed claim already rated leaves and records NotAsked`() {
    assertThat(reduceRating(Idle, ExitRequested(ExitVia.SystemBack), alreadyRated))
      .isEqualTo(RatingTransition(Leaving(ExitVia.SystemBack), ClaimClosedAnswer.NotAsked))
  }

  @Test
  fun `exit on an eligible claim opens the stars step`() {
    assertThat(reduceRating(Idle, ExitRequested(ExitVia.SystemBack), eligible))
      .isEqualTo(RatingTransition(Asking(Stars, ExitVia.SystemBack), null))
  }

  @Test
  fun `skip on the stars step dismisses and leaves`() {
    assertThat(reduceRating(Asking(Stars, ExitVia.SystemBack), Skip, eligible))
      .isEqualTo(RatingTransition(Leaving(ExitVia.SystemBack), ClaimClosedAnswer.Dismissed))
  }

  @Test
  fun `closing the sheet on the stars step dismisses and leaves`() {
    assertThat(reduceRating(Asking(Stars, ExitVia.SystemBack), SheetDismissed, eligible))
      .isEqualTo(RatingTransition(Leaving(ExitVia.SystemBack), ClaimClosedAnswer.Dismissed))
  }

  @Test
  fun `picking a star moves to the comment step`() {
    assertThat(reduceRating(Asking(Stars, ExitVia.SystemBack), StarSelected(4), eligible))
      .isEqualTo(RatingTransition(Asking(Comment(4, ""), ExitVia.SystemBack), null))
  }

  @Test
  fun `changing the star on the comment step keeps the comment`() {
    assertThat(reduceRating(Asking(Comment(4, "Snabbt"), ExitVia.SystemBack), StarSelected(2), eligible))
      .isEqualTo(RatingTransition(Asking(Comment(2, "Snabbt"), ExitVia.SystemBack), null))
  }

  @Test
  fun `typing on the comment step updates the comment without an answer`() {
    assertThat(reduceRating(Asking(Comment(4, ""), ExitVia.SystemBack), CommentChanged("Sna"), eligible))
      .isEqualTo(RatingTransition(Asking(Comment(4, "Sna"), ExitVia.SystemBack), null))
  }

  @Test
  fun `send submits the rating and trimmed comment and shows thank you`() {
    assertThat(reduceRating(Asking(Comment(4, "  Snabbt \n"), ExitVia.SystemBack), Send, eligible))
      .isEqualTo(RatingTransition(Asking(ThankYou(4), ExitVia.SystemBack), ClaimClosedAnswer.Rated(4, "Snabbt")))
  }

  @Test
  fun `send with a blank comment submits null`() {
    assertThat(reduceRating(Asking(Comment(3, "   "), ExitVia.SystemBack), Send, eligible))
      .isEqualTo(RatingTransition(Asking(ThankYou(3), ExitVia.SystemBack), ClaimClosedAnswer.Rated(3, null)))
  }

  @Test
  fun `skip on the comment step submits the rating without a comment`() {
    assertThat(reduceRating(Asking(Comment(2, "Långsamt"), ExitVia.SystemBack), Skip, eligible))
      .isEqualTo(RatingTransition(Asking(ThankYou(2), ExitVia.SystemBack), ClaimClosedAnswer.Rated(2, null)))
  }

  @Test
  fun `closing the sheet on the comment step submits the rating and leaves`() {
    assertThat(reduceRating(Asking(Comment(5, "x"), ExitVia.SystemBack), SheetDismissed, eligible))
      .isEqualTo(RatingTransition(Leaving(ExitVia.SystemBack), ClaimClosedAnswer.Rated(5, null)))
  }

  @Test
  fun `finishing or closing the thank you step leaves without another answer`() {
    assertThat(reduceRating(Asking(ThankYou(5), ExitVia.SystemBack), Finish, eligible))
      .isEqualTo(RatingTransition(Leaving(ExitVia.SystemBack), null))
    assertThat(reduceRating(Asking(ThankYou(2), ExitVia.SystemBack), SheetDismissed, eligible))
      .isEqualTo(RatingTransition(Leaving(ExitVia.SystemBack), null))
  }

  @Test
  fun `exit requested while asking is ignored`() {
    listOf(Stars, Comment(4, "x"), ThankYou(4)).forEach { step ->
      val asking = Asking(step, ExitVia.TopBar)
      assertThat(reduceRating(asking, ExitRequested(ExitVia.SystemBack), eligible))
        .isEqualTo(RatingTransition(asking, null))
    }
  }

  @Test
  fun `events while leaving are ignored`() {
    val leaving = Leaving(ExitVia.SystemBack)
    listOf(ExitRequested(ExitVia.TopBar), StarSelected(3), CommentChanged("x"), Send, Skip, SheetDismissed, Finish)
      .forEach { event ->
        assertThat(reduceRating(leaving, event, eligible)).isEqualTo(RatingTransition(leaving, null))
      }
  }

  @Test
  fun `sheet events while idle are ignored`() {
    listOf(StarSelected(3), CommentChanged("x"), Send, Skip, SheetDismissed, Finish).forEach { event ->
      assertThat(reduceRating(Idle, event, eligible)).isEqualTo(RatingTransition(Idle, null))
    }
  }

  @Test
  fun `the original exit via is kept through the flow`() {
    val events = listOf(ExitRequested(ExitVia.TopBar), StarSelected(5), Send, Finish)
    val finalState = events.fold<RatingEvent, RatingState>(Idle) { state, event ->
      reduceRating(state, event, eligible).state
    }
    assertThat(finalState).isEqualTo(Leaving(ExitVia.TopBar))
  }
}
