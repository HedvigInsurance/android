package com.hedvig.android.feature.home.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
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
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.compose.pager.indicator.HorizontalPagerIndicator
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.crosssells.BundleProgress
import com.hedvig.android.crosssells.CrossSellBottomSheet
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.CrossSellsSection
import com.hedvig.android.crosssells.PillowRow
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.data.addons.data.FlowType
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTooltip
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.StartClaimBottomSheet
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TooltipDefaults
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.TopEnd
import com.hedvig.android.design.system.hedvig.TooltipDefaults.TooltipStyle.Inbox
import com.hedvig.android.design.system.hedvig.TopAppBarLayoutForActions
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HedvigLogotype
import com.hedvig.android.design.system.hedvig.icon.HelipadOutline
import com.hedvig.android.design.system.hedvig.icon.Reload
import com.hedvig.android.design.system.hedvig.icon.Travel
import com.hedvig.android.design.system.hedvig.notificationCircle
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.feature.home.home.data.HomeData.ClaimStatusCardsData
import com.hedvig.android.feature.home.home.data.HomeData.VeryImportantMessage
import com.hedvig.android.feature.home.home.data.HomeData.VeryImportantMessage.LinkInfo
import com.hedvig.android.feature.home.home.ui.HomeText.Active
import com.hedvig.android.feature.home.home.ui.HomeTopBarAction.ChatAction
import com.hedvig.android.feature.home.home.ui.HomeTopBarAction.CrossSellsAction
import com.hedvig.android.feature.home.home.ui.HomeTopBarAction.FirstVetAction
import com.hedvig.android.feature.home.home.ui.HomeUiState.Success
import com.hedvig.android.memberreminders.MemberReminder
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
import hedvig.resources.ADDON_FLOW_LEARN_MORE_BUTTON
import hedvig.resources.CHAT_NEW_MESSAGE
import hedvig.resources.CROSS_SELL_SUBTITLE
import hedvig.resources.DASHBOARD_OPEN_CHAT
import hedvig.resources.HC_QUICK_ACTIONS_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE
import hedvig.resources.HC_QUICK_ACTIONS_UPDATE_ADDRESS
import hedvig.resources.Res
import hedvig.resources.TAB_REFERRALS_TITLE
import hedvig.resources.TOAST_NEW_OFFER
import hedvig.resources.blur_background
import hedvig.resources.home_tab_active_in_future_info
import hedvig.resources.home_tab_claim_button_text
import hedvig.resources.home_tab_get_help
import hedvig.resources.home_tab_pending_switchable_welcome_title_without_name
import hedvig.resources.home_tab_pending_unknown_title_without_name
import hedvig.resources.home_tab_terminated_welcome_title_without_name
import hedvig.resources.home_tab_welcome_title_without_name
import hedvig.resources.important_message_hide
import hedvig.resources.important_message_read_more
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeDestination(
  viewModel: HomeViewModel,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToClaimChat: () -> Unit,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToHelpCenter: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  openAppSettings: () -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  navigateToFirstVet: (List<FirstVetSection>) -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipId: () -> Unit,
  imageLoader: ImageLoader,
  navigateToForever: () -> Unit,
  navigateToTravelCertificate: () -> Unit,
  navigateToAddonPurchaseFlow: (List<String>) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val notificationPermissionState = rememberNotificationPermissionState()
  HomeScreen(
    uiState = uiState,
    notificationPermissionState = notificationPermissionState,
    reload = { viewModel.emit(HomeEvent.RefreshData) },
    onNavigateToInbox = onNavigateToInbox,
    onNavigateToNewConversation = onNavigateToNewConversation,
    navigateToClaimChat = navigateToClaimChat,
    onClaimDetailCardClicked = onClaimDetailCardClicked,
    navigateToConnectPayment = navigateToConnectPayment,
    navigateToConnectPayout = navigateToConnectPayout,
    navigateToHelpCenter = navigateToHelpCenter,
    navigateToMovingFlow = navigateToMovingFlow,
    openUrl = openUrl,
    openCrossSellUrl = openCrossSellUrl,
    openAppSettings = openAppSettings,
    navigateToMissingInfo = navigateToMissingInfo,
    markMessageAsSeen = { viewModel.emit(HomeEvent.MarkMessageAsSeen(it)) },
    navigateToFirstVet = navigateToFirstVet,
    markCrossSellsNotificationAsSeen = { viewModel.emit(HomeEvent.MarkCardCrossSellsAsSeen) },
    navigateToContactInfo = navigateToContactInfo,
    navigateToChipIdScreen = navigateToChipId,
    setEpochDayWhenLastToolTipShown = { epochDay ->
      viewModel.emit(HomeEvent.CrossSellToolTipShown(epochDay))
    },
    imageLoader = imageLoader,
    navigateToForever = navigateToForever,
    navigateToTravelCertificate = navigateToTravelCertificate,
    navigateToAddonPurchaseFlow = navigateToAddonPurchaseFlow,
  )
}

