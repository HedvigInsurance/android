package com.hedvig.feature.claim.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ClaimChatDestination(
  claimChatViewModel: ClaimChatViewModel,
) {
  ClaimChatScreen()
}


@Composable
fun ClaimChatScreen() {
  Box(Modifier.fillMaxSize(), Alignment.Center) {
    BasicText("Hello Multiplatform")
  }
}
