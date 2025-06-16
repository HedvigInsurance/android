package com.hedvig.android.feature.odyssey.step.informdeflect

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import coil.ImageLoader
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.DeflectPartner
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Small
import com.hedvig.android.design.system.hedvig.ExpandablePlusCard
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun DeflectTowingDestination(
  deflectTowing: ClaimFlowDestination.DeflectTowing,
  onNavigateToNewConversation: () -> Unit,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  DeflectTowingScreen(
    partners = deflectTowing.partners,
    onNavigateToNewConversation = onNavigateToNewConversation,
    closeClaimFlow = closeClaimFlow,
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    imageLoader = imageLoader,
  )
}

@Composable
private fun DeflectTowingScreen(
  partners: List<DeflectPartner>,
  onNavigateToNewConversation: () -> Unit,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    topAppBarText = stringResource(id = R.string.SUBMIT_CLAIM_TOWING_TITLE),
  ) {
    Spacer(Modifier.height(8.dp))
    HedvigNotificationCard(
      message = stringResource(id = R.string.SUBMIT_CLAIM_TOWING_INFO_LABEL),
      priority = Info,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(id = R.string.SUBMIT_CLAIM_PARTNER_SINGULAR_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
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
            Spacer(Modifier.height(16.dp))
            HedvigText(
              text = stringResource(id = R.string.SUBMIT_CLAIM_TOWING_ONLINE_BOOKING_LABEL),
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            val context = LocalContext.current
            HedvigButton(
              text = stringResource(id = R.string.SUBMIT_CLAIM_TOWING_ONLINE_BOOKING_BUTTON),
              enabled = true,
              buttonSize = Medium,
              onClick = dropUnlessResumed {
                val phoneNumber = partner.phoneNumber
                if (phoneNumber != null) {
                  try {
                    context.startActivity(
                      Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:$phoneNumber"),
                      ),
                    )
                  } catch (exception: Throwable) {
                    logcat(ERROR, exception) {
                      "Could not open dial activity in deflect towing destination"
                    }
                  }
                } else {
                  logcat(ERROR) {
                    "Partner phone number was null for DeflectTowingDestination! Deflect partner: $partner."
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
      text = stringResource(id = R.string.SUBMIT_CLAIM_TOWING_HOW_IT_WORKS_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(24.dp))
    QuestionsAndAnswers(
      Modifier
        .padding(horizontal = 16.dp),
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
      enabled = true,
      buttonSize = Small,
      onClick = dropUnlessResumed { onNavigateToNewConversation() },
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
    stringResource(R.string.SUBMIT_CLAIM_TOWING_Q1) to stringResource(R.string.SUBMIT_CLAIM_TOWING_A1),
    stringResource(R.string.SUBMIT_CLAIM_TOWING_Q2) to stringResource(R.string.SUBMIT_CLAIM_TOWING_A2),
    stringResource(R.string.SUBMIT_CLAIM_TOWING_Q3) to stringResource(R.string.SUBMIT_CLAIM_TOWING_A3),
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
        titleDescription = faqItem.first,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDeflectTowingScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DeflectTowingScreen(
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
      )
    }
  }
}
