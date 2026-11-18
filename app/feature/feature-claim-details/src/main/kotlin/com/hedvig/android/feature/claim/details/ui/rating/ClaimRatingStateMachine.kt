package com.hedvig.android.feature.claim.details.ui.rating

import com.hedvig.android.data.cross.sell.after.claim.closed.ClosedClaimInfo
import com.hedvig.android.feature.claim.details.data.ClaimClosedAnswer

internal data class ClosedClaimFlow(val claim: ClosedClaimInfo, val askForRating: Boolean)

internal enum class ExitVia { TopBar, SystemBack }

internal sealed interface RatingState {
  data object Idle : RatingState

  data class Asking(val step: RatingStep, val via: ExitVia) : RatingState

  /** The destination pops the screen the way [via] says, then sends ExitHandled. */
  data class Leaving(val via: ExitVia) : RatingState
}

internal sealed interface RatingStep {
  data object Stars : RatingStep

  data class Comment(val rating: Int, val comment: String) : RatingStep

  /** A [rating] of 4 or more shows the Trustpilot ask. */
  data class ThankYou(val rating: Int) : RatingStep
}

internal sealed interface RatingEvent {
  data class ExitRequested(val via: ExitVia) : RatingEvent

  data class StarSelected(val rating: Int) : RatingEvent

  data class CommentChanged(val text: String) : RatingEvent

  data object Send : RatingEvent

  data object Skip : RatingEvent

  data object SheetDismissed : RatingEvent

  /** "Fortsätt", "Nej tack" or the Trustpilot button on the thank you step. */
  data object Finish : RatingEvent
}

/** [answer] is non-null when the event ends the member's answer and it must be recorded. */
internal data class RatingTransition(val state: RatingState, val answer: ClaimClosedAnswer?)

internal fun reduceRating(state: RatingState, event: RatingEvent, flow: ClosedClaimFlow?): RatingTransition {
  val unchanged = RatingTransition(state, null)
  return when (state) {
    RatingState.Idle -> {
      if (event !is RatingEvent.ExitRequested) return unchanged
      when {
        flow == null -> RatingTransition(RatingState.Leaving(event.via), null)
        !flow.askForRating -> RatingTransition(RatingState.Leaving(event.via), ClaimClosedAnswer.NotAsked)
        else -> RatingTransition(RatingState.Asking(RatingStep.Stars, event.via), null)
      }
    }

    is RatingState.Asking -> {
      reduceAsking(state, event) ?: unchanged
    }

    is RatingState.Leaving -> {
      unchanged
    }
  }
}

private fun reduceAsking(state: RatingState.Asking, event: RatingEvent): RatingTransition? {
  fun ask(step: RatingStep, answer: ClaimClosedAnswer? = null) = RatingTransition(state.copy(step = step), answer)

  fun leave(answer: ClaimClosedAnswer? = null) = RatingTransition(RatingState.Leaving(state.via), answer)

  return when (val step = state.step) {
    RatingStep.Stars -> when (event) {
      is RatingEvent.StarSelected -> ask(RatingStep.Comment(event.rating, ""))
      RatingEvent.Skip, RatingEvent.SheetDismissed -> leave(ClaimClosedAnswer.Dismissed)
      else -> null
    }

    is RatingStep.Comment -> when (event) {
      is RatingEvent.StarSelected -> ask(step.copy(rating = event.rating))

      is RatingEvent.CommentChanged -> ask(step.copy(comment = event.text))

      RatingEvent.Send -> ask(
        RatingStep.ThankYou(step.rating),
        ClaimClosedAnswer.Rated(step.rating, step.comment.trim().ifEmpty { null }),
      )

      RatingEvent.Skip -> ask(RatingStep.ThankYou(step.rating), ClaimClosedAnswer.Rated(step.rating, null))

      RatingEvent.SheetDismissed -> leave(ClaimClosedAnswer.Rated(step.rating, null))

      else -> null
    }

    is RatingStep.ThankYou -> when (event) {
      RatingEvent.Finish, RatingEvent.SheetDismissed -> leave()
      else -> null
    }
  }
}
