package com.hedvig.feature.claim.chat.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay
import com.hedvig.android.design.system.hedvig.icon.ArrowDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.ClaimChatUiState
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import com.hedvig.feature.claim.chat.FreeTextRestrictions
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.FieldId
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepId
import com.hedvig.feature.claim.chat.ui.audiorecording.AudioRecorderBubble
import hedvig.resources.CLAIMS_TEXT_INPUT_PLACEHOLDER
import hedvig.resources.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER
import hedvig.resources.CLAIM_CHAT_EDIT_EXPLANATION
import hedvig.resources.CLAIM_CHAT_FORM_NUMBER_MAX_CHAR
import hedvig.resources.CLAIM_CHAT_FORM_NUMBER_MIN_CHAR
import hedvig.resources.CLAIM_CHAT_FORM_REQUIRED_FIELD
import hedvig.resources.CLAIM_CHAT_SKIPPED_LABEL
import hedvig.resources.GENERAL_ARE_YOU_SURE
import hedvig.resources.Res
import hedvig.resources.claims_edit_button
import hedvig.resources.claims_skip_button
import hedvig.resources.general_continue_button
import hedvig.resources.general_error
import hedvig.resources.important_message_read_more
import hedvig.resources.something_went_wrong
import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ClaimChatDestination(
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToClaimOutcome: (ClaimIntentOutcome) -> Unit,
  navigateToDeflect: (StepContent.Deflect) -> Unit,
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
      claimChatViewModel = claimChatViewModel,
      shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
      openAppSettings = openAppSettings,
      onNavigateToImageViewer = onNavigateToImageViewer,
      navigateToClaimOutcome = {
        claimChatViewModel.emit(ClaimChatEvent.HandledOutcomeNavigation)
        navigateToClaimOutcome(it)
      },
      navigateToDeflect = { stepId, deflect ->
        claimChatViewModel.emit(ClaimChatEvent.HandledDeflectNavigation(stepId))
        navigateToDeflect(deflect)
      },
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
  navigateToClaimOutcome: (ClaimIntentOutcome) -> Unit,
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
) {
  val uiState = claimChatViewModel.uiState.collectAsState().value

  Box(Modifier.fillMaxSize(), Alignment.Center) {
    when (uiState) {
      ClaimChatUiState.FailedToStart -> BasicText("FailedToStart") // todo
      ClaimChatUiState.Initializing -> HedvigFullScreenCenterAlignedProgress()
      is ClaimChatUiState.ClaimChat -> {
        if (uiState.outcome != null) {
          LaunchedEffect(uiState.outcome) {
            navigateToClaimOutcome(uiState.outcome)
          }
        }
        ClaimChatScreen(
          uiState = uiState,
          onEvent = claimChatViewModel::emit,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          openAppSettings = openAppSettings,
          onNavigateToImageViewer = onNavigateToImageViewer,
          navigateToDeflect = navigateToDeflect,
          appPackageId = appPackageId,
          imageLoader = imageLoader,
        )
      }
    }
  }
}