@Composable
private fun HomeScreen(
  uiState: HomeUiState,
  notificationPermissionState: NotificationPermissionState,
  reload: () -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToClaimChat: () -> Unit,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToHelpCenter: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  markMessageAsSeen: (String) -> Unit,
  openAppSettings: () -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  navigateToFirstVet: (List<FirstVetSection>) -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipIdScreen: () -> Unit,
  markCrossSellsNotificationAsSeen: () -> Unit,
  setEpochDayWhenLastToolTipShown: (Long) -> Unit,
  imageLoader: ImageLoader,
  navigateToForever: () -> Unit,
  navigateToTravelCertificate: () -> Unit,
  navigateToAddonPurchaseFlow: (List<String>) -> Unit,
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
    onCrossSellClick = openCrossSellUrl,
    imageLoader = imageLoader,
  )
  val startClaimBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  StartClaimBottomSheet(
    state = startClaimBottomSheetState,
    navigateToClaimChat = navigateToClaimChat,
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

        is Success -> {
          HomeScreenSuccess(
            uiState = uiState,
            pullRefreshState = pullRefreshState,
            toolbarHeight = toolbarHeight,
            notificationPermissionState = notificationPermissionState,
            onClaimDetailCardClicked = onClaimDetailCardClicked,
            navigateToConnectPayment = navigateToConnectPayment,
            navigateToConnectPayout = navigateToConnectPayout,
            navigateToHelpCenter = navigateToHelpCenter,
            navigateToMovingFlow = navigateToMovingFlow,
            onNavigateToInbox = onNavigateToInbox,
            openClaimFlowSheet = startClaimBottomSheetState::show,
            openAppSettings = openAppSettings,
            openUrl = openUrl,
            navigateToMissingInfo = navigateToMissingInfo,
            onNavigateToNewConversation = onNavigateToNewConversation,
            markMessageAsSeen = markMessageAsSeen,
            navigateToContactInfo = navigateToContactInfo,
            navigateToChipIdScreen = navigateToChipIdScreen,
            openCrossSellUrl = openCrossSellUrl,
            imageLoader = imageLoader,
            navigateToForever = navigateToForever,
            navigateToTravelCertificate = navigateToTravelCertificate,
            navigateToAddonPurchaseFlow = navigateToAddonPurchaseFlow,
          )
        }
      }
    }

    Column {
      TopAppBarLayoutForActions {
        val currentState = uiState as? Success
        if (currentState != null) {
          val actionsList = buildList {
            if (currentState.crossSellsAction != null) add(currentState.crossSellsAction)
            if (currentState.firstVetAction != null) add(currentState.firstVetAction)
            if (currentState.chatAction != null) add(currentState.chatAction)
          }
          OnHeroGradient {
            actionsList.forEach { action ->
              when (action) {
                ChatAction -> {
                  // The FirstVet/cross-sell icons are colored images (theme-independent), but this one is a
                  // tinted glyph on a tonal surface, so it must use the light scheme on the light gradient.
                  ToolbarChatIcon(
                    onClick = onNavigateToInbox,
                    modifier = Modifier.notificationCircle(uiState.hasUnseenChatMessages),
                  )
                }

                is CrossSellsAction -> {
                  ToolbarCrossSellsIcon(
                    onClick = {
                      crossSellBottomSheetState.show(
                        action.crossSells,
                      )
                    },
                    modifier = Modifier.notificationCircle(
                      action.crossSellRecommendationNotification.hasUnreadRecommendation,
                    ),
                  )
                }

                is FirstVetAction -> {
                  val sections = action.sections
                  ToolbarFirstVetIcon(
                    onClick = { navigateToFirstVet(sections) },
                  )
                }
              }
            }
          }
        }
      }
      if ((uiState as? Success)?.chatAction != null) {
        val updatedHasUnseenChatMessages by rememberUpdatedState(uiState.hasUnseenChatMessages)
        val shouldShowNewMessageTooltip by produceState(false) {
          snapshotFlow { updatedHasUnseenChatMessages }.drop(1).collectLatest {
            value = it
          }
        }
        if (shouldShowNewMessageTooltip) {
          HedvigTooltip(
            message = stringResource(Res.string.CHAT_NEW_MESSAGE),
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
        message = stringResource(Res.string.TOAST_NEW_OFFER),
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun HomeScreenSuccess(
  uiState: Success,
  pullRefreshState: PullRefreshState,
  toolbarHeight: Dp,
  notificationPermissionState: NotificationPermissionState,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToHelpCenter: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  onNavigateToInbox: () -> Unit,
  openClaimFlowSheet: () -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  markMessageAsSeen: (String) -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipIdScreen: () -> Unit,
  openCrossSellUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  navigateToForever: () -> Unit,
  navigateToTravelCertificate: () -> Unit,
  navigateToAddonPurchaseFlow: (List<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val consumedWindowInsets = remember { MutableWindowInsets() }
  // Capture the viewport size in the layout phase (cheaper than BoxWithConstraints, and available on
  // the first frame) so the greeting hero can size itself relative to the screen height, and the list
  // can center+cap its content to the screen width.
  var viewportSize by remember { mutableStateOf(IntSize.Zero) }
  Box(
    modifier = modifier
      .fillMaxSize()
      .layout { measurable, constraints ->
        if (constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
          viewportSize = IntSize(constraints.maxWidth, constraints.maxHeight)
        }
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
      }
      .onConsumedWindowInsetsChanged { consumedWindowInsets.insets = it }
      .pullRefresh(pullRefreshState),
  ) {
    // Full-screen blur gradient behind the whole home screen. Sections that need a solid surface draw
    // their own background on top to "hide" it (the content cards already do; so do the pinned pills).
    Image(
      painter = painterResource(Res.drawable.blur_background),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.matchParentSize(),
    )
    NotificationPermissionDialog(notificationPermissionState, openAppSettings)
    val horizontalInsets =
      WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).exclude(consumedWindowInsets).asPaddingValues()
    val topInsets =
      WindowInsets.safeDrawing.only(WindowInsetsSides.Top).exclude(consumedWindowInsets).asPaddingValues()
    val bottomInsets =
      WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).exclude(consumedWindowInsets).asPaddingValues()
    val applicableReminders =
      uiState.memberReminders.onlyApplicableReminders(notificationPermissionState.status.isGranted)
    val visibleSections = homeSectionOrder.filter { section ->
      when (section) {
        HomeSection.Welcome -> {
          true
        }

        HomeSection.QuickActionCarousel -> {
          true
        }

        HomeSection.ClaimStatusCards -> {
          uiState.claimStatusCardsData != null
        }

        HomeSection.VeryImportantMessages -> {
          uiState.veryImportantMessages.isNotEmpty()
        }

        HomeSection.MemberReminders -> {
          uiState.homeText is HomeText.ActiveInFuture || applicableReminders.isNotEmpty()
        }

        HomeSection.Offers -> {
          uiState.crossSellsPartition.offersCrossSell != null
        }

        HomeSection.DiscoverInsurances -> {
          uiState.crossSellsPartition.discoverCrossSells.isNotEmpty()
        }

        HomeSection.Addons -> {
          uiState.addonBannerInfos.isNotEmpty()
        }

        HomeSection.QuickActionTiles -> {
          true
        }
      }
    }
    // Status-bar inset + floating-toolbar height. Baked into the greeting and the sticky pills
    // (instead of contentPadding) because a stickyHeader pins at the viewport top and ignores
    // contentPadding.top — so without this the pills would pin OVER the floating icons.
    val pinnedTopOffset = toolbarHeight + topInsets.calculateTopPadding()
    // The pinned sticky header's bottom edge, in LazyColumn coordinates. Scrolling sections clip their
    // content to below this line so nothing bleeds through the transparent pills as it scrolls up.
    var stickyHeaderBottomPx by remember { mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()
    // Collapsing hero via nested scroll: upward scroll is first consumed to shrink the hero (the list
    // stays put, so content tracks the finger 1:1), then released to the list once fully collapsed;
    // scrolling back to the top expands it again. `heroCollapsePx` is the current shrink; the hero's
    // layout below publishes `maxHeroCollapsePx` (its full collapsible range) for the connection to clamp.
    val heroCollapsePx = rememberSaveable { mutableFloatStateOf(0f) }
    val maxHeroCollapsePx = remember { mutableFloatStateOf(0f) }
    val heroCollapseConnection = remember {
      object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
          val delta = available.y
          if (delta >= 0f) return Offset.Zero // only collapse on upward scroll
          val room = (maxHeroCollapsePx.floatValue - heroCollapsePx.floatValue).coerceAtLeast(0f)
          val consume = (-delta).coerceAtMost(room)
          if (consume <= 0f) return Offset.Zero
          heroCollapsePx.floatValue += consume
          return Offset(0f, -consume)
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
          val delta = available.y
          if (delta <= 0f) return Offset.Zero // expand only with leftover downward scroll (list at top)
          val release = delta.coerceAtMost(heroCollapsePx.floatValue)
          if (release <= 0f) return Offset.Zero
          heroCollapsePx.floatValue -= release
          return Offset(0f, release)
        }
      }
    }
    // Opaque sheet backdrop from the pinned lid down to the bottom, so the sheet fills the screen even
    // when the content is short (e.g. landscape) — no blur gap below the last section. Drawn behind the
    // LazyColumn (on top of the blur); the hero + pills above the lid stay transparent on the gradient.
    val sheetColor = HedvigTheme.colorScheme.backgroundPrimary
    val contentMaxWidth = 600.dp
    Box(
      Modifier
        .matchParentSize()
        .drawBehind {
          val top = stickyHeaderBottomPx
          if (top > 0f && top < size.height) {
            // Match the LazyColumn's capped, centered width so the backdrop doesn't span the full screen.
            val width = contentMaxWidth.toPx().coerceAtMost(size.width)
            val left = (size.width - width) / 2f
            drawRect(color = sheetColor, topLeft = Offset(left, top), size = Size(width, size.height - top))
          }
        },
    )
    // Cap the content to a comfortable column on wide/expanded windows (no-op on phones); the blur stays
    // full-bleed behind it. The list itself fills the full width so dragging anywhere (incl. the side
    // margins on landscape) scrolls it — the cap is applied to the CONTENT via horizontal padding instead.
    val horizontalContentPadding = with(LocalDensity.current) {
      ((viewportSize.width.toDp() - contentMaxWidth) / 2).coerceAtLeast(0.dp)
    }
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .nestedScroll(heroCollapseConnection),
      contentPadding = PaddingValues(
        start = horizontalContentPadding,
        end = horizontalContentPadding,
        bottom = 16.dp + bottomInsets.calculateBottomPadding(),
      ),
    ) {
      // Greeting scrolls away; the pills below pin under the toolbar. Both carry the same top offset
      // so the pinned pills clear the icons and the greeting stays visually centered.
      if (HomeSection.Welcome in visibleSections) {
        item(key = HomeSection.Welcome, contentType = "welcome") {
          // The greeting sits low in a tall hero (collapsible space ABOVE it), so it reads as centered
          // in the area below the toolbar like the design. The hero only shrinks by a bounded amount on
          // scroll, so the list doesn't race the finger. All computed in the layout phase, so it
          // re-lays-out (no recomposition) and is correct on the first frame.
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val clearancePx = pinnedTopOffset.roundToPx()
                // Resting floor: greeting fully visible just below the toolbar clearance.
                val restingFloor = clearancePx + placeable.height
                // Fixed space added above the greeting at rest (NOT the leftover viewport, which would
                // void out tall screens). This is both the resting breathing room AND the extra collapse
                // drag, so keep it modest. Mainly affects portrait; landscape is viewport-capped below.
                val addedSpacePx = 56.dp.roundToPx()
                // Room kept below the hero for the pills + a sheet peek on short/landscape windows.
                val reservedPx = (pinnedTopOffset + 132.dp).roundToPx()
                val fullHero = minOf(restingFloor + addedSpacePx, viewportSize.height - reservedPx)
                  .coerceAtLeast(restingFloor)
                // Collapse the hero all the way to zero, so at full collapse the pills sit at a SINGLE
                // toolbar clearance (supplied by the sticky header) instead of a doubled one. Publish the
                // range for the nested-scroll connection to clamp.
                maxHeroCollapsePx.floatValue = fullHero.toFloat()
                val heroHeight = (fullHero - heroCollapsePx.floatValue.roundToInt()).coerceIn(0, fullHero)
                val parallaxPx = 24.dp.toPx()
                layout(placeable.width, heroHeight) {
                  val y = (heroHeight - placeable.height).coerceAtLeast(0)
                  // Greeting fades over the final phase (as its space is squeezed into the clearance) and
                  // parallaxes: lifts slightly and scales down as it goes.
                  val greetingAlpha = ((heroHeight - clearancePx).toFloat() / placeable.height).coerceIn(0f, 1f)
                  val fade = 1f - greetingAlpha
                  placeable.placeWithLayer(0, y) {
                    alpha = greetingAlpha
                    scaleX = 1f - 0.12f * fade
                    scaleY = 1f - 0.12f * fade
                    translationY = -fade * parallaxPx
                  }
                }
              },
          ) {
            OnHeroGradient {
              WelcomeSection(uiState.homeText)
            }
          }
        }
      }
      if (HomeSection.QuickActionCarousel in visibleSections) {
        stickyHeader(key = HomeSection.QuickActionCarousel, contentType = "pills") {
          // Pills float transparently on the blur; the sheet "lid" (drag handle) is part of the same
          // pinned header. We record the header's bottom edge so scrolling sections can clip to it.
          Column(
            Modifier
              .fillMaxWidth()
              .onPlaced {
                stickyHeaderBottomPx = it.positionInParent().y + it.size.height
              }
              // Swallow taps over the lid so they don't fall through to the section clipped behind it.
              // Children (the pills) are hit first and keep their clicks; drags still scroll the list.
              .pointerInput(Unit) { detectTapGestures {} },
          ) {
            Spacer(Modifier.height(pinnedTopOffset))
            OnHeroGradient {
              QuickActionCarouselSection(
                isHelpCenterEnabled = uiState.isHelpCenterEnabled,
                onMakeClaim = openClaimFlowSheet,
                onHelpAndSupport = navigateToHelpCenter,
                onContactUs = onNavigateToInbox,
                onForever = navigateToForever,
                horizontalInsets = horizontalInsets,
                modifier = Modifier.padding(bottom = 8.dp),
              )
            }
            HomeSheetDragHandle(
              Modifier
                .fillMaxWidth()
                .background(
                  color = HedvigTheme.colorScheme.backgroundPrimary,
                  shape = HedvigTheme.shapes.cornerXLargeTop,
                ),
            )
          }
        }
      }
      val scrollingSections = visibleSections.filterNot {
        it == HomeSection.Welcome || it == HomeSection.QuickActionCarousel
      }
      itemsIndexed(scrollingSections, key = { _, section -> section }) { index, section ->
        val previous = if (index == 0) HomeSection.QuickActionCarousel else scrollingSections[index - 1]
        // Every scrolling section continues the opaque sheet surface, and clips its own content (and
        // background) to below the pinned header so nothing bleeds through the transparent pills.
        var itemTopPx by remember { mutableFloatStateOf(0f) }
        Column(
          Modifier
            .fillMaxWidth()
            .onPlaced { itemTopPx = it.positionInParent().y }
            .drawWithContent {
              val clipTop = (stickyHeaderBottomPx - itemTopPx).coerceIn(0f, size.height)
              clipRect(top = clipTop) { this@drawWithContent.drawContent() }
            }
            .background(HedvigTheme.colorScheme.backgroundPrimary),
        ) {
          Spacer(Modifier.height(gapBefore(section, previous)))
          when (section) {
            HomeSection.Welcome, HomeSection.QuickActionCarousel -> Unit

            // pinned above the scrolling content

            HomeSection.ClaimStatusCards -> ClaimStatusCardsSection(
              claimStatusCardsData = uiState.claimStatusCardsData,
              onClaimDetailCardClicked = onClaimDetailCardClicked,
              horizontalInsets = horizontalInsets,
            )

            HomeSection.VeryImportantMessages -> VeryImportantMessagesSection(
              list = uiState.veryImportantMessages,
              openUrl = openUrl,
              markMessageAsSeen = markMessageAsSeen,
              horizontalInsets = horizontalInsets,
            )

            HomeSection.MemberReminders -> MemberRemindersSection(
              homeText = uiState.homeText,
              applicableReminders = applicableReminders,
              navigateToConnectPayment = navigateToConnectPayment,
              navigateToConnectPayout = navigateToConnectPayout,
              navigateToMissingInfo = navigateToMissingInfo,
              onNavigateToNewConversation = onNavigateToNewConversation,
              openUrl = openUrl,
              navigateToContactInfo = navigateToContactInfo,
              navigateToChipIdScreen = navigateToChipIdScreen,
              horizontalInsets = horizontalInsets,
            )

            HomeSection.Offers -> uiState.crossSellsPartition.offersCrossSell?.let { recommended ->
              OffersSection(
                recommendedCrossSell = recommended,
                onCrossSellClick = openCrossSellUrl,
                imageLoader = imageLoader,
                horizontalInsets = horizontalInsets,
              )
            }

            HomeSection.DiscoverInsurances -> DiscoverInsurancesSection(
              crossSells = uiState.crossSellsPartition.discoverCrossSells,
              onCrossSellClick = openCrossSellUrl,
              imageLoader = imageLoader,
            )

            HomeSection.Addons -> AddonsSection(
              addonBannerInfos = uiState.addonBannerInfos,
              navigateToAddonPurchaseFlow = navigateToAddonPurchaseFlow,
              horizontalInsets = horizontalInsets,
              imageLoader = imageLoader,
            )

            HomeSection.QuickActionTiles -> QuickActionTilesSection(
              isHelpCenterEnabled = uiState.isHelpCenterEnabled,
              onHelpAndSupport = navigateToHelpCenter,
              onChangeAddress = navigateToMovingFlow,
              onTravelCertificate = navigateToTravelCertificate,
              horizontalInsets = horizontalInsets,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HomeSheetDragHandle(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp)
      // Purely decorative handle; not interactive and there's no real sheet to drag.
      .semantics { hideFromAccessibility() },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      Modifier
        .width(40.dp)
        .height(4.dp)
        .background(
          color = HedvigTheme.colorScheme.surfaceSecondary,
          shape = HedvigTheme.shapes.cornerSmall,
        ),
    )
  }
}

private enum class HomeSection {
  Welcome,
  QuickActionCarousel,
  ClaimStatusCards,
  VeryImportantMessages,
  MemberReminders,
  Offers,
  DiscoverInsurances,
  Addons,
  QuickActionTiles,
}

// The single source of truth for the home section order; reorder here.
private val homeSectionOrder: List<HomeSection> = listOf(
  HomeSection.Welcome,
  HomeSection.QuickActionCarousel,
  HomeSection.ClaimStatusCards,
  HomeSection.VeryImportantMessages,
  HomeSection.MemberReminders,
  HomeSection.Offers,
  HomeSection.QuickActionTiles,
  HomeSection.DiscoverInsurances,
  HomeSection.Addons,
)

// Reproduces the inter-section gaps of the previous layout, now in a top-aligned list.
private fun gapBefore(section: HomeSection, previous: HomeSection?): Dp {
  if (previous == null) return 0.dp
  return when (section) {
    HomeSection.Welcome -> 0.dp
    HomeSection.QuickActionCarousel -> 8.dp
    HomeSection.ClaimStatusCards -> 24.dp
    HomeSection.VeryImportantMessages -> 16.dp
    HomeSection.MemberReminders -> if (previous == HomeSection.VeryImportantMessages) 8.dp else 16.dp
    HomeSection.Offers -> 16.dp
    HomeSection.DiscoverInsurances -> 16.dp
    HomeSection.Addons -> 16.dp
    HomeSection.QuickActionTiles -> 16.dp
  }
}

/**
 * The hero gradient ([Res.drawable.blur_background]) is the same light image in both light and dark
 * themes, so content drawn on it (greeting, pills, the tonal chat icon) must always render with the
 * light color scheme. Otherwise dark-theme text and tints turn near-invisible against the light gradient.
 */
@Composable
private fun OnHeroGradient(content: @Composable () -> Unit) {
  HedvigTheme(darkTheme = false, content = content)
}

@Composable
private fun WelcomeSection(homeText: HomeText) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    WelcomeMessage(
      homeText = homeText,
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        .testTag("welcome_message")
        .semantics {
          hideFromAccessibility()
        },
    )
  }
}

@Composable
private fun ClaimStatusCardsSection(
  claimStatusCardsData: ClaimStatusCardsData?,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  horizontalInsets: PaddingValues,
) {
  if (claimStatusCardsData != null) {
    ClaimStatusCards(
      onClick = onClaimDetailCardClicked,
      claimStatusCardsUiState = claimStatusCardsData.claimStatusCardsUiState,
      contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets,
    )
  }
}

@Composable
private fun VeryImportantMessagesSection(
  list: List<VeryImportantMessage>,
  openUrl: (String) -> Unit,
  markMessageAsSeen: (String) -> Unit,
  horizontalInsets: PaddingValues,
) {
  ImportantMessages(
    list = list,
    openUrl = openUrl,
    hideImportantMessage = markMessageAsSeen,
    contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets,
  )
}

@Composable
private fun MemberRemindersSection(
  homeText: HomeText,
  applicableReminders: List<MemberReminder>,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipIdScreen: () -> Unit,
  horizontalInsets: PaddingValues,
) {
  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    if (homeText is HomeText.ActiveInFuture) {
      HedvigNotificationCard(
        message = stringResource(Res.string.home_tab_active_in_future_info, homeText.inception),
        priority = NotificationPriority.Info,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .padding(horizontalInsets),
      )
    }
    MemberReminderCardsWithoutNotification(
      memberReminders = applicableReminders,
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToConnectPayout = navigateToConnectPayout,
      navigateToAddMissingInfo = navigateToMissingInfo,
      onNavigateToNewConversation = onNavigateToNewConversation,
      openUrl = openUrl,
      contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets,
      navigateToContactInfo = navigateToContactInfo,
      navigateToChipId = navigateToChipIdScreen,
    )
  }
}

