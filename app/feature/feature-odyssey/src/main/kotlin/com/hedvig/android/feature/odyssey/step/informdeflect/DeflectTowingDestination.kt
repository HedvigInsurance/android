package com.hedvig.android.feature.odyssey.step.informdeflect

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.alwaysBlackContainer
import com.hedvig.android.core.designsystem.material3.onAlwaysBlackContainer
import com.hedvig.android.core.designsystem.material3.rememberShapedColorPainter
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.card.ExpandablePlusCard
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.DeflectPartner
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
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
    VectorInfoCard(
      text = stringResource(id = R.string.SUBMIT_CLAIM_TOWING_INFO_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(id = R.string.SUBMIT_CLAIM_PARTNER_SINGULAR_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    partners.forEachIndexed { index, partner ->
      if (index > 0) {
        Spacer(Modifier.height(8.dp))
      }
      HedvigCard(
        colors = CardDefaults.outlinedCardColors(
          containerColor = MaterialTheme.colorScheme.alwaysBlackContainer,
          contentColor = MaterialTheme.colorScheme.onAlwaysBlackContainer,
        ),
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      ) {
        Column(Modifier.padding(16.dp)) {
          AsyncImage(
            model = partner.imageUrl,
            contentDescription = null,
            imageLoader = imageLoader,
            placeholder = rememberShapedColorPainter(MaterialTheme.colorScheme.surface),
            modifier = Modifier
              .padding(16.dp)
              .fillMaxWidth()
              .height(40.dp),
          )
          Spacer(Modifier.height(16.dp))
          Text(
            text = stringResource(id = R.string.SUBMIT_CLAIM_TOWING_ONLINE_BOOKING_LABEL),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.height(16.dp))
          val context = LocalContext.current
          HedvigContainedSmallButton(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.onAlwaysBlackContainer,
              contentColor = MaterialTheme.colorScheme.alwaysBlackContainer,
            ),
            text = stringResource(id = R.string.SUBMIT_CLAIM_TOWING_ONLINE_BOOKING_BUTTON),
            onClick = {
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
          )
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    Text(
      text = stringResource(R.string.SUBMIT_CLAIM_HOW_IT_WORKS_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = stringResource(id = R.string.SUBMIT_CLAIM_TOWING_HOW_IT_WORKS_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(24.dp))
    QuestionsAndAnswers(
      Modifier
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(32.dp))
    Text(
      text = stringResource(R.string.SUBMIT_CLAIM_NEED_HELP_TITLE),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Text(
      text = stringResource(R.string.SUBMIT_CLAIM_NEED_HELP_LABEL),
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    HedvigContainedSmallButton(
      text = stringResource(R.string.open_chat),
      onClick = onNavigateToNewConversation,
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
        titleText = faqItem.first,
        expandedText = faqItem.second,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDeflectTowingScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      DeflectTowingScreen(
        partners = listOf(
          DeflectPartner(
            id = "1",
            imageUrl = "test",
            phoneNumber = "1234",
            url = "test",
          ),
          DeflectPartner(
            id = "2",
            imageUrl = "test2",
            phoneNumber = "4321",
            url = "test2",
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
