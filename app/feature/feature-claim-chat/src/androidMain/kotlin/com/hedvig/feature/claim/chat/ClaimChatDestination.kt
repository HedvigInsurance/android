package com.hedvig.feature.claim.com.hedvig.feature.claim.chat

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.feature.claim.chat.ClaimChatDestination
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import kotlinx.serialization.Serializable

@Serializable
object ClaimChatDestination : Destination

fun NavGraphBuilder.claimChatGraph() {
  navdestination<ClaimChatDestination> {
    ClaimChatDestination()
  }
}
