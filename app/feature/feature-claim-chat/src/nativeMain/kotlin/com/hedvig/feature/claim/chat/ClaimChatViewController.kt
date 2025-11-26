package com.hedvig.feature.claim.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import org.koin.compose.viewmodel.koinViewModel
import platform.UIKit.UIViewController

@Suppress("unused", "FunctionName") // Used from iOS
fun ClaimChatViewController(isDevelopmentFlow: Boolean, messageId: String?): UIViewController =
  ComposeUIViewController {
    Box(Modifier.fillMaxSize()) {
      ClaimChatDestination(isDevelopmentFlow, messageId)
    }
  }
