package com.hedvig.feature.claim.chat

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil3.ImageLoader
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.ui.force.upgrade.ForceUpgradeBlockingScreen
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.ui.ClaimChatDestination
import com.hedvig.feature.claim.chat.ui.outcome.ClaimOutcomeDeflectDestination
import com.hedvig.feature.claim.chat.ui.outcome.ClaimOutcomeNewClaimDestination
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data class ClaimChatDestination(
  val isDevelopmentFlow: Boolean = false,
  val messageId: String? = null,
) : Destination

@Serializable
internal data class ClaimOutcomeDeflectDestination(
  val deflect: StepContent.Deflect,
) : Destination {
  companion object Companion : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(typeOf<StepContent.Deflect>())
  }
}

@Serializable
internal data class ClaimOutcomeNewClaimDestination(
  val outcome: ClaimIntentOutcome.Claim,
) : Destination {
  companion object Companion : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(typeOf<ClaimIntentOutcome.Claim>())
  }
}

@Serializable
internal data object UpdateAppDestination : Destination

fun NavGraphBuilder.claimChatGraph(
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
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
  navdestination<ClaimChatDestination> {
    ClaimChatDestination(
      isDevelopmentFlow = isDevelopmentFlow,
      shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
      openAppSettings = openAppSettings,
      onNavigateToImageViewer = onNavigateToImageViewer,
      navigateToClaimOutcome = { outcome ->
        when (outcome) {
          is ClaimIntentOutcome.Claim -> {
            navController.navigate(ClaimOutcomeNewClaimDestination(outcome = outcome)) {
              typedPopUpTo<ClaimChatDestination> {
                inclusive = true
              }
            }
          }
        }
      },
      navigateToDeflect = { deflect: StepContent.Deflect ->
        navController.navigate(ClaimOutcomeDeflectDestination(deflect = deflect))
      },
      appPackageId = appPackageId,
      imageLoader = imageLoader,
      navigateUp = navController::navigateUp,
      openPlayStore = openPlayStore,
    )
  }
  navdestination<ClaimOutcomeDeflectDestination>(ClaimOutcomeDeflectDestination) {
    ClaimOutcomeDeflectDestination(
      deflect = deflect,
      imageLoader = imageLoader,
      navigateUp = navController::navigateUp,
      openUrl = openUrl,
      tryToDialPhone = tryToDialPhone,
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
    )
  }
  navdestination<ClaimOutcomeNewClaimDestination>(ClaimOutcomeNewClaimDestination) {
    ClaimOutcomeNewClaimDestination(
      navController::navigateUp,
    )
  }
  navdestination<UpdateAppDestination> {
    ForceUpgradeBlockingScreen(
      goToPlayStore = tryOpenPlayStore,
    )
  }
}
