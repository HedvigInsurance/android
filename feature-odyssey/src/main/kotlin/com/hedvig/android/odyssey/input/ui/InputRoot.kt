package com.hedvig.android.odyssey.input.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import coil.ImageLoader
import com.hedvig.android.core.ui.FullScreenProgressOverlay
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.odyssey.input.InputViewModel
import com.hedvig.android.odyssey.input.InputViewState
import com.hedvig.android.odyssey.input.ui.audiorecorder.AudioRecorderScreen
import com.hedvig.android.odyssey.input.ui.audiorecorder.AudioRecorderViewModel
import com.hedvig.android.odyssey.model.Input
import com.hedvig.app.ui.compose.composables.ErrorDialog

@Composable
fun InputRoot(
  inputViewModel: InputViewModel,
  viewState: InputViewState,
  imageLoader: ImageLoader,
  onFinish: () -> Unit,
  audioRecorderViewModel: AudioRecorderViewModel,
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
    Crossfade(
      targetState = viewState.isLoading,
      modifier = Modifier.padding(paddingValues),
      label = "ProgressOverlayOrContent",
    ) { loading ->
      if (loading) {
        FullScreenProgressOverlay(show = true)
      } else {
        AnimatedContent(
          targetState = viewState.currentInput,
          label = "Input",
        ) { input ->
          Input(
            input = input,
            viewState = viewState,
            viewModel = inputViewModel,
            audioRecorderViewModel = audioRecorderViewModel,
            imageLoader = imageLoader,
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

    LaunchedEffect(viewState.shouldExit) {
      if (viewState.shouldExit) {
        onFinish()
      }
    }
  }
}

@Composable
private fun Input(
  input: Input?,
  viewState: InputViewState,
  viewModel: InputViewModel,
  imageLoader: ImageLoader,
  audioRecorderViewModel: AudioRecorderViewModel,
) {
  when (input) {
    is Input.AudioRecording -> AudioRecorderScreen(
      audioRecorderViewModel = audioRecorderViewModel,
      questions = input.questions,
      onAudioFile = viewModel::onAudioFile,
      onNext = viewModel::onNext,
    )
    is Input.DateOfOccurrencePlusLocation -> DateOfOccurrenceAndLocation(
      state = viewState.claimState,
      imageLoader = imageLoader,
      onDateOfOccurrence = viewModel::onDateOfOccurrence,
      onLocation = viewModel::onLocation,
      locationOptions = input.locationOptions,
      onNext = viewModel::onNext,
    )
    is Input.DateOfOccurrence -> DateOfOccurrence(viewModel)
    is Input.Location -> Location(viewModel)
    is Input.PhoneNumber -> PhoneNumber(
      currentPhoneNumber = viewState.phoneNumber,
      onPhoneNumber = viewModel::onPhoneNumber,
      updatePhoneNumber = viewModel::updatePhoneNumber,
      onNext = viewModel::onNext,
    )
    is Input.SingleItem -> SingleItem(
      state = viewState.claimState,
      input = input,
      imageLoader = imageLoader,
      onDateOfPurchase = viewModel::onDateOfPurchase,
      onTypeOfDamage = viewModel::onTypeOfDamage,
      onModelOption = viewModel::onModelOption,
      onPurchasePrice = viewModel::onPurchasePrice,
      onNext = viewModel::onNext,
    )
    Input.Unknown -> {}
    null -> {}
  }
}
