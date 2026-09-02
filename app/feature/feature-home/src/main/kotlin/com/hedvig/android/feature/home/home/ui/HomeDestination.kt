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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
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
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.compose.pager.indicator.CardCarousel
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.common.image.storyblokResized
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
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.RoundedLiquidGlass
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.RoundedPrimary
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.DraftClaimDialog
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTooltip
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.StartClaimBottomSheet
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.TopEnd
import com.hedvig.android.design.system.hedvig.TooltipDefaults.TooltipStyle.Campaign
import com.hedvig.android.design.system.hedvig.TooltipDefaults.TooltipStyle.Campaign.Brightness
import com.hedvig.android.design.system.hedvig.TooltipDefaults.TooltipStyle.Inbox
import com.hedvig.android.design.system.hedvig.TopAppBarLayoutForActions
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadOutline
import com.hedvig.android.design.system.hedvig.icon.Reload
import com.hedvig.android.design.system.hedvig.icon.Settings
import com.hedvig.android.design.system.hedvig.icon.Travel
import com.hedvig.android.design.system.hedvig.notificationCircle
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.home.home.data.HomeData.ClaimStatusCardsData
import com.hedvig.android.feature.home.home.data.HomeData.DraftClaim
import com.hedvig.android.feature.home.home.data.HomeData.VeryImportantMessage
import com.hedvig.android.feature.home.home.data.HomeData.VeryImportantMessage.LinkInfo
import com.hedvig.android.feature.home.home.data.OngoingShopSession
import com.hedvig.android.feature.home.home.ui.HomeEvent.CrossSellToolTipShown
import com.hedvig.android.feature.home.home.ui.HomeEvent.DeleteDraftClaim
import com.hedvig.android.feature.home.home.ui.HomeEvent.MarkCardCrossSellsAsSeen
import com.hedvig.android.feature.home.home.ui.HomeEvent.MarkMessageAsSeen
import com.hedvig.android.feature.home.home.ui.HomeEvent.RefreshData
import com.hedvig.android.feature.home.home.ui.HomeNoticeCard.Important
import com.hedvig.android.feature.home.home.ui.HomeNoticeCard.Renewal
import com.hedvig.android.feature.home.home.ui.HomeNoticeCard.Status
import com.hedvig.android.feature.home.home.ui.HomeText.Active
import com.hedvig.android.feature.home.home.ui.HomeText.ActiveInFuture
import com.hedvig.android.feature.home.home.ui.HomeText.Pending
import com.hedvig.android.feature.home.home.ui.HomeText.Switching
import com.hedvig.android.feature.home.home.ui.HomeText.Terminated
import com.hedvig.android.feature.home.home.ui.HomeTopBarAction.ChatAction
import com.hedvig.android.feature.home.home.ui.HomeTopBarAction.CrossSellsAction
import com.hedvig.android.feature.home.home.ui.HomeTopBarAction.FirstVetAction
import com.hedvig.android.feature.home.home.ui.HomeUiState.Error
import com.hedvig.android.feature.home.home.ui.HomeUiState.Loading
import com.hedvig.android.feature.home.home.ui.HomeUiState.Success
import com.hedvig.android.memberquickactions.InnerHelpCenterDestination.FirstVet
import com.hedvig.android.memberquickactions.QuickAction
import com.hedvig.android.memberquickactions.QuickAction.MultiSelectExpandedLink
import com.hedvig.android.memberquickactions.QuickAction.StandaloneQuickLink
import com.hedvig.android.memberquickactions.QuickLinkDestination
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkChangeAddress
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkChangeTier
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminder.PaymentReminder.ConnectPayment
import com.hedvig.android.memberreminders.MemberReminder.UpcomingRenewal
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.memberreminders.ui.MemberReminderToDoList
import com.hedvig.android.memberreminders.ui.homeActionRequiredReminders
import com.hedvig.android.memberreminders.ui.homeInformationalReminders
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
import com.hedvig.android.ui.claimstatus.model.ClaimCardUiState
import com.hedvig.android.ui.claimstatus.model.ClaimCardUiState.Draft
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Claim
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Closed.NotCompensated
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentText.Closed
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType.INACTIVE
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.android.ui.emergency.FirstVetSection
import hedvig.resources.CHAT_NEW_MESSAGE
import hedvig.resources.DASHBOARD_OPEN_CHAT
import hedvig.resources.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_EDIT_INSURANCE_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_EDIT_INSURANCE_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_TITLE
import hedvig.resources.HOME_ADDONS_READ_MORE_BUTTON
import hedvig.resources.HOME_DISCOVER_SECTION_TITLE
import hedvig.resources.HOME_DISCOVER_SEE_PRICE_BUTTON
import hedvig.resources.HOME_GREETING_SUBTITLE
import hedvig.resources.HOME_GREETING_TITLE
import hedvig.resources.HOME_QUOTES_SECTION_TITLE
import hedvig.resources.HOME_TODO_SECTION_TITLE
import hedvig.resources.INSURANCE_ADDONS_SUBHEADING
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.RESUME_CLAIM_DELETE_BODY
import hedvig.resources.RESUME_CLAIM_DELETE_BUTTON
import hedvig.resources.RESUME_CLAIM_DELETE_TITLE
import hedvig.resources.RESUME_CLAIM_EXPIRED_BODY
import hedvig.resources.RESUME_CLAIM_EXPIRED_TITLE
import hedvig.resources.Res.drawable
import hedvig.resources.Res.string
import hedvig.resources.TOAST_NEW_OFFER
import hedvig.resources.blur_background
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import hedvig.resources.home_tab_claim_button_text
import hedvig.resources.home_tab_get_help
import hedvig.resources.home_tab_welcome_title_without_name
import hedvig.resources.ongoing_shop_session_dismiss_offer
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.milliseconds
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
  navigateToClaimChat: (resumeClaim: Boolean) -> Unit,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToHelpCenter: () -> Unit,
  navigateToQuickLink: (QuickLinkDestination) -> Unit,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  openAppSettings: () -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  navigateToFirstVet: (List<FirstVetSection>) -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipId: () -> Unit,
  navigateToUsageData: () -> Unit,
  imageLoader: ImageLoader,
  navigateToAddonPurchaseFlow: (List<String>) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val notificationPermissionState = rememberNotificationPermissionState()
  HomeScreen(
    uiState = uiState,
    notificationPermissionState = notificationPermissionState,
    reload = { viewModel.emit(RefreshData) },
    onNavigateToInbox = onNavigateToInbox,
    onNavigateToNewConversation = onNavigateToNewConversation,
    navigateToClaimChat = navigateToClaimChat,
    onClaimDetailCardClicked = onClaimDetailCardClicked,
    navigateToConnectPayment = navigateToConnectPayment,
    navigateToConnectPayout = navigateToConnectPayout,
    navigateToHelpCenter = navigateToHelpCenter,
    navigateToQuickLink = navigateToQuickLink,
    openUrl = openUrl,
    openCrossSellUrl = openCrossSellUrl,
    openAppSettings = openAppSettings,
    navigateToMissingInfo = navigateToMissingInfo,
    markMessageAsSeen = { viewModel.emit(MarkMessageAsSeen(it)) },
    deleteDraftClaim = { draftId -> viewModel.emit(DeleteDraftClaim(draftId)) },
    dismissOngoingShopSession = { sessionId ->
      viewModel.emit(HomeEvent.DismissOngoingShopSession(sessionId))
    },
    navigateToFirstVet = navigateToFirstVet,
    markCrossSellsNotificationAsSeen = { viewModel.emit(MarkCardCrossSellsAsSeen) },
    navigateToContactInfo = navigateToContactInfo,
    navigateToChipIdScreen = navigateToChipId,
    navigateToUsageData = navigateToUsageData,
    setEpochDayWhenLastToolTipShown = { epochDay ->
      viewModel.emit(CrossSellToolTipShown(epochDay))
    },
    imageLoader = imageLoader,
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
  navigateToClaimChat: (resumeClaim: Boolean) -> Unit,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToHelpCenter: () -> Unit,
  navigateToQuickLink: (QuickLinkDestination) -> Unit,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  markMessageAsSeen: (String) -> Unit,
  deleteDraftClaim: (String) -> Unit,
  dismissOngoingShopSession: (String) -> Unit,
  openAppSettings: () -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  navigateToFirstVet: (List<FirstVetSection>) -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipIdScreen: () -> Unit,
  navigateToUsageData: () -> Unit,
  markCrossSellsNotificationAsSeen: () -> Unit,
  setEpochDayWhenLastToolTipShown: (Long) -> Unit,
  imageLoader: ImageLoader,
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

  val editInsuranceSheetState = rememberHedvigBottomSheetState<MultiSelectExpandedLink>()
  EditInsuranceQuickActionSheet(
    state = editInsuranceSheetState,
    onQuickLink = navigateToQuickLink,
  )

  val startClaimBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  StartClaimBottomSheet(
    state = startClaimBottomSheetState,
    navigateToClaimChat = {
      navigateToClaimChat(false)
    },
  )
  val draftClaim = (uiState as? Success)?.draftClaim
  var showDraftClaimDialog by remember { mutableStateOf(false) }
  var showDraftExpiredDialog by remember { mutableStateOf(false) }
  var draftIdPendingDeleteConfirmation by remember { mutableStateOf<String?>(null) }
  if (showDraftClaimDialog) {
    DraftClaimDialog(
      onDismissRequest = { showDraftClaimDialog = false },
      onContinueDraft = {
        showDraftClaimDialog = false
        navigateToClaimChat(true)
      },
      onStartNewClaim = {
        showDraftClaimDialog = false
        startClaimBottomSheetState.show(Unit)
      },
    )
  }
  if (showDraftExpiredDialog) {
    // The draft is expired, so acknowledging the notice (Close button, scrim, or back) removes it.
    // Matches the Ready-for-dev design: single Close, closing removes the draft claim card.
    ErrorDialog(
      title = stringResource(string.RESUME_CLAIM_EXPIRED_TITLE),
      message = stringResource(string.RESUME_CLAIM_EXPIRED_BODY),
      onDismiss = {
        showDraftExpiredDialog = false
        draftClaim?.let { deleteDraftClaim(it.id) }
      },
    )
  }
  val draftIdToDelete = draftIdPendingDeleteConfirmation
  if (draftIdToDelete != null) {
    HedvigAlertDialog(
      title = stringResource(string.RESUME_CLAIM_DELETE_TITLE),
      text = stringResource(string.RESUME_CLAIM_DELETE_BODY),
      confirmButtonLabel = stringResource(string.RESUME_CLAIM_DELETE_BUTTON),
      dismissButtonLabel = stringResource(string.general_cancel_button),
      onDismissRequest = { draftIdPendingDeleteConfirmation = null },
      onConfirmClick = {
        draftIdPendingDeleteConfirmation = null
        deleteDraftClaim(draftIdToDelete)
      },
    )
  }
  Box(Modifier.fillMaxSize()) {
    val toolbarHeight = 64.dp
    val transition = updateTransition(targetState = uiState, label = "home ui state")
    transition.AnimatedContent(
      modifier = Modifier.fillMaxSize(),
      contentKey = { it::class },
    ) { uiState ->
      when (uiState) {
        Loading -> {
          HedvigFullScreenCenterAlignedProgressDebounced(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.safeDrawing),
          )
        }

        is Error -> {
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
            navigateToQuickLink = navigateToQuickLink,
            onEditInsurance = { editInsuranceSheetState.show(it) },
            onNavigateToInbox = onNavigateToInbox,
            openClaimFlowSheet = {
              if (draftClaim != null) {
                showDraftClaimDialog = true
              } else {
                startClaimBottomSheetState.show(Unit)
              }
            },
            onContinueDraftClaim = {
              if (draftClaim != null) {
                if (draftClaim.isExpired(System.now())) {
                  showDraftExpiredDialog = true
                } else {
                  navigateToClaimChat(true)
                }
              }
            },
            onDeleteDraftClaim = { draftId -> draftIdPendingDeleteConfirmation = draftId },
            openAppSettings = openAppSettings,
            openUrl = openUrl,
            navigateToMissingInfo = navigateToMissingInfo,
            onNavigateToNewConversation = onNavigateToNewConversation,
            markMessageAsSeen = markMessageAsSeen,
            dismissOngoingShopSession = dismissOngoingShopSession,
            navigateToContactInfo = navigateToContactInfo,
            navigateToChipIdScreen = navigateToChipIdScreen,
            navigateToUsageData = navigateToUsageData,
            openCrossSellUrl = openCrossSellUrl,
            imageLoader = imageLoader,
            navigateToAddonPurchaseFlow = navigateToAddonPurchaseFlow,
            onChatIconClick = onNavigateToInbox,
            onCrossSellsIconClick = { crossSells ->
              crossSellBottomSheetState.show(
                crossSells,
              )
            },
            navigateToFirstVet = { sections -> navigateToFirstVet(sections) },
            setEpochDayWhenLastToolTipShown = setEpochDayWhenLastToolTipShown,
          )
        }
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

@Composable
private fun HomeScreenTopBar(
  uiState: HomeUiState,
  onChatIconClick: () -> Unit,
  onCrossSellsIconClick: (crossSells: CrossSellSheetData) -> Unit,
  navigateToFirstVet: (sections: List<FirstVetSection>) -> Unit,
  setEpochDayWhenLastToolTipShown: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
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
                onClick = onChatIconClick,
                modifier = Modifier.notificationCircle(uiState.hasUnseenChatMessages),
              )
            }

            is CrossSellsAction -> {
              ToolbarCrossSellsIcon(
                onClick = {
                  onCrossSellsIconClick(
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
          message = stringResource(string.CHAT_NEW_MESSAGE),
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
}

@OptIn(ExperimentalTime::class)
@Composable
private fun ColumnScope.CrossSellsTooltip(uiState: Success, setEpochDayWhenLastToolTipShown: (Long) -> Unit) {
  if (uiState.crossSellsAction != null) {
    val shouldShowCrossSellsTooltip = uiState.crossSellsAction.crossSellRecommendationNotification.showToolTip
    var shouldSetEpochDayWhenLastToolTipShown by remember { mutableStateOf(false) }
    LaunchedEffect(shouldSetEpochDayWhenLastToolTipShown) {
      if (shouldSetEpochDayWhenLastToolTipShown) {
        val today = System.now().toLocalDateTime(
          TimeZone.currentSystemDefault(),
        ).date.toEpochDays()
        delay(5000.milliseconds)
        setEpochDayWhenLastToolTipShown(today)
      }
    }
    if (shouldShowCrossSellsTooltip) {
      HedvigTooltip(
        message = stringResource(string.TOAST_NEW_OFFER),
        showTooltip = true,
        tooltipStyle = Campaign(
          subMessage = null,
          Brightness.BRIGHT,
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
  navigateToQuickLink: (QuickLinkDestination) -> Unit,
  onEditInsurance: (MultiSelectExpandedLink) -> Unit,
  onNavigateToInbox: () -> Unit,
  openClaimFlowSheet: () -> Unit,
  onContinueDraftClaim: () -> Unit,
  onDeleteDraftClaim: (String) -> Unit,
  dismissOngoingShopSession: (String) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  markMessageAsSeen: (String) -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipIdScreen: () -> Unit,
  navigateToUsageData: () -> Unit,
  openCrossSellUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  navigateToAddonPurchaseFlow: (List<String>) -> Unit,
  onChatIconClick: () -> Unit,
  onCrossSellsIconClick: (crossSells: CrossSellSheetData) -> Unit,
  navigateToFirstVet: (sections: List<FirstVetSection>) -> Unit,
  setEpochDayWhenLastToolTipShown: (Long) -> Unit,
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
    // Full-screen blur gradient behind the whole home screen, in the light theme only; the dark theme
    // keeps the regular background. Sections that need a solid surface draw their own background on top
    // to "hide" it (the content cards already do; so do the pinned pills).
    if (HedvigTheme.colorScheme.isLight) {
      Image(
        painter = painterResource(drawable.blur_background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.matchParentSize(),
      )
    }
    NotificationPermissionDialog(notificationPermissionState, openAppSettings)
    val horizontalInsets =
      WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).exclude(consumedWindowInsets).asPaddingValues()
    val topInsets =
      WindowInsets.safeDrawing.only(WindowInsetsSides.Top).exclude(consumedWindowInsets).asPaddingValues()
    val bottomInsets =
      WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).exclude(consumedWindowInsets).asPaddingValues()
    val applicableReminders =
      uiState.memberReminders.onlyApplicableReminders(notificationPermissionState.status.isGranted)
    val informationalReminders = applicableReminders.homeInformationalReminders()
    val visibleSections = homeSectionOrder.filter { section ->
      when (section) {
        HomeSection.Welcome -> {
          true
        }

        HomeSection.MainActionCarousel -> {
          true
        }

        HomeSection.ClaimStatusCards -> {
          uiState.claimStatusCardsData != null || uiState.draftClaim != null
        }

        HomeSection.VeryImportantMessages -> {
          uiState.veryImportantMessages.isNotEmpty() ||
            informationalReminders.isNotEmpty() ||
            uiState.homeText != Active
        }

        HomeSection.MemberReminders -> {
          applicableReminders.homeActionRequiredReminders().isNotEmpty()
        }

        HomeSection.Quotes -> {
          uiState.ongoingShopSessions.isNotEmpty()
        }

        HomeSection.DiscoverInsurances -> {
          uiState.discoverCrossSells.isNotEmpty()
        }

        HomeSection.Addons -> {
          uiState.addonBannerInfos.isNotEmpty()
        }

        HomeSection.QuickActionTiles -> {
          uiState.quickActions.isNotEmpty()
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
    // The greeting's current fade, published from the hero's layout so the floating icons — which sit
    // above the list, not inside it — can leave on exactly the same curve.
    val heroContentAlpha = remember { mutableFloatStateOf(1f) }
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
    // The sheet's surface is painted by three things that have to agree: this backdrop, the pinned lid,
    // and every scrolling section's own background. Only the parts the sections don't cover are actually
    // this backdrop, so changing one of the three alone leaves the rest at the old color.
    val sheetColor = HedvigTheme.colorScheme.backgroundPrimary
    val sheetShape = HedvigTheme.shapes.cornerXLargeTop
    // Opaque sheet backdrop from the pinned lid down to the bottom, so the sheet fills the screen even
    // when the content is short (e.g. landscape) — no blur gap below the last section. Drawn behind the
    // LazyColumn (on top of the blur); the hero + pills above the lid stay transparent on the gradient.
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
                val parallaxPx = heroParallaxDistance.toPx()
                layout(placeable.width, heroHeight) {
                  val y = (heroHeight - placeable.height).coerceAtLeast(0)
                  // Greeting fades over the final phase (as its space is squeezed into the clearance) and
                  // parallaxes: lifts slightly and scales down as it goes.
                  val greetingAlpha = ((heroHeight - clearancePx).toFloat() / placeable.height).coerceIn(0f, 1f)
                  heroContentAlpha.floatValue = greetingAlpha
                  val fade = 1f - greetingAlpha
                  placeable.placeWithLayer(0, y) {
                    alpha = greetingAlpha
                    scaleX = 1f - HERO_SCALE_DOWN * fade
                    scaleY = 1f - HERO_SCALE_DOWN * fade
                    translationY = -fade * parallaxPx
                  }
                }
              },
          ) {
            WelcomeSection(uiState.firstName)
          }
        }
      }
      if (HomeSection.MainActionCarousel in visibleSections) {
        stickyHeader(key = HomeSection.MainActionCarousel, contentType = "pills") {
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
            // The toolbar half of the clearance is only owed while the icons are actually on screen, so it
            // shrinks on their fade and the pills pin directly under the status bar once they're gone. Same
            // state as the icons, measured in the layout phase, so the two can't disagree about the space.
            Spacer(
              Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                  val clearance = topInsets.calculateTopPadding().toPx() +
                    toolbarHeight.toPx() * heroContentAlpha.floatValue
                  val placeable = measurable.measure(constraints)
                  layout(placeable.width, clearance.roundToInt()) {}
                },
            )
            MainActionCarouselSection(
              isHelpCenterEnabled = uiState.isHelpCenterEnabled,
              onMakeClaim = openClaimFlowSheet,
              onHelpAndSupport = navigateToHelpCenter,
              onContactUs = onNavigateToInbox,
              horizontalInsets = horizontalInsets,
              modifier = Modifier.padding(bottom = 16.dp),
            )
            HomeSheetDragHandle(
              Modifier
                .fillMaxWidth()
                .background(color = sheetColor, shape = sheetShape)
                .topEdgeBorder(sheetShape, HedvigTheme.colorScheme.borderPrimary),
            )
          }
        }
      }
      val scrollingSections = visibleSections.filterNot {
        it == HomeSection.Welcome || it == HomeSection.MainActionCarousel
      }
      itemsIndexed(scrollingSections, key = { _, section -> section }) { index, section ->
        val next = scrollingSections.getOrNull(index + 1)
        var itemTopPx by remember { mutableFloatStateOf(0f) }
        Column(
          Modifier
            .fillMaxWidth()
            .onPlaced { itemTopPx = it.positionInParent().y }
            .drawWithContent {
              // Clip content sliding up under the transparent pinned pills. Section spacing is carried as
              // trailing room (below the content), so a card's drop-shadow renders inside the section's own
              // bounds instead of bleeding past the clip or under the pinned header.
              val clipTop = (stickyHeaderBottomPx - itemTopPx).coerceIn(0f, size.height)
              clipRect(top = clipTop) { this@drawWithContent.drawContent() }
            }
            .background(sheetColor),
        ) {
          // Each section carries its gap to the NEXT one as trailing room, giving drop-shadows space
          // within the section. The first one starts flush under the pinned lid.
          when (section) {
            HomeSection.Welcome, HomeSection.MainActionCarousel -> Unit

            // pinned above the scrolling content

            HomeSection.ClaimStatusCards -> ClaimStatusCardsSection(
              claimStatusCardsData = uiState.claimStatusCardsData,
              draftClaim = uiState.draftClaim,
              onClaimDetailCardClicked = onClaimDetailCardClicked,
              onContinueDraftClaim = onContinueDraftClaim,
              onDeleteDraftClaim = onDeleteDraftClaim,
              horizontalInsets = horizontalInsets,
            )

            HomeSection.VeryImportantMessages -> VeryImportantMessagesSection(
              homeText = uiState.homeText,
              list = uiState.veryImportantMessages,
              informationalReminders = informationalReminders,
              openUrl = openUrl,
              markMessageAsSeen = markMessageAsSeen,
              horizontalInsets = horizontalInsets,
            )

            HomeSection.MemberReminders -> MemberRemindersSection(
              applicableReminders = applicableReminders,
              navigateToConnectPayment = navigateToConnectPayment,
              navigateToConnectPayout = navigateToConnectPayout,
              navigateToMissingInfo = navigateToMissingInfo,
              onNavigateToNewConversation = onNavigateToNewConversation,
              navigateToContactInfo = navigateToContactInfo,
              navigateToChipIdScreen = navigateToChipIdScreen,
              navigateToUsageData = navigateToUsageData,
              horizontalInsets = horizontalInsets,
            )

            HomeSection.Quotes -> uiState.ongoingShopSessions.takeIf { it.isNotEmpty() }?.let { sessions ->
              QuotesSection(
                sessions = sessions,
                onResumeClick = openUrl,
                onDismiss = dismissOngoingShopSession,
                imageLoader = imageLoader,
                horizontalInsets = horizontalInsets,
              )
            }

            HomeSection.DiscoverInsurances -> DiscoverInsurancesSection(
              crossSells = uiState.discoverCrossSells,
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
              quickActions = uiState.quickActions,
              onQuickLink = navigateToQuickLink,
              onEditInsurance = onEditInsurance,
              horizontalInsets = horizontalInsets,
            )
          }
          if (next != null) {
            Spacer(Modifier.height(homeSectionGap))
          }
        }
      }
    }
    // The icons float over the list, in the clearance the greeting and the pinned pills both leave at the
    // top of the window, and leave on the greeting's curve: same fade, lift and shrink, driven by the alpha
    // the hero publishes as it collapses. They shrink toward their top edge, so the row stays put instead of
    // drifting down toward the tooltip that shares this column. Fully faded, they aren't placed at all, so
    // an invisible icon can't take a tap. All layout-phase, so scrolling neither recomposes nor re-measures.
    HomeScreenTopBar(
      uiState = uiState,
      onChatIconClick = onChatIconClick,
      onCrossSellsIconClick = onCrossSellsIconClick,
      navigateToFirstVet = navigateToFirstVet,
      setEpochDayWhenLastToolTipShown = setEpochDayWhenLastToolTipShown,
      modifier = Modifier
        .align(Alignment.TopStart)
        .layout { measurable, constraints ->
          val placeable = measurable.measure(constraints)
          val parallaxPx = heroParallaxDistance.toPx()
          layout(placeable.width, placeable.height) {
            val heroAlpha = heroContentAlpha.floatValue
            when {
              heroAlpha <= 0f -> {
                Unit
              }

              // The icons' glass rim and drop shadow are blur effects, and a layer around them flattens
              // those into a rectangle the size of this row. So: no layer at all at rest, and a layer that
              // modulates each drawing command rather than compositing the row off-screen while it leaves.
              heroAlpha >= 1f -> {
                placeable.place(0, 0)
              }

              else -> {
                val fade = 1f - heroAlpha
                placeable.placeWithLayer(0, 0) {
                  compositingStrategy = CompositingStrategy.ModulateAlpha
                  alpha = heroAlpha
                  transformOrigin = TransformOrigin(0.5f, 0f)
                  scaleX = 1f - HERO_SCALE_DOWN * fade
                  scaleY = 1f - HERO_SCALE_DOWN * fade
                  translationY = -fade * parallaxPx
                }
              }
            }
          }
        },
    )
  }
}

/**
 * Strokes only the top of [shape]: its rounded corners and the edge between them. The stroke runs down
 * the sides for the height of the element it is applied to and stops there, and the bottom edge is
 * clipped away entirely, so the surface below continues borderless.
 */
private fun Modifier.topEdgeBorder(shape: Shape, color: Color, width: Dp = 1.dp): Modifier = drawWithContent {
  drawContent()
  val stroke = width.toPx()
  // Outline sized and offset so the whole stroke lands inside the element rather than straddling its edge.
  val outline = shape.createOutline(Size(size.width - stroke, size.height), layoutDirection, this)
  clipRect(bottom = size.height - stroke) {
    translate(left = stroke / 2f, top = stroke / 2f) {
      drawOutline(outline, color = color, style = Stroke(stroke))
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
  MainActionCarousel,
  ClaimStatusCards,
  VeryImportantMessages,
  MemberReminders,
  Quotes,
  DiscoverInsurances,
  Addons,
  QuickActionTiles,
}

// The single source of truth for the home section order; reorder here.
private val homeSectionOrder: List<HomeSection> = listOf(
  HomeSection.Welcome,
  HomeSection.MainActionCarousel,
  HomeSection.ClaimStatusCards,
  HomeSection.VeryImportantMessages,
  HomeSection.MemberReminders,
  HomeSection.Quotes,
  HomeSection.QuickActionTiles,
  HomeSection.DiscoverInsurances,
  HomeSection.Addons,
)

// Gap between two consecutive scrolling sections, carried as trailing room below a section's content so
// a card's drop-shadow renders inside the section's own bounds.
private val homeSectionGap = 40.dp

// How far the hero's content lifts, and how much it shrinks, over its fade. Shared by the greeting and
// the floating icons: they leave on one curve, so the two have to read the same numbers.
private val heroParallaxDistance = 24.dp
private const val HERO_SCALE_DOWN = 0.12f

@Composable
private fun WelcomeSection(firstName: String) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    WelcomeMessage(
      firstName = firstName,
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
  draftClaim: DraftClaim?,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  onContinueDraftClaim: () -> Unit,
  onDeleteDraftClaim: (String) -> Unit,
  horizontalInsets: PaddingValues,
) {
  val claimCards: NonEmptyList<ClaimCardUiState>? = buildList {
    draftClaim?.let { add(Draft(it.id, it.displayName, it.startedAt)) }
    claimStatusCardsData?.claimStatusCardsUiState?.forEach { add(ClaimCardUiState.Claim(it)) }
  }.toNonEmptyListOrNull()
  if (claimCards != null) {
    ClaimStatusCards(
      onClick = onClaimDetailCardClicked,
      onContinueDraftClaim = onContinueDraftClaim,
      onDeleteDraftClaim = onDeleteDraftClaim,
      claimCardsUiState = claimCards,
      contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets,
    )
  }
}

@Composable
private fun VeryImportantMessagesSection(
  homeText: HomeText,
  list: List<VeryImportantMessage>,
  informationalReminders: List<UpcomingRenewal>,
  openUrl: (String) -> Unit,
  markMessageAsSeen: (String) -> Unit,
  horizontalInsets: PaddingValues,
) {
  val statusCard = if (homeText == Active) null else Status(homeText)
  val cards = list.map { Important(it) } +
    listOfNotNull(statusCard) +
    informationalReminders.map { Renewal(it) }
  HomeNoticeCarousel(
    cards = cards,
    openUrl = openUrl,
    hideImportantMessage = markMessageAsSeen,
    contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets,
  )
}

@Composable
private fun MemberRemindersSection(
  applicableReminders: List<MemberReminder>,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipIdScreen: () -> Unit,
  navigateToUsageData: () -> Unit,
  horizontalInsets: PaddingValues,
) {
  val toDoReminders = applicableReminders.homeActionRequiredReminders()
  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    if (toDoReminders.isNotEmpty()) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .padding(horizontalInsets),
      ) {
        HedvigText(
          text = stringResource(string.HOME_TODO_SECTION_TITLE),
          style = HedvigTheme.typography.headlineSmall,
          modifier = Modifier.semantics { heading() },
        )
        MemberReminderToDoList(
          memberReminders = toDoReminders,
          navigateToConnectPayment = navigateToConnectPayment,
          navigateToConnectPayout = navigateToConnectPayout,
          navigateToAddMissingInfo = navigateToMissingInfo,
          onNavigateToNewConversation = onNavigateToNewConversation,
          navigateToContactInfo = navigateToContactInfo,
          navigateToChipId = navigateToChipIdScreen,
          navigateToUsageData = navigateToUsageData,
        )
      }
    }
  }
}

private val PillowSize = 48.dp

@Composable
private fun QuotesSection(
  sessions: List<OngoingShopSession>,
  onResumeClick: (String) -> Unit,
  onDismiss: (String) -> Unit,
  imageLoader: ImageLoader,
  horizontalInsets: PaddingValues,
) {
  val contentPadding = PaddingValues(horizontal = 16.dp) + horizontalInsets
  Column(Modifier.fillMaxWidth()) {
    HedvigText(
      text = stringResource(string.HOME_QUOTES_SECTION_TITLE),
      style = HedvigTheme.typography.headlineSmall,
      modifier = Modifier
        .padding(contentPadding)
        .semantics { heading() },
    )
    Spacer(Modifier.height(8.dp))
    CardCarousel(
      items = sessions,
      contentPadding = contentPadding,
      key = { it.id },
    ) { session, cardModifier ->
      QuoteCard(
        session = session,
        onResumeClick = onResumeClick,
        onDismiss = onDismiss,
        imageLoader = imageLoader,
        modifier = cardModifier,
      )
    }
  }
}

@Composable
private fun QuoteCard(
  session: OngoingShopSession,
  onResumeClick: (String) -> Unit,
  onDismiss: (String) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = { onResumeClick(session.resumeUrl) },
    color = HedvigTheme.colorScheme.fillNegative,
    borderColor = HedvigTheme.colorScheme.borderPrimary,
    modifier = modifier
      .fillMaxWidth()
      .hedvigDropShadow(HedvigTheme.shapes.cornerXLarge),
  ) {
    Box(Modifier.fillMaxWidth().padding(16.dp)) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (session.pillowImageUrl != null) {
            val pillowPx = with(LocalDensity.current) { PillowSize.roundToPx() }
            AsyncImage(
              model = storyblokResized(session.pillowImageUrl, pillowPx),
              contentDescription = null,
              imageLoader = imageLoader,
              contentScale = ContentScale.Fit,
              modifier = Modifier.size(PillowSize),
            )
            Spacer(Modifier.width(12.dp))
          }
          Column(Modifier.weight(1f)) {
            HedvigText(text = session.title, style = HedvigTheme.typography.bodySmall)
            val secondary = session.monthlyNet?.let {
              stringResource(
                string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                it,
              )
            } ?: session.subtitle

            if (secondary != null) {
              HedvigText(
                text = secondary,
                style = HedvigTheme.typography.label,
                color = HedvigTheme.colorScheme.textSecondary,
              )
            }
          }
          IconButton(
            onClick = { onDismiss(session.id) },
            modifier = Modifier
              .align(Alignment.Top)
              .size(24.dp)
              .wrapContentSize(unbounded = true),
          ) {
            Icon(
              imageVector = HedvigIcons.Close,
              contentDescription = stringResource(string.ongoing_shop_session_dismiss_offer),
            )
          }
        }
        Spacer(Modifier.height(12.dp))
        HedvigButton(
          text = stringResource(string.general_continue_button),
          onClick = { onResumeClick(session.resumeUrl) },
          buttonStyle = Secondary,
          buttonSize = ButtonSize.Medium,
          enabled = true,
          shape = HedvigTheme.shapes.cornerFull,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun QuickActionTilesSection(
  quickActions: List<QuickAction>,
  onQuickLink: (QuickLinkDestination) -> Unit,
  onEditInsurance: (MultiSelectExpandedLink) -> Unit,
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
      text = stringResource(string.HC_QUICK_ACTIONS_TITLE),
      style = HedvigTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Max),
    ) {
      quickActions.forEach { action ->
        HomeActionTile(
          icon = action.homeIcon(),
          text = stringResource(action.shortTitleRes),
          onClick = {
            when (action) {
              is StandaloneQuickLink -> onQuickLink(action.quickLinkDestination)
              is MultiSelectExpandedLink -> onEditInsurance(action)
            }
          },
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        )
      }
    }
  }
}