@Composable
private fun ClaimChatScreen(
  uiState: ClaimChatUiState.ClaimChat,
  onEvent: (ClaimChatEvent) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
  openAppSettings: () -> Unit,
) {
  FreeTextOverlay(
    freeTextMaxLength = uiState.showFreeTextOverlay?.maxLength ?: 2000,
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
    shouldShowOverlay = uiState.showFreeTextOverlay != null,
    overlaidContent = {
      ClaimChatScreenContent(
        uiState = uiState,
        onEvent = onEvent,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        openAppSettings = openAppSettings,
        onNavigateToImageViewer = onNavigateToImageViewer,
        navigateToDeflect = navigateToDeflect,
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
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  if (uiState.errorSubmittingStep != null) {
    ErrorDialog(
      title = stringResource(Res.string.general_error),
      message = uiState.errorSubmittingStep.message
        ?: stringResource(Res.string.something_went_wrong),
      onDismiss = {
        onEvent(ClaimChatEvent.DismissErrorDialog)
      },
    )
  }
  if (uiState.showConfirmEditDialogForStep != null) {
    HedvigAlertDialog(
      title = stringResource(Res.string.GENERAL_ARE_YOU_SURE),
      text = stringResource(Res.string.CLAIM_CHAT_EDIT_EXPLANATION),
      onDismissRequest = {
        onEvent(ClaimChatEvent.DismissConfirmEditDialog)
      },
      onConfirmClick = {
        onEvent(ClaimChatEvent.Regret(uiState.showConfirmEditDialogForStep))
      },
    )
  }
  val lazyListState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val isScrolled by remember(lazyListState) {
    derivedStateOf {
      lazyListState.lastScrolledBackward || (lazyListState.lastScrolledForward &&
        lazyListState.firstVisibleItemIndex < uiState.steps.lastIndex - 1)
    }
  }
  Box(
    modifier = modifier
      .padding(horizontal = 16.dp)
      .fillMaxSize(),
  ) {


    LazyColumn(
      modifier = Modifier.fillMaxSize(),
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
        val isLastItem = item == uiState.steps.lastOrNull()
        if (isLastItem) {
          Column(
            modifier = Modifier.fillParentMaxHeight(),
          ) {
            StepContentSection(
              stepItem = item,
              freeText = uiState.freeText,
              isCurrentStep = isCurrentStep,
              currentContinueButtonLoading = uiState.currentContinueButtonLoading,
              currentSkipButtonLoading = uiState.currentSkipButtonLoading,
              autoNavigateForDeflectStepId = uiState.autoNavigateForDeflectStepId,
              onEvent = onEvent,
              shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
              onNavigateToImageViewer = onNavigateToImageViewer,
              navigateToDeflect = navigateToDeflect,
              appPackageId = appPackageId,
              imageLoader = imageLoader,
              openAppSettings = openAppSettings,
              spacerModifier = Modifier.weight(1f),
              showBottomContent = !isScrolled,
              showFakeAiDot = uiState.showFakeAiDot,
            )
          }
        } else {
          StepContentSection(
            stepItem = item,
            freeText = uiState.freeText,
            isCurrentStep = isCurrentStep,
            currentContinueButtonLoading = uiState.currentContinueButtonLoading,
            currentSkipButtonLoading = uiState.currentSkipButtonLoading,
            autoNavigateForDeflectStepId = uiState.autoNavigateForDeflectStepId,
            onEvent = onEvent,
            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
            onNavigateToImageViewer = onNavigateToImageViewer,
            navigateToDeflect = navigateToDeflect,
            appPackageId = appPackageId,
            imageLoader = imageLoader,
            openAppSettings = openAppSettings,
            spacerModifier = Modifier,
            showBottomContent = true,
            showFakeAiDot = false,
          )
        }
      }
    }
    if (isScrolled) {
      ScrollToBottomButton(
        onClick = {
          coroutineScope.launch {
            lazyListState.animateScrollToItem(index = uiState.steps.lastIndex)
          }
        },
        modifier = Modifier.align(Alignment.BottomCenter).padding(
          WindowInsets.safeDrawing.asPaddingValues()
            .plus(PaddingValues(vertical = 16.dp)),
        ),
      )
    }
  }
  LaunchedEffect(uiState.steps.size) {
    if (uiState.steps.isNotEmpty()) {
      lazyListState.animateScrollToItem(index = uiState.steps.lastIndex)
    }
  }
}

@Composable
private fun ScrollToBottomButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  IconButton(
    onClick = onClick,
    modifier = modifier,
  ) {
    val whiteColor = HedvigTheme.colorScheme.backgroundWhite
    Box(
      contentAlignment = Alignment.Center,
    ) {
      Canvas(
        modifier = Modifier.size(42.dp),
        onDraw = {
          drawCircle(color = whiteColor)
        },
      )
      Icon(
        HedvigIcons.ArrowDown,
        "Scroll down", //todo
      )
    }
  }
}

@Composable
private fun StepContentSection(
  stepItem: ClaimIntentStep,
  freeText: String?,
  isCurrentStep: Boolean,
  currentContinueButtonLoading: Boolean,
  currentSkipButtonLoading: Boolean,
  autoNavigateForDeflectStepId: StepId?,
  onEvent: (ClaimChatEvent) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
  openAppSettings: () -> Unit,
  spacerModifier: Modifier,
  showBottomContent: Boolean,
  showFakeAiDot: Boolean,
) {
  AnimatedContent(
    showFakeAiDot,
    transitionSpec = {
    fadeIn(animationSpec = tween(220, delayMillis = 90))
      .togetherWith(fadeOut(animationSpec = tween(90)))
  }
  ) { target ->
    if (target && stepItem.stepContent !is StepContent.Task) {
      LaunchedEffect(Unit) {
        delay(1000)
        onEvent(ClaimChatEvent.FakeGreenAiDotShown)
      }
      BlinkingAiDot()
    } else {
      Column {
        stepItem.text?.let {
          HedvigText(stepItem.text)
        }
        if (stepItem.stepContent is StepContent.Task) {
          Spacer(Modifier.height(16.dp))
          TaskStep(
            taskContent = stepItem.stepContent,
          )
        }
        stepItem.text?.let {
          Spacer(Modifier.height(16.dp))
        }
        AnimatedVisibility(
          visible = showBottomContent,
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          StepBottomContent(
            stepItem = stepItem,
            freeText = freeText,
            isCurrentStep = isCurrentStep,
            currentContinueButtonLoading = currentContinueButtonLoading,
            currentSkipButtonLoading = currentSkipButtonLoading,
            autoNavigateForDeflectStepId = autoNavigateForDeflectStepId,
            onEvent = onEvent,
            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
            onNavigateToImageViewer = onNavigateToImageViewer,
            navigateToDeflect = navigateToDeflect,
            appPackageId = appPackageId,
            imageLoader = imageLoader,
            openAppSettings = openAppSettings,
            spacerModifier = spacerModifier
          )
        }
      }
    }
  }
}

@Composable
private fun StepBottomContent(
  stepItem: ClaimIntentStep,
  freeText: String?,
  isCurrentStep: Boolean,
  currentContinueButtonLoading: Boolean,
  currentSkipButtonLoading: Boolean,
  autoNavigateForDeflectStepId: StepId?,
  onEvent: (ClaimChatEvent) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
  openAppSettings: () -> Unit,
  spacerModifier: Modifier,
) {
  Column {
    Spacer(spacerModifier)
    when (stepItem.stepContent) {
      is StepContent.AudioRecording -> AudioRecordingStep(
        item = stepItem,
        stepContent = stepItem.stepContent,
        onShowFreeText = {
          onEvent(ClaimChatEvent.AudioRecording.ShowFreeText(stepItem.id))
        },
        onShowAudioRecording = {
          onEvent(ClaimChatEvent.AudioRecording.ShowAudioRecording(stepItem.id))
        },
        onLaunchFullScreenEditText = { restrictions ->
          onEvent(ClaimChatEvent.OpenFreeTextOverlay(restrictions))
        },
        startRecording = {
          onEvent(ClaimChatEvent.AudioRecording.StartRecording(stepItem.id))
        },
        stopRecording = {
          onEvent(ClaimChatEvent.AudioRecording.StopRecording(stepItem.id))
        },
        redoRecording = {
          onEvent(ClaimChatEvent.AudioRecording.RedoRecording(stepItem.id))
        },
        submitFreeText = {
          onEvent(ClaimChatEvent.AudioRecording.SubmitTextInput(stepItem.id))
        },
        submitAudioFile = {
          onEvent(ClaimChatEvent.AudioRecording.SubmitAudioFile(stepItem.id))
        },
        onSkip = {
          onEvent(ClaimChatEvent.Skip(stepItem.id))
        },
        isCurrentStep = isCurrentStep,
        clock = Clock.System,
        onShouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        openAppSettings = openAppSettings,
        freeText = freeText,
        onEvent = onEvent,
        continueButtonLoading = currentContinueButtonLoading,
        skipButtonLoading = currentSkipButtonLoading,
      )

      is StepContent.ContentSelect -> ContentSelectStep(
        item = stepItem,
        isCurrentStep = isCurrentStep,
        options = stepItem.stepContent.options,
        selectedOptionId = stepItem.stepContent.selectedOptionId,
        onEvent = onEvent,
      )

      is StepContent.FileUpload -> UploadFilesStep(
        isCurrentStep = isCurrentStep,
        stepContent = stepItem.stepContent,
        itemId = stepItem.id,
        onNavigateToImageViewer = onNavigateToImageViewer,
        appPackageId = appPackageId,
        imageLoader = imageLoader,
        localFiles = stepItem.stepContent.localFiles,
        onEvent = onEvent,
        canEdit = stepItem.isRegrettable,
        continueButtonLoading = currentContinueButtonLoading,
        skipButtonLoading = currentSkipButtonLoading,
      )

      is StepContent.Form -> FormStep(
        itemId = stepItem.id,
        content = stepItem.stepContent,
        onEvent = onEvent,
        isCurrentStep = isCurrentStep,
        canSkip = stepItem.stepContent.isSkippable,
        canBeChanged = stepItem.isRegrettable,
        continueButtonLoading = currentContinueButtonLoading,
        skipButtonLoading = currentSkipButtonLoading,
      )

      is StepContent.Summary -> ChatClaimSummary(
        recordingUrls = stepItem.stepContent.audioRecordings.map { it.url },
        displayItems = stepItem.stepContent.items.map { (title, value) -> title to value },
        onSubmit = {
          onEvent(ClaimChatEvent.SubmitClaim(stepItem.id))
        },
        isCurrentStep = isCurrentStep,
        onNavigateToImageViewer = onNavigateToImageViewer,
        imageLoader = imageLoader,
        fileUploads = stepItem.stepContent.fileUploads.map {
          UiFile(
            name = it.fileName,
            localPath = null,
            url = it.url,
            mimeType = it.contentType,
            id = it.url,
          )
        },
        freeTexts = stepItem.stepContent.freeTexts,
        continueButtonLoading = currentContinueButtonLoading,
        spacerModifier = spacerModifier,
      )

      is StepContent.Deflect -> {
        DeflectStep(
          stepId = stepItem.id,
          text = stepItem.text,
          deflect = stepItem.stepContent,
          navigateToDeflect = navigateToDeflect,
          autoNavigateForDeflectStepId = autoNavigateForDeflectStepId,
        )
      }

      is StepContent.Task -> {}

      StepContent.Unknown -> HedvigText("Unknown") // todo
    }
  }
}

@Composable
private fun UploadFilesStep(
  itemId: StepId,
  stepContent: StepContent.FileUpload,
  appPackageId: String,
  isCurrentStep: Boolean,
  canEdit: Boolean,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  imageLoader: ImageLoader,
  localFiles: List<UiFile>,
  onEvent: (ClaimChatEvent) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    if (isCurrentStep) {
      UploadFilesBubble(
        addLocalFile = { uri ->
          onEvent(
            ClaimChatEvent.AddFile(
              itemId,
              uri.toString(), // todo: check!
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
          enabled = !continueButtonLoading,
          onClick = {
            onEvent(
              ClaimChatEvent.FileSubmit(
                itemId,
              ),
            )
          },
          isLoading = continueButtonLoading,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      if (stepContent.isSkippable && stepContent.localFiles.isEmpty()) {
        HedvigButton(
          text = stringResource(Res.string.claims_skip_button),
          enabled = !skipButtonLoading,
          onClick = {
            onEvent(ClaimChatEvent.Skip(itemId))
          },
          isLoading = skipButtonLoading,
          modifier = Modifier.fillMaxWidth(),
          buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
        )
      }
    } else {
      if (localFiles.isNotEmpty()) {
        FilesRow(
          uiFiles = localFiles,
          onRemoveFile = null,
          imageLoader = imageLoader,
          onNavigateToImageViewer = onNavigateToImageViewer,
          alignment = Alignment.End,
        )
      } else {
        SkippedLabel()
      }
      EditButton(
        canEdit,
        onRegret = {
          onEvent(ClaimChatEvent.ShowConfirmEditDialog(itemId))
        },
      )
    }
  }
}

@Composable
internal fun SkippedLabel() {
  Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End,
  ) {
    val skippedLabelText = stringResource(Res.string.CLAIM_CHAT_SKIPPED_LABEL)
    MemberSentAnswer(
      onClick = null,
    ) {
      HedvigText(
        skippedLabelText,
        color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      )
    }
  }
}

@Composable
private fun DeflectStep(
  stepId: StepId,
  text: String?,
  deflect: StepContent.Deflect,
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  autoNavigateForDeflectStepId: StepId?,
  modifier: Modifier = Modifier,
) {
  if (autoNavigateForDeflectStepId != null) {
    LaunchedEffect(Unit) {
      navigateToDeflect(stepId, deflect)
    }
  }
  if (text != null) {
    HedvigNotificationCard(
      message = text,
      priority = NotificationDefaults.NotificationPriority.InfoInline,
      style = NotificationDefaults.InfoCardStyle.Button(
        buttonText = stringResource(Res.string.important_message_read_more),
        onButtonClick = { navigateToDeflect(stepId, deflect) },
      ),
      modifier = modifier,
    )
  }

}

@Composable
private fun TaskStep(
  taskContent: StepContent.Task,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {

    if (taskContent.descriptions.isNotEmpty()) {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          BlinkingAiDot()
          if (taskContent.descriptions.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            AnimatedContent(taskContent.descriptions.last()) { target ->
              MemberSentAnswer(
                onClick = null,
              ) {
                HedvigText(target)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BlinkingAiDot() {
  val infiniteTransition = rememberInfiniteTransition(label = "blink")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 500),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "alpha",
  )
  val color = HedvigTheme.colorScheme.signalGreenElement
  Spacer(
    Modifier
      .wrapContentSize(Alignment.Center)
      .size(20.dp)
      .padding(1.dp)
      .alpha(alpha)
      .background(color, CircleShape),
  )
}

@Composable
private fun FormStep(
  itemId: StepId,
  content: StepContent.Form,
  onEvent: (ClaimChatEvent) -> Unit,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  canBeChanged: Boolean,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    FormContent(
      content = content,
      onSkip = {
        onEvent(ClaimChatEvent.Skip(itemId))
      },
      isCurrentStep = isCurrentStep,
      canSkip = canSkip,
      canBeChanged = canBeChanged,
      onRegret = {
        onEvent(ClaimChatEvent.ShowConfirmEditDialog(itemId))
      },
      onSelectFieldAnswer = { fieldId, answer ->
        onEvent(ClaimChatEvent.UpdateFieldAnswer(itemId, fieldId, answer))
      },
      onSubmit = {
        onEvent(ClaimChatEvent.SubmitForm(itemId))
      },
      continueButtonLoading = continueButtonLoading,
      skipButtonLoading = skipButtonLoading,
    )
  }
}

@Composable
private fun getErrorText(field: StepContent.Form.Field): String? {
  return when (field.hasError) {
    StepContent.Form.FieldError.Missing -> stringResource(Res.string.CLAIM_CHAT_FORM_REQUIRED_FIELD)
    StepContent.Form.FieldError.LessThanMinValue -> stringResource(Res.string.CLAIM_CHAT_FORM_NUMBER_MIN_CHAR)
    StepContent.Form.FieldError.BiggerThanMaxValue -> stringResource(Res.string.CLAIM_CHAT_FORM_NUMBER_MAX_CHAR)
    null -> null
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
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  onSelectFieldAnswer: (fieldId: FieldId, answer: StepContent.Form.FieldOption?) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier,
  ) {
    if (isCurrentStep) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        content.fields.forEach { field ->
          when (field.type) {
            StepContent.Form.FieldType.TEXT -> {
              TextInputBubble(
                questionLabel = field.title,
                text = field.selectedOptions.getOrNull(0)?.text,
                suffix = field.suffix,
                onInput = { answer ->
                  onSelectFieldAnswer(
                    field.id,
                    answer?.let { StepContent.Form.FieldOption(it, it) },
                  )
                },
                errorText = getErrorText(field),
              )
            }

            StepContent.Form.FieldType.DATE -> {
              DateSelectBubble(
                questionLabel = field.title,
                datePickerState = field.datePickerUiState!!, // todo - check "!!"
                modifier = Modifier.fillMaxWidth(),
                errorText = getErrorText(field),
              )
            }

            StepContent.Form.FieldType.NUMBER -> {
              TextInputBubble(
                questionLabel = field.title,
                text = field.selectedOptions.getOrNull(0)?.text,
                suffix = field.suffix,
                onInput = { answer ->
                  onSelectFieldAnswer(
                    field.id,
                    answer?.let { StepContent.Form.FieldOption(it, it) },
                  )
                },
                keyboardType = KeyboardType.Number,
                errorText = getErrorText(field),
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
                  if (option != null) {
                    RadioOptionId(option.value)
                  } else {
                    null
                  }
                },
                onSelect = { optionId ->
                  onSelectFieldAnswer(
                    field.id,
                    field.options.firstOrNull { it.value == optionId.id },
                  )
                },
                modifier = Modifier.fillMaxWidth(),
                errorText = getErrorText(field),
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
                      it.value == option.id
                    },
                  )
                },
                modifier = Modifier.fillMaxWidth(),
                errorText = getErrorText(field),
              )
            }

            StepContent.Form.FieldType.BINARY -> YesNoBubble(
              answerSelected = field.selectedOptions.firstOrNull()?.text,
              onSelect = {
                onSelectFieldAnswer(
                  field.id,
                  StepContent.Form.FieldOption(it, it),
                )
              },
              questionText = field.title,
              errorText = getErrorText(field),
            )

            null -> {
              if (canSkip) {
                onSkip()
              }
            }
          }
        }
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(Res.string.general_continue_button),
        enabled = !continueButtonLoading,
        isLoading = continueButtonLoading,
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth(),
      )
      if (canSkip) {
        Spacer(Modifier.height(8.dp))
        HedvigButton(
          text = stringResource(Res.string.claims_skip_button),
          enabled = !skipButtonLoading,
          onClick = onSkip,
          isLoading = skipButtonLoading,
          modifier = Modifier.fillMaxWidth(),
          buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
        )
      }
    } else {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (content.fields.flatMap { it.selectedOptions }.isNotEmpty()) {
          content.fields.forEach { field ->
            val textValue = field.selectedOptions.joinToString { it.text }
            Column(
              Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.End,
            ) {
              HedvigText(
                field.title,
                style = HedvigTheme.typography.label,
                color = HedvigTheme.colorScheme.textAccordion,
              )
              if (textValue.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                MemberSentAnswer(
                  onClick = null,
                ) {
                  HedvigText(textValue)
                }
              } else {
                SkippedLabel()
              }
            }
          }
        } else {
          SkippedLabel()
        }
      }
      EditButton(canBeChanged, onRegret)
    }
  }
}

