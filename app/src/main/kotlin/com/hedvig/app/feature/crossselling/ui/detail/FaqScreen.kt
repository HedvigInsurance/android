package com.hedvig.app.feature.crossselling.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.app.R
import com.hedvig.app.feature.faq.FAQCard
import com.hedvig.app.feature.faq.FAQItem

@Composable
fun FaqScreen(
  onUpClick: () -> Unit,
  openSheet: (FAQItem) -> Unit,
  openChat: () -> Unit,
  onCtaClick: () -> Unit,
  items: List<FAQItem>,
) {
  Box {
    Column(
      modifier = Modifier.fillMaxSize(),
    ) {
      TopAppBarWithBack(
        onClick = onUpClick,
        title = stringResource(hedvig.resources.R.string.cross_sell_info_common_questions_title),
      )
      Column(
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp),
      ) {
        Spacer(Modifier.height(16.dp))
        FAQCard(
          openSheet = openSheet,
          items = items,
        )
        Spacer(Modifier.height(40.dp))
        Text(
          text = stringResource(hedvig.resources.R.string.cross_sell_info_faq_chat_headline),
          style = MaterialTheme.typography.subtitle2,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(16.dp))
        LargeOutlinedButton(onClick = openChat) {
          Image(
            painter = painterResource(R.drawable.ic_chat_black),
            contentDescription = null,
          )
          Spacer(Modifier.width(8.dp))
          Text(
            text = stringResource(hedvig.resources.R.string.cross_sell_info_faq_chat_button),
          )
        }
        Spacer(Modifier.height(104.dp))
      }
    }
    LargeContainedButton(
      onClick = onCtaClick,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
      Text(text = stringResource(id = hedvig.resources.R.string.cross_selling_card_se_accident_cta))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFaqScreen() {
  HedvigTheme {
    Surface {
      FaqScreen(
        onUpClick = {},
        openSheet = {},
        openChat = {},
        onCtaClick = {},
        items = listOf(
          FAQItem(
            headline = "What is included in my home insurance?",
            body = "",
          ),
          FAQItem(
            headline = "Why should I choose Hedvig?",
            body = "",
          ),
          FAQItem(
            headline = "Can I get Hedvig even though I already have an insurance policy?",
            body = "",
          ),
          FAQItem(
            headline = "How do I pay for my insurance?",
            body = "",
          ),
          FAQItem(
            headline = "How do I make a claim?",
            body = "",
          ),
        ),
      )
    }
  }
}
