package com.hedvig.feature.claim.chat.navigation

import androidx.compose.runtime.snapshots.Snapshot
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
import com.hedvig.feature.claim.chat.ui.StartClaimPledgeDestination
import com.hedvig.feature.claim.chat.ui.outcome.ClaimOutcomeDeflectDestination
import com.hedvig.feature.claim.chat.ui.outcome.ClaimOutcomeNewClaimDestination
import kotlinx.serialization.Serializable

@Serializable
data class ClaimChatKey(
  val isDevelopmentFlow: Boolean = false,
  val messageId: String? = null,
  val resumableClaimId: String? = null,
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

@Serializable
internal data object StartClaimPledgeKey: HedvigNavKey

fun EntryProviderScope<HedvigNavKey>.claimChatEntries(
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
  // Lets the host drop the entries that launched this flow (e.g. the inbox) once a claim is
  // committed, so leaving the outcome screen returns past them rather than back into the launcher.
  clearClaimEntryPoints: () -> Unit,
) {
  entry<ClaimChatKey> { key ->
    ClaimChatDestination(
      resumableClaimId = key.resumableClaimId,
      isDevelopmentFlow = key.isDevelopmentFlow,
      shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
      openAppSettings = openAppSettings,
      onNavigateToImageViewer = onNavigateToImageViewer,
      navigateToClaimOutcome = { outcome ->
        when (outcome) {
          is ClaimIntentOutcome.Claim -> {
            Snapshot.withMutableSnapshot {
              clearClaimEntryPoints()
              backstack.navigateAndPopUpTo<ClaimChatKey>(
                ClaimOutcomeNewClaimKey(outcome = outcome),
                inclusive = true,
              )
            }
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
    ClaimOutcomeNewClaimDestination(backstack::popBackstack)
  }
  entry<UpdateAppKey> {
    ForceUpgradeBlockingScreen(
      goToPlayStore = tryOpenPlayStore,
    )
  }
  entry<StartClaimPledgeKey> { _ ->
    StartClaimPledgeDestination(
      navigateUp = backstack::navigateUp,
      navigateToClaimChat = {
        backstack.navigateAndPopUpTo<StartClaimPledgeKey>(
          ClaimChatKey(),
          inclusive = true,
        )
      }
    )
  }
}
