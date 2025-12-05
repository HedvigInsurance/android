package com.hedvig.feature.claim.chat

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil3.ImageLoader
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.ui.force.upgrade.ForceUpgradeBlockingScreen
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import com.hedvig.feature.claim.chat.ui.ClaimChatDestination
import com.hedvig.feature.claim.chat.ui.outcome.ClaimOutcomeDeflectDestination
import com.hedvig.feature.claim.chat.ui.outcome.ClaimOutcomeNewClaimDestination
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data class ClaimChatDestination(
  val isDevelopmentFlow: Boolean,
  val messageId: String?,
) : Destination

@Serializable
internal data class ClaimOutcomeDeflectDestination(
  val outcome: ClaimIntentOutcome.Deflect,
) : Destination {
  companion object Companion : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(typeOf<ClaimIntentOutcome.Deflect>())
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
internal data object ClaimOutcomeUpdateAppDestination : Destination

fun NavGraphBuilder.claimChatGraph(
  navController: NavController,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToClaimDetails: (claimId: String) -> Unit,
  tryOpenPlayStore: () -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
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

          is ClaimIntentOutcome.Deflect -> {
            navController.navigate(ClaimOutcomeDeflectDestination(outcome = outcome))
          }

          ClaimIntentOutcome.Unknown -> {
            navController.navigate(ClaimOutcomeUpdateAppDestination) {
              typedPopUpTo<ClaimChatDestination> {
                inclusive = true
              }
            }
          }
        }
      },
      appPackageId = appPackageId,
      imageLoader = imageLoader,
    )
  }
  navdestination<ClaimOutcomeDeflectDestination>(ClaimOutcomeDeflectDestination) {
    ClaimOutcomeDeflectDestination(deflect = outcome)
  }
  navdestination<ClaimOutcomeNewClaimDestination>(ClaimOutcomeNewClaimDestination) {
    ClaimOutcomeNewClaimDestination(
      claim = outcome,
      navigateToClaimDetails = { navigateToClaimDetails(outcome.claimId) },
    )
  }
  navdestination<ClaimOutcomeUpdateAppDestination> {
    ForceUpgradeBlockingScreen(
      goToPlayStore = tryOpenPlayStore,
    )
  }
}