private fun QuickAction.homeIcon(): ImageVector = when (this) {
  is MultiSelectExpandedLink -> HedvigIcons.Settings

  is StandaloneQuickLink -> when (quickLinkDestination) {
    QuickLinkChangeAddress -> HedvigIcons.Reload
    QuickLinkTravelCertificate -> HedvigIcons.Travel
    is FirstVet -> HedvigIcons.HelipadOutline
    else -> HedvigIcons.Settings
  }
}

@Composable
private fun EditInsuranceQuickActionSheet(
  state: HedvigBottomSheetState<MultiSelectExpandedLink>,
  onQuickLink: (QuickLinkDestination) -> Unit,
) {
  HedvigBottomSheet(state) { editInsurance ->
    HedvigText(
      text = stringResource(editInsurance.titleRes),
      style = HedvigTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      editInsurance.links.forEach { link ->
        HedvigCard(
          onClick = {
            state.dismiss()
            onQuickLink(link.quickLinkDestination)
          },
          color = HedvigTheme.colorScheme.fillNegative,
          borderColor = HedvigTheme.colorScheme.borderPrimary,
          modifier = Modifier.fillMaxWidth()
            .hedvigDropShadow(HedvigTheme.shapes.cornerXLarge),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 14.dp, horizontal = 12.dp),
          ) {
            HedvigText(text = stringResource(link.titleRes))
            Spacer(Modifier.height(6.dp))
            HedvigText(
              text = stringResource(link.hintTextRes),
              style = HedvigTheme.typography.label,
              color = HedvigTheme.colorScheme.textSecondary,
            )
          }
        }
      }
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun HomeActionTile(icon: ImageVector, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  HedvigCard(
    onClick = onClick,
    color = HedvigTheme.colorScheme.fillNegative,
    borderColor = HedvigTheme.colorScheme.borderPrimary,
    modifier = modifier.hedvigDropShadow(HedvigTheme.shapes.cornerXLarge),
  ) {
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
private fun MainActionCarouselSection(
  isHelpCenterEnabled: Boolean,
  onMakeClaim: () -> Unit,
  onHelpAndSupport: () -> Unit,
  onContactUs: () -> Unit,
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
    HedvigButton(
      text = stringResource(string.home_tab_claim_button_text),
      onClick = onMakeClaim,
      enabled = true,
      buttonStyle = RoundedPrimary,
    )
    if (isHelpCenterEnabled) {
      HedvigButton(
        text = stringResource(string.home_tab_get_help),
        onClick = onHelpAndSupport,
        enabled = true,
        buttonStyle = RoundedLiquidGlass,
      )
    }
    HedvigButton(
      text = stringResource(string.DASHBOARD_OPEN_CHAT),
      onClick = onContactUs,
      enabled = true,
      buttonStyle = RoundedLiquidGlass,
    )
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
    HedvigText(
      text = stringResource(string.INSURANCE_ADDONS_SUBHEADING),
      style = HedvigTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    addonBannerInfos.forEach { addon ->
      PillowRow(
        title = addon.title,
        subtitle = addon.description,
        pillowImage = null,
        pillow = { AddonPillow(addon.flowType) },
        buttonText = stringResource(string.HOME_ADDONS_READ_MORE_BUTTON),
        onButtonClick = { navigateToAddonPurchaseFlow(addon.eligibleInsurancesIds) },
        imageLoader = imageLoader,
        modifier = Modifier.fillMaxWidth(),
        buttonSize = ButtonSize.Small,
        buttonShape = HedvigTheme.shapes.cornerFull,
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
    title = stringResource(string.HOME_DISCOVER_SECTION_TITLE),
    crossSells = crossSells,
    onCrossSellClick = onCrossSellClick,
    modifier = Modifier.padding(horizontal = 16.dp),
    onSheetDismissed = {},
    imageLoader = imageLoader,
    buttonText = stringResource(string.HOME_DISCOVER_SEE_PRICE_BUTTON),
    buttonSize = ButtonSize.Small,
    buttonShape = HedvigTheme.shapes.cornerFull,
  )
}

@Composable
private fun WelcomeMessage(firstName: String, modifier: Modifier = Modifier) {
  // todo custom style since new DS does not have this specification
  //  https://hedviginsurance.slack.com/archives/C03U9C6Q7TP/p1727365167917719
  val titleStyle = HedvigTheme.typography.headlineMedium.copy(
    fontSize = 24.0.sp,
    lineBreak = LineBreak.Heading,
    textAlign = TextAlign.Center,
  )
  if (firstName.isBlank()) {
    HedvigText(
      text = stringResource(string.home_tab_welcome_title_without_name),
      style = titleStyle,
      modifier = modifier.fillMaxWidth(),
    )
    return
  }
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    HedvigText(
      text = stringResource(string.HOME_GREETING_TITLE, firstName),
      style = titleStyle,
      modifier = Modifier.fillMaxWidth(),
    )
    HedvigText(
      text = stringResource(string.HOME_GREETING_SUBTITLE),
      color = HedvigTheme.colorScheme.textSecondary,
      style = titleStyle,
      modifier = Modifier.fillMaxWidth(),
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
          firstName = "Richard",
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
          quickActions = previewQuickActions,
          hasUnseenChatMessages = hasUnseenChatMessages,
          discoverCrossSells = emptyList(),
          ongoingShopSessions = listOf(
            OngoingShopSession(
              id = "preview-1",
              title = "Home + Accident",
              subtitle = "Studio apartment, Stockholm",
              monthlyNet = null,
              resumeUrl = "",
              pillowImageUrl = null,
            ),
            OngoingShopSession(
              id = "preview-2",
              title = "Car",
              subtitle = "ABC 123",
              monthlyNet = null,
              resumeUrl = "",
              pillowImageUrl = null,
            ),
          ),
          crossSellsAction = CrossSellsAction(
            CrossSellSheetData(
              recommendedCrossSell = RecommendedCrossSell(
                crossSell = CrossSell(
                  "rh",
                  "Car Insurance",
                  "For you and your car",
                  "",
                  ImageAsset("", "", ""),
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
          draftClaim = null,
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
        navigateToQuickLink = {},
        openUrl = {},
        openCrossSellUrl = {},
        openAppSettings = {},
        navigateToMissingInfo = { _, _ -> },
        markMessageAsSeen = {},
        deleteDraftClaim = {},
        dismissOngoingShopSession = {},
        navigateToFirstVet = {},
        markCrossSellsNotificationAsSeen = {},
        navigateToContactInfo = {},
        navigateToChipIdScreen = {},
        navigateToUsageData = {},
        setEpochDayWhenLastToolTipShown = {},
        imageLoader = rememberPreviewImageLoader(),
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
        uiState = Error(null),
        notificationPermissionState = rememberPreviewNotificationPermissionState(),
        reload = {},
        onNavigateToInbox = {},
        onNavigateToNewConversation = {},
        navigateToClaimChat = {},
        onClaimDetailCardClicked = {},
        navigateToConnectPayment = {},
        navigateToConnectPayout = {},
        navigateToHelpCenter = {},
        navigateToQuickLink = {},
        openUrl = {},
        openCrossSellUrl = {},
        openAppSettings = {},
        navigateToMissingInfo = { _, _ -> },
        markMessageAsSeen = {},
        deleteDraftClaim = {},
        dismissOngoingShopSession = {},
        navigateToFirstVet = {},
        markCrossSellsNotificationAsSeen = {},
        navigateToContactInfo = {},
        navigateToChipIdScreen = {},
        navigateToUsageData = {},
        setEpochDayWhenLastToolTipShown = {},
        imageLoader = rememberPreviewImageLoader(),
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
          ongoingShopSessions = listOf(
            OngoingShopSession(
              id = "session-id",
              title = "Title",
              subtitle = null,
              monthlyNet = null,
              resumeUrl = "",
              pillowImageUrl = null,
            ),
          ),
          isHelpCenterEnabled = false,
          quickActions = previewQuickActions,
          hasUnseenChatMessages = false,
          crossSellsAction = null,
          firstVetAction = null,
          chatAction = ChatAction,
          addonBannerInfos = emptyList(),
          isProduction = true,
          draftClaim = null,
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
        navigateToQuickLink = {},
        openUrl = {},
        openCrossSellUrl = {},
        openAppSettings = {},
        navigateToMissingInfo = { _, _ -> },
        markMessageAsSeen = {},
        deleteDraftClaim = {},
        dismissOngoingShopSession = {},
        navigateToFirstVet = {},
        markCrossSellsNotificationAsSeen = {},
        navigateToContactInfo = {},
        navigateToChipIdScreen = {},
        navigateToUsageData = {},
        setEpochDayWhenLastToolTipShown = {},
        imageLoader = rememberPreviewImageLoader(),
        navigateToAddonPurchaseFlow = {},
      )
    }
  }
}

private val previewQuickActions: List<QuickAction> = listOf(
  MultiSelectExpandedLink(
    titleRes = string.HC_QUICK_ACTIONS_EDIT_INSURANCE_TITLE,
    hintTextRes = string.HC_QUICK_ACTIONS_EDIT_INSURANCE_SUBTITLE,
    links = listOf(
      StandaloneQuickLink(
        titleRes = string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_TITLE,
        hintTextRes = string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE,
        quickLinkDestination = QuickLinkChangeTier,
      ),
    ),
  ),
  StandaloneQuickLink(
    titleRes = string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE,
    hintTextRes = string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE,
    quickLinkDestination = QuickLinkChangeAddress,
  ),
)

private class HomeTextPreviewParameterProvider : CollectionPreviewParameterProvider<HomeText>(
  listOf(
    Active,
    ActiveInFuture(LocalDate.parse("2025-01-01")),
    Pending,
    Switching,
    Terminated,
  ),
)
