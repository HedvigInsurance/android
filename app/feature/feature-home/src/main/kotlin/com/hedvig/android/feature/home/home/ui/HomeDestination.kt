package com.hedvig.android.feature.home.home.ui

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.nonEmptyListOf
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.core.common.android.SHARED_PREFERENCE_NAME
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.containedButtonContainer
import com.hedvig.android.core.designsystem.material3.onContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.compose.notificationCircle
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.ui.appbar.m3.ToolbarChatIcon
import com.hedvig.android.core.ui.appbar.m3.TopAppBarLayoutForActions
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.feature.home.home.ChatTooltip
import com.hedvig.android.feature.home.home.data.HomeData
import com.hedvig.android.memberreminders.MemberReminder
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
import com.hedvig.android.ui.claimstatus.model.ClaimPillType
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import hedvig.resources.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun HomeDestination(
  viewModel: HomeViewModel,
  onStartChat: () -> Unit,
  onClaimDetailCardClicked: (String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  onStartClaim: () -> Unit,
  navigateToHelpCenter: () -> Unit,
  openUrl: (String) -> Unit,
  openAppSettings: () -> Unit,
  navigateToMissingInfo: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val notificationPermissionState = rememberNotificationPermissionState()
  HomeScreen(
    uiState = uiState,
    notificationPermissionState = notificationPermissionState,
    reload = { viewModel.emit(HomeEvent.RefreshData) },
    onStartChat = onStartChat,
    onClaimDetailCardClicked = onClaimDetailCardClicked,
    navigateToConnectPayment = navigateToConnectPayment,
    onStartClaim = onStartClaim,
    navigateToHelpCenter = navigateToHelpCenter,
    openUrl = openUrl,
    openAppSettings = openAppSettings,
    navigateToMissingInfo = navigateToMissingInfo,
  )
}

@Composable
private fun HomeScreen(
  uiState: HomeUiState,
  notificationPermissionState: NotificationPermissionState,
  reload: () -> Unit,
  onStartChat: () -> Unit,
  onClaimDetailCardClicked: (String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  onStartClaim: () -> Unit,
  navigateToHelpCenter: () -> Unit,
  openUrl: (String) -> Unit,
  openAppSettings: () -> Unit,
  navigateToMissingInfo: (String) -> Unit,
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
          )
        }
      }
    }
    if (uiState.showChatIcon) {
      Column {
        TopAppBarLayoutForActions {
          ToolbarChatIcon(
            onClick = onStartChat,
            modifier = Modifier.notificationCircle(uiState.hasUnseenChatMessages),
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

@ExperimentalMaterial3Api
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
  navigateToMissingInfo: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var fullScreenSize: IntSize? by remember { mutableStateOf(null) }
  Box(
    modifier = modifier
      .fillMaxSize()
      .onSizeChanged { fullScreenSize = it }
      .pullRefresh(pullRefreshState)
      .verticalScroll(rememberScrollState()),
  ) {
    NotificationPermissionDialog(notificationPermissionState, openAppSettings)
    val fullScreenSizeValue = fullScreenSize
    if (fullScreenSizeValue != null) {
      HomeLayout(
        fullScreenSize = fullScreenSizeValue,
        welcomeMessage = {
          WelcomeMessage(
            homeText = uiState.homeText,
            modifier = Modifier
              .padding(horizontal = 24.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
              .testTag("welcome_message"),
          )
        },
        claimStatusCards = {
          if (uiState.claimStatusCardsData != null) {
            var consumedWindowInsets by remember { mutableStateOf(WindowInsets(0.dp)) }
            ClaimStatusCards(
              onClick = onClaimDetailCardClicked,
              claimStatusCardsUiState = uiState.claimStatusCardsData.claimStatusCardsUiState,
              contentPadding = PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing
                .exclude(consumedWindowInsets)
                .only(WindowInsetsSides.Horizontal)
                .asPaddingValues(),
              modifier = Modifier.onConsumedWindowInsetsChanged { consumedWindowInsets = it },
            )
          }
        },
        veryImportantMessages = {
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
          ) {
            for (veryImportantMessage in uiState.veryImportantMessages) {
              VeryImportantMessageCard(openUrl, veryImportantMessage)
            }
          }
        },
        memberReminderCards = {
          val memberReminders =
            uiState.memberReminders.onlyApplicableReminders(notificationPermissionState.status.isGranted)
          var consumedWindowInsets by remember { mutableStateOf(WindowInsets(0.dp)) }

          MemberReminderCardsWithoutNotification(
            memberReminders = memberReminders,
            navigateToConnectPayment = navigateToConnectPayment,
            navigateToAddMissingInfo = navigateToMissingInfo,
            openUrl = openUrl,
            contentPadding = PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing
              .exclude(consumedWindowInsets)
              .only(WindowInsetsSides.Horizontal)
              .asPaddingValues(),
            modifier = Modifier.onConsumedWindowInsetsChanged { consumedWindowInsets = it },
          )
        },
        startClaimButton = {
          HedvigContainedButton(
            text = stringResource(R.string.home_tab_claim_button_text),
            onClick = onStartClaimClicked,
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
          )
        },
        helpCenterButton = {
          if (uiState.isHelpCenterEnabled) {
            HedvigSecondaryContainedButton(
              text = stringResource(R.string.home_tab_get_help),
              onClick = navigateToHelpCenter,
              modifier = Modifier
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
private fun VeryImportantMessageCard(
  openUrl: (String) -> Unit,
  veryImportantMessage: HomeData.VeryImportantMessage,
  modifier: Modifier = Modifier,
) {
  key(veryImportantMessage.id) {
    VectorInfoCard(
      text = veryImportantMessage.message,
      icon = Icons.Hedvig.WarningFilled,
      iconColor = MaterialTheme.colorScheme.warningElement,
      colors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.warningContainer,
        contentColor = MaterialTheme.colorScheme.onWarningContainer,
      ),
      modifier = modifier,
    ) {
      HedvigContainedSmallButton(
        text = stringResource(R.string.important_message_read_more),
        onClick = { openUrl(veryImportantMessage.link) },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.containedButtonContainer,
          contentColor = MaterialTheme.colorScheme.onContainedButtonContainer,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun WelcomeMessage(homeText: HomeText, modifier: Modifier = Modifier) {
  val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG) }
  val headlineText = when (homeText) {
    is HomeText.Active -> stringResource(R.string.home_tab_welcome_title_without_name)
    is HomeText.ActiveInFuture -> {
      stringResource(
        R.string.home_tab_active_in_future_welcome_title_without_name,
        formatter.format(homeText.inception.toJavaLocalDate()),
      )
    }

    is HomeText.Pending -> stringResource(R.string.home_tab_pending_unknown_title_without_name)
    is HomeText.Switching -> stringResource(R.string.home_tab_pending_switchable_welcome_title_without_name)
    is HomeText.Terminated -> stringResource(R.string.home_tab_terminated_welcome_title_without_name)
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

private fun Context.getSharedPreferences() = this.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

@HedvigPreview
@Composable
private fun PreviewHomeScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasUnseenChatMessages: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HomeScreen(
        uiState = HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = HomeData.ClaimStatusCardsData(
            nonEmptyListOf(
              ClaimStatusCardUiState(
                id = "id",
                pillTypes = listOf(ClaimPillType.Open, ClaimPillType.Closed.NotCompensated),
                claimProgressItemsUiState = listOf(
                  ClaimProgressSegment(ClaimProgressSegment.SegmentText.Closed, ClaimProgressSegment.SegmentType.PAID),
                ),
                claimType = "Broken item",
                insuranceDisplayName = "Home Insurance Homeowner",
              ),
            ),
          ),
          veryImportantMessages = persistentListOf(HomeData.VeryImportantMessage("id", "Beware of the earthquake", "")),
          memberReminders = MemberReminders(
            connectPayment = MemberReminder.ConnectPayment(),
          ),
          isHelpCenterEnabled = true,
          showChatIcon = true,
          hasUnseenChatMessages = hasUnseenChatMessages,
        ),
        notificationPermissionState = rememberPreviewNotificationPermissionState(),
        reload = {},
        onStartChat = {},
        onClaimDetailCardClicked = {},
        navigateToConnectPayment = {},
        onStartClaim = {},
        navigateToHelpCenter = {},
        openUrl = {},
        openAppSettings = {},
        navigateToMissingInfo = {},
      )
    }
  }
}
