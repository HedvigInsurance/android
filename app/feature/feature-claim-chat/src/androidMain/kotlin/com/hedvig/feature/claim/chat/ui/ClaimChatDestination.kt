package com.hedvig.feature.claim.com.hedvig.feature.claim.chat.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay
import com.hedvig.android.ui.claimflow.HedvigChip
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.ClaimChatUiState
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.FieldId
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepId
import com.hedvig.feature.claim.chat.ui.AudioRecorderBubble
import com.hedvig.feature.claim.chat.ui.BlurredGradientBackground
import com.hedvig.feature.claim.chat.ui.ChatClaimSummary
import com.hedvig.feature.claim.chat.ui.ContentSelectChips
import com.hedvig.feature.claim.chat.ui.DateSelectBubble
import com.hedvig.feature.claim.chat.ui.MultiSelectBubbleWithDialog
import com.hedvig.feature.claim.chat.ui.SingleSelectBubbleWithDialog
import com.hedvig.feature.claim.chat.ui.TextInputBubble
import com.hedvig.feature.claim.chat.ui.YesNoBubble
import hedvig.resources.R
import kotlin.time.Clock
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
    freeTextHint = stringResource(R.string.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER), //todo
    freeTextTitle = stringResource(R.string.CLAIMS_TEXT_INPUT_PLACEHOLDER), //todo
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
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
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
        is StepContent.AudioRecording -> AudioRecordingStep(
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
          freeText = uiState.freeText,
        )

        is StepContent.ContentSelect -> ContentSelectStep(
          item = item,
          currentStep = uiState.currentStep,
          options = item.stepContent.options,
          selectedOptionId = item.stepContent.selectedOptionId,
          onEvent = onEvent,
        )


        is StepContent.FileUpload -> UploadFilesStep(
          itemText = item.text,
          stepContent = item.stepContent,
        )

        is StepContent.Form -> FormStep(
          item = item,
          content = item.stepContent,
          onEvent = onEvent,
          isCurrentStep = item == uiState.currentStep,
          canSkip = item.stepContent.isSkippable,
          canBeChanged = item.stepContent.isRegrettable,
        )

        is StepContent.Summary -> ChatClaimSummary(
          text = item.text,
          recordingUrls = item.stepContent.audioRecordings.map { it.url },
          displayItems = item.stepContent.items.map { (title, value) -> title to value },
          onSubmit = {
            onEvent(ClaimChatEvent.SubmitClaim)
          },
          isCurrentStep = item == uiState.currentStep,
        )

        is StepContent.Task -> TaskStep(
          itemText = item.text,
          taskContent = item.stepContent,
        )

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
private fun UploadFilesStep(
  itemText: String,
  stepContent: StepContent.FileUpload,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(
      itemText,
    )
    Spacer(Modifier.height(8.dp))
//            UploadFilesBubble(
//              isCurrentStep = TODO(),
//              canSkip = stepContent.isSkippable,
//              canBeChanged = stepContent.isRegrettable, //todo
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

@Composable
private fun TaskStep(
  itemText: String,
  taskContent: StepContent.Task,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(itemText)
    if (taskContent.descriptions.isNotEmpty()) {
      Column {
        Spacer(Modifier.height(8.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val color = HedvigTheme.colorScheme.signalGreenElement
          Spacer(
            Modifier
              .wrapContentSize(Alignment.Center)
              .size(20.dp)
              .padding(1.dp)
              .background(color, CircleShape),
          )
          Spacer(Modifier.width(8.dp))
          AnimatedContent(taskContent.descriptions.last()) { target ->
            HedvigChip(
              item = target,
              showChipAnimatable = remember { Animatable(1f) },
              itemDisplayName = {
                target
              },
              isSelected = false, //should be grey according to figma
              onItemClick = {},
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FormStep(
  item: ClaimIntentStep,
  content: StepContent.Form,
  onEvent: (ClaimChatEvent) -> Unit,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  canBeChanged: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(
      item.text,
    )
    Spacer(Modifier.height(32.dp))
    FormContent(
      item = item,
      content = content,
      onSkip = {
        onEvent(ClaimChatEvent.Skip(item.id))
      },
      isCurrentStep = isCurrentStep,
      canSkip = canSkip,
      canBeChanged = canBeChanged,
      onRegret = {
        onEvent(ClaimChatEvent.Regret(item.id))
      },
      onSelectFieldAnswer = { fieldId, answer ->
        onEvent(ClaimChatEvent.SelectFieldAnswer(item.id, fieldId, answer))
      },
      onSubmit = {
        onEvent(ClaimChatEvent.FormSubmit(item.id))
      },
    )
  }
}

@Composable
private fun FormContent(
  item: ClaimIntentStep,
  content: StepContent.Form,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  canBeChanged: Boolean,
  onRegret: () -> Unit,
  onSubmit: () -> Unit,
  onSelectFieldAnswer: (fieldId: FieldId, answer: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (isCurrentStep) {
      content.fields.forEach { field ->
        when (field.type) {
          StepContent.Form.FieldType.TEXT -> {
//          TextInputBubble(
//            //todo
//          )
          }

          StepContent.Form.FieldType.DATE -> {
//          DateSelectBubble(
//            //todo
//          )
          }

          StepContent.Form.FieldType.NUMBER -> {
//          TextInputBubble(
//            //todo
//          )
          } //add: keyboard choice
          StepContent.Form.FieldType.SINGLE_SELECT -> {
//          SingleSelectBubbleWithDialog(
//            //todo
//          )
          }

          StepContent.Form.FieldType.MULTI_SELECT -> {
//          MultiSelectBubbleWithDialog(
//            //todo
//          )
          }

          StepContent.Form.FieldType.BINARY -> YesNoBubble(
            answerSelected = field.selectedOptions.firstOrNull(),
            onSelect = {
              onSelectFieldAnswer(
                field.id,
                it,
              )
            },
            questionText = field.title,
          )

          null -> {
            if (canSkip) {
              onSkip()      //todo: check
            }
          }
        }
      }
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        text = stringResource(R.string.general_continue_button),
        enabled = true,
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth(),
      )
      if (canSkip) {
        HedvigButton(
          text = stringResource(R.string.claims_skip_button),
          enabled = true,
          onClick = onSkip,
          modifier = Modifier.fillMaxWidth(),
          buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
        )
      }
    } else {
      content.fields.forEach { field ->
        val textValue = field.selectedOptions.joinToString()
        if (textValue.isNotEmpty()) {
          Column(Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End) {
            HedvigText(field.title, style = HedvigTheme.typography.label,
              color = HedvigTheme.colorScheme.textAccordion)
            Spacer(Modifier.height(4.dp))
            HedvigChip(
              item = textValue,
              showChipAnimatable = remember { Animatable(1f) },
              itemDisplayName = {
                textValue
              },
              isSelected = false,
              onItemClick = {},
            )
          }
        }
      }
      EditButton(canBeChanged, onRegret)
    }
  }
}

@Composable
private fun EditButton(
  canBeChanged: Boolean,
  onRegret: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (canBeChanged) {
    Row(
      modifier = modifier.fillMaxWidth().padding(top = 8.dp),
      horizontalArrangement = Arrangement.End) {
      HedvigButton(
        text = stringResource(R.string.claims_edit_button),
        enabled = true,
        onClick = onRegret,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        buttonSize = ButtonDefaults.ButtonSize.Small
      )
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
    Spacer(Modifier.height(32.dp))
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
  onEvent: (ClaimChatEvent) -> Unit,
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
          Spacer(Modifier.height(32.dp))
          ContentSelectChips(
            options = options,
            selectedOption = null,
            onOptionClick = { option ->
              onEvent(
                ClaimChatEvent.Select(
                  item.id,
                  option.id,
                ),
              )
            },
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
        EditButton(item.stepContent.isRegrettable,
          onRegret = {
            onEvent(ClaimChatEvent.Regret(item.id))
          })
      }
    }
  }
}
