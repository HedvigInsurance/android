package com.hedvig.android.feature.claim.chat.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * One answer the member has already given, placed the way the transcript places every one of them.
 *
 * Whatever the answer is made of, a pill, a voice player, a "Skipped" label, it ends flush with the content
 * edge. Content that caps its own width has to take its alignment from here: the audio player stops growing
 * at 500dp, so on anything wider than a phone in portrait it would otherwise settle against the start edge
 * while every text answer above it sits against the end edge.
 *
 * The start padding keeps a long answer short of the full width, so it still reads as one side of a
 * conversation rather than as the conversation's own text.
 */
@Composable
internal fun SentAnswerRow(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Column(
    modifier = modifier.fillMaxWidth().padding(start = SENT_ANSWER_START_PADDING),
    horizontalAlignment = Alignment.End,
    content = content,
  )
}

private val SENT_ANSWER_START_PADDING = 48.dp
