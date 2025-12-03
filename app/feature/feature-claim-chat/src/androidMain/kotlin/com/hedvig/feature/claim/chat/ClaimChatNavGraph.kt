package com.hedvig.feature.claim.chat

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil3.ImageLoader
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.feature.claim.chat.ui.ClaimChatDestination
import com.hedvig.feature.claim.chat.ui.outcome.ClaimIntentOutcomeDestination
import kotlinx.serialization.Serializable
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Serializable
data class ClaimChatDestination(
  val isDevelopmentFlow: Boolean,
  val messageId: String?,
) : Destination

@Serializable
internal data class ClaimIntentOutcomeDestination(
  val outcome: ClaimIntentOutcome,
) : Destination {
  companion object : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(typeOf<ClaimIntentOutcome>())
  }
}

fun NavGraphBuilder.claimChatGraph(
  navController: NavController,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToClaimDetails: (claimId: String) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
) {
  navdestination<ClaimChatDestination> {
    ClaimChatDestination(
      isDevelopmentFlow = isDevelopmentFlow,
      shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
      openAppSettings = openAppSettings,
      onNavigateToImageViewer = onNavigateToImageViewer,
      navigateToClaimOutcome = {
        navController.navigate(ClaimIntentOutcomeDestination(it))
      },
      appPackageId = appPackageId,
      imageLoader = imageLoader,
    )
  }
  navdestination<ClaimIntentOutcomeDestination>(ClaimIntentOutcomeDestination) {
    ClaimIntentOutcomeDestination(outcome, { navigateToClaimDetails(it) })
  }
}
