package com.hedvig.feature.claim.chat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import coil3.ImageLoader
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.design.system.hedvig.TopAppBarColors
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay
import com.hedvig.android.design.system.hedvig.icon.ArrowDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.ClaimChatUiState
import com.hedvig.feature.claim.chat.ClaimChatViewModel
import com.hedvig.feature.claim.chat.data.ClaimIntentOutcome
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepId
import com.hedvig.feature.claim.chat.ui.common.BlinkingAiDot
import com.hedvig.feature.claim.chat.ui.common.RoundCornersPill
import com.hedvig.feature.claim.chat.ui.step.ChatClaimSummaryBottomContent
import com.hedvig.feature.claim.chat.ui.step.ChatClaimSummaryTopContent
import com.hedvig.feature.claim.chat.ui.step.ContentSelectStep
import com.hedvig.feature.claim.chat.ui.step.DeflectStep
import com.hedvig.feature.claim.chat.ui.step.FormStep
import com.hedvig.feature.claim.chat.ui.step.TaskStepBottomContent
import com.hedvig.feature.claim.chat.ui.step.TaskStepTopContent
import com.hedvig.feature.claim.chat.ui.step.UploadFilesStep
import com.hedvig.feature.claim.chat.ui.step.audiorecording.AudioRecordingStep
import hedvig.resources.A11Y_SCROLL_DOWN
import hedvig.resources.CHAT_CONVERSATION_CLAIM_TITLE
import hedvig.resources.CLAIMS_TEXT_INPUT_PLACEHOLDER
import hedvig.resources.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER
import hedvig.resources.CLAIM_CHAT_EDIT_ANSWER_BUTTON
import hedvig.resources.CLAIM_CHAT_EDIT_EXPLANATION
import hedvig.resources.GENERAL_ARE_YOU_SURE
import hedvig.resources.NETWORK_ERROR_ALERT_MESSAGE
import hedvig.resources.Res
import hedvig.resources.claims_alert_body
import hedvig.resources.general_cancel_button
import hedvig.resources.general_error
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
  navigateUp: () -> Unit,
) {
  val claimChatViewModel = koinViewModel<ClaimChatViewModel> {
    parametersOf(isDevelopmentFlow)
  }
  Box(Modifier.fillMaxSize(), propagateMinConstraints = true) {
    BlurredGradientBackground()
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
      navigateUp = navigateUp,
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
  navigateUp: () -> Unit,
) {
  val uiState = claimChatViewModel.uiState.collectAsState().value

  Box(Modifier.fillMaxSize(), Alignment.Center) {
    when (uiState) {
      ClaimChatUiState.FailedToStart -> {
        HedvigErrorSection(
          { claimChatViewModel.emit(ClaimChatEvent.RetryInitializing) },
        )
      }

      ClaimChatUiState.Initializing -> {
        HedvigFullScreenCenterAlignedProgress()
      }

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
          navigateUp = navigateUp,
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
  navigateUp: () -> Unit,
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
        navigateUp = navigateUp,
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
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showCloseFlowDialog by rememberSaveable { mutableStateOf(false) }

  NavigationEventHandler(
    state = rememberNavigationEventState(NavigationEventInfo.None),
    isBackEnabled = uiState.steps.size > 1,
  ) {
    showCloseFlowDialog = true
  }

  if (uiState.errorSubmittingStep != null) {
    ErrorDialog(
      title = stringResource(Res.string.general_error),
      message = stringResource(Res.string.NETWORK_ERROR_ALERT_MESSAGE),
      onDismiss = {
        onEvent(ClaimChatEvent.DismissErrorDialog)
      },
    )
  }
  if (uiState.showConfirmEditDialogForStep != null) {
    HedvigAlertDialog(
      title = stringResource(Res.string.GENERAL_ARE_YOU_SURE),
      text = stringResource(Res.string.CLAIM_CHAT_EDIT_EXPLANATION),
      confirmButtonLabel = stringResource(Res.string.CLAIM_CHAT_EDIT_ANSWER_BUTTON),
      dismissButtonLabel = stringResource(Res.string.general_cancel_button),
      onDismissRequest = {
        onEvent(ClaimChatEvent.DismissConfirmEditDialog)
      },
      onConfirmClick = {
        onEvent(ClaimChatEvent.Regret(uiState.showConfirmEditDialogForStep))
      },
    )
  }
  if (showCloseFlowDialog) {
    HedvigAlertDialog(
      title = stringResource(Res.string.GENERAL_ARE_YOU_SURE),
      text = stringResource(Res.string.claims_alert_body),
      onDismissRequest = {
        showCloseFlowDialog = false
      },
      onConfirmClick = navigateUp,
    )
  }
  val lazyListState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val showScrollArrow by remember(lazyListState, uiState.currentStep?.id) {
    derivedStateOf {
      val layoutInfo = lazyListState.layoutInfo
      val lazyListItemsCount = layoutInfo.totalItemsCount
      val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
      (lastVisibleItem?.index != lazyListItemsCount - 1 && lazyListItemsCount > 0)
    }
  }
  // Track the size of the last item to scroll when it grows
  val lastItemSize by remember(lazyListState, uiState.steps.lastOrNull()?.id) {
    derivedStateOf {
      val layoutInfo = lazyListState.layoutInfo
      val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
      if (lastItem?.index == uiState.steps.lastIndex) {
        lastItem.size
      } else {
        null
      }
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(Modifier.matchParentSize()) {
      TopAppBar(
        title = stringResource(Res.string.CHAT_CONVERSATION_CLAIM_TITLE),
        actionType = TopAppBarActionType.BACK,
        onActionClick = {
          if (uiState.steps.size > 1) {
            showCloseFlowDialog = true
          } else {
            navigateUp()
          }
        },
        customTopAppBarColors = TopAppBarColors(
          containerColor = Color.Transparent,
          contentColor = HedvigTheme.colorScheme.textPrimary,
        ),
      )
      Box(
        Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
      ) {
        HorizontalDivider()
        uiState.progress?.let {
          val animatedProgress = animateFloatAsState(
            targetValue = uiState.progress,
            animationSpec = tween(durationMillis = 1000),
          )
          Box(
            modifier = Modifier
              .height(3.dp)
              .fillMaxWidth(animatedProgress.value)
              .background(
                HedvigTheme.colorScheme.fillPrimary,
                HedvigTheme.shapes.cornerLargeEnd,
              ),
          )
        }
      }
      ClaimChatScrollableContent(
        uiState = uiState,
        lazyListState = lazyListState,
        onEvent = onEvent,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        onNavigateToImageViewer = onNavigateToImageViewer,
        navigateToDeflect = navigateToDeflect,
        appPackageId = appPackageId,
        imageLoader = imageLoader,
        openAppSettings = openAppSettings,
        modifier = Modifier.fillMaxSize(),
      )
    }
    if (showScrollArrow) {
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

  LaunchedEffect(lastItemSize) {
    if (lastItemSize != null && uiState.steps.isNotEmpty()) {
      lazyListState.animateScrollBy(
        value = 3000f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
      )
    }
  }
}

@Composable
private fun ClaimChatScrollableContent(
  uiState: ClaimChatUiState.ClaimChat,
  lazyListState: LazyListState,
  onEvent: (ClaimChatEvent) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  onNavigateToImageViewer: (String, String) -> Unit,
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
  openAppSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current
  val spaceBetweenItems = 16.dp
  val contentPadding = WindowInsets.safeDrawing
    .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
    .asPaddingValues()
    .plus(PaddingValues(16.dp))

  val lastItemHeightAdjustingState = rememberLastItemHeightAdjustingState(
    density = density,
    spaceBetweenItems = spaceBetweenItems,
    steps = uiState.steps,
  )

  Box(modifier, propagateMinConstraints = true) {
    Box(
      Modifier
        .padding(contentPadding)
        .onSizeChanged { size ->
          lastItemHeightAdjustingState.onContainerSizeChanged(size)
        },
    )
    LazyColumn(
      state = lazyListState,
      contentPadding = contentPadding,
      verticalArrangement = Arrangement.spacedBy(spaceBetweenItems, Alignment.Top),
    ) {
      items(
        items = uiState.steps,
        key = { step -> step.id.value },
        contentType = { it.stepContent::class },
      ) { item ->
        val isCurrentStep = item.id == uiState.steps.lastOrNull()?.id
        val showAnimationSequence = isCurrentStep &&
          item.stepContent !is StepContent.Task &&
          !uiState.stepsWithShownAnimations.contains(item.id)
        val isLastItem = item == uiState.steps.lastOrNull()

        StepContentSection(
          stepItem = item,
          freeText = uiState.freeText,
          isCurrentStep = isCurrentStep,
          showAnimationSequence = showAnimationSequence,
          currentContinueButtonLoading = uiState.currentContinueButtonLoading,
          currentSkipButtonLoading = uiState.currentSkipButtonLoading,
          onEvent = onEvent,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          onNavigateToImageViewer = onNavigateToImageViewer,
          navigateToDeflect = navigateToDeflect,
          appPackageId = appPackageId,
          imageLoader = imageLoader,
          openAppSettings = openAppSettings,
          onResponseHeightChanged = { size ->
            lastItemHeightAdjustingState.onItemHeightChanged(item.id, size)
          },
          modifier = if (isLastItem) {
            Modifier.requiredHeightIn(lastItemHeightAdjustingState.preferredMinHeightForFullScreenItem)
          } else {
            Modifier
          },
        )
      }
    }
  }
}

@Composable
private fun ScrollToBottomButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  IconButton(
    onClick = onClick,
    modifier = modifier.size(50.dp),
  ) {
    val whiteColor = HedvigTheme.colorScheme.fillNegative
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.padding(4.dp),
    ) {
      Canvas(
        modifier = Modifier.size(42.dp)
          .shadow(1.dp, shape = CircleShape),
        onDraw = {
          drawCircle(color = whiteColor)
        },
      )
      Icon(
        HedvigIcons.ArrowDown,
        stringResource(Res.string.A11Y_SCROLL_DOWN),
        tint = HedvigTheme.colorScheme.fillPrimary,
        modifier = Modifier.size(24.dp),
      )
    }
  }
}

@Composable
private fun StepContentSection(
  stepItem: ClaimIntentStep,
  freeText: String?,
  isCurrentStep: Boolean,
  showAnimationSequence: Boolean,
  currentContinueButtonLoading: Boolean,
  currentSkipButtonLoading: Boolean,
  onEvent: (ClaimChatEvent) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
  openAppSettings: () -> Unit,
  onResponseHeightChanged: (IntSize) -> Unit,
  modifier: Modifier = Modifier,
) {
  // AnimationSequence has 3 stages one after another:
  // 1) fake ai dot
  // 2) top part of content
  // 3) bottom part of content

  var isAnimationInProcess by rememberSaveable(stepItem.id) {
    mutableStateOf(showAnimationSequence)
  }
  var showAiDot by rememberSaveable(stepItem.id) {
    mutableStateOf(showAnimationSequence)
  }
  var showTopContent by rememberSaveable(stepItem.id) {
    mutableStateOf(!showAnimationSequence)
  }
  var showBottomContent by rememberSaveable(stepItem.id) {
    mutableStateOf(!showAnimationSequence)
  }

  val bottomContentAnimationDuration = 300

  LaunchedEffect(stepItem.id) {
    if (isAnimationInProcess) {
      delay(1000)
      showAiDot = false
      delay(100)
      showTopContent = true
    }
  }

  LaunchedEffect(showBottomContent) {
    if (showBottomContent && isAnimationInProcess) {
      delay(bottomContentAnimationDuration.toLong())
      isAnimationInProcess = false
      onEvent(ClaimChatEvent.AddToShownAnimations(stepItem.id))
    }
  }

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    if (showAiDot) {
      CommonPaddingWrapper {
        BlinkingAiDot()
      }
    } else if (showTopContent) {
      StepTopContent(
        stepItem = stepItem,
        hasAnimation = isAnimationInProcess,
        onAnimationFinished = {
          showBottomContent = true
        },
        onNavigateToImageViewer = onNavigateToImageViewer,
        imageLoader = imageLoader,
      )
    }

    if (showAiDot || showTopContent) {
      Spacer(Modifier.height(32.dp))
    }

    AnimatedVisibility(
      visible = showBottomContent && !isAnimationInProcess,
      enter = fadeIn(animationSpec = tween(bottomContentAnimationDuration)),
      exit = ExitTransition.None,
    ) {
      StepBottomContent(
        stepItem = stepItem,
        freeText = freeText,
        isCurrentStep = isCurrentStep,
        currentContinueButtonLoading = currentContinueButtonLoading,
        currentSkipButtonLoading = currentSkipButtonLoading,
        onEvent = onEvent,
        shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
        onNavigateToImageViewer = onNavigateToImageViewer,
        navigateToDeflect = navigateToDeflect,
        appPackageId = appPackageId,
        imageLoader = imageLoader,
        openAppSettings = openAppSettings,
        modifier = Modifier.onSizeChanged { size ->
          onResponseHeightChanged(size)
        },
      )
    }
  }
}

@Composable
private fun StepTopContent(
  stepItem: ClaimIntentStep,
  hasAnimation: Boolean,
  onAnimationFinished: () -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  val hint = stepItem.hint?.let {
    "\n\n$it"
  }
  val stepItemText = when {
    stepItem.text != null && hint != null -> stepItem.text + hint
    stepItem.text != null -> stepItem.text
    hint != null -> hint
    else -> null
  }

  Column(modifier) {
    if (hasAnimation) {
      if (stepItemText != null) {
        CommonPaddingWrapper {
          AnimatedRevealText(
            text = stepItemText,
            visibleState = remember(stepItem.id) {
              MutableTransitionState(false).apply { targetState = true }
            },
            onAnimationFinished = onAnimationFinished,
            modifier = Modifier.semantics {
              heading()
            },
          )
        }
      } else {
        LaunchedEffect(stepItem.id) {
          onAnimationFinished()
        }
      }
    } else {
      stepItemText?.let {
        CommonPaddingWrapper {
          HedvigText(
            stepItemText,
            Modifier.semantics {
              heading()
            },
          )
        }
      }
    }

    if (stepItem.stepContent is StepContent.Task) {
      TaskStepTopContent(
        taskContent = stepItem.stepContent,
      )
    }

    AnimatedVisibility(
      stepItem.stepContent is StepContent.Summary,
      enter = if (hasAnimation) fadeIn(animationSpec = tween()) else EnterTransition.None,
      exit = ExitTransition.None,
    ) {
      Column {
        stepItemText?.let {
          Spacer(Modifier.height(16.dp))
        }
        if (stepItem.stepContent is StepContent.Summary) {
          ChatClaimSummaryTopContent(
            recordingUrls = stepItem.stepContent.audioRecordings.map { it.url },
            displayItems = stepItem.stepContent.items.map { (title, value) -> title to value },
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
          )
        }
      }
    }
  }
}

// to align blinking dot, task step and animated and not-animated questions to appear in the same place vertically
@Composable
private fun CommonPaddingWrapper(content: @Composable () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    content()
    RoundCornersPill(
      onClick = null,
      modifier = Modifier.withoutPlacement(),
    ) {
      HedvigText("C")
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
  onEvent: (ClaimChatEvent) -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  appPackageId: String,
  imageLoader: ImageLoader,
  openAppSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    when (stepItem.stepContent) {
      is StepContent.AudioRecording -> {
        AudioRecordingStep(
          item = stepItem,
          stepContent = stepItem.stepContent,
          onShowFreeText = {
            onEvent(ClaimChatEvent.AudioRecording.SwitchToFreeText(stepItem.id))
          },
          onSwitchToAudioRecording = {
            onEvent(ClaimChatEvent.AudioRecording.SwitchToAudioRecording(stepItem.id))
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
      }

      is StepContent.ContentSelect -> {
        ContentSelectStep(
          isCurrentStep = isCurrentStep,
          options = stepItem.stepContent.options,
          selectedOptionId = stepItem.stepContent.selectedOptionId,
          onEvent = onEvent,
          currentContinueButtonLoading = currentContinueButtonLoading,
          canSkip = stepItem.stepContent.isSkippable,
          onSkip = {
            onEvent(ClaimChatEvent.Skip(stepItem.id))
          },
          skipButtonLoading = currentSkipButtonLoading,
          stepContent = stepItem.stepContent,
          itemId = stepItem.id,
          isRegrettable = stepItem.isRegrettable,
        )
      }

      is StepContent.FileUpload -> {
        UploadFilesStep(
          isCurrentStep = isCurrentStep,
          stepContent = stepItem.stepContent,
          itemId = stepItem.id,
          onNavigateToImageViewer = onNavigateToImageViewer,
          appPackageId = appPackageId,
          imageLoader = imageLoader,
          onEvent = onEvent,
          canEdit = stepItem.isRegrettable,
          continueButtonLoading = currentContinueButtonLoading,
          skipButtonLoading = currentSkipButtonLoading,
        )
      }

      is StepContent.Form -> {
        FormStep(
          itemId = stepItem.id,
          content = stepItem.stepContent,
          onEvent = onEvent,
          isCurrentStep = isCurrentStep,
          canSkip = stepItem.stepContent.isSkippable,
          canBeChanged = stepItem.isRegrettable,
          continueButtonLoading = currentContinueButtonLoading,
          skipButtonLoading = currentSkipButtonLoading,
          firstFieldWithError = stepItem.stepContent.fields.firstOrNull { it.hasError != null },
          imageLoader = imageLoader,
        )
      }

      is StepContent.Summary -> {
        ChatClaimSummaryBottomContent(
          onSubmit = {
            onEvent(ClaimChatEvent.SubmitClaim(stepItem.id))
          },
          isCurrentStep = isCurrentStep,
          continueButtonLoading = currentContinueButtonLoading,
        )
      }

      is StepContent.Deflect -> {
        DeflectStep(
          stepId = stepItem.id,
          buttonText = stepItem.stepContent.buttonText,
          deflect = stepItem.stepContent,
          navigateToDeflect = navigateToDeflect,
          modifier = Modifier.fillMaxWidth(),
        )
      }

      is StepContent.Task -> {
        TaskStepBottomContent(
          stepItem.stepContent,
          onRetrySubmittingTask = {
            onEvent(ClaimChatEvent.RetrySubmittingTaskStep(stepItem.id))
          },
          modifier = Modifier.fillMaxWidth(),
        )
      }

      StepContent.Unknown -> {
        LaunchedEffect(Unit) {
          logcat(LogPriority.ERROR) { "StepContent.Unknown received in StepBottomContent" }
        }
      }
    }
  }
}
