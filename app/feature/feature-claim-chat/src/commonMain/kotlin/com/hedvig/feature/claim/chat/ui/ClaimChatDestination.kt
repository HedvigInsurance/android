package com.hedvig.feature.claim.chat.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DatePickerUiState
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay
import com.hedvig.android.logger.logcat
import com.hedvig.android.ui.claimflow.HedvigChip
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.ClaimChatUiState
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.FieldId
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepId
import hedvig.resources.CLAIMS_TEXT_INPUT_PLACEHOLDER
import hedvig.resources.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER
import hedvig.resources.Res
import hedvig.resources.claims_edit_button
import hedvig.resources.claims_skip_button
import hedvig.resources.general_continue_button
import hedvig.resources.general_error
import hedvig.resources.something_went_wrong
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ClaimChatDestination(
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
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
      onNavigateToImageViewer = onNavigateToImageViewer,
      appPackageId = appPackageId,
      imageLoader = imageLoader,
    )
  }
}

@Composable
internal fun ClaimChatScreenContent(
  claimChatViewModel: ClaimChatViewModel,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
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
          onNavigateToImageViewer = onNavigateToImageViewer,
          appPackageId = appPackageId,
          imageLoader = imageLoader,
      )
    }
  }
}

@Composable
private fun ClaimChatScreen(
  uiState: ClaimChatUiState.ClaimChat,
  onEvent: (ClaimChatEvent) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
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
          onNavigateToImageViewer = onNavigateToImageViewer,
          appPackageId = appPackageId,
          imageLoader = imageLoader,
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
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  if (uiState.errorSubmittingStep != null) {
    ErrorDialog(
      title = org.jetbrains.compose.resources.stringResource(Res.string.general_error),
      message = uiState.errorSubmittingStep.message
        ?: org.jetbrains.compose.resources.stringResource(Res.string.something_went_wrong),
      onDismiss = {
        onEvent(ClaimChatEvent.DismissErrorDialog)
      },
    )
  }
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
      val isCurrentStep = item.id == uiState.currentStep?.id
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
          isCurrentStep = isCurrentStep,
          clock = Clock.System,
          onShouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          openAppSettings = openAppSettings,
          freeText = uiState.freeText,
        )

        is StepContent.ContentSelect -> ContentSelectStep(
          item = item,
          isCurrentStep = isCurrentStep,
          options = item.stepContent.options,
          selectedOptionId = item.stepContent.selectedOptionId,
          onEvent = onEvent,
        )


        is StepContent.FileUpload -> UploadFilesStep(
          itemText = item.text,
          isCurrentStep = isCurrentStep,
          stepContent = item.stepContent,
          itemId = item.id,
          onNavigateToImageViewer = onNavigateToImageViewer,
          appPackageId = appPackageId,
          imageLoader = imageLoader,
          localFiles = item.stepContent.localFiles,
          onEvent = onEvent,
        )

        is StepContent.Form -> FormStep(
          itemId = item.id,
          itemText = item.text,
          content = item.stepContent,
          onEvent = onEvent,
          isCurrentStep = isCurrentStep,
          canSkip = item.stepContent.isSkippable,
          canBeChanged = item.isRegrettable,
        )

        is StepContent.Summary -> ChatClaimSummary(
          text = item.text,
          recordingUrls = item.stepContent.audioRecordings.map { it.url },
          displayItems = item.stepContent.items.map { (title, value) -> title to value },
          onSubmit = {
            onEvent(ClaimChatEvent.SubmitClaim(item.id))
          },
          isCurrentStep = isCurrentStep,
        )

        is StepContent.Task -> TaskStep(
          itemText = item.text,
          taskContent = item.stepContent,
        )

        StepContent.Unknown -> HedvigText("Unknown") //todo
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
  itemId: StepId,
  itemText: String,
  stepContent: StepContent.FileUpload,
  appPackageId: String,
  isCurrentStep: Boolean,
  imageLoader: ImageLoader,
  localFiles: List<UiFile>,
  onEvent: (ClaimChatEvent) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(
      itemText,
    )
    if (isCurrentStep) {
      Spacer(Modifier.height(8.dp))
      UploadFilesBubble(
        addLocalFile = { uri ->
          onEvent(
              ClaimChatEvent.AddFile(
                  itemId,
                  uri.toString(), //todo: check!
              ),
          )
        },
        onRemoveFile = { fileId ->
          onEvent(
              ClaimChatEvent.RemoveFile(
                  itemId,
                  fileId,
              ),
          )
        },
        appPackageId = appPackageId,
        localFiles = localFiles,
        imageLoader = imageLoader,
        onNavigateToImageViewer = onNavigateToImageViewer,
      )
      Spacer(Modifier.height(8.dp))
      if (stepContent.localFiles.isNotEmpty()) {
        HedvigButton(
          text = stringResource(Res.string.general_continue_button),
          enabled = true, //todo
          onClick = {
            onEvent(
              ClaimChatEvent.FileSubmit(
                itemId,
              ),
            )
          },
          modifier = Modifier.fillMaxWidth(),
        )
      }
      if (stepContent.isSkippable) {
        HedvigButton(
          text = stringResource(Res.string.claims_skip_button),
          enabled = true,
          onClick = {
            onEvent(ClaimChatEvent.Skip(itemId))
          },
          modifier = Modifier.fillMaxWidth(),
          buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
        )
      }
    }
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
              isSelected = false,
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
  itemId: StepId,
  itemText: String,
  content: StepContent.Form,
  onEvent: (ClaimChatEvent) -> Unit,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  canBeChanged: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(
      itemText,
    )
    Spacer(Modifier.height(32.dp))
    FormContent(
      content = content,
      onSkip = {
        onEvent(ClaimChatEvent.Skip(itemId))
      },
      isCurrentStep = isCurrentStep,
      canSkip = canSkip,
      canBeChanged = canBeChanged,
      onRegret = {
        onEvent(ClaimChatEvent.Regret(itemId))
      },
      onSelectFieldAnswer = { fieldId, answer ->
        logcat { "Mariia. onSelectFieldAnswer answer: $answer" }
        onEvent(ClaimChatEvent.UpdateFieldAnswer(itemId, fieldId, answer))
      },
      onSubmit = {
        onEvent(ClaimChatEvent.FormSubmit(itemId))
      },
    )
  }
}

