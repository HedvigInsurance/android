package com.hedvig.android.feature.home.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.nonEmptyListOf
import coil.ImageLoader
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.compose.pager.indicator.HorizontalPagerIndicator
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.crosssells.CrossSellBottomSheet
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.addons.data.TravelAddonBannerInfo
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTooltip
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TooltipDefaults
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.TopEnd
import com.hedvig.android.design.system.hedvig.TooltipDefaults.TooltipStyle.Inbox
import com.hedvig.android.design.system.hedvig.TopAppBarLayoutForActions
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HedvigLogotype
import com.hedvig.android.design.system.hedvig.notificationCircle
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.tokens.HedvigSerif
import com.hedvig.android.feature.home.home.data.HomeData
import com.hedvig.android.feature.home.home.data.HomeData.ClaimStatusCardsData
import com.hedvig.android.feature.home.home.data.HomeData.VeryImportantMessage
import com.hedvig.android.feature.home.home.data.HomeData.VeryImportantMessage.LinkInfo
import com.hedvig.android.feature.home.home.ui.HomeText.Active
import com.hedvig.android.feature.home.home.ui.HomeTopBarAction.ChatAction
import com.hedvig.android.feature.home.home.ui.HomeTopBarAction.CrossSellsAction
import com.hedvig.android.feature.home.home.ui.HomeTopBarAction.FirstVetAction
import com.hedvig.android.feature.home.home.ui.HomeUiState.Success
import com.hedvig.android.memberreminders.MemberReminder.PaymentReminder.ConnectPayment
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.memberreminders.ui.MemberReminderCardsWithoutNotification
import com.hedvig.android.notification.permission.NotificationPermissionDialog
import com.hedvig.android.notification.permission.NotificationPermissionState
import com.hedvig.android.notification.permission.rememberNotificationPermissionState
import com.hedvig.android.notification.permission.rememberPreviewNotificationPermissionState
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.PullRefreshState
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import com.hedvig.android.ui.claimstatus.ClaimStatusCards
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Claim
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Closed.NotCompensated
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentText.Closed
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.INACTIVE
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.android.ui.emergency.FirstVetSection
import hedvig.resources.R
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun HomeDestination(
  viewModel: HomeViewModel,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onClaimDetailCardClicked: (String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  onStartClaim: () -> Unit,
  navigateToHelpCenter: () -> Unit,
  openUrl: (String) -> Unit,
  openAppSettings: () -> Unit,
  navigateToMissingInfo: (String) -> Unit,
  navigateToFirstVet: (List<FirstVetSection>) -> Unit,
  navigateToContactInfo: () -> Unit,
  imageLoader: ImageLoader,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val notificationPermissionState = rememberNotificationPermissionState()
  HomeScreen(
    uiState = uiState,
    notificationPermissionState = notificationPermissionState,
    reload = { viewModel.emit(HomeEvent.RefreshData) },
    onNavigateToInbox = onNavigateToInbox,
    onNavigateToNewConversation = onNavigateToNewConversation,
    onClaimDetailCardClicked = onClaimDetailCardClicked,
    navigateToConnectPayment = navigateToConnectPayment,
    onStartClaim = onStartClaim,
    navigateToHelpCenter = navigateToHelpCenter,
    openUrl = openUrl,
    openAppSettings = openAppSettings,
    navigateToMissingInfo = navigateToMissingInfo,
    markMessageAsSeen = { viewModel.emit(HomeEvent.MarkMessageAsSeen(it)) },
    navigateToFirstVet = navigateToFirstVet,
    markCrossSellsNotificationAsSeen = { viewModel.emit(HomeEvent.MarkCardCrossSellsAsSeen) },
    navigateToContactInfo = navigateToContactInfo,
    setEpochDayWhenLastToolTipShown = { epochDay ->
      viewModel.emit(HomeEvent.CrossSellToolTipShown(epochDay))
    },
    imageLoader = imageLoader,
  )
}

@Composable
private fun HomeScreen(
  uiState: HomeUiState,
  notificationPermissionState: NotificationPermissionState,
  reload: () -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onClaimDetailCardClicked: (String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  onStartClaim: () -> Unit,
  navigateToHelpCenter: () -> Unit,
  openUrl: (String) -> Unit,
  markMessageAsSeen: (String) -> Unit,
  openAppSettings: () -> Unit,
  navigateToMissingInfo: (String) -> Unit,
  navigateToFirstVet: (List<FirstVetSection>) -> Unit,
  navigateToContactInfo: () -> Unit,
  markCrossSellsNotificationAsSeen: () -> Unit,
  setEpochDayWhenLastToolTipShown: (Long) -> Unit,
  imageLoader: ImageLoader,
) {
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = uiState.isReloading,
    onRefresh = reload,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )
  val crossSellBottomSheetState = rememberHedvigBottomSheetState<CrossSellSheetData>()
  CrossSellBottomSheet(
    state = crossSellBottomSheetState,
    markCrossSellsNotificationAsSeen = markCrossSellsNotificationAsSeen,
    onCrossSellClick = openUrl,
    imageLoader = imageLoader,
  )
  Box(Modifier.fillMaxSize()) {
    val toolbarHeight = 64.dp
    val transition = updateTransition(targetState = uiState, label = "home ui state")
    transition.AnimatedContent(
      modifier = Modifier.fillMaxSize(),
      contentKey = { it::class },
    ) { uiState ->
      when (uiState) {
        HomeUiState.Loading -> {
          HedvigFullScreenCenterAlignedProgressDebounced(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.safeDrawing),
          )
        }

        is HomeUiState.Error -> {
          HedvigErrorSection(
            onButtonClick = reload,
            modifier = Modifier
              .padding(16.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing),
          )
        }

        is HomeUiState.Success -> {
          HomeScreenSuccess(
            uiState = uiState,
            pullRefreshState = pullRefreshState,
            toolbarHeight = toolbarHeight,
            notificationPermissionState = notificationPermissionState,
            onClaimDetailCardClicked = onClaimDetailCardClicked,
            navigateToConnectPayment = navigateToConnectPayment,
            navigateToHelpCenter = navigateToHelpCenter,
            onStartClaimClicked = onStartClaim,
            openAppSettings = openAppSettings,
            openUrl = openUrl,
            navigateToMissingInfo = navigateToMissingInfo,
            onNavigateToNewConversation = onNavigateToNewConversation,
            markMessageAsSeen = markMessageAsSeen,
            navigateToContactInfo = navigateToContactInfo,
          )
        }
      }
    }

    Column {
      TopAppBarLayoutForActions {
        val currentState = uiState as? HomeUiState.Success
        if (currentState != null) {
          val actionsList = buildList {
            if (currentState.crossSellsAction != null) add(currentState.crossSellsAction)
            if (currentState.firstVetAction != null) add(currentState.firstVetAction)
            if (currentState.chatAction != null) add(currentState.chatAction)
          }
          actionsList.forEach { action ->
            when (action) {
              HomeTopBarAction.ChatAction -> ToolbarChatIcon(
                onClick = onNavigateToInbox,
                modifier = Modifier.notificationCircle(uiState.hasUnseenChatMessages),
              )

              is HomeTopBarAction.CrossSellsAction -> {
                ToolbarCrossSellsIcon(
                  onClick = {
                    crossSellBottomSheetState.show(
                      action.crossSells,
                    )
                  },
                  modifier = Modifier
                    .notificationCircle(
                      action.crossSellRecommendationNotification.hasUnreadRecommendation,
                    ),
                )
              }

              is HomeTopBarAction.FirstVetAction -> {
                val sections = action.sections
                ToolbarFirstVetIcon(
                  onClick = { navigateToFirstVet(sections) },
                )
              }
            }
          }
        }
      }
      if ((uiState as? HomeUiState.Success)?.chatAction != null) {
        val updatedHasUnseenChatMessages by rememberUpdatedState(uiState.hasUnseenChatMessages)
        val shouldShowNewMessageTooltip by produceState(false) {
          snapshotFlow { updatedHasUnseenChatMessages }
            .drop(1)
            .collectLatest {
              value = it
            }
        }
        if (shouldShowNewMessageTooltip) {
          HedvigTooltip(
            message = stringResource(R.string.CHAT_NEW_MESSAGE),
            showTooltip = shouldShowNewMessageTooltip,
            tooltipStyle = Inbox,
            beakDirection = TopEnd,
            tooltipShown = {},
            modifier = Modifier
              .align(Alignment.End)
              .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
              )
              .padding(horizontal = 16.dp),
          )
        } else {
          CrossSellsTooltip(uiState, setEpochDayWhenLastToolTipShown)
        }
      } else if (uiState is Success) {
        CrossSellsTooltip(uiState, setEpochDayWhenLastToolTipShown)
      }
    }
    PullRefreshIndicator(
      refreshing = uiState.isReloading,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun ColumnScope.CrossSellsTooltip(uiState: Success, setEpochDayWhenLastToolTipShown: (Long) -> Unit) {
  if (uiState.crossSellsAction != null) {
    val shouldShowCrossSellsTooltip = uiState.crossSellsAction.crossSellRecommendationNotification.showToolTip
    var shouldSetEpochDayWhenLastToolTipShown by remember { mutableStateOf(false) }
    LaunchedEffect(shouldSetEpochDayWhenLastToolTipShown) {
      if (shouldSetEpochDayWhenLastToolTipShown) {
        val today = Clock.System.now().toLocalDateTime(
          TimeZone.currentSystemDefault(),
        ).date.toEpochDays().toLong()
        delay(5000)
        setEpochDayWhenLastToolTipShown(today)
      }
    }
    if (shouldShowCrossSellsTooltip) {
      HedvigTooltip(
        message = stringResource(R.string.TOAST_NEW_OFFER),
        showTooltip = true,
        tooltipStyle = TooltipDefaults.TooltipStyle.Campaign(
          subMessage = null,
          TooltipDefaults.TooltipStyle.Campaign.Brightness.BRIGHT,
        ),
        beakDirection = TopEnd,
        tooltipShown = {
          shouldSetEpochDayWhenLastToolTipShown = true
        },
        modifier = Modifier
          .align(Alignment.End)
          .windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
          )
          .padding(start = 16.dp, end = getCrossSellsToolTipEndPadding(uiState).dp),
      )
    }
  }
}

private fun getCrossSellsToolTipEndPadding(uiState: Success): Int {
  val initialEndPadding = 16
  var endPadding = initialEndPadding
  if (uiState.firstVetAction != null) endPadding += 48
  if (uiState.chatAction != null) endPadding += 48
  return endPadding
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeScreenSuccess(
  uiState: HomeUiState.Success,
  pullRefreshState: PullRefreshState,
  toolbarHeight: Dp,
  notificationPermissionState: NotificationPermissionState,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToHelpCenter: () -> Unit,
  onStartClaimClicked: () -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  markMessageAsSeen: (String) -> Unit,
  navigateToMissingInfo: (String) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToContactInfo: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isInPreview = LocalInspectionMode.current
  val windowInfo = LocalWindowInfo.current
  var fullScreenSize: IntSize? by remember { mutableStateOf(if (isInPreview) windowInfo.containerSize else null) }
  val consumedWindowInsets = remember { MutableWindowInsets() }
  Box(
    modifier = modifier
      .fillMaxSize()
      .layout { measurable, constraints ->
        fullScreenSize = IntSize(constraints.maxWidth, constraints.maxHeight)
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
          placeable.place(0, 0)
        }
      }
      .onConsumedWindowInsetsChanged { consumedWindowInsets.insets = it }
      .pullRefresh(pullRefreshState)
      .verticalScroll(rememberScrollState()),
  ) {
    NotificationPermissionDialog(notificationPermissionState, openAppSettings)
    val fullScreenSizeValue = fullScreenSize
    if (fullScreenSizeValue != null) {
      val horizontalInsets = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Horizontal)
        .exclude(consumedWindowInsets)
        .asPaddingValues()
      HomeLayout(
        fullScreenSize = fullScreenSizeValue,
        welcomeMessage = {
          WelcomeMessage(
            homeText = uiState.homeText,
            modifier = Modifier
              .padding(horizontal = 24.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
              .testTag("welcome_message")
              .semantics {
                hideFromAccessibility()
              },
          )
        },
        claimStatusCards = {
          if (uiState.claimStatusCardsData != null) {
            ClaimStatusCards(
              onClick = onClaimDetailCardClicked,
              claimStatusCardsUiState = uiState.claimStatusCardsData.claimStatusCardsUiState,
              contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets,
            )
          }
        },
        veryImportantMessages = {
          ImportantMessages(
            list = uiState.veryImportantMessages,
            openUrl = openUrl,
            hideImportantMessage = markMessageAsSeen,
            contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets,
          )
        },
        memberReminderCards = {
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (uiState.homeText is HomeText.ActiveInFuture) {
              HedvigNotificationCard(
                message = stringResource(R.string.home_tab_active_in_future_info, uiState.homeText.inception),
                priority = NotificationPriority.Info,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp)
                  .padding(horizontalInsets),
              )
            }
            val memberReminders =
              uiState.memberReminders.onlyApplicableReminders(notificationPermissionState.status.isGranted)
            MemberReminderCardsWithoutNotification(
              memberReminders = memberReminders,
              navigateToConnectPayment = navigateToConnectPayment,
              navigateToAddMissingInfo = navigateToMissingInfo,
              onNavigateToNewConversation = onNavigateToNewConversation,
              openUrl = openUrl,
              contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets,
              navigateToContactInfo = navigateToContactInfo,
            )
          }
        },
        startClaimButton = {
          HedvigButton(
            text = stringResource(R.string.home_tab_claim_button_text),
            onClick = onStartClaimClicked,
            enabled = true,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
          )
        },
        helpCenterButton = {
          if (uiState.isHelpCenterEnabled) {
            HedvigButton(
              text = stringResource(R.string.home_tab_get_help),
              onClick = navigateToHelpCenter,
              buttonStyle = Secondary,
              enabled = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            )
          }
        },
        topSpacer = {
          Spacer(
            Modifier
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
              .height(toolbarHeight),
          )
        },
        bottomSpacer = {
          Spacer(
            Modifier
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
              .height(16.dp),
          )
        },
      )
    }
  }
}

