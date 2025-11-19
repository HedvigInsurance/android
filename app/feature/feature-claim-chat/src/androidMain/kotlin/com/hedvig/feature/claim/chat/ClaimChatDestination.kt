package com.hedvig.feature.claim.com.hedvig.feature.claim.chat

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.feature.claim.chat.BlurredGradientBackground
import com.hedvig.feature.claim.chat.ClaimChatDestination
import com.hedvig.feature.claim.chat.ui.PlatformBlurContainer
import kotlinx.serialization.Serializable

@Serializable
data class ClaimChatDestination (
  val isDevelopmentFlow: Boolean,
  val messageId: String?
): Destination

fun NavGraphBuilder.claimChatGraph() {
  navdestination<ClaimChatDestination> {
    PlatformBlurContainer(radius = 10) {
      BlurredGradientBackground()
    }
    ClaimChatDestination(isDevelopmentFlow, messageId)
  }
}
