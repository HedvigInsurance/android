package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
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
      Box(Modifier.fillMaxWidth()) {
        RichText(modifier = Modifier.padding(horizontal = 12.dp)) {
          Markdown(content = answer)
        }
      }
    },
  )
}

@HedvigPreview
@Composable
private fun PreviewCommonQuestion() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      var isExpanded by remember { mutableStateOf(false) }
      CommonQuestion("Question".repeat(2), "Answer".repeat(10), isExpanded) {
        isExpanded = !isExpanded
      }
    }
  }
}
