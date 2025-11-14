package com.hedvig.feature.claim.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hedvig.android.design.system.hedvig.HedvigTheme

class MockActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val viewModel: ClaimChatViewModel = viewModel()
      val uiState by viewModel.state.collectAsState()

      HedvigTheme {
        PlatformBlurContainer(radius = 10) {
          BlurredGradientBackground()
        }
        ConversationScreen(
          state = uiState,
          onAction = viewModel::processUserAction,
        )
      }
    }
  }
}
