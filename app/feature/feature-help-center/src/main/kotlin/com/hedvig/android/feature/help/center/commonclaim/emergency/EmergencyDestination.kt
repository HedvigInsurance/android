package com.hedvig.android.feature.help.center.commonclaim.emergency

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import com.hedvig.android.shared.partners.deflect.DeflectData
import com.hedvig.android.shared.partners.deflect.ui.PartnerDeflectDestination

@Composable
internal fun EmergencyDestination(
  deflect: DeflectData,
  imageLoader: ImageLoader,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  PartnerDeflectDestination(
    deflect = deflect,
    imageLoader = imageLoader,
    navigateUp = navigateUp,
    openUrl = openUrl,
    tryToDialPhone = tryToDialPhone,
    onNavigateToNewConversation = onNavigateToNewConversation,
  )
}
