package com.hedvig.android.odyssey.input.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import com.hedvig.android.core.ui.FullScreenProgressOverlay
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.odyssey.input.InputViewState
import com.hedvig.android.odyssey.input.InputViewModel
import com.hedvig.android.odyssey.input.ui.audiorecorder.AudioRecorderScreen
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.app.ui.compose.composables.ErrorDialog

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InputRoot(
  inputViewModel: InputViewModel,
  viewState: InputViewState,
  onFinish: () -> Unit,
) {
  BackHandler {
    inputViewModel.onBack()
  }

  Scaffold(
    topBar = {
      TopAppBarWithBack(
        onClick = {
          inputViewModel.onBack()
        },
        title = viewState.title,
      )
    },
  ) { paddingValues ->

    Crossfade(targetState = viewState.isLoading) { loading ->
      if (loading) {
        FullScreenProgressOverlay(show = true)
      } else {
        AnimatedContent(targetState = viewState.currentInput) { input ->
          Input(
            input = input,
            claimState = viewState.claimState,
            viewModel = inputViewModel,
          )
        }
      }
    }

    if (viewState.errorMessage != null) {
      ErrorDialog(
        message = viewState.errorMessage,
        onDismiss = { inputViewModel.onDismissError() },
      )
    }

    if (viewState.shouldExit) {
      onFinish()
    }
  }
}

@Composable
private fun Input(
  input: Input?,
  claimState: ClaimState,
  viewModel: InputViewModel,
) {
  when (input) {
    is Input.AudioRecording -> AudioRecorderScreen(
      questions = input.questions,
      onAudioFile = viewModel::onAudioFile,
      onNext = viewModel::onNext,
    )
    is Input.DateOfOccurrencePlusLocation -> DateOfOccurrenceAndLocation(
      state = claimState,
      onDateOfOccurrence = viewModel::onDateOfOccurrence,
      onLocation = viewModel::onLocation,
      locationOptions = input.locationOptions,
      onNext = viewModel::onNext,
    )
    is Input.DateOfOccurrence -> DateOfOccurrence(viewModel)
    is Input.Location -> Location(viewModel)
    is Input.PhoneNumber -> PhoneNumber(viewModel)
    is Input.SingleItem -> SingleItem(
      state = claimState,
      input = input,
      onDateOfPurchase = viewModel::onDateOfPurchase,
      onTypeOfDamage = viewModel::onTypeOfDamage,
      onPurchasePrice = viewModel::onPurchasePrice,
      onNext = viewModel::onNext,
    )
    Input.Unknown -> {}
    null -> {}
  }
}
