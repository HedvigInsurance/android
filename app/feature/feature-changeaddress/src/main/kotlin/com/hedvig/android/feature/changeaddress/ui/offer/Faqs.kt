package com.hedvig.android.feature.changeaddress.ui.offer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.card.ExpandablePlusCard
import hedvig.resources.R

@Composable
internal fun Faqs(faqItems: List<Pair<String, String>>, modifier: Modifier = Modifier) {
  var expandedItemIndex by rememberSaveable { mutableIntStateOf(-1) }
  Column(modifier) {
    Text(
      text = stringResource(id = R.string.CHANGE_ADDRESS_QA),
      style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.height(24.dp))
    faqItems.mapIndexed { index, faq ->
      FaqItem(
        faqDisplayName = faq.first,
        faqText = faq.second,
        onClick = {
          expandedItemIndex = if (expandedItemIndex == index) {
            -1
          } else {
            index
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
private fun FaqItem(faqDisplayName: String, faqText: String, onClick: () -> Unit, isExpanded: Boolean) {
  ExpandablePlusCard(
    isExpanded = isExpanded,
    onClick = onClick,
    titleText = faqDisplayName,
    expandedText = faqText,
  )
}

@HedvigPreview
@Composable
private fun PreviewFaqItem() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      var isExpanded by remember { mutableStateOf(true) }
      @Suppress("ktlint:standard:max-line-length")
      FaqItem(
        faqText = "ReseförsäkringReseförs äkring Rese försäkring Res eförsäkr inResefö rsäkrin ReseförsäkringReseförsäkringReseförsäkrin",
        onClick = { isExpanded = !isExpanded },
        isExpanded = isExpanded,
        faqDisplayName = "Test",
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFaqs() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Faqs(
        faqItems = listOf("test" to "testbody"),
      )
    }
  }
}
