package com.hedvig.android.app.navigation

import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.feature.chat.navigation.cbmChatEntries
import com.hedvig.android.feature.claim.chat.navigation.ClaimChatKey
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.add

/**
 * Registers the destinations of the features only Android can render, the host half of
 * [hedvigEntryProvider]. A closure that [context] provides is taken from it and never rebuilt here; closures
 * it does not provide are built here.
 */
internal fun EntryProviderScope<HedvigNavKey>.androidHostEntries(context: HostEntryContext) {
  addChatEntries(
    backstack = context.backstack,
    hedvigBuildConstants = context.hedvigBuildConstants,
    imageLoader = context.imageLoader,
    openUrl = context.openUrl,
    openCrossSellUrl = context.openCrossSellUrl,
    onNavigateToImageViewer = context.onNavigateToImageViewer,
    navigateToNewConversation = context.navigateToNewConversation,
  )
}

private fun EntryProviderScope<HedvigNavKey>.addChatEntries(
  backstack: BackstackController,
  hedvigBuildConstants: HedvigBuildConstants,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Boolean,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToNewConversation: () -> Unit,
) {
  cbmChatEntries(
    hedvigBuildConstants = hedvigBuildConstants,
    imageLoader = imageLoader,
    openUrl = openUrl,
    openCrossSellUrl = openCrossSellUrl,
    onNavigateToClaimDetails = { claimId ->
      logcat { "Navigating to claim details from chat" }
      backstack.add(ClaimDetailsKey(claimId))
    },
    onNavigateToImageViewer = onNavigateToImageViewer,
    onNavigateToNewConversation = navigateToNewConversation,
    navigateToClaimChat = { resumeClaim ->
      backstack.add(ClaimChatKey(messageId = null, isDevelopmentFlow = false, resumeClaim = resumeClaim))
    },
    backstack = backstack,
  )
}