@Composable
private fun OffersSection(
  recommendedCrossSell: RecommendedCrossSell,
  onCrossSellClick: (String) -> Unit,
  imageLoader: ImageLoader,
  horizontalInsets: PaddingValues,
) {
  val crossSell = recommendedCrossSell.crossSell
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .padding(horizontalInsets),
  ) {
    // TODO: Add "Your quotes" / "Dina prisförslag" to Lokalise.
    HedvigText(
      text = "Your quotes",
      style = HedvigTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    HedvigCard(
      onClick = { onCrossSellClick(crossSell.storeUrl) },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          AsyncImage(
            model = crossSell.pillowImage.src,
            contentDescription = null,
            imageLoader = imageLoader,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(48.dp),
          )
          Spacer(Modifier.width(12.dp))
          Column(Modifier.weight(1f)) {
            HedvigText(text = crossSell.title, style = HedvigTheme.typography.bodySmall)
            HedvigText(
              text = recommendedCrossSell.bannerText,
              style = HedvigTheme.typography.label,
              color = HedvigTheme.colorScheme.textSecondary,
            )
          }
          val discountText = recommendedCrossSell.discountText
          if (discountText != null) {
            Spacer(Modifier.width(8.dp))
            HighlightLabel(
              labelText = discountText,
              size = HighlightLabelDefaults.HighLightSize.Small,
              color = HighlightLabelDefaults.HighlightColor.Green(HighlightLabelDefaults.HighlightShade.LIGHT),
            )
          }
        }
        Spacer(Modifier.height(12.dp))
        HedvigButton(
          text = recommendedCrossSell.buttonText,
          onClick = { onCrossSellClick(crossSell.storeUrl) },
          buttonStyle = Secondary,
          buttonSize = ButtonSize.Medium,
          enabled = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun QuickActionTilesSection(
  isHelpCenterEnabled: Boolean,
  onHelpAndSupport: () -> Unit,
  onChangeAddress: () -> Unit,
  onTravelCertificate: () -> Unit,
  horizontalInsets: PaddingValues,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .padding(horizontalInsets),
  ) {
    HedvigText(
      text = stringResource(Res.string.HC_QUICK_ACTIONS_TITLE),
      style = HedvigTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Max),
    ) {
      if (isHelpCenterEnabled) {
        HomeActionTile(
          icon = HedvigIcons.HelipadOutline,
          text = stringResource(Res.string.home_tab_get_help),
          onClick = onHelpAndSupport,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        )
      }
      HomeActionTile(
        icon = HedvigIcons.Reload,
        text = stringResource(Res.string.HC_QUICK_ACTIONS_UPDATE_ADDRESS),
        onClick = onChangeAddress,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
      )
      HomeActionTile(
        icon = HedvigIcons.Travel,
        text = stringResource(Res.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE),
        onClick = onTravelCertificate,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
      )
    }
  }
}

@Composable
private fun HomeActionTile(icon: ImageVector, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  HedvigCard(onClick = onClick, modifier = modifier) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 14.dp, horizontal = 12.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = HedvigTheme.colorScheme.fillPrimary,
        modifier = Modifier.size(24.dp),
      )
      Spacer(Modifier.height(6.dp))
      HedvigText(text = text, style = HedvigTheme.typography.label)
    }
  }
}

