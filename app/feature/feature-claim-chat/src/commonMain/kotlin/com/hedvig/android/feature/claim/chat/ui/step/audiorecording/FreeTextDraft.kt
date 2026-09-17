package com.hedvig.android.feature.claim.chat.ui.step.audiorecording

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * The answer the member is writing, held above both of the cards that can draw it.
 *
 * The inline card and the full screen editor are separate subtrees, and which one is on screen follows the
 * window's height, so a rotation swaps one for the other and disposes whichever was showing. State held
 * inside either card is lost at that moment however it is remembered, so the draft lives in the screen that
 * outlives both of them. It travels as a [TextFieldValue] rather than a [String] so the caret stays where the
 * member left it: a field composed from text alone starts its own selection at the beginning, which would put
 * the next keystroke in front of the answer.
 */
@Stable
internal class FreeTextDraftState(
  initialValue: TextFieldValue = TextFieldValue(),
  initialSessionId: String? = null,
) {
  var value: TextFieldValue by mutableStateOf(initialValue)

  /** The session the draft in hand belongs to, so a later one is not handed the previous answer. */
  var sessionId: String? by mutableStateOf(initialSessionId)
    private set

  fun syncToSession(sessionId: String?, storedAnswer: String) {
    val seed = freeTextDraftSeed(sessionId, this.sessionId, storedAnswer)
    if (seed is FreeTextDraftSeed.Replace) {
      // The caret goes after the seeded answer: an edited answer is there to be added to, and a field left
      // to place its own would put the next keystroke in front of it.
      value = TextFieldValue(seed.text, TextRange(seed.text.length))
    }
    this.sessionId = sessionId
  }

  companion object {
    val Saver: Saver<FreeTextDraftState, Any> = listSaver(
      save = { listOf(it.value.text, it.value.selection.start, it.value.selection.end, it.sessionId) },
      restore = {
        FreeTextDraftState(
          initialValue = TextFieldValue(it[0] as String, TextRange(it[1] as Int, it[2] as Int)),
          initialSessionId = it[3] as String?,
        )
      },
    )
  }
}

/** What a draft should hold now that the editor is open for [activeSessionId]. */
internal sealed interface FreeTextDraftSeed {
  /** The draft in hand belongs to this session, so whatever has been typed stands. */
  data object Keep : FreeTextDraftSeed

  data class Replace(val text: String) : FreeTextDraftSeed
}

/**
 * A session is one visit to the free text editor, named by the step being answered. It outlives the card
 * drawing it, so the answer is only ever seeded at its start: from the answer the step already holds, which
 * is what an edited or resumed step comes back with. Leaving the editor ends the session and drops the draft,
 * so the next step does not open on the previous one's answer.
 */
internal fun freeTextDraftSeed(
  activeSessionId: String?,
  seededSessionId: String?,
  storedAnswer: String,
): FreeTextDraftSeed = when {
  activeSessionId == seededSessionId -> FreeTextDraftSeed.Keep
  activeSessionId == null -> FreeTextDraftSeed.Replace("")
  else -> FreeTextDraftSeed.Replace(storedAnswer)
}

/**
 * [sessionId] names the step being answered while the editor is open, and is null whenever it is not.
 */
@Composable
internal fun rememberFreeTextDraftState(sessionId: String?, storedAnswer: String): FreeTextDraftState {
  val state = rememberSaveable(saver = FreeTextDraftState.Saver) { FreeTextDraftState() }
  // Seeded during composition rather than from an effect: both cards read the draft in this same pass, and an
  // effect would let them draw the previous session's answer for a frame first. Every call after the first of
  // a session is a no-op, so this settles rather than feeding back into another composition.
  state.syncToSession(sessionId, storedAnswer)
  return state
}
