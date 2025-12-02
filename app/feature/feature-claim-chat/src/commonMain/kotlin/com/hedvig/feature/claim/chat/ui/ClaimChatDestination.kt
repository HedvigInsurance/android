package com.hedvig.feature.claim.chat.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay
import com.hedvig.android.ui.claimflow.HedvigChip
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.ClaimChatUiState
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.StepContent
import hedvig.resources.Res
import hedvig.resources.CLAIMS_TEXT_INPUT_PLACEHOLDER
import hedvig.resources.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ClaimChatDestination(
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  isDevelopmentFlow: Boolean,
) {
  val claimChatViewModel = koinViewModel<ClaimChatViewModel> {
    parametersOf(isDevelopmentFlow)
  }
  Box(Modifier.fillMaxSize(), propagateMinConstraints = true) {
    BlurredGradientBackground(radius = 100)
    ClaimChatScreenContent(
      claimChatViewModel,
      shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
      openAppSettings = openAppSettings,
    )
  }
}

@Composable
internal fun ClaimChatScreenContent(
  claimChatViewModel: ClaimChatViewModel,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
) {
  val uiState = claimChatViewModel.uiState.collectAsState().value

  Box(Modifier.fillMaxSize(), Alignment.Center) {
    when (uiState) {
      ClaimChatUiState.FailedToStart -> BasicText("FailedToStart") //todo
      ClaimChatUiState.Initializing -> HedvigFullScreenCenterAlignedProgress()
      is ClaimChatUiState.ClaimChat -> ClaimChatScreen(
        uiState = uiState,
        onEvent = claimChatViewModel::emit,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        openAppSettings = openAppSettings,
      )
    }
  }
}

@Composable
private fun ClaimChatScreen(
  uiState: ClaimChatUiState.ClaimChat,
  onEvent: (ClaimChatEvent) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
) {
  FreeTextOverlay(
    freeTextMaxLength = 3000,
    freeTextValue = uiState.freeText,
    freeTextHint = stringResource(Res.string.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER),
    freeTextTitle = stringResource(Res.string.CLAIMS_TEXT_INPUT_PLACEHOLDER),
    freeTextOnCancelClick = {
      onEvent(ClaimChatEvent.CloseFreeChatOverlay)
    },
    freeTextOnSaveClick = { feedback ->
      onEvent(ClaimChatEvent.UpdateFreeText(feedback))
      onEvent(ClaimChatEvent.CloseFreeChatOverlay)
    },
    shouldShowOverlay = uiState.showFreeTextOverlay,
    overlaidContent = {
      ClaimChatScreenContent(
        uiState = uiState,
        onEvent = onEvent,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        openAppSettings = openAppSettings,
      )
    },
  )
}

@Composable
private fun ClaimChatScreenContent(
  uiState: ClaimChatUiState.ClaimChat,
  onEvent: (ClaimChatEvent) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    state = lazyListState,
    contentPadding = WindowInsets.safeDrawing.asPaddingValues().plus(PaddingValues(vertical = 16.dp)),
    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
  ) {
    items(
      items = uiState.steps,
      key = { step -> step.id.value },
      contentType = { it.stepContent::class },
    ) { item ->
      when (item.stepContent) {
        is StepContent.AudioRecording -> {
          AudioRecordingStep(
            item = item,
            stepContent = item.stepContent,
            onShowFreeText = {
              onEvent(ClaimChatEvent.AudioRecording.ShowFreeText(item.id))
            },
            onShowAudioRecording = {
              onEvent(ClaimChatEvent.AudioRecording.ShowAudioRecording(item.id))
            },
            onLaunchFullScreenEditText = {
              onEvent(ClaimChatEvent.OpenFreeTextOverlay)
            },
            startRecording = {
              onEvent(ClaimChatEvent.AudioRecording.StartRecording(item.id))
            },
            stopRecording = {
              onEvent(ClaimChatEvent.AudioRecording.StopRecording(item.id))
            },
            redoRecording = {
              onEvent(ClaimChatEvent.AudioRecording.RedoRecording(item.id))
            },
            submitFreeText = {
              onEvent(ClaimChatEvent.AudioRecording.SubmitTextInput(item.id))
            },
            submitAudioFile = {
              onEvent(ClaimChatEvent.AudioRecording.SubmitAudioFile(item.id))
            },
            onSkip = {
              onEvent(ClaimChatEvent.Skip(item.id))
            },
            isCurrentStep = item == uiState.currentStep,
            clock = Clock.System,
            onShouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
            openAppSettings = openAppSettings,
            modifier = Modifier.padding(horizontal = 16.dp),
            freeText = uiState.freeText,
          )
        }

        is StepContent.ContentSelect -> ContentSelectStep(
          item = item,
          currentStep = uiState.currentStep,
          options = item.stepContent.options,
          selectedOptionId = item.stepContent.selectedOptionId,
          onOptionClick = { option ->
            onEvent(
              ClaimChatEvent.Select(
                item.id,
                option.id,
              ),
            )
          },
          modifier = Modifier.padding(horizontal = 16.dp),
        )


        is StepContent.FileUpload -> {
          Column(Modifier.padding(horizontal = 16.dp)) {
            HedvigText(
              item.text,
            )
            Spacer(Modifier.height(8.dp))
//            UploadFilesBubble(
//              isCurrentStep = TODO(),
//              canSkip = item.stepContent.isSkippable,
//              canBeChanged = item.stepContent.isRegrettable, //todo
//              onSkip = {
//                onEvent(ClaimChatEvent.Skip(item.id))
//              },
//              addLocalFile = TODO(),
//              onRemoveFile = TODO(),
//              onSubmitFiles = {
//                onEvent(ClaimChatEvent.FileUpload())
//              },
//              appPackageId = appPackageId,
//              localFiles = TODO(),
//              uploadedFiles = TODO(),
//              imageLoader = TODO(),
//              onNavigateToImageViewer = TODO(),
//              modifier = TODO(),
//            )
          }
        }

        is StepContent.Form -> {
          HedvigText(
            item.text,
            Modifier
              .padding(horizontal = 16.dp)
//              .clickable {
//                onEvent(
//                  ClaimChatEvent.Form(
//                    item.id,
//                    item.stepContent.fields.associate {
//                      it.id to it.defaultValues
//                    },
//                  ),
//                )
//              },
          )
          Spacer(Modifier.height(8.dp))
          FormContent(
            item, item.stepContent,
            onSkip = {
              onEvent(ClaimChatEvent.Skip(item.id))
            },
            modifier =  Modifier
              .padding(horizontal = 16.dp)
          )
        }

        is StepContent.Summary -> BasicText("Summary")
        is StepContent.Task -> {
          Column {
            BasicText("Task")
            for (description in item.stepContent.descriptions) {
              BasicText(description)
            }
          }
        }

        StepContent.Unknown -> BasicText("Unknown")
      }
    }
  }

  LaunchedEffect(uiState.steps.size) {
    if (uiState.steps.isNotEmpty()) {
      lazyListState.animateScrollToItem(index = uiState.steps.lastIndex)
    }
  }
}

