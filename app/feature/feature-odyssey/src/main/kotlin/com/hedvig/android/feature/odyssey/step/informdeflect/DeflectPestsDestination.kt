package com.hedvig.android.feature.odyssey.step.informdeflect

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import coil.ImageLoader
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.DeflectPartner
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Small
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun DeflectPestsDestination(
  deflectPests: ClaimFlowDestination.DeflectPests,
  onNavigateToNewConversation: () -> Unit,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
) {
  DeflectPestsScreen(
    partners = deflectPests.partners,
    onNavigateToNewConversation = onNavigateToNewConversation,
    closeClaimFlow = closeClaimFlow,
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    imageLoader = imageLoader,
    openUrl = openUrl,
  )
}

@Composable
private fun DeflectPestsScreen(
  partners: List<DeflectPartner>,
  onNavigateToNewConversation: () -> Unit,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  ) {
    Spacer(Modifier.height(8.dp))
    HedvigNotificationCard(
      message = stringResource(R.string.SUBMIT_CLAIM_PESTS_INFO_LABEL),
      priority = Info,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(R.string.SUBMIT_CLAIM_PARTNER_TITLE),
      modifier = Modifier.padding(horizontal = 20.dp),
    )
    Spacer(Modifier.height(16.dp))
    partners.forEachIndexed { index, partner ->
      if (index > 0) {
        Spacer(Modifier.height(8.dp))
      }
      HedvigTheme(darkTheme = true) {
        Surface(
          color = HedvigTheme.colorScheme.backgroundPrimary.copy(0.95f)
            .compositeOver(HedvigTheme.colorScheme.fillWhite),
          contentColor = HedvigTheme.colorScheme.fillPrimary,
          shape = HedvigTheme.shapes.cornerXLarge,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        ) {
          Column(Modifier.padding(16.dp)) {
            PartnerImage(partner, imageLoader, Modifier.padding(16.dp))
            Spacer(Modifier.height(8.dp))
            HedvigText(
              text = stringResource(R.string.SUBMIT_CLAIM_PESTS_CUSTOMER_SERVICE_LABEL),
              textAlign = TextAlign.Center,
              style = LocalTextStyle.current.copy(
                lineBreak = LineBreak.Heading,
              ),
              modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            HedvigButton(
              text = stringResource(R.string.SUBMIT_CLAIM_PESTS_CUSTOMER_SERVICE_BUTTON),
              enabled = true,
              buttonSize = Medium,
              onClick = dropUnlessResumed {
                val url = partner.url
                if (url != null) {
                  openUrl(url)
                } else {
                  logcat(ERROR) {
                    """
                  |Partner URL was null for DeflectPestsDestination! Deflect partner:[$this]. 
                  |This is problematic because the UI offers no real help to the member, the CTA button does nothing.
                    """.trimMargin()
                  }
                }
              },
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    HedvigText(
      text = stringResource(R.string.SUBMIT_CLAIM_HOW_IT_WORKS_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = stringResource(R.string.SUBMIT_CLAIM_PESTS_HOW_IT_WORKS_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(32.dp))
    HedvigText(
      text = stringResource(R.string.SUBMIT_CLAIM_NEED_HELP_TITLE),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    HedvigText(
      text = stringResource(R.string.SUBMIT_CLAIM_NEED_HELP_LABEL),
      textAlign = TextAlign.Center,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    HedvigButton(
      text = stringResource(R.string.open_chat),
      onClick = dropUnlessResumed { onNavigateToNewConversation() },
      buttonSize = Small,
      enabled = true,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewDeflectPestsScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DeflectPestsScreen(
        partners = listOf(
          DeflectPartner(
            id = "1",
            imageUrl = "test",
            phoneNumber = "1234",
            url = "test",
            preferredImageHeight = null,
          ),
        ),
        onNavigateToNewConversation = {},
        closeClaimFlow = {},
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        navigateUp = {},
        imageLoader = rememberPreviewImageLoader(),
        openUrl = {},
      )
    }
  }
}