@Composable
private fun HomeActionChip(text: String, onClick: () -> Unit) {
  Surface(
    onClick = onClick,
    shape = HedvigTheme.shapes.cornerXLarge,
    color = HedvigTheme.colorScheme.surfacePrimaryTransparent,
    role = Role.Button,
  ) {
    HedvigText(
      text = text,
      style = HedvigTheme.typography.label,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    )
  }
}

@Composable
private fun QuickActionCarouselSection(
  isHelpCenterEnabled: Boolean,
  onMakeClaim: () -> Unit,
  onHelpAndSupport: () -> Unit,
  onContactUs: () -> Unit,
  onForever: () -> Unit,
  horizontalInsets: PaddingValues,
  modifier: Modifier = Modifier,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 16.dp)
      .padding(horizontalInsets),
  ) {
    HomeActionChip(stringResource(Res.string.home_tab_claim_button_text), onMakeClaim)
    if (isHelpCenterEnabled) {
      HomeActionChip(stringResource(Res.string.home_tab_get_help), onHelpAndSupport)
    }
    HomeActionChip(stringResource(Res.string.DASHBOARD_OPEN_CHAT), onContactUs)
    HomeActionChip(stringResource(Res.string.TAB_REFERRALS_TITLE), onForever)
  }
}

@Composable
private fun AddonsSection(
  addonBannerInfos: List<AddonBannerInfo>,
  navigateToAddonPurchaseFlow: (List<String>) -> Unit,
  horizontalInsets: PaddingValues,
  imageLoader: ImageLoader,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .padding(horizontalInsets),
  ) {
    // TODO: Add an "Addons" / "Tilläggsförsäkringar" section header to Lokalise.
    HedvigText(
      text = "Addons",
      style = HedvigTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    addonBannerInfos.forEach { addon ->
      // Addons carry no imagery, so the pillow falls back to a generic placeholder.
      PillowRow(
        title = addon.title,
        subtitle = addon.description,
        pillowImage = null,
        buttonText = stringResource(Res.string.ADDON_FLOW_LEARN_MORE_BUTTON),
        onButtonClick = { navigateToAddonPurchaseFlow(addon.eligibleInsurancesIds) },
        imageLoader = imageLoader,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun DiscoverInsurancesSection(
  crossSells: List<CrossSell>,
  onCrossSellClick: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  CrossSellsSection(
    title = stringResource(Res.string.CROSS_SELL_SUBTITLE),
    crossSells = crossSells,
    onCrossSellClick = onCrossSellClick,
    modifier = Modifier.padding(horizontal = 16.dp),
    onSheetDismissed = {},
    imageLoader = imageLoader,
  )
}

@Composable
private fun ImportantMessages(
  list: List<VeryImportantMessage>,
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
  veryImportantMessage: VeryImportantMessage,
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
          leftButtonText = stringResource(Res.string.important_message_hide),
          rightButtonText = veryImportantMessage.linkInfo.buttonText
            ?: stringResource(Res.string.important_message_read_more),
          onLeftButtonClick = { hideImportantMessage(veryImportantMessage.id) },
          onRightButtonClick = { openUrl(veryImportantMessage.linkInfo.link) },
        )
      } else {
        NotificationDefaults.InfoCardStyle.Button(
          buttonText = stringResource(Res.string.important_message_hide),
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
        .height(40.dp)
        .padding(horizontal = 16.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    )
  } else {
    val headlineText = when (homeText) {
      is Active -> stringResource(Res.string.home_tab_welcome_title_without_name)
      is HomeText.ActiveInFuture -> error("Image shows here instead")
      is HomeText.Pending -> stringResource(Res.string.home_tab_pending_unknown_title_without_name)
      is HomeText.Switching -> stringResource(Res.string.home_tab_pending_switchable_welcome_title_without_name)
      is HomeText.Terminated -> stringResource(Res.string.home_tab_terminated_welcome_title_without_name)
    }
    HedvigText(
      text = headlineText,
      // todo custom style since new DS does not have this specification
      //  https://hedviginsurance.slack.com/archives/C03U9C6Q7TP/p1727365167917719
      style = HedvigTheme.typography.headlineMedium.copy(
        fontFamily = HedvigTheme.typography.serif,
        fontSize = 28.0.sp,
        lineBreak = LineBreak.Heading,
        textAlign = TextAlign.Center,
        color = HedvigTheme.colorScheme.textSecondary,
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
    snapshotFlow { state.isVisible }.distinctUntilChanged().collect { isVisible ->
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
                backgroundPillowImages = ("ds" to "ds"),
                bundleProgress = BundleProgress(1, 15),
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
              recommendedAddon = null,
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
          addonBannerInfos = listOf(
            AddonBannerInfo(
              title = "Title",
              description = "description",
              labels = listOf("Label"),
              eligibleInsurancesIds = nonEmptyListOf("id"),
              flowType = FlowType.APP_TRAVEL_PLUS_SELL_OR_UPGRADE,
            ),
          ),
          isProduction = true,
        ),
        notificationPermissionState = rememberPreviewNotificationPermissionState(),
        reload = {},
        onNavigateToInbox = {},
        onNavigateToNewConversation = {},
        navigateToClaimChat = {},
        onClaimDetailCardClicked = {},
        navigateToConnectPayment = {},
        navigateToConnectPayout = {},
        navigateToHelpCenter = {},
        navigateToMovingFlow = {},
        openUrl = {},
        openCrossSellUrl = {},
        openAppSettings = {},
        navigateToMissingInfo = { _, _ -> },
        markMessageAsSeen = {},
        navigateToFirstVet = {},
        markCrossSellsNotificationAsSeen = {},
        navigateToContactInfo = {},
        navigateToChipIdScreen = {},
        setEpochDayWhenLastToolTipShown = {},
        imageLoader = rememberPreviewImageLoader(),
        navigateToForever = {},
        navigateToTravelCertificate = {},
        navigateToAddonPurchaseFlow = {},
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
        navigateToClaimChat = {},
        onClaimDetailCardClicked = {},
        navigateToConnectPayment = {},
        navigateToConnectPayout = {},
        navigateToHelpCenter = {},
        navigateToMovingFlow = {},
        openUrl = {},
        openCrossSellUrl = {},
        openAppSettings = {},
        navigateToMissingInfo = { _, _ -> },
        markMessageAsSeen = {},
        navigateToFirstVet = {},
        markCrossSellsNotificationAsSeen = {},
        navigateToContactInfo = {},
        navigateToChipIdScreen = {},
        setEpochDayWhenLastToolTipShown = {},
        imageLoader = rememberPreviewImageLoader(),
        navigateToForever = {},
        navigateToTravelCertificate = {},
        navigateToAddonPurchaseFlow = {},
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
        uiState = Success(
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
          chatAction = ChatAction,
          addonBannerInfos = emptyList(),
          isProduction = true,
        ),
        notificationPermissionState = rememberPreviewNotificationPermissionState(),
        reload = {},
        onNavigateToInbox = {},
        onNavigateToNewConversation = {},
        navigateToClaimChat = {},
        onClaimDetailCardClicked = {},
        navigateToConnectPayment = {},
        navigateToConnectPayout = {},
        navigateToHelpCenter = {},
        navigateToMovingFlow = {},
        openUrl = {},
        openCrossSellUrl = {},
        openAppSettings = {},
        navigateToMissingInfo = { _, _ -> },
        markMessageAsSeen = {},
        navigateToFirstVet = {},
        markCrossSellsNotificationAsSeen = {},
        navigateToContactInfo = {},
        navigateToChipIdScreen = {},
        setEpochDayWhenLastToolTipShown = {},
        imageLoader = rememberPreviewImageLoader(),
        navigateToForever = {},
        navigateToTravelCertificate = {},
        navigateToAddonPurchaseFlow = {},
      )
    }
  }
}

private class HomeTextPreviewParameterProvider : CollectionPreviewParameterProvider<HomeText>(
  listOf(
    Active,
    HomeText.ActiveInFuture(LocalDate.parse("2025-01-01")),
    HomeText.Pending,
    HomeText.Switching,
    HomeText.Terminated,
  ),
)