@Composable
private fun FormContent(
  content: StepContent.Form,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  canBeChanged: Boolean,
  onRegret: () -> Unit,
  onSubmit: () -> Unit,
  onSelectFieldAnswer: (fieldId: FieldId, answer: StepContent.Form.FieldOption?) -> Unit,
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
            TextInputBubble(
              questionLabel = field.title,
              text = field.selectedOptions.getOrNull(0)?.text,
              suffix = field.suffix,
              onInput = { answer ->
                onSelectFieldAnswer(field.id,
                  answer?.let {StepContent.Form.FieldOption(it,it)})
              },
            )
          }

          StepContent.Form.FieldType.DATE -> {
            DateSelectBubble(
              questionLabel = field.title,
              datePickerState = field.datePickerUiState!!, //todo - check "!!"
              modifier = Modifier.fillMaxWidth(),
            )
          }

          StepContent.Form.FieldType.NUMBER -> {
            TextInputBubble(
              questionLabel = field.title,
              text = field.selectedOptions.getOrNull(0)?.text,
              suffix = field.suffix,
              onInput = { answer ->
                onSelectFieldAnswer(field.id,
                  answer?.let {StepContent.Form.FieldOption(it,it)})
              },
              keyboardType = KeyboardType.Number,
            )
          }

          StepContent.Form.FieldType.SINGLE_SELECT -> {
            SingleSelectBubbleWithDialog(
              questionLabel = field.title,
              options = field.options.map {
                RadioOption(
                  id = RadioOptionId(it.value),
                  text = it.text,
                  iconResource = null,
                )
              },
              selectedOptionId = field.selectedOptions.getOrNull(0)?.let { selected ->
                val option = field.options.firstOrNull { it.value == selected.value }
                if (option != null)
                  RadioOptionId(option.value) else null
              },
              onSelect = { optionId ->
                onSelectFieldAnswer(
                  field.id,
                  field.options.firstOrNull { it.value == optionId.id },
                )
              },
              modifier = Modifier.fillMaxWidth(),
            )
          }

          StepContent.Form.FieldType.MULTI_SELECT -> {
            MultiSelectBubbleWithDialog(
              questionLabel = field.title,
              options = field.options.map {
                RadioOption(
                  id = RadioOptionId(it.value),
                  text = it.text,
                  iconResource = null,
                )
              },
              selectedOptionIds = field.selectedOptions.mapNotNull { selected ->
                field.options.firstOrNull { it.value == selected.value }
                  ?.let { RadioOptionId(it.value) }
              },
              onSelect = { option ->
                onSelectFieldAnswer(
                  field.id,
                  field.options.firstOrNull {
                    it.value == option.id },
                )
              },
              modifier = Modifier.fillMaxWidth(),
            )
          }

          StepContent.Form.FieldType.BINARY -> YesNoBubble(
            answerSelected = field.selectedOptions.firstOrNull()?.text,
            onSelect = {
              onSelectFieldAnswer(
                field.id,
                StepContent.Form.FieldOption(it,it)
              )
            },
            questionText = field.title,
          )

          null -> {
            if (canSkip) {
              onSkip()
            }
          }
        }
      }
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        text = stringResource(Res.string.general_continue_button),
        enabled = content.canContinue(),
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth(),
      )
      if (canSkip) {
        HedvigButton(
          text = stringResource(Res.string.claims_skip_button),
          enabled = true,
          onClick = onSkip,
          modifier = Modifier.fillMaxWidth(),
          buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
        )
      }
    } else {
      content.fields.forEach { field ->
        val textValue = field.selectedOptions.joinToString { it.text }
        if (textValue.isNotEmpty()) {
          Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
          ) {
            HedvigText(
              field.title, style = HedvigTheme.typography.label,
              color = HedvigTheme.colorScheme.textAccordion,
            )
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
  modifier: Modifier = Modifier,
) {
  if (canBeChanged) {
    Row(
      modifier = modifier.fillMaxWidth().padding(top = 8.dp),
      horizontalArrangement = Arrangement.End,
    ) {
      HedvigButton(
        text = stringResource(Res.string.claims_edit_button),
        enabled = true,
        onClick = onRegret,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        buttonSize = ButtonDefaults.ButtonSize.Small,
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
      submitAudioFile = {
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
  isCurrentStep: Boolean,
  options: List<StepContent.ContentSelect.Option>,
  selectedOptionId: String?,
  onEvent: (ClaimChatEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(
      item.text,
    )
    AnimatedContent(
      isCurrentStep,
      transitionSpec = {
        (fadeIn() + scaleIn()).togetherWith(fadeOut())
      },
    ) { targetState ->
      Column {
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
    AnimatedContent(
      options.firstOrNull { it.id == selectedOptionId },
      transitionSpec = {
        (fadeIn() + scaleIn()).togetherWith(fadeOut())
      },
    ) { targetState ->
      if (targetState != null) {
        Column {
          Spacer(Modifier.height(8.dp))
          Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
              .fillMaxWidth(),
          ) {
            HedvigChip(
              item = item,
              showChipAnimatable = remember {
                Animatable(1.0f)
              },
              itemDisplayName = {
                targetState.title
              },
              isSelected = false,
              onItemClick = {},
            )
          }
          EditButton(
            item.isRegrettable,
            onRegret = {
              onEvent(ClaimChatEvent.Regret(item.id))
            },
          )
        }
      }
    }
  }
}
