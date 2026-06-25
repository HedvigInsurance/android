package com.hedvig.android.feature.home.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.systemGestureExclusion
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Dp
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
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.data.addons.data.FlowType
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.FeatureAddonBanner
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
import hedvig.resources.ADDON_FLOW_SEE_PRICE_BUTTON
import hedvig.resources.CHAT_NEW_MESSAGE
import hedvig.resources.DASHBOARD_OPEN_CHAT
import hedvig.resources.HC_QUICK_ACTIONS_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE
import hedvig.resources.Res
import hedvig.resources.TAB_REFERRALS_TITLE
import hedvig.resources.TOAST_NEW_OFFER
import hedvig.resources.home_tab_active_in_future_info
import hedvig.resources.home_tab_claim_button_text
import hedvig.resources.home_tab_get_help
import hedvig.resources.home_tab_pending_switchable_welcome_title_without_name
import hedvig.resources.home_tab_pending_unknown_title_without_name
import hedvig.resources.home_tab_terminated_welcome_title_without_name
import hedvig.resources.home_tab_welcome_title_without_name
import hedvig.resources.important_message_hide
import hedvig.resources.important_message_read_more
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
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeDestination(
  viewModel: HomeViewModel,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToClaimChat: () -> Unit,
  navigateToClaimChatInDevMode: () -> Unit,
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
    navigateToClaimChatInDevMode = navigateToClaimChatInDevMode,
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
  navigateToClaimChatInDevMode: () -> Unit,
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
    navigateToClaimChatInDevMode = navigateToClaimChatInDevMode,
    isStagingEnvironment = (uiState as? Success)?.isProduction?.not() ?: false,
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
          actionsList.forEach { action ->
            when (action) {
              ChatAction -> {
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
  Box(
    modifier = modifier
      .fillMaxSize()
      .onConsumedWindowInsetsChanged { consumedWindowInsets.insets = it }
      .pullRefresh(pullRefreshState),
  ) {
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
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(
        top = toolbarHeight + topInsets.calculateTopPadding(),
        bottom = 16.dp + bottomInsets.calculateBottomPadding(),
      ),
    ) {
      // Greeting is a tall item that scrolls away; the pills below pin under the toolbar/top insets.
      if (HomeSection.Welcome in visibleSections) {
        item(key = HomeSection.Welcome, contentType = "welcome") {
          Box(
            Modifier
              .fillMaxWidth()
              .padding(vertical = 56.dp),
          ) {
            WelcomeSection(uiState.firstName, uiState.homeText)
          }
        }
      }
      if (HomeSection.QuickActionCarousel in visibleSections) {
        stickyHeader(key = HomeSection.QuickActionCarousel, contentType = "pills") {
          QuickActionCarouselSection(
            isHelpCenterEnabled = uiState.isHelpCenterEnabled,
            onMakeClaim = openClaimFlowSheet,
            onHelpAndSupport = navigateToHelpCenter,
            onContactUs = onNavigateToInbox,
            onForever = navigateToForever,
            horizontalInsets = horizontalInsets,
            modifier = Modifier
              .fillMaxWidth()
              .background(HedvigTheme.colorScheme.backgroundPrimary)
              .padding(vertical = 8.dp),
          )
        }
      }
      val scrollingSections = visibleSections.filterNot {
        it == HomeSection.Welcome || it == HomeSection.QuickActionCarousel
      }
      itemsIndexed(scrollingSections, key = { _, section -> section }) { index, section ->
        Column {
          val previous = if (index == 0) HomeSection.QuickActionCarousel else scrollingSections[index - 1]
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
  HomeSection.VeryImportantMessages,
  HomeSection.ClaimStatusCards,
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

@Composable
private fun WelcomeSection(firstName: String, homeText: HomeText) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    if (firstName.isNotBlank() && homeText !is HomeText.ActiveInFuture) {
      // TODO: Add "Hi %1$s" / "Hej %1$s" to Lokalise — no with-name welcome string exists yet.
      HedvigText(
        text = "Hi $firstName",
        style = HedvigTheme.typography.headlineMedium.copy(
          fontFamily = HedvigTheme.typography.serif,
          fontSize = 28.0.sp,
          lineBreak = LineBreak.Heading,
          textAlign = TextAlign.Center,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
          .testTag("welcome_greeting")
          .semantics {
            hideFromAccessibility()
          },
      )
    }
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
    HedvigText(text = "Your quotes", style = HedvigTheme.typography.headlineSmall)
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
    HedvigText(text = stringResource(Res.string.HC_QUICK_ACTIONS_TITLE), style = HedvigTheme.typography.headlineSmall)
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
      // TODO: Add "Change address" / "Byt adress" to Lokalise.
      HomeActionTile(
        icon = HedvigIcons.Reload,
        text = "Change address",
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
  // TODO: tonal-glass chip interpretation (no live blur); swap to a DS chip/glass button when it exists.
  Surface(
    onClick = onClick,
    shape = HedvigTheme.shapes.cornerXLarge,
    color = HedvigTheme.colorScheme.surfacePrimaryTransparent,
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
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .padding(horizontalInsets),
  ) {
    // TODO: Add an "Addons" / "Tillägg" section header to Lokalise.
    HedvigText(text = "Addons", style = HedvigTheme.typography.headlineSmall)
    addonBannerInfos.forEach { addon ->
      FeatureAddonBanner(
        title = addon.title,
        description = addon.description,
        buttonText = stringResource(Res.string.ADDON_FLOW_SEE_PRICE_BUTTON),
        labels = addon.labels,
        onButtonClick = { navigateToAddonPurchaseFlow(addon.eligibleInsurancesIds) },
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
    // TODO: Add "Discover our insurances" / "Upptäck våra försäkringar" to Lokalise.
    title = "Discover our insurances",
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
        navigateToClaimChatInDevMode = {},
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
        navigateToClaimChatInDevMode = {},
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
        navigateToClaimChatInDevMode = {},
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
