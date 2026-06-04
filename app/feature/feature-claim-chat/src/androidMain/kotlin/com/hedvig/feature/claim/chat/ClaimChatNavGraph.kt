package com.hedvig.feature.claim.chat

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.ui.force.upgrade.ForceUpgradeBlockingScreen
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.ui.ClaimChatDestination
import com.hedvig.feature.claim.chat.ui.outcome.ClaimOutcomeDeflectDestination
import com.hedvig.feature.claim.chat.ui.outcome.ClaimOutcomeNewClaimDestination
import kotlinx.serialization.Serializable

@Serializable
data class ClaimChatKey(
  val isDevelopmentFlow: Boolean = false,
  val messageId: String? = null,
) : HedvigNavKey

@Serializable
internal data class ClaimOutcomeDeflectKey(
  val deflect: StepContent.Deflect,
) : HedvigNavKey

@Serializable
internal data class ClaimOutcomeNewClaimKey(
  val outcome: ClaimIntentOutcome.Claim,
) : HedvigNavKey

@Serializable
internal data object UpdateAppKey : HedvigNavKey

fun EntryProviderScope<HedvigNavKey>.claimChatGraph(
  backstack: Backstack,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToClaimDetails: (claimId: String) -> Unit,
  tryOpenPlayStore: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
  onNavigateToNewConversation: () -> Unit,
  openPlayStore: () -> Unit,
) {
  entry<ClaimChatKey> { key ->
    ClaimChatDestination(
      isDevelopmentFlow = key.isDevelopmentFlow,
      shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
      openAppSettings = openAppSettings,
      onNavigateToImageViewer = onNavigateToImageViewer,
      navigateToClaimOutcome = { outcome ->
        when (outcome) {
          is ClaimIntentOutcome.Claim -> {
            backstack.navigateAndPopUpTo<ClaimChatKey>(
              ClaimOutcomeNewClaimKey(outcome = outcome),
              inclusive = true,
            )
          }
        }
      },
      navigateToDeflect = { deflect: StepContent.Deflect ->
        backstack.add(ClaimOutcomeDeflectKey(deflect = deflect))
      },
      appPackageId = appPackageId,
      imageLoader = imageLoader,
      navigateUp = backstack::navigateUp,
      openPlayStore = openPlayStore,
    )
  }
  entry<ClaimOutcomeDeflectKey> { key ->
    ClaimOutcomeDeflectDestination(
      deflect = key.deflect.deflectData,
      imageLoader = imageLoader,
      navigateUp = backstack::navigateUp,
      openUrl = openUrl,
      tryToDialPhone = tryToDialPhone,
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
    )
  }
  entry<ClaimOutcomeNewClaimKey> {
    ClaimOutcomeNewClaimDestination(
      backstack::navigateUp,
    )
  }
  entry<UpdateAppKey> {
    ForceUpgradeBlockingScreen(
      goToPlayStore = tryOpenPlayStore,
    )
  }
}
