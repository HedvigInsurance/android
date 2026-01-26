package com.hedvig.feature.claim.chat.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.ImageLoader
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.design.system.hedvig.TopAppBarColors
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay
import com.hedvig.android.design.system.hedvig.icon.ArrowDown
import com.hedvig.android.design.system.hedvig.icon.ChevronDown
import com.hedvig.android.design.system.hedvig.icon.Close
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
import hedvig.resources.A11Y_SCROLL_DOWN
import hedvig.resources.CHAT_CONVERSATION_CLAIM_TITLE
import hedvig.resources.CLAIMS_TEXT_INPUT_PLACEHOLDER
import hedvig.resources.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER
import hedvig.resources.CLAIM_CHAT_EDIT_EXPLANATION
import hedvig.resources.CLAIM_CHAT_FILE_UPLOAD_SEND_BUTTON
import hedvig.resources.CLAIM_CHAT_FORM_NUMBER_MAX_CHAR
import hedvig.resources.CLAIM_CHAT_FORM_NUMBER_MIN_CHAR
import hedvig.resources.CLAIM_CHAT_FORM_REQUIRED_FIELD
import hedvig.resources.CLAIM_CHAT_SKIPPED_STEP
import hedvig.resources.CLAIM_CHAT_TASK_CONTENT_DESCRIPTION
import hedvig.resources.GENERAL_ARE_YOU_SURE
import hedvig.resources.Res
import hedvig.resources.claims_alert_body
import hedvig.resources.claims_edit_button
import hedvig.resources.claims_skip_button
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import hedvig.resources.general_error
import hedvig.resources.something_went_wrong
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
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
  val isScrolled by remember(lazyListState, uiState.currentStep?.id) {
    derivedStateOf {
      val layoutInfo = lazyListState.layoutInfo
      val lazyListItemsCount = layoutInfo.totalItemsCount
      val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
      lastVisibleItem?.index != lazyListItemsCount - 1
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
        onActionClick = navigateUp,
        customTopAppBarColors = TopAppBarColors(
          containerColor = Color.Transparent,
          contentColor = HedvigTheme.colorScheme.textPrimary,
        ),
        topAppBarActions = {
          IconButton(
            onClick = {
              showCloseFlowDialog = true
            },
          ) {
            Icon(
              HedvigIcons.Close,
              stringResource(Res.string.general_close_button),
            )
          }
        },
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
    .plus(PaddingValues(bottom = 16.dp, top = 16.dp))

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
      modifier = Modifier.padding(horizontal = 16.dp),
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

        val heightModifier = if (isLastItem) {
          Modifier.requiredHeightIn(lastItemHeightAdjustingState.preferredMinHeightForFullScreenItem)
        } else {
          Modifier
        }

        Column(
          modifier = heightModifier,
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
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
          )
        }
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
private fun ColumnScope.StepContentSection(
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

  if (showAiDot) {
    CommonPaddingWrapper {
      BlinkingAiDot()
    }
  }
  if (showTopContent) {
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

  if (showBottomContent && !isAnimationInProcess) {
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
  } else if (isAnimationInProcess) {
    AnimatedVisibility(
      visible = showBottomContent,
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
      stepItemText?.let {
        Spacer(Modifier.height(16.dp))
      }
      TaskStep(
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
    stepItemText?.let {
      Spacer(Modifier.height(16.dp))
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
private fun AnimatedRevealText(
  text: String,
  visibleState: MutableTransitionState<Boolean>,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current,
  onAnimationFinished: () -> Unit = {},
) {
  val charAnimDuration: Int = 150
  var visibleChars by remember { mutableStateOf(0) }

  val charDelay = calculateCharDelay(text)

  LaunchedEffect(visibleState.targetState, text) {
    if (visibleState.targetState) {
      visibleChars = 0
      text.toCharArray().forEachIndexed { index, char ->
        visibleChars = index + 1
        val specialCharDelayMultiplier = when (char) {
          ',' -> 5
          in listOf('.', '?', '!', '\n', '\t') -> 10
          else -> 1
        }
        delay(charDelay * specialCharDelayMultiplier)
      }
      delay(charAnimDuration.toLong())
      onAnimationFinished()
    } else {
      visibleChars = 0
    }
  }
  val baseColor = style.color.takeOrElse { LocalContentColor.current }
  HedvigText(
    text = buildAnnotatedString {
      text.forEachIndexed { index, char ->
        if (index < visibleChars) {
          val progress by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(charAnimDuration),
            label = "char_$index",
          )
          withStyle(
            style = SpanStyle(color = baseColor.copy(alpha = progress)),
          ) {
            append(char)
          }
        } else {
          withStyle(style = SpanStyle(color = baseColor.copy(alpha = 0f))) {
            append(char)
          }
        }
      }
    },
    style = style,
    modifier = modifier,
  )
}

/**
 * Speed multiplier decreases for longer text to avoid tedious animations
 * Short text (≤50 chars): full speed (1.0x multiplier)
 * Medium text (50-450 chars): linear interpolation
 * Long text (≥450 chars): 5x faster (0.2x multiplier)
 */
@Composable
private fun calculateCharDelay(text: String): Duration = remember(text) {
  val textLength = text.length
  val baseRegularDelayMillis = 20

  val shortTextThreshold = 50
  val longTextThreshold = 350
  val slowestMultiplier = 1.0
  val fastestMultiplier = 0.2

  // Extreme fallback for extreme cases
  val superLongThreshold = 800
  val superFastestMultiplier = 0.05

  val speedMultiplier = when {
    textLength <= shortTextThreshold -> slowestMultiplier
    textLength >= superLongThreshold -> superFastestMultiplier
    textLength >= longTextThreshold -> fastestMultiplier
    else -> {
      val characterRange = longTextThreshold - shortTextThreshold
      val charactersAboveThreshold = textLength - shortTextThreshold
      val interpolationProgress = charactersAboveThreshold.toDouble() / characterRange
      slowestMultiplier - interpolationProgress * (slowestMultiplier - fastestMultiplier)
    }
  }.coerceIn(fastestMultiplier, slowestMultiplier)

  val minimumDelayRatio = 0.2
  (baseRegularDelayMillis * speedMultiplier)
    .coerceAtLeast((baseRegularDelayMillis * minimumDelayRatio))
    .milliseconds
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
        firstFieldWithError = stepItem.stepContent.fields.firstOrNull { it.hasError != null },
      )

      is StepContent.Summary -> ChatClaimSummaryBottomContent(
        onSubmit = {
          onEvent(ClaimChatEvent.SubmitClaim(stepItem.id))
        },
        isCurrentStep = isCurrentStep,
        continueButtonLoading = currentContinueButtonLoading,
      )

      is StepContent.Deflect -> {
        DeflectStep(
          stepId = stepItem.id,
          buttonText = stepItem.stepContent.buttonText,
          deflect = stepItem.stepContent,
          navigateToDeflect = navigateToDeflect,
          modifier = Modifier.fillMaxWidth(),
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
          text = stringResource(Res.string.CLAIM_CHAT_FILE_UPLOAD_SEND_BUTTON),
          enabled = !continueButtonLoading,
          onClick = {
            onEvent(
              ClaimChatEvent.SubmitFile(
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
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
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
    val skippedLabelText = stringResource(Res.string.CLAIM_CHAT_SKIPPED_STEP)
    RoundCornersPill(
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
  buttonText: String,
  deflect: StepContent.Deflect,
  navigateToDeflect: (StepId, StepContent.Deflect) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigButton(
    modifier = modifier,
    text = buttonText,
    onClick = dropUnlessResumed { navigateToDeflect(stepId, deflect) },
    enabled = true,
  )
}

@Composable
private fun TaskStep(taskContent: StepContent.Task, modifier: Modifier = Modifier) {
  val taskContentDescription = stringResource(Res.string.CLAIM_CHAT_TASK_CONTENT_DESCRIPTION)
  Column(
    modifier.clearAndSetSemantics { contentDescription = taskContentDescription },
  ) {
    if (taskContent.descriptions.isNotEmpty()) {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          BlinkingAiDot()
          if (taskContent.descriptions.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            AnimatedContent(taskContent.descriptions.last()) { target ->
              RoundCornersPill(
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
private fun BlinkingAiDot(durationMillis: Int = 500) {
  val infiniteTransition = rememberInfiniteTransition(label = "blink")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis),
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
  firstFieldWithError: StepContent.Form.Field?,
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
      firstFieldWithError = firstFieldWithError,
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
  firstFieldWithError: StepContent.Form.Field?,
  modifier: Modifier = Modifier,
) {
  val errorDescription = firstFieldWithError?.let { "${getErrorText(it)}: ${it.title?.let { title -> title }}" }
  Column(
    modifier,
  ) {
    if (isCurrentStep) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        content.fields.forEach { field ->
          val errorText = getErrorText(field)
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
                errorText = errorText,
              )
            }

            StepContent.Form.FieldType.DATE -> {
              LaunchedEffect(field.datePickerUiState?.datePickerState?.selectedDateMillis) {
                onSelectFieldAnswer(
                  field.id,
                  field.datePickerUiState?.datePickerState?.selectedDateMillis?.let {
                    StepContent.Form.FieldOption(it.toString(), it.toString())
                  },
                )
              }
              DateSelectBubble(
                questionLabel = field.title,
                datePickerState = field.datePickerUiState!!, // todo - check "!!"
                modifier = Modifier.fillMaxWidth(),
                errorText = errorText,
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
                errorText = errorText,
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
                errorText = errorText,
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
                errorText = errorText,
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
              errorText = errorText,
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
        modifier = Modifier.fillMaxWidth().semantics {
          if (errorDescription != null) {
            contentDescription = errorDescription
          }
        },
      )
      if (canSkip) {
        Spacer(Modifier.height(8.dp))
        HedvigButton(
          text = stringResource(Res.string.claims_skip_button),
          enabled = !skipButtonLoading,
          onClick = onSkip,
          isLoading = skipButtonLoading,
          modifier = Modifier.fillMaxWidth(),
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        )
      }
    } else {
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        if (content.fields.flatMap { it.selectedOptions }.isNotEmpty()) {
          content.fields.forEach { field ->
            val textValue = field.selectedOptions.joinToString { it.text }
            Column(
              Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.End,
            ) {
              if (textValue.isNotEmpty()) {
                RoundCornersPill(
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
      modifier = modifier.fillMaxWidth().padding(top = 4.dp),
      horizontalArrangement = Arrangement.End,
    ) {
      RoundCornersPill(
        onClick = onRegret,
        modifier = Modifier.semantics(true) {
          role = Role.Button
        },
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          HedvigText(
            stringResource(Res.string.claims_edit_button),
            fontStyle = HedvigTheme.typography.label.fontStyle,
          )
          Spacer(Modifier.width(6.dp))
          Icon(
            HedvigIcons.ChevronDown,
            null,
            tint = HedvigTheme.colorScheme.fillTertiaryTransparent,
          )
        }
      }
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
  stepContent: StepContent.ContentSelect,
  itemId: StepId,
  isRegrettable: Boolean,
  isCurrentStep: Boolean,
  options: List<StepContent.ContentSelect.Option>,
  selectedOptionId: String?,
  onEvent: (ClaimChatEvent) -> Unit,
  currentContinueButtonLoading: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  skipButtonLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    AnimatedContent(
      isCurrentStep,
      transitionSpec = {
        (fadeIn() + scaleIn()).togetherWith(fadeOut(animationSpec = tween(0)))
      },
    ) { targetState ->
      Column {
        if (targetState) {
          Spacer(Modifier.height(32.dp))
          ContentSelectChips(
            options = options,
            onOptionClick = { option ->
              if (!currentContinueButtonLoading) {
                onEvent(
                  ClaimChatEvent.Select(
                    itemId,
                    option.id,
                  ),
                )
              }
            },
            selectedOptionId = stepContent.selectedOptionId,
            style = stepContent.style,
          )
          if (canSkip) {
            Spacer(Modifier.height(16.dp))
            HedvigButton(
              stringResource(Res.string.claims_skip_button),
              onClick = onSkip,
              isLoading = skipButtonLoading,
              enabled = !skipButtonLoading,
              modifier = Modifier.fillMaxWidth(),
              buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
            )
            Spacer(Modifier.height(16.dp))
          }
        } else {
          Column {
            val selected = options.firstOrNull { it.id == selectedOptionId }
            Spacer(Modifier.height(8.dp))
            if (selected != null) {
              Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                  .fillMaxWidth(),
              ) {
                RoundCornersPill(
                  onClick = null,
                ) {
                  HedvigText(selected.title)
                }
              }
            } else {
              SkippedLabel()
            }
            EditButton(
              isRegrettable,
              onRegret = {
                onEvent(ClaimChatEvent.ShowConfirmEditDialog(itemId))
              },
            )
          }
        }
      }
    }
  }
}
