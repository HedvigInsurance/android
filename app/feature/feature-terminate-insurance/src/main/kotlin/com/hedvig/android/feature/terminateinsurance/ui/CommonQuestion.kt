package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.hedvig.android.core.ui.card.ExpandablePlusCard

@Composable
internal fun CommonQuestion(question: String, answer: String, isExpanded: Boolean, onClick: () -> Unit) {
  ExpandablePlusCard(
    isExpanded = isExpanded,
    onClick = onClick,
    content = {
      Text(
        text = question,
        modifier = Modifier.weight(1f, true),
      )
    },
    expandedContent = {
      RichText(modifier = Modifier.padding(horizontal = 12.dp)) {
        Markdown(content = answer)
      }
    },
  )
}
