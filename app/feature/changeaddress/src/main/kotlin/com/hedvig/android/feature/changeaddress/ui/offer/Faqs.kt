package com.hedvig.android.feature.changeaddress.ui.offer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
internal fun Faqs(
  modifier: Modifier = Modifier,
) {
  val faqItems = listOf(
    // todo get faq items from inside ui state
    "Vad ingår i en hemförsäkring?",
    "Måste man ha hemförsäkring?",
    "Vad kostar en hemförsäkring?",
    "Vem gäller hemförsäkringen för?",
    "Vad betyder lösöre?",
  )
  var expandedItemIndex by rememberSaveable { mutableStateOf(-1) }
  Column(modifier) {
    Text(
      text = "Frågor och svar",
      style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.height(24.dp))
    faqItems.forEachIndexed { index, faqText ->
      FaqItem(
        faqText = faqText,
        onClick = {
          if (expandedItemIndex == index) {
            expandedItemIndex = -1
          } else {
            expandedItemIndex = index
          }
        },
        isExpanded = expandedItemIndex == index,
      )
      if (index != faqItems.lastIndex) {
        Spacer(Modifier.height(4.dp))
      }
    }
  }
}

@Composable
private fun FaqItem(
  faqText: String,
  onClick: () -> Unit,
  isExpanded: Boolean,
) {
  ExpandablePlusRow(
    isExpanded = isExpanded,
    onClick = onClick,
    content = {
      Text(
        text = faqText,
        fontSize = 18.sp,
        modifier = Modifier.weight(1f, true),
      )
    },
    expandedContent = {
      Text("Information about $faqText. ${"Lorem Ipsum".repeat(15)}")
    },
  )
}

@HedvigPreview
@Composable
private fun PreviewFaqItem() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      var isExpanded by remember { mutableStateOf(false) }
      FaqItem(
        faqText = "Reseförsäkring",
        onClick = { isExpanded = !isExpanded },
        isExpanded = isExpanded,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFaqs() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      Faqs()
    }
  }
}