@Composable
private fun ImportantMessages(
  list: List<HomeData.VeryImportantMessage>,
  openUrl: (String) -> Unit,
  hideImportantMessage: (id: String) -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  AnimatedContent(
    targetState = list,
    modifier = modifier,
  ) { animatedList ->
    if (animatedList.size == 1) {
      VeryImportantMessageCard(
        openUrl = openUrl,
        hideImportantMessage = hideImportantMessage,
        veryImportantMessage = animatedList.first(),
        modifier = Modifier.padding(contentPadding),
      )
    } else { // todo: should we probably check for else if (animatedList.size>1) here?
      val pagerState = rememberPagerState(pageCount = { animatedList.size })
      Column {
        HorizontalPager(
          state = pagerState,
          contentPadding = contentPadding,
          beyondViewportPageCount = 1,
          pageSpacing = 8.dp,
          modifier = Modifier
            .fillMaxWidth()
            .systemGestureExclusion(),
        ) { page: Int ->
          val currentMessage = animatedList[page]
          VeryImportantMessageCard(
            openUrl = openUrl,
            hideImportantMessage = hideImportantMessage,
            veryImportantMessage = currentMessage,
          )
        }
        Spacer(Modifier.height(16.dp))
        HorizontalPagerIndicator(
          pagerState = pagerState,
          pageCount = animatedList.size,
          activeColor = LocalContentColor.current,
          modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(contentPadding),
        )
      }
    }
  }
}

