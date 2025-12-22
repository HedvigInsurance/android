package com.hedvig.feature.claim.chat.ui.outcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.feature.claim.chat.data.StepContent

@Composable
internal fun ClaimOutcomeDeflectDestination(
  deflect: StepContent.Deflect,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
) {
  HedvigScaffold(
    topAppBarText = deflect.title ?: "",
    navigateUp = navigateUp,
    modifier = modifier,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(8.dp))
      if (deflect.infoText != null) {
        HedvigNotificationCard(
          message = deflect.infoText,
          modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
          style = NotificationDefaults.InfoCardStyle.Default,
          priority = NotificationDefaults.NotificationPriority.Attention,
        )
        Spacer(Modifier.height(8.dp))
      }
      if (deflect.warningText != null) {
        HedvigNotificationCard(
          message = deflect.warningText,
          modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
          style = NotificationDefaults.InfoCardStyle.Default,
          priority = NotificationDefaults.NotificationPriority.Attention,
        )
        Spacer(Modifier.height(8.dp))
      }
      deflect.partners.forEach { partner ->
        HedvigTheme(darkTheme = true) {
          Surface(
            color = HedvigTheme.colorScheme.backgroundPrimary.copy(
              0.95f,
            ).compositeOver(HedvigTheme.colorScheme.fillWhite),
            contentColor = HedvigTheme.colorScheme.fillPrimary,
            shape = HedvigTheme.shapes.cornerXLarge,
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .fillMaxWidth(),
          ) {
            Column(Modifier.padding(16.dp)) {
              Spacer(Modifier.height(16.dp))
              Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                if (partner.imageUrl != null) {
                  AsyncImage(
                    model = partner.imageUrl,
                    contentDescription = null,
                    imageLoader = imageLoader,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(80.dp),
                  )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                  if (partner.title != null) {
                    HedvigText(
                      text = partner.title,
                      textAlign = TextAlign.Center,
                      style = HedvigTheme.typography.bodySmall,
                      modifier = Modifier.fillMaxWidth(),
                    )
                  }
                  if (partner.description != null) {
                    HedvigText(
                      text = partner.description,
                      textAlign = TextAlign.Center,
                      color = HedvigTheme.colorScheme.textSecondary,
                      style = HedvigTheme.typography.bodySmall.copy(
                        lineBreak = LineBreak.Heading,
                      ),
                      modifier = Modifier.fillMaxWidth(),
                    )
                  }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  if (partner.url != null) {
                    HedvigButton(
                      buttonSize = ButtonDefaults.ButtonSize.Medium,
                      enabled = true,
                      text = partner.urlButtonTitle ?: partner.url,
                      onClick = {
                        openUrl(partner.url)
                      },
                      modifier = Modifier.fillMaxWidth(),
                    )
                  }
                  if (partner.phoneNumber != null) {
                    val style = if (partner.url != null) {
                      ButtonDefaults.ButtonStyle.Ghost
                    } else {
                      ButtonDefaults.ButtonStyle.SecondaryAlt
                    }
                    HedvigButton(
                      buttonStyle = style,
                      buttonSize = ButtonDefaults.ButtonSize.Medium,
                      text = partner.phoneNumber,
                      enabled = true,
                      onClick = {
                        tryToDialPhone(partner.phoneNumber)
                      },
                      modifier = Modifier.fillMaxWidth(),
                    )
                  }
                }
              }
              if (partner.info != null) {
                Spacer(Modifier.height(16.dp))
                HedvigText(
                  text = partner.info,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth(),
                  color = HedvigTheme.colorScheme.textSecondary,
                  style = HedvigTheme.typography.finePrint,
                )
              }
            }
          }
        }
        Spacer(Modifier.height(8.dp))
      }

      // TODO: partnersInfo section when design exists

      Spacer(Modifier.height(16.dp))
      HedvigText(
        text = deflect.content.title,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = deflect.content.description,
        modifier = Modifier.padding(horizontal = 16.dp),
        style = HedvigTheme.typography.bodySmall,
        color = HedvigTheme.colorScheme.textSecondary,
      )

      // FAQ section
      if (deflect.faq.isNotEmpty()) {
        Spacer(Modifier.height(24.dp))
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(24.dp))
        QuestionsAndAnswers(
          faqList = deflect.faq,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun QuestionsAndAnswers(
  faqList: List<StepContent.Deflect.InfoBlock>,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    AccordionList(
      faqList.map { faq ->
        AccordionData(title = faq.title, description = faq.description)
      },
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimOutcomeDeflectDestination() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimOutcomeDeflectDestination(
        deflect = StepContent.Deflect(
          title = "title",
          infoText = "infoText",
          warningText = "warningText",
          partners = emptyList(),
          partnersInfo = StepContent.Deflect.InfoBlock("title", "description"),
          content = StepContent.Deflect.InfoBlock("title", "description"),
          faq = emptyList(),
          buttonText = "See our partners"
        ),
        imageLoader = rememberPreviewImageLoader(),
        modifier = Modifier,
        navigateUp = {},
        openUrl = {},
        tryToDialPhone = {},
      )
    }
  }
}
