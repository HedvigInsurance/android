package com.hedvig.android.feature.claim.details.ui.rating

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.claim.details.data.ClaimRatingAnswer
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.Finish
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.OpenRequested
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.Send
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.SheetDismissed
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.Skip
import com.hedvig.android.feature.claim.details.ui.rating.RatingEvent.StarSelected
import com.hedvig.android.feature.claim.details.ui.rating.RatingState.Closed
import com.hedvig.android.feature.claim.details.ui.rating.RatingState.Open
import com.hedvig.android.feature.claim.details.ui.rating.RatingStep.Comment
import com.hedvig.android.feature.claim.details.ui.rating.RatingStep.Stars
import com.hedvig.android.feature.claim.details.ui.rating.RatingStep.ThankYou
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
    assertThat(reduceRating(Open(Stars), SheetDismissed("")))
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
  fun `send submits the rating and trimmed comment and shows thank you`() {
    assertThat(reduceRating(Open(Comment(4)), Send("  Snabbt \n")))
      .isEqualTo(RatingTransition(Open(ThankYou(4)), ClaimRatingAnswer.Rated(4, "Snabbt")))
  }

  @Test
  fun `send with a blank comment submits no comment`() {
    assertThat(reduceRating(Open(Comment(3)), Send("   ")))
      .isEqualTo(RatingTransition(Open(ThankYou(3)), ClaimRatingAnswer.Rated(3, null)))
  }

  @Test
  fun `skip on the comment step submits the rating without a comment and shows thank you`() {
    assertThat(reduceRating(Open(Comment(2)), Skip))
      .isEqualTo(RatingTransition(Open(ThankYou(2)), ClaimRatingAnswer.Rated(2, null)))
  }

  @Test
  fun `closing the sheet on the comment step submits the rating without the typed comment and closes`() {
    assertThat(reduceRating(Open(Comment(5)), SheetDismissed("Bra")))
      .isEqualTo(RatingTransition(Closed, ClaimRatingAnswer.Rated(5, null)))
  }

  @Test
  fun `finishing the thank you step closes without another answer`() {
    assertThat(reduceRating(Open(ThankYou(5)), Finish)).isEqualTo(RatingTransition(Closed, null))
  }

  @Test
  fun `closing the sheet on the thank you step closes without another answer`() {
    assertThat(reduceRating(Open(ThankYou(2)), SheetDismissed(""))).isEqualTo(RatingTransition(Closed, null))
  }

  @Test
  fun `sheet events while closed are ignored`() {
    listOf(StarSelected(3), Send("x"), Skip, SheetDismissed("x"), Finish).forEach { event ->
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