@Composable
private fun VeryImportantMessageCard(
  openUrl: (String) -> Unit,
  hideImportantMessage: (id: String) -> Unit,
  veryImportantMessage: HomeData.VeryImportantMessage,
  modifier: Modifier = Modifier,
) {
  key(veryImportantMessage.id) {
    HedvigNotificationCard(
      message = veryImportantMessage.message,
      priority = NotificationPriority.Attention,
      modifier = modifier.fillMaxSize(),
      withIcon = false,
      style = if (veryImportantMessage.linkInfo != null) {
        NotificationDefaults.InfoCardStyle.Buttons(
          leftButtonText = stringResource(R.string.important_message_hide),
          rightButtonText =
            veryImportantMessage.linkInfo.buttonText ?: stringResource(R.string.important_message_read_more),
          onLeftButtonClick = { hideImportantMessage(veryImportantMessage.id) },
          onRightButtonClick = { openUrl(veryImportantMessage.linkInfo.link) },
        )
      } else {
        NotificationDefaults.InfoCardStyle.Button(
          buttonText = stringResource(R.string.important_message_hide),
          onButtonClick = { hideImportantMessage(veryImportantMessage.id) },
        )
      },
    )
  }
}

@Composable
private fun WelcomeMessage(homeText: HomeText, modifier: Modifier = Modifier) {
  if (homeText is HomeText.ActiveInFuture) {
    Image(
      HedvigIcons.HedvigLogotype,
      null,
      modifier
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .padding(horizontal = 16.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    )
  } else {
    val headlineText = when (homeText) {
      is HomeText.Active -> stringResource(R.string.home_tab_welcome_title_without_name)
      is HomeText.ActiveInFuture -> error("Image shows here instead")
      is HomeText.Pending -> stringResource(R.string.home_tab_pending_unknown_title_without_name)
      is HomeText.Switching -> stringResource(R.string.home_tab_pending_switchable_welcome_title_without_name)
      is HomeText.Terminated -> stringResource(R.string.home_tab_terminated_welcome_title_without_name)
    }
    HedvigText(
      text = headlineText,
      // todo custom style since new DS does not have this specification
      //  https://hedviginsurance.slack.com/archives/C03U9C6Q7TP/p1727365167917719
      style = HedvigTheme.typography.headlineMedium.copy(
        fontFamily = FontFamily.HedvigSerif,
        fontSize = 28.0.sp,
        lineBreak = LineBreak.Heading,
        textAlign = TextAlign.Center,
      ),
      modifier = modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun CrossSellBottomSheet(
  state: HedvigBottomSheetState<CrossSellSheetData>,
  markCrossSellsNotificationAsSeen: () -> Unit,
  onCrossSellClick: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  LaunchedEffect(state) {
    snapshotFlow { state.isVisible }
      .distinctUntilChanged()
      .collect { isVisible ->
        if (isVisible) {
          markCrossSellsNotificationAsSeen()
        }
      }
  }
  CrossSellBottomSheet(
    state = state,
    onCrossSellClick = onCrossSellClick,
    imageLoader = imageLoader,
  )
}

@HedvigPreview
@Composable
private fun PreviewHomeScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasUnseenChatMessages: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HomeScreen(
        uiState = Success(
          isReloading = false,
          homeText = Active,
          claimStatusCardsData = ClaimStatusCardsData(
            nonEmptyListOf(
              ClaimStatusCardUiState(
                id = "id",
                pillTypes = listOf(Claim, NotCompensated),
                claimProgressItemsUiState = listOf(
                  ClaimProgressSegment(
                    Closed,
                    INACTIVE,
                  ),
                ),
                claimType = "Broken item",
                insuranceDisplayName = "Home Insurance Homeowner",
                submittedDate = Instant.parse("2024-05-01T00:00:00Z"),
              ),
            ),
          ),
          veryImportantMessages = listOf(
            VeryImportantMessage(
              "id",
              "Beware of the earthquake",
              LinkInfo(
                "Read more",
                "",
              ),
            ),
          ),
          memberReminders = MemberReminders(
            connectPayment = ConnectPayment(),
          ),
          isHelpCenterEnabled = true,
          hasUnseenChatMessages = hasUnseenChatMessages,
          crossSellsAction = CrossSellsAction(
            CrossSellSheetData(
              recommendedCrossSell = RecommendedCrossSell(
                crossSell = CrossSell(
                  "rh",
                  "Car Insurance",
                  "For you and your car",
                  "",
                  ImageAsset("", "", ""),
                ),
                bannerText = "50% discount the first year",
                discountText = "-50%",
                buttonText = "Explore offer",
                buttonDescription = "Limited time offer",
              ),
              otherCrossSells = listOf(
                CrossSell(
                  "rf",
                  "Pet insurance",
                  "For your dog or cat",
                  "",
                  ImageAsset("", "", ""),
                ),
              ),
            ),
            crossSellRecommendationNotification = CrossSellRecommendationNotification(
              true,
              java.time.LocalDate.now().toEpochDay(),
            ),
          ),
          firstVetAction = FirstVetAction(
            listOf(
              FirstVetSection(
                "",
                "",
                "",
                "",
              ),
            ),
          ),
          chatAction = ChatAction,
          travelAddonBannerInfo = TravelAddonBannerInfo(
            title = "Title",
            description = "description",
            labels = listOf("Label"),
            eligibleInsurancesIds = nonEmptyListOf("id"),
          ),
        ),
        notificationPermissionState = rememberPreviewNotificationPermissionState(),
        reload = {},
        onNavigateToInbox = {},
        onNavigateToNewConversation = {},
        onClaimDetailCardClicked = {},
        navigateToConnectPayment = {},
        onStartClaim = {},
        navigateToHelpCenter = {},
        openUrl = {},
        openAppSettings = {},
        navigateToMissingInfo = {},
        markMessageAsSeen = {},
        navigateToFirstVet = {},
        markCrossSellsNotificationAsSeen = {},
        navigateToContactInfo = {},
        setEpochDayWhenLastToolTipShown = {},
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHomeScreenWithError() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HomeScreen(
        uiState = HomeUiState.Error(null),
        notificationPermissionState = rememberPreviewNotificationPermissionState(),
        reload = {},
        onNavigateToInbox = {},
        onNavigateToNewConversation = {},
        onClaimDetailCardClicked = {},
        navigateToConnectPayment = {},
        onStartClaim = {},
        navigateToHelpCenter = {},
        openUrl = {},
        openAppSettings = {},
        navigateToMissingInfo = {},
        markMessageAsSeen = {},
        navigateToFirstVet = {},
        markCrossSellsNotificationAsSeen = {},
        navigateToContactInfo = {},
        setEpochDayWhenLastToolTipShown = {},
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHomeScreenAllHomeTextTypes(
  @PreviewParameter(HomeTextPreviewParameterProvider::class) homeText: HomeText,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HomeScreen(
        uiState = HomeUiState.Success(
          homeText = homeText,
          isReloading = false,
          claimStatusCardsData = null,
          veryImportantMessages = emptyList(),
          memberReminders = MemberReminders(
            connectPayment = null,
            upcomingRenewals = null,
            enableNotifications = null,
            coInsuredInfo = null,
            updateContactInfo = null,
          ),
          isHelpCenterEnabled = false,
          hasUnseenChatMessages = false,
          crossSellsAction = null,
          firstVetAction = null,
          chatAction = null,
          travelAddonBannerInfo = null,
        ),
        notificationPermissionState = rememberPreviewNotificationPermissionState(),
        reload = {},
        onNavigateToInbox = {},
        onNavigateToNewConversation = {},
        onClaimDetailCardClicked = {},
        navigateToConnectPayment = {},
        onStartClaim = {},
        navigateToHelpCenter = {},
        openUrl = {},
        openAppSettings = {},
        navigateToMissingInfo = {},
        markMessageAsSeen = {},
        navigateToFirstVet = {},
        markCrossSellsNotificationAsSeen = {},
        navigateToContactInfo = {},
        setEpochDayWhenLastToolTipShown = {},
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}

private class HomeTextPreviewParameterProvider : CollectionPreviewParameterProvider<HomeText>(
  listOf(
    HomeText.Active,
    HomeText.ActiveInFuture(LocalDate.parse("2025-01-01")),
    HomeText.Pending,
    HomeText.Switching,
    HomeText.Terminated,
  ),
)
