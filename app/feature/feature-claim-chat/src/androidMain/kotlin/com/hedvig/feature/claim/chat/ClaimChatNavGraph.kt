package com.hedvig.feature.claim.chat

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.feature.claim.com.hedvig.feature.claim.chat.ui.ClaimChatDestination
import kotlinx.serialization.Serializable

@Serializable
data class ClaimChatDestination(
  val isDevelopmentFlow: Boolean,
  val messageId: String?,
) : Destination

fun NavGraphBuilder.claimChatGraph(
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
) {
  navdestination<ClaimChatDestination> {
    ClaimChatDestination(
      isDevelopmentFlow = isDevelopmentFlow,
      shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
      openAppSettings = openAppSettings
    )
  }
}