@Composable
private fun FormContent(
  item:  ClaimIntentStep,
  content: StepContent.Form,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    content.fields.forEach { field ->
//      when (field.type) {
//        StepContent.Form.FieldType.TEXT -> TextInputBubble(
//          //todo
//        )
//        StepContent.Form.FieldType.DATE -> DateSelectBubble(
//          //todo
//        )
//        StepContent.Form.FieldType.NUMBER -> TextInputBubble(
//          //todo
//        ) //add: keyboard choice
//        StepContent.Form.FieldType.SINGLE_SELECT -> SingleSelectBubbleWithDialog(
//          //todo
//        )
//        StepContent.Form.FieldType.MULTI_SELECT -> MultiSelectBubbleWithDialog(
//          //todo
//        )
//        StepContent.Form.FieldType.BINARY -> YesNoBubble(
//          //todo
//        )
//        null -> {
//          onSkip() //todo: check
//        }
//      }
    }
  }
}

@Composable
private fun AudioRecordingStep(
  item: ClaimIntentStep,
  freeText: String?,
  stepContent: StepContent.AudioRecording,
  onShowFreeText: () -> Unit,
  onShowAudioRecording: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
  submitFreeText: () -> Unit,
  submitAudioFile: () -> Unit,
  stopRecording: () -> Unit,
  redoRecording: () -> Unit,
  onSkip: () -> Unit,
  isCurrentStep: Boolean,
  clock: Clock,
  onShouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  startRecording: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(item.text)
    Spacer(Modifier.height(8.dp))
    AudioRecorderBubble(
      recordingState = stepContent.recordingState,
      clock = clock,
      onShouldShowRequestPermissionRationale = onShouldShowRequestPermissionRationale,
      startRecording = startRecording,
      stopRecording = stopRecording,
      submitAudioFile = { _ ->
        submitAudioFile()
      },
      redoRecording = redoRecording,
      openAppSettings = openAppSettings,
      freeTextAvailable = true,
      submitFreeText = submitFreeText,
      onShowFreeText = onShowFreeText,
      onShowAudioRecording = onShowAudioRecording,
      onLaunchFullScreenEditText = onLaunchFullScreenEditText,
      canSkip = stepContent.isSkippable,
      onSkip = onSkip,
      isCurrentStep = isCurrentStep,
      freeText = freeText,
    )
  }
}

@Composable
private fun ContentSelectStep(
  item: ClaimIntentStep,
  currentStep: ClaimIntentStep?,
  options: List<StepContent.ContentSelect.Option>,
  selectedOptionId: String?,
  onOptionClick: (StepContent.ContentSelect.Option) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    AnimatedContent(
      item == currentStep,
      transitionSpec = {
        (fadeIn(animationSpec = tween(220, delayMillis = 90))
          .togetherWith(fadeOut(animationSpec = tween(90))))
      },
    ) { targetState ->
      Column {
        HedvigText(
          item.text,
        )
        if (targetState) {
          Spacer(Modifier.height(8.dp))
          ContentSelectChips(
            options = options,
            selectedOption = null,
            onOptionClick = onOptionClick,
          )
        }

      }

    }
    val selected = options.firstOrNull { it.id == selectedOptionId }
    if (
      selected != null
    ) {
      Column {
        Spacer(Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.End,
          modifier = Modifier
            .fillMaxWidth(),
        ) {
          val showChipAnimatable = remember {
            Animatable(0.0f)
          }
          LaunchedEffect(Unit) {
            showChipAnimatable.animateTo(
              1.0f,
              animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
              ),
            )
          }
          HedvigChip(
            item = item,
            showChipAnimatable = showChipAnimatable,
            itemDisplayName = {
              selected.title
            },
            isSelected = false, //should be grey according to figma
            onItemClick = {},
          )
        }
      }

    }
  }


}
