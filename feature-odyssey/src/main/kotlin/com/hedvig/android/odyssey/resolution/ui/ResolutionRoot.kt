package com.hedvig.android.odyssey.resolution.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.hedvig.android.core.ui.FullScreenProgressOverlay
import com.hedvig.android.core.ui.appbar.TopAppBarWithClose
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.android.odyssey.resolution.ResolutionViewModel
import com.hedvig.android.odyssey.resolution.ResolutionViewState
import com.hedvig.app.ui.compose.composables.ErrorDialog

@Composable
fun ResolutionRoot(
  viewModel: ResolutionViewModel,
  resolution: Resolution,
  onFinish: () -> Unit,
) {
  val viewState by viewModel.viewState.collectAsState()

  BackHandler {
    onFinish()
  }

  Scaffold(
    topBar = {
      TopAppBarWithClose(
        onClick = onFinish,
        title = viewState.title,
        backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
      )
    },
  ) { paddingValues ->
    Crossfade(
      targetState = viewState.isLoading,
      modifier = Modifier.padding(paddingValues),
      label = "ProgressOverlayOrContent",
    ) { loading ->
      if (loading) {
        FullScreenProgressOverlay(show = true)
      } else {
        Resolution(
          resolution = resolution,
          viewModel = viewModel,
          onFinish = onFinish,
          viewState = viewState,
        )
      }
    }

    if (viewState.errorMessage != null) {
      ErrorDialog(
        message = viewState.errorMessage,
        onDismiss = { viewModel.onDismissError() },
      )
    }
  }
}

@Composable
private fun Resolution(
  resolution: Resolution,
  viewModel: ResolutionViewModel,
  viewState: ResolutionViewState,
  onFinish: () -> Unit,
) {
  when (resolution) {
    Resolution.ManualHandling -> Success(
      onExit = onFinish,
    )
    is Resolution.SingleItemPayout -> SingleItemPayout(
      resolution = resolution,
      isLoadingPayout = viewState.isLoadingPayout,
      isCompleted = viewState.isCompleted,
      onPayout = viewModel::payout,
      onFinish = onFinish,
    )
    Resolution.None -> {}
  }
}
