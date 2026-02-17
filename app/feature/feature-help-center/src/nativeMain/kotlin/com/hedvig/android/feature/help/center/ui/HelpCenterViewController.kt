package com.hedvig.android.feature.help.center.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.hedvig.android.feature.help.center.HelpCenterViewModel
import com.hedvig.android.feature.help.center.home.HelpCenterHomeDestination
import org.koin.compose.viewmodel.koinViewModel
import platform.UIKit.UIViewController

@Suppress("unused", "FunctionName") // Used from iOS
fun HelpCenterViewController(): UIViewController {
  return ComposeUIViewController {
    Box(Modifier.fillMaxSize()) {
      val viewModel = koinViewModel<HelpCenterViewModel>()
      HelpCenterHomeDestination(
        viewModel,
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
