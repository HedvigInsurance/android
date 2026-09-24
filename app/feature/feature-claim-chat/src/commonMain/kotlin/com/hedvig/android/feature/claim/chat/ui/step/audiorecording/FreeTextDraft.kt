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
internal class FreeTextDraftState(initialValue: TextFieldValue = TextFieldValue()) {
  var value: TextFieldValue by mutableStateOf(initialValue)

  companion object {
    /**
     * A draft opened on [storedAnswer], with the caret after it rather than in front of it: an answer being
     * edited is there to be added to, and a field left to place its own selection would start at index 0.
     */
    fun seededFrom(storedAnswer: String): FreeTextDraftState =
      FreeTextDraftState(TextFieldValue(storedAnswer, TextRange(storedAnswer.length)))

    val Saver: Saver<FreeTextDraftState, Any> = listSaver(
      save = { listOf(it.value.text, it.value.selection.start, it.value.selection.end) },
      restore = { FreeTextDraftState(TextFieldValue(it[0] as String, TextRange(it[1] as Int, it[2] as Int))) },
    )
  }
}

/**
 * [sessionId] names the step being answered while the editor is open, and is null whenever it is not, which
 * makes it exactly the thing a draft may not outlive.
 *
 * A session is one visit to the free text editor. Keying the draft on it draws the line in one place: an
 * unchanged session is remembered through a configuration change, which is the rotation the two cards cannot
 * survive on their own, while any change to it re-runs the factory and opens on the answer the step itself
 * holds. Leaving the editor is such a change, so a draft that was cancelled is gone before the same step can
 * be opened again, and so is moving on, so the next step never opens on the previous one's answer.
 */
@Composable
internal fun rememberFreeTextDraftState(sessionId: String?, storedAnswer: String): FreeTextDraftState =
  rememberSaveable(sessionId, saver = FreeTextDraftState.Saver) {
    FreeTextDraftState.seededFrom(storedAnswer)
  }
