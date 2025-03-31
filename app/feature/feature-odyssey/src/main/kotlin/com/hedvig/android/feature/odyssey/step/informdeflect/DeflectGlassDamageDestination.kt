package com.hedvig.android.feature.odyssey.step.informdeflect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.DeflectPartner
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Small
import com.hedvig.android.design.system.hedvig.ExpandablePlusCard
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.rememberShapedColorPainter
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun DeflectGlassDamageDestination(
  deflectGlassDamage: ClaimFlowDestination.DeflectGlassDamage,
  onNavigateToNewConversation: () -> Unit,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  DeflectGlassDamageScreen(
    partners = deflectGlassDamage.partners,
    onNavigateToNewConversation = onNavigateToNewConversation,
    closeClaimFlow = closeClaimFlow,
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    openUrl = openUrl,
    imageLoader = imageLoader,
  )
}

@Composable
private fun DeflectGlassDamageScreen(
  partners: List<DeflectPartner>,
  onNavigateToNewConversation: () -> Unit,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    topAppBarText = stringResource(id = R.string.SUBMIT_CLAIM_GLASS_DAMAGE_TITLE),
  ) {
    Spacer(Modifier.height(8.dp))
    HedvigNotificationCard(
      message = stringResource(R.string.SUBMIT_CLAIM_GLASS_DAMAGE_INFO_LABEL),
      priority = Info,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(R.string.SUBMIT_CLAIM_PARTNER_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    partners.forEachIndexed { index, partner ->
      if (index > 0) {
        Spacer(Modifier.height(8.dp))
      }
      HedvigTheme(darkTheme = true) {
        HedvigCard(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        ) {
          Surface(
            color = HedvigTheme.colorScheme.backgroundPrimary.copy(
              0.95f,
            ).compositeOver(HedvigTheme.colorScheme.fillWhite),
            contentColor = HedvigTheme.colorScheme.fillPrimary,
          ) {
            Column(Modifier.padding(16.dp)) {
              AsyncImage(
                model = partner.imageUrl,
                contentDescription = null,
                imageLoader = imageLoader,
                placeholder = rememberShapedColorPainter(HedvigTheme.colorScheme.surfacePrimary),
                modifier = Modifier
                  .padding(16.dp)
                  .fillMaxWidth()
                  .height((partner.preferredImageHeight ?: 40).dp),
              )
              Spacer(Modifier.height(16.dp))
              HedvigText(
                text = stringResource(R.string.SUBMIT_CLAIM_GLASS_DAMAGE_ONLINE_BOOKING_LABEL),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
              )
              Spacer(Modifier.height(16.dp))
              HedvigButton(
                text = stringResource(R.string.SUBMIT_CLAIM_GLASS_DAMAGE_ONLINE_BOOKING_BUTTON),
                buttonSize = ButtonDefaults.ButtonSize.Small,
                enabled = true,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                  val url = partner.url
                  if (url != null) {
                    openUrl(url)
                  } else {
                    logcat(ERROR) {
                      """
                  |Partner URL was null for DeflectGlassDamageDestination! Deflect partner:[$this]. 
                  |This is problematic because the UI offers no real help to the member, the CTA button does nothing.
                      """.trimMargin()
                    }
                  }
                },
              )
            }
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
      text = stringResource(R.string.SUBMIT_CLAIM_GLASS_DAMAGE_HOW_IT_WORKS_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(24.dp))
    QuestionsAndAnswers(Modifier.padding(horizontal = 16.dp))
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
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
private fun QuestionsAndAnswers(modifier: Modifier = Modifier) {
  var expandedItem by rememberSaveable { mutableIntStateOf(-1) }
  val faqList = listOf(
    stringResource(
      R.string.SUBMIT_CLAIM_WHAT_COST_TITLE,
    ) to stringResource(R.string.SUBMIT_CLAIM_GLASS_DAMAGE_WHAT_COST_LABEL),
    stringResource(
      R.string.SUBMIT_CLAIM_HOW_BOOK_TITLE,
    ) to stringResource(R.string.SUBMIT_CLAIM_GLASS_DAMAGE_HOW_BOOK_LABEL),
    stringResource(
      R.string.SUBMIT_CLAIM_WORKSHOP_TITLE,
    ) to stringResource(R.string.SUBMIT_CLAIM_GLASS_DAMAGE_WORKSHOP_LABEL),
  )
  Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    faqList.forEachIndexed { index, faqItem ->
      ExpandablePlusCard(
        isExpanded = expandedItem == index,
        onClick = {
          expandedItem = if (expandedItem == index) {
            -1
          } else {
            index
          }
        },
        content = {
          HedvigText(faqItem.first)
        },
        expandedContent = {
          HedvigText(
            text = faqItem.second,
            color = HedvigTheme.colorScheme.textSecondary,
            modifier = Modifier.padding(end = 24.dp, top = 12.dp),
          )
        },
        contentPadding = PaddingValues(12.dp),
      )
    }
  }
}

@HedvigPreview
@Preview(device = "spec:width=1080px,height=3340px,dpi=440")
@Composable
private fun PreviewDeflectGlassDamageScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DeflectGlassDamageScreen(
        partners = listOf(
          DeflectPartner(
            id = "1",
            imageUrl = "test",
            phoneNumber = "1234",
            url = "test",
            preferredImageHeight = null,
          ),
          DeflectPartner(
            id = "2",
            imageUrl = "test2",
            phoneNumber = "4321",
            url = "test2",
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
