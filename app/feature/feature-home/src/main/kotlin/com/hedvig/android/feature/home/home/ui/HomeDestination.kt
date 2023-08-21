package com.hedvig.android.feature.home.home.ui

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.nonEmptyListOf
import coil.ImageLoader
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.core.common.android.SHARED_PREFERENCE_NAME
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ArrowForward
import com.hedvig.android.core.ui.appbar.m3.ToolbarChatIcon
import com.hedvig.android.core.ui.appbar.m3.TopAppBarLayoutForActions
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.feature.home.claimdetail.ui.previewList
import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimsData
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyActivity
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyData
import com.hedvig.android.feature.home.claimstatus.ClaimStatusCards
import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressUiState
import com.hedvig.android.feature.home.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.android.feature.home.claimstatus.data.PillUiState
import com.hedvig.android.feature.home.data.HomeData
import com.hedvig.android.feature.home.home.ChatTooltip
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.memberreminders.ui.MemberReminderCards
import com.hedvig.android.notification.permission.NotificationPermissionDialog
import com.hedvig.android.notification.permission.NotificationPermissionState
import com.hedvig.android.notification.permission.rememberNotificationPermissionState
import com.hedvig.android.notification.permission.rememberPreviewNotificationPermissionState
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun HomeDestination(
  viewModel: HomeViewModel,
  onStartChat: () -> Unit,
  onClaimDetailCardClicked: (String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  onStartClaim: () -> Unit,
  onStartMovingFlow: () -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onOpenCommonClaim: (CommonClaimsData) -> Unit,
  openUrl: (String) -> Unit,
  tryOpenUri: (Uri) -> Unit,
  openAppSettings: () -> Unit,
  imageLoader: ImageLoader,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val notificationPermissionState = rememberNotificationPermissionState()
  HomeScreen(
    uiState = uiState,
    notificationPermissionState = notificationPermissionState,
    reload = { viewModel.emit(HomeEvent.RefreshData) },
    snoozeNotificationPermissionReminder = { viewModel.emit(HomeEvent.SnoozeNotificationPermissionReminder) },
    onStartChat = onStartChat,
    onClaimDetailCardClicked = onClaimDetailCardClicked,
    navigateToConnectPayment = navigateToConnectPayment,
    onStartClaim = onStartClaim,
    onStartMovingFlow = onStartMovingFlow,
    onGenerateTravelCertificateClicked = onGenerateTravelCertificateClicked,
    onOpenCommonClaim = onOpenCommonClaim,
    openUrl = openUrl,
    tryOpenUri = tryOpenUri,
    openAppSettings = openAppSettings,
    imageLoader = imageLoader,
  )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HomeScreen(
  uiState: HomeUiState,
  notificationPermissionState: NotificationPermissionState,
  reload: () -> Unit,
  snoozeNotificationPermissionReminder: () -> Unit,
  onStartChat: () -> Unit,
  onClaimDetailCardClicked: (String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  onStartClaim: () -> Unit,
  onStartMovingFlow: () -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onOpenCommonClaim: (CommonClaimsData) -> Unit,
  openUrl: (String) -> Unit,
  tryOpenUri: (Uri) -> Unit,
  openAppSettings: () -> Unit,
  imageLoader: ImageLoader,
) {
  val context = LocalContext.current
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = uiState.isReloading,
    onRefresh = reload,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )

  val coroutineScope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(true)
  var showEditYourInfoBottomSheet by rememberSaveable { mutableStateOf(false) }

  if (showEditYourInfoBottomSheet) {
    OtherServicesBottomSheet(
      options = listOf(
        CommonClaim.Chat,// TODO Provide this from the uiState
        CommonClaim.ChangeAddress,
        CommonClaim.GenerateTravelCertificate,
        CommonClaim.Emergency(
          EmergencyData(
            iconUrls = ThemedIconUrls("", ""),
            color = HedvigColor.DarkGray,
            title = "Test",
            eligibleToClaim = true,
            emergencyNumber = "123",
          ),
        ),
      ),
      onChatClicked = {
        coroutineScope.launch {
          sheetState.hide()
          showEditYourInfoBottomSheet = false
          onStartChat()
        }
      },
      onStartMovingFlow = {
        coroutineScope.launch {
          sheetState.hide()
          showEditYourInfoBottomSheet = false
          onStartMovingFlow()
        }
      },
      onEmergencyClaimClicked = {
        val emergencyActivity = EmergencyActivity.newInstance(context = context, data = it)
        coroutineScope.launch {
          sheetState.hide()
          showEditYourInfoBottomSheet = false
          context.startActivity(emergencyActivity)
        }
      },
      onGenerateTravelCertificateClicked = {
        coroutineScope.launch {
          sheetState.hide()
          showEditYourInfoBottomSheet = false
          onGenerateTravelCertificateClicked()
        }
      },
      onCommonClaimClicked = {
        coroutineScope.launch {
          sheetState.hide()
          showEditYourInfoBottomSheet = false
          onOpenCommonClaim(it)
        }
      },
      onDismiss = {
        coroutineScope.launch {
          sheetState.hide()
          showEditYourInfoBottomSheet = false
        }
      },
      sheetState = sheetState,
    )
  }

  Box(Modifier.fillMaxSize()) {
    AnimatedContent(
      targetState = uiState,
      modifier = Modifier.fillMaxSize(),
      label = "home ui state",
    ) { uiState ->
      Column(
        Modifier
          .fillMaxSize()
          .pullRefresh(pullRefreshState)
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        Spacer(Modifier.height(64.dp)) // Room for TopAppBarLayoutForActions
        when (uiState) {
          HomeUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced(Modifier.weight(1f))
          is HomeUiState.Error -> HedvigErrorSection(retry = reload, modifier = Modifier.weight(1f))
          is HomeUiState.Success -> {
            HomeScreenSuccess(
              uiState = uiState,
              notificationPermissionState = notificationPermissionState,
              imageLoader = imageLoader,
              snoozeNotificationPermissionReminder = snoozeNotificationPermissionReminder,
              onStartMovingFlow = onStartMovingFlow,
              onClaimDetailCardClicked = onClaimDetailCardClicked,
              navigateToConnectPayment = navigateToConnectPayment,
              onEmergencyClaimClicked = { emergencyData ->
                context.startActivity(
                  EmergencyActivity.newInstance(
                    context = context,
                    data = emergencyData,
                  ),
                )
              },
              onGenerateTravelCertificateClicked = onGenerateTravelCertificateClicked,
              onCommonClaimClicked = { commonClaimsData ->
                onOpenCommonClaim(commonClaimsData)
              },
              onStartClaimClicked = onStartClaim,
              onUpcomingRenewalClick = tryOpenUri,
              openAppSettings = openAppSettings,
              openUrl = openUrl,
            )
          }
        }
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
    }
    Column {
      TopAppBarLayoutForActions {
        ToolbarChatIcon(
          onClick = onStartChat,
        )
      }
      val shouldShowTooltip by produceState(false) {
        val daysSinceLastTooltipShown = daysSinceLastTooltipShown(context)
        value = daysSinceLastTooltipShown
      }
      ChatTooltip(
        showTooltip = shouldShowTooltip,
        tooltipShown = {
          context.setLastEpochDayWhenChatTooltipWasShown(java.time.LocalDate.now().toEpochDay())
        },
        modifier = Modifier
          .align(Alignment.End)
          .padding(horizontal = 16.dp),
      )
    }
    PullRefreshIndicator(
      refreshing = uiState.isReloading,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

private suspend fun daysSinceLastTooltipShown(context: Context): Boolean {
  val currentEpochDay = java.time.LocalDate.now().toEpochDay()
  val lastEpochDayOpened = withContext(Dispatchers.IO) {
    context.getLastEpochDayWhenChatTooltipWasShown()
  }
  val diff = currentEpochDay - lastEpochDayOpened
  val daysSinceLastTooltipShown = diff >= 30
  return daysSinceLastTooltipShown
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.HomeScreenSuccess(
  uiState: HomeUiState.Success,
  notificationPermissionState: NotificationPermissionState,
  imageLoader: ImageLoader,
  snoozeNotificationPermissionReminder: () -> Unit,
  onStartMovingFlow: () -> Unit,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  onEmergencyClaimClicked: (EmergencyData) -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onCommonClaimClicked: (CommonClaimsData) -> Unit,
  onStartClaimClicked: () -> Unit,
  onUpcomingRenewalClick: (Uri) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
) {
  for ((index, veryImportantMessage) in uiState.veryImportantMessages.withIndex()) {
    VeryImportantMessageBanner(openUrl, veryImportantMessage)
    if (index == uiState.veryImportantMessages.lastIndex) {
      Spacer(Modifier.height(16.dp))
    }
  }
  WelcomeMessage(
    homeText = uiState.homeText,
    modifier = Modifier.padding(horizontal = 24.dp),
  )
  Spacer(Modifier.height(24.dp))
  if (uiState.claimStatusCardsData != null) {
    ClaimStatusCards(
      goToDetailScreen = onClaimDetailCardClicked,
      claimStatusCardsData = uiState.claimStatusCardsData,
      contentPadding = PaddingValues(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
  val memberReminders = uiState.memberReminders.onlyApplicableReminders(notificationPermissionState.status.isGranted)
  NotificationPermissionDialog(notificationPermissionState, openAppSettings)
  MemberReminderCards(
    memberReminders = memberReminders,
    navigateToConnectPayment = navigateToConnectPayment,
    openUrl = openUrl,
    notificationPermissionState = notificationPermissionState,
    snoozeNotificationPermissionReminder = snoozeNotificationPermissionReminder,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  if (memberReminders.hasAnyReminders) {
    Spacer(Modifier.height(16.dp))
  }
  Spacer(Modifier.weight(1f))
  HedvigContainedButton(
    text = stringResource(R.string.home_tab_claim_button_text),
    onClick = onStartClaimClicked,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(8.dp))
  HedvigTextButton(
    text = stringResource(R.string.home_tab_other_services),
    onClick = {}, // todo open bottom sheet with other services here
    modifier = Modifier.padding(horizontal = 16.dp),
  )
}

@Composable
private fun VeryImportantMessageBanner(
  openUrl: (String) -> Unit,
  veryImportantMessage: HomeData.VeryImportantMessage,
) {
  Surface(
    onClick = { openUrl(veryImportantMessage.link) },
    color = MaterialTheme.colorScheme.warningContainer,
    contentColor = MaterialTheme.colorScheme.onWarningContainer,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .minimumInteractiveComponentSize()
        .fillMaxWidth()
        .padding(horizontal = 32.dp, vertical = 8.dp),
    ) {
      Text(
        text = veryImportantMessage.message,
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(16.dp))
      Icon(
        imageVector = Icons.Hedvig.ArrowForward,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

@Composable
private fun WelcomeMessage(
  homeText: HomeText,
  modifier: Modifier = Modifier,
) {
  val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG) }
  val firstName = homeText.name
  val headlineText = when (homeText) {
    is HomeText.Active -> if (firstName != null) {
      stringResource(R.string.home_tab_welcome_title, firstName)
    } else {
      stringResource(R.string.home_tab_welcome_title_without_name)
    }
    is HomeText.ActiveInFuture -> if (firstName != null) {
      stringResource(
        R.string.home_tab_active_in_future_welcome_title,
        firstName,
        formatter.format(homeText.inception.toJavaLocalDate()),
      )
    } else {
      stringResource(
        R.string.home_tab_active_in_future_welcome_title_without_name,
        formatter.format(homeText.inception.toJavaLocalDate()),
      )
    }
    is HomeText.Pending -> if (firstName != null) {
      stringResource(R.string.home_tab_pending_unknown_title, firstName)
    } else {
      stringResource(R.string.home_tab_pending_unknown_title_without_name)
    }
    is HomeText.Switching -> if (firstName != null) {
      stringResource(R.string.home_tab_pending_switchable_welcome_title, firstName)
    } else {
      stringResource(R.string.home_tab_pending_switchable_welcome_title_without_name)
    }
    is HomeText.Terminated -> if (firstName != null) {
      stringResource(R.string.home_tab_terminated_welcome_title, firstName)
    } else {
      stringResource(R.string.home_tab_terminated_welcome_title_without_name)
    }
  }
  Text(
    text = headlineText,
    style = MaterialTheme.typography.headlineMedium,
    modifier = modifier.fillMaxWidth(),
  )
}

private const val SHARED_PREFERENCE_LAST_OPEN = "shared_preference_last_open"

private fun Context.setLastEpochDayWhenChatTooltipWasShown(epochDay: Long) =
  getSharedPreferences().edit().putLong(SHARED_PREFERENCE_LAST_OPEN, epochDay).commit()

private fun Context.getLastEpochDayWhenChatTooltipWasShown() =
  getSharedPreferences().getLong(SHARED_PREFERENCE_LAST_OPEN, 0)

private fun Context.getSharedPreferences() =
  this.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

@HedvigPreview
@Composable
private fun PreviewHomeScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HomeScreen(
        uiState = HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active("John"),
          claimStatusCardsData = HomeData.ClaimStatusCardsData(
            nonEmptyListOf(
              ClaimStatusCardUiState(
                id = "id",
                pillsUiState = PillUiState.previewList(),
                title = "Insurance Case",
                subtitle = "Home Insurance renter",
                claimProgressItemsUiState = ClaimProgressUiState.previewList(),
              ),
            ),
          ),
          veryImportantMessages = persistentListOf(HomeData.VeryImportantMessage("Beware of the earthquake", "")),
          memberReminders = MemberReminders(
            connectPayment = MemberReminder.ConnectPayment,
          ),
          allowAddressChange = true,
          allowGeneratingTravelCertificate = true,
          emergencyData = null,
        ),
        notificationPermissionState = rememberPreviewNotificationPermissionState(),
        reload = {},
        snoozeNotificationPermissionReminder = {},
        onStartChat = {},
        onClaimDetailCardClicked = {},
        navigateToConnectPayment = {},
        onStartClaim = {},
        onStartMovingFlow = {},
        onGenerateTravelCertificateClicked = {},
        onOpenCommonClaim = {},
        openUrl = {},
        tryOpenUri = {},
        openAppSettings = {},
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}

fun todo() {
  showOtherServices = {
    showEditYourInfoBottomSheet = true
  },

  HedvigTextButton(
    text = stringResource(id = R.string.home_tab_other_services),
    onClick = { showOtherServices() },
    modifier = Modifier.padding(horizontal = 16.dp)
  )
}