@Composable
private fun EditButton(canBeChanged: Boolean, onRegret: () -> Unit, modifier: Modifier = Modifier) {
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
  onLaunchFullScreenEditText: (restrictions: FreeTextRestrictions) -> Unit,
  submitFreeText: () -> Unit,
  submitAudioFile: () -> Unit,
  stopRecording: () -> Unit,
  redoRecording: () -> Unit,
  onSkip: () -> Unit,
  isCurrentStep: Boolean,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  clock: Clock,
  onShouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  startRecording: () -> Unit,
  onEvent: (ClaimChatEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
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
      onLaunchFullScreenEditText = {
        onLaunchFullScreenEditText(
          FreeTextRestrictions(
            stepContent.freeTextMinLength,
            stepContent.freeTextMaxLength,
          ),
        )
      },
      canSkip = stepContent.isSkippable,
      onSkip = onSkip,
      isCurrentStep = isCurrentStep,
      freeText = freeText,
      continueButtonLoading = continueButtonLoading,
      skipButtonLoading = skipButtonLoading,
    )
    if (item.isRegrettable && !isCurrentStep) {
      EditButton(
        item.isRegrettable,
        onRegret = {
          onEvent(ClaimChatEvent.ShowConfirmEditDialog(item.id))
        },
      )
    }
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
            MemberSentAnswer(
              onClick = null,
            ) {
              HedvigText(targetState.title)
            }
          }
          EditButton(
            item.isRegrettable,
            onRegret = {
              onEvent(ClaimChatEvent.ShowConfirmEditDialog(item.id))
            },
          )
        }
      }
    }
  }
}
