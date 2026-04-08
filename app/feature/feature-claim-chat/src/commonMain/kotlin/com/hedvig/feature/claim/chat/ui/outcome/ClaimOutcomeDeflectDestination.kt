package com.hedvig.feature.claim.chat.ui.outcome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.shared.partners.deflect.DeflectData
import com.hedvig.android.shared.partners.deflect.ui.PartnerDeflectDestination

@Composable
internal fun ClaimOutcomeDeflectDestination(
  deflect: DeflectData,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  PartnerDeflectDestination(
    deflect = deflect,
    imageLoader = imageLoader,
    modifier = modifier,
    navigateUp = navigateUp,
    openUrl = openUrl,
    tryToDialPhone = tryToDialPhone,
    onNavigateToNewConversation = onNavigateToNewConversation,
  )
}

@HedvigPreview
@Composable
private fun PreviewClaimOutcomeDeflectDestination() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimOutcomeDeflectDestination(
        deflect = DeflectData(
          title = "title",
          infoText = "infoText",
          warningText = "warningText",
          partnersContainer = null,
          partnersInfo = DeflectData.InfoBlock("title", "description"),
          content = DeflectData.InfoBlock("title", "description"),
          faq = emptyList(),
          buttonText = "See our partners",
        ),
        imageLoader = rememberPreviewImageLoader(),
        modifier = Modifier,
        navigateUp = {},
        openUrl = {},
        tryToDialPhone = {},
        onNavigateToNewConversation = {},
      )
    }
  }
}
