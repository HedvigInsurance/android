package com.hedvig.android.feature.claim.details.ui.rating

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.claim.details.data.ClaimRatingAnswer
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.Finish
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.OpenRequested
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.Send
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.SendFailed
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.SendSucceeded
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.SheetDismissed
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.Skip
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.StarSelected
import com.hedvig.android.feature.claim.details.ui.rating.RatingState.Closed
import com.hedvig.android.feature.claim.details.ui.rating.RatingState.Open
import com.hedvig.android.feature.claim.details.ui.rating.RatingStep.Comment
import com.hedvig.android.feature.claim.details.ui.rating.RatingStep.Stars
import com.hedvig.android.feature.claim.details.ui.rating.RatingStep.ThankYou
import com.hedvig.android.feature.claim.details.ui.rating.SendStatus.Failed
import com.hedvig.android.feature.claim.details.ui.rating.SendStatus.Sending
import org.junit.Test

class ClaimRatingStateMachineTest {
  @Test
  fun `opening the rating shows the stars step with no answer`() {
    assertThat(reduceRating(Closed, OpenRequested)).isEqualTo(RatingTransition(Open(Stars), null))
  }

  @Test
  fun `skip on the stars step dismisses and closes`() {
    assertThat(reduceRating(Open(Stars), Skip)).isEqualTo(RatingTransition(Closed, ClaimRatingAnswer.Dismissed))
  }

  @Test
  fun `closing the sheet on the stars step dismisses and closes`() {
    assertThat(reduceRating(Open(Stars), SheetDismissed))
      .isEqualTo(RatingTransition(Closed, ClaimRatingAnswer.Dismissed))
  }

  @Test
  fun `picking a star moves to the comment step`() {
    assertThat(reduceRating(Open(Stars), StarSelected(4))).isEqualTo(RatingTransition(Open(Comment(4)), null))
  }

  @Test
  fun `changing the star on the comment step stays on the comment step`() {
    assertThat(reduceRating(Open(Comment(4)), StarSelected(2))).isEqualTo(RatingTransition(Open(Comment(2)), null))
  }

  @Test
  fun `send submits the rating and trimmed comment and waits on the comment step`() {
    assertThat(reduceRating(Open(Comment(4)), Send("  Snabbt \n")))
      .isEqualTo(RatingTransition(Open(Comment(4, Sending)), ClaimRatingAnswer.Rated(4, "Snabbt")))
  }

  @Test
  fun `send with a blank comment submits no comment`() {
    assertThat(reduceRating(Open(Comment(3)), Send("   ")))
      .isEqualTo(RatingTransition(Open(Comment(3, Sending)), ClaimRatingAnswer.Rated(3, null)))
  }

  @Test
  fun `a successful send shows thank you`() {
    assertThat(reduceRating(Open(Comment(4, Sending)), SendSucceeded))
      .isEqualTo(RatingTransition(Open(ThankYou(4)), null))
  }

  @Test
  fun `a failed send stays on the comment step showing the failure`() {
    assertThat(reduceRating(Open(Comment(4, Sending)), SendFailed))
      .isEqualTo(RatingTransition(Open(Comment(4, Failed)), null))
  }

  @Test
  fun `send after a failure submits again`() {
    assertThat(reduceRating(Open(Comment(4, Failed)), Send("x")))
      .isEqualTo(RatingTransition(Open(Comment(4, Sending)), ClaimRatingAnswer.Rated(4, "x")))
  }

  @Test
  fun `changing the star after a failure clears the failure`() {
    assertThat(reduceRating(Open(Comment(4, Failed)), StarSelected(5)))
      .isEqualTo(RatingTransition(Open(Comment(5)), null))
  }

  @Test
  fun `closing the sheet while sending closes without a dismissal`() {
    assertThat(reduceRating(Open(Comment(4, Sending)), SheetDismissed)).isEqualTo(RatingTransition(Closed, null))
  }

  @Test
  fun `member input while sending is ignored`() {
    listOf(StarSelected(2), Send("x"), Skip, Finish).forEach { event ->
      val sending = Open(Comment(4, Sending))
      assertThat(reduceRating(sending, event)).isEqualTo(RatingTransition(sending, null))
    }
  }

  @Test
  fun `send results outside of a send are ignored`() {
    listOf(Closed, Open(Stars), Open(Comment(4)), Open(Comment(4, Failed)), Open(ThankYou(4))).forEach { state ->
      listOf(SendSucceeded, SendFailed).forEach { event ->
        assertThat(reduceRating(state, event)).isEqualTo(RatingTransition(state, null))
      }
    }
  }

  @Test
  fun `an interrupted send is reset so it can be sent again`() {
    assertThat(Open(Comment(4, Sending)).withInterruptedSendReset()).isEqualTo(Open(Comment(4)))
  }

  @Test
  fun `resetting an interrupted send leaves other states untouched`() {
    listOf(Closed, Open(Stars), Open(Comment(4, Failed)), Open(ThankYou(4))).forEach { state ->
      assertThat(state.withInterruptedSendReset()).isEqualTo(state)
    }
  }

  @Test
  fun `skip on the comment step dismisses without submitting the rating and closes`() {
    assertThat(reduceRating(Open(Comment(2)), Skip)).isEqualTo(RatingTransition(Closed, ClaimRatingAnswer.Dismissed))
  }

  @Test
  fun `closing the sheet on the comment step dismisses without submitting the rating and closes`() {
    assertThat(reduceRating(Open(Comment(5)), SheetDismissed))
      .isEqualTo(RatingTransition(Closed, ClaimRatingAnswer.Dismissed))
  }

  @Test
  fun `finishing the thank you step closes without another answer`() {
    assertThat(reduceRating(Open(ThankYou(5)), Finish)).isEqualTo(RatingTransition(Closed, null))
  }

  @Test
  fun `closing the sheet on the thank you step closes without another answer`() {
    assertThat(reduceRating(Open(ThankYou(2)), SheetDismissed)).isEqualTo(RatingTransition(Closed, null))
  }

  @Test
  fun `sheet events while closed are ignored`() {
    listOf(StarSelected(3), Send("x"), Skip, SheetDismissed, Finish).forEach { event ->
      assertThat(reduceRating(Closed, event)).isEqualTo(RatingTransition(Closed, null))
    }
  }

  @Test
  fun `opening while the sheet is open is ignored`() {
    listOf(Stars, Comment(4), ThankYou(4)).forEach { step ->
      assertThat(reduceRating(Open(step), OpenRequested)).isEqualTo(RatingTransition(Open(step), null))
    }
  }

  @Test
  fun `events that do not belong to the current step are ignored`() {
    listOf(
      Open(Stars) to Send("x"),
      Open(Stars) to Finish,
      Open(Comment(4)) to Finish,
      Open(ThankYou(4)) to StarSelected(2),
      Open(ThankYou(4)) to Send("x"),
      Open(ThankYou(4)) to Skip,
    ).forEach { (state, event) ->
      assertThat(reduceRating(state, event)).isEqualTo(RatingTransition(state, null))
    }
  }
}
