package com.hedvig.android.feature.claim.details.ui.rating

import com.hedvig.android.feature.claim.details.data.ClaimRatingAnswer

internal sealed interface RatingState {
  data object Closed : RatingState

  data class Open(val step: RatingStep) : RatingState
}

internal sealed interface RatingStep {
  data object Stars : RatingStep

  data class Comment(val rating: Int) : RatingStep

  /** A [rating] of 4 or more shows the Trustpilot ask. */
  data class ThankYou(val rating: Int) : RatingStep
}

internal sealed interface RatingEvent {
  data object OpenRequested : RatingEvent

  data class StarSelected(val rating: Int) : RatingEvent

  data class Send(val comment: String) : RatingEvent

  data object Skip : RatingEvent

  data object SheetDismissed : RatingEvent

  /** "Fortsätt", "Nej tack" or the Trustpilot button on the thank you step. */
  data object Finish : RatingEvent
}

/** [answer] is non-null when the event ends the member's answer and it must be recorded. */
internal data class RatingTransition(val state: RatingState, val answer: ClaimRatingAnswer?)

internal fun reduceRating(state: RatingState, event: RatingEvent): RatingTransition {
  val unchanged = RatingTransition(state, null)
  return when (state) {
    RatingState.Closed -> if (event == RatingEvent.OpenRequested) {
      RatingTransition(RatingState.Open(RatingStep.Stars), null)
    } else {
      unchanged
    }

    is RatingState.Open -> reduceOpen(state.step, event) ?: unchanged
  }
}

private fun reduceOpen(step: RatingStep, event: RatingEvent): RatingTransition? {
  fun show(step: RatingStep, answer: ClaimRatingAnswer? = null) = RatingTransition(RatingState.Open(step), answer)

  fun close(answer: ClaimRatingAnswer? = null) = RatingTransition(RatingState.Closed, answer)

  return when (step) {
    RatingStep.Stars -> when (event) {
      is RatingEvent.StarSelected -> show(RatingStep.Comment(event.rating))
      RatingEvent.Skip, RatingEvent.SheetDismissed -> close(ClaimRatingAnswer.Dismissed)
      else -> null
    }

    is RatingStep.Comment -> when (event) {
      is RatingEvent.StarSelected -> show(RatingStep.Comment(event.rating))

      is RatingEvent.Send -> show(
        RatingStep.ThankYou(step.rating),
        ClaimRatingAnswer.Rated(step.rating, event.comment.trim().ifEmpty { null }),
      )

      RatingEvent.Skip -> show(RatingStep.ThankYou(step.rating), ClaimRatingAnswer.Rated(step.rating, null))

      RatingEvent.SheetDismissed -> close(ClaimRatingAnswer.Rated(step.rating, null))

      else -> null
    }

    is RatingStep.ThankYou -> when (event) {
      RatingEvent.Finish, RatingEvent.SheetDismissed -> close()
      else -> null
    }
  }
}
