package com.hedvig.android.feature.odyssey.step.informdeflect

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.DeflectPartner
import com.hedvig.android.data.claimflow.IdProtectionDeflectPartner
import com.hedvig.android.data.claimflow.IdProtectionDeflectPartner.ButtonsState.Both
import com.hedvig.android.data.claimflow.IdProtectionDeflectPartner.ButtonsState.None
import com.hedvig.android.data.claimflow.IdProtectionDeflectPartner.ButtonsState.PhoneNumber
import com.hedvig.android.data.claimflow.IdProtectionDeflectPartner.ButtonsState.Url
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Small
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.rememberShapedColorPainter
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun DeflectIdProtectionDestination(
  deflectIdProtection: ClaimFlowDestination.DeflectIdProtection,
  onNavigateToNewConversation: () -> Unit,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  DeflectIdProtectionScreen(
    title = deflectIdProtection.title,
    description = deflectIdProtection.description,
    partners = deflectIdProtection.partners,
    onNavigateToNewConversation = onNavigateToNewConversation,
    closeClaimFlow = closeClaimFlow,
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    imageLoader = imageLoader,
  )
}

@Composable
private fun DeflectIdProtectionScreen(
  title: String,
  description: String?,
  partners: List<IdProtectionDeflectPartner>,
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
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(8.dp))
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = sideSpacingModifier,
    ) {
      HedvigTheme(darkTheme = true) {
        for (partner in partners) {
          Surface(
            color = HedvigTheme.colorScheme.backgroundPrimary.copy(0.95f)
              .compositeOver(HedvigTheme.colorScheme.fillWhite),
            contentColor = HedvigTheme.colorScheme.fillPrimary,
            shape = HedvigTheme.shapes.cornerXLarge,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(Modifier.padding(16.dp)) {
              Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                AsyncImage(
                  model = partner.imageUrl,
                  contentDescription = null,
                  imageLoader = imageLoader,
                  placeholder = rememberShapedColorPainter(HedvigTheme.colorScheme.surfacePrimary),
                  modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .height((partner.preferredImageHeight ?: 40).dp),
                )
                PartnerTitleAndDescription(partner.title, partner.description)
                when (val buttonsState = partner.buttonsState) {
                  is PhoneNumber -> {
                    PartnerPhoneNumberButton(buttonsState.phoneNumber, Primary)
                  }

                  is Url -> {
                    PartnerUrlButton(buttonsState.urlButtonTitle, buttonsState.url)
                  }

                  is Both -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                      PartnerUrlButton(buttonsState.urlButtonTitle, buttonsState.url)
                      PartnerPhoneNumberButton(buttonsState.phoneNumber, Ghost)
                    }
                  }

                  None -> {}
                }
              }
              partner.info?.let { info ->
                Spacer(Modifier.height(16.dp))
                HedvigText(
                  text = info,
                  textAlign = TextAlign.Center,
                  style = HedvigTheme.typography.finePrint.copy(
                    lineBreak = LineBreak.Heading,
                  ),
                  color = HedvigTheme.colorScheme.textSecondary,
                  modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally),
                )
              }
            }
          }
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    HedvigText(
      text = title,
      modifier = sideSpacingModifier.padding(horizontal = 2.dp),
    )
    if (description != null) {
      HedvigText(
        text = description,
        modifier = sideSpacingModifier.padding(horizontal = 2.dp),
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    Surface {
      Column {
        Spacer(Modifier.height(16.dp))
        HedvigText(
          text = stringResource(R.string.SUBMIT_CLAIM_NEED_HELP_TITLE),
          textAlign = TextAlign.Center,
          modifier = sideSpacingModifier.fillMaxWidth(),
        )
        HedvigText(
          text = stringResource(R.string.SUBMIT_CLAIM_NEED_HELP_LABEL),
          textAlign = TextAlign.Center,
          color = HedvigTheme.colorScheme.textSecondary,
          modifier = sideSpacingModifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        HedvigButton(
          text = stringResource(R.string.open_chat),
          onClick = dropUnlessResumed { onNavigateToNewConversation() },
          buttonSize = Small,
          enabled = true,
          modifier = sideSpacingModifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
    }
  }
}

@Composable
private fun PartnerTitleAndDescription(title: String?, description: String?) {
  if (title != null || description != null) {
    Column {
      if (title != null) {
        HedvigText(
          text = title,
          textAlign = TextAlign.Center,
          style = LocalTextStyle.current.copy(
            lineBreak = LineBreak.Heading,
          ),
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally),
        )
      }
      if (description != null) {
        HedvigText(
          text = description,
          textAlign = TextAlign.Center,
          style = LocalTextStyle.current.copy(
            lineBreak = LineBreak.Heading,
          ),
          color = HedvigTheme.colorScheme.textSecondary,
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally),
        )
      }
    }
  }
}

@Composable
private fun PartnerUrlButton(buttonText: String, url: String) {
  val uriHandler = LocalUriHandler.current
  HedvigButton(
    text = buttonText,
    enabled = true,
    buttonSize = Medium,
    buttonStyle = Primary,
    onClick = dropUnlessResumed { uriHandler.openUri(url) },
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun PartnerPhoneNumberButton(phoneNumber: String, buttonStyle: ButtonDefaults.ButtonStyle) {
  val context = LocalContext.current
  HedvigButton(
    text = stringResource(R.string.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_CALL_LABEL, phoneNumber),
    enabled = true,
    buttonSize = Medium,
    buttonStyle = buttonStyle,
    onClick = dropUnlessResumed {
      try {
        context.startActivity(
          Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:$phoneNumber"),
          ),
        )
      } catch (exception: Throwable) {
        logcat(ERROR, exception) {
          "Could not open dial activity in deflect emergency destination"
        }
      }
    },
    modifier = Modifier.fillMaxWidth(),
  )
}

@HedvigPreview
@Composable
private fun PreviewDeflectIdProtectionScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DeflectIdProtectionScreen(
        title = "title",
        description = "description",
        partners = List(2) { index ->
          IdProtectionDeflectPartner(
            title = "ID Protection".takeIf { index == 0 },
            description = "Lorem ipsum dolor sit amet consectetur. Id vel lectus venenatis nunc. In arcu non ut sed."
              .takeIf { index == 0 },
            info = "info",
            urlButtonTitle = "Go to ID Protection Portal",
            partner = DeflectPartner(
              id = "id",
              imageUrl = "imageUrl",
              phoneNumber = "004600460046",
              url = "url".takeIf { index == 0 },
              preferredImageHeight = null,
            ),
          )
        },
        onNavigateToNewConversation = {},
        closeClaimFlow = {},
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        navigateUp = {},
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}
