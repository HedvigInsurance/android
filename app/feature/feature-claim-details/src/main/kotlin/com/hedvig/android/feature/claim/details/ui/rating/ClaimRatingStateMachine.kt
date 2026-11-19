package com.hedvig.android.feature.claim.details.ui.rating

import com.hedvig.android.feature.claim.details.data.ClaimRatingAnswer

internal sealed interface RatingState {
  data object Closed : RatingState

  data class Open(val step: RatingStep) : RatingState
}

internal sealed interface RatingStep {
  data object Stars : RatingStep

  data class Comment(val rating: Int, val sendStatus: SendStatus = SendStatus.NotSent) : RatingStep

  /** A [rating] of 4 or more shows the Trustpilot ask. */
  data class ThankYou(val rating: Int) : RatingStep
}

internal enum class SendStatus {
  NotSent,
  Sending,
  Failed,
}

internal sealed interface RatingEvent {
  data object OpenRequested : RatingEvent

  data class StarSelected(val rating: Int) : RatingEvent

  data class Send(val comment: String) : RatingEvent

  data object SendSucceeded : RatingEvent

  data object SendFailed : RatingEvent

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

    is RatingStep.Comment -> if (step.sendStatus == SendStatus.Sending) {
      when (event) {
        RatingEvent.SendSucceeded -> show(RatingStep.ThankYou(step.rating))
        RatingEvent.SendFailed -> show(step.copy(sendStatus = SendStatus.Failed))
        RatingEvent.SheetDismissed -> close()
        else -> null
      }
    } else {
      when (event) {
        is RatingEvent.StarSelected -> show(RatingStep.Comment(event.rating))

        is RatingEvent.Send -> show(
          step.copy(sendStatus = SendStatus.Sending),
          ClaimRatingAnswer.Rated(step.rating, event.comment.trim().ifEmpty { null }),
        )

        RatingEvent.Skip, RatingEvent.SheetDismissed -> close(ClaimRatingAnswer.Dismissed)

        else -> null
      }
    }

    is RatingStep.ThankYou -> when (event) {
      RatingEvent.Finish, RatingEvent.SheetDismissed -> close()
      else -> null
    }
  }
}

/** A send still in flight when the presenter stopped was cancelled with it, so the member can send again. */
internal fun RatingState.withInterruptedSendReset(): RatingState {
  val step = (this as? RatingState.Open)?.step as? RatingStep.Comment ?: return this
  if (step.sendStatus != SendStatus.Sending) return this
  return RatingState.Open(step.copy(sendStatus = SendStatus.NotSent))
}
