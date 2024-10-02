package com.hedvig.android.feature.profile.tab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.compose.ui.preview.PreviewContentWithProvidedParametersAnimatedOnClick
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigRedTextButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Eurobonus
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.ID
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.icon.InfoOutline
import com.hedvig.android.design.system.hedvig.icon.MultipleDocuments
import com.hedvig.android.design.system.hedvig.icon.Settings
import com.hedvig.android.design.system.hedvig.plus
import com.hedvig.android.memberreminders.ui.MemberReminderCards
import com.hedvig.android.notification.permission.NotificationPermissionDialog
import com.hedvig.android.notification.permission.rememberNotificationPermissionState
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.placeholder
import com.hedvig.android.placeholder.shimmer
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import hedvig.resources.R

@Composable
internal fun ProfileDestination(
  navigateToEurobonus: () -> Unit,
  navigateToMyInfo: () -> Unit,
  navigateToAboutApp: () -> Unit,
  navigateToSettings: () -> Unit,
  navigateToTravelCertificate: () -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToAddMissingInfo: (contractId: String) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  viewModel: ProfileViewModel,
  testChangeTierFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ProfileScreen(
    uiState = uiState,
    reload = { viewModel.emit(ProfileUiEvent.Reload) },
    navigateToEurobonus = navigateToEurobonus,
    navigateToMyInfo = navigateToMyInfo,
    navigateToAboutApp = navigateToAboutApp,
    navigateToSettings = navigateToSettings,
    navigateToTravelCertificate = navigateToTravelCertificate,
    navigateToConnectPayment = navigateToConnectPayment,
    navigateToAddMissingInfo = navigateToAddMissingInfo,
    openAppSettings = openAppSettings,
    openUrl = openUrl,
    snoozeNotificationPermission = { viewModel.emit(ProfileUiEvent.SnoozeNotificationPermission) },
    onLogout = { viewModel.emit(ProfileUiEvent.Logout) },
    onNavigateToNewConversation = onNavigateToNewConversation,
    testChangeTierFlow = testChangeTierFlow,
  )
}

@Composable
private fun ProfileScreen(
  uiState: ProfileUiState,
  reload: () -> Unit,
  navigateToEurobonus: () -> Unit,
  navigateToMyInfo: () -> Unit,
  navigateToAboutApp: () -> Unit,
  navigateToSettings: () -> Unit,
  navigateToTravelCertificate: () -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToAddMissingInfo: (contractId: String) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  snoozeNotificationPermission: () -> Unit,
  onLogout: () -> Unit,
  testChangeTierFlow: () -> Unit,
) {
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = uiState is ProfileUiState.Loading,
    onRefresh = reload,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )
  var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
  if (showLogoutDialog) {
    HedvigAlertDialog(
      title = stringResource(id = R.string.PROFILE_LOGOUT_DIALOG_MESSAGE),
      onDismissRequest = { showLogoutDialog = false },
      onConfirmClick = onLogout,
      text = null,
      confirmButtonLabel = stringResource(R.string.GENERAL_YES),
      dismissButtonLabel = stringResource(id = R.string.general_cancel_button),
    )
  }

  Box(Modifier.fillMaxSize()) {
    Column(
      Modifier
        .matchParentSize()
        .pullRefresh(pullRefreshState)
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .height(64.dp)
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      ) {
        HedvigText(
          text = stringResource(id = R.string.PROFILE_TITLE),
          style = HedvigTheme.typography.headlineSmall,
        )
      }
      ProfileRows(
        profileUiState = uiState,
        showMyInfo = navigateToMyInfo,
        showSettings = navigateToSettings,
        showAboutApp = navigateToAboutApp,
        navigateToEurobonus = navigateToEurobonus,
        navigateToTravelCertificate = navigateToTravelCertificate,
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.weight(1f))
      val notificationPermissionState = rememberNotificationPermissionState()

      NotificationPermissionDialog(notificationPermissionState, openAppSettings)
      var consumedWindowInsets by remember { mutableStateOf(WindowInsets(0.dp)) }
      if (uiState is ProfileUiState.Success) {
        val memberReminders =
          uiState.memberReminders.onlyApplicableReminders(notificationPermissionState.status.isGranted)
        val padding = PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing
          .exclude(consumedWindowInsets)
          .only(WindowInsetsSides.Horizontal)
          .asPaddingValues()
        MemberReminderCards(
          memberReminders = memberReminders,
          navigateToConnectPayment = navigateToConnectPayment,
          navigateToAddMissingInfo = navigateToAddMissingInfo,
          openUrl = openUrl,
          notificationPermissionState = notificationPermissionState,
          snoozeNotificationPermissionReminder = snoozeNotificationPermission,
          contentPadding = padding,
          modifier = Modifier.onConsumedWindowInsetsChanged { consumedWindowInsets = it },
          onNavigateToNewConversation = onNavigateToNewConversation,
        )
        if (memberReminders.isNotEmpty()) {
          Spacer(Modifier.height(16.dp))
        }
      }

      // todo: remove mock!!!!
      HedvigButton(
        text = "Test change tier flow",
        enabled = true,
        onClick = testChangeTierFlow,
      )

      HedvigRedTextButton(
        text = stringResource(R.string.LOGOUT_BUTTON),
        onClick = {
          showLogoutDialog = true
        },
        modifier = Modifier
          .padding(horizontal = 16.dp).fillMaxWidth()
          .testTag("logout"),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    PullRefreshIndicator(
      refreshing = uiState is ProfileUiState.Loading,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

@Composable
private fun ProfileRows(
  profileUiState: ProfileUiState,
  showMyInfo: () -> Unit,
  showSettings: () -> Unit,
  showAboutApp: () -> Unit,
  navigateToEurobonus: () -> Unit,
  navigateToTravelCertificate: () -> Unit,
) {
  AnimatedContent(
    targetState = profileUiState,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
  ) { state ->
    Column(Modifier.fillMaxSize()) {
      when (state) {
        is ProfileUiState.Loading -> {
          ProfileItemRowsPlaceholders()
        }

        is ProfileUiState.Success -> {
          ProfileItemRows(
            profileUiState = state,
            showMyInfo = showMyInfo,
            showSettings = showSettings,
            showAboutApp = showAboutApp,
            navigateToEurobonus = navigateToEurobonus,
            navigateToTravelCertificate = navigateToTravelCertificate,
          )
        }
      }
    }
  }
}

@Composable
private fun ColumnScope.ProfileItemRowsPlaceholders() {
  ProfileRow(
    title = stringResource(R.string.PROFILE_MY_INFO_ROW_TITLE),
    icon = HedvigIcons.InfoFilled,
    isLoading = true,
    onClick = {},
  )
  ProfileRow(
    title = stringResource(R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
    icon = HedvigIcons.InfoFilled,
    isLoading = true,
    onClick = {},
  )
  ProfileRow(
    title = stringResource(R.string.PROFILE_ABOUT_ROW),
    icon = HedvigIcons.InfoFilled,
    isLoading = true,
    onClick = {},
  )
  ProfileRow(
    title = stringResource(R.string.profile_appSettingsSection_row_headline),
    icon = HedvigIcons.InfoFilled,
    isLoading = true,
    onClick = {},
  )
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.ProfileItemRows(
  profileUiState: ProfileUiState.Success,
  showMyInfo: () -> Unit,
  showSettings: () -> Unit,
  showAboutApp: () -> Unit,
  navigateToEurobonus: () -> Unit,
  navigateToTravelCertificate: () -> Unit,
) {
  ProfileRow(
    title = stringResource(R.string.PROFILE_MY_INFO_ROW_TITLE),
    icon = HedvigIcons.ID,
    onClick = showMyInfo,
    isLoading = false,
  )
  if (profileUiState.travelCertificateAvailable) {
    ProfileRow(
      title = stringResource(R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
      icon = HedvigIcons.MultipleDocuments,
      onClick = navigateToTravelCertificate,
      isLoading = false,
    )
  }
  if (profileUiState.euroBonus != null) {
    ProfileRow(
      title = stringResource(R.string.sas_integration_title),
      icon = HedvigIcons.Eurobonus,
      onClick = navigateToEurobonus,
      isLoading = false,
    )
  }
  ProfileRow(
    title = stringResource(R.string.PROFILE_ABOUT_ROW),
    icon = HedvigIcons.InfoOutline,
    onClick = showAboutApp,
    isLoading = false,
  )
  ProfileRow(
    title = stringResource(R.string.profile_appSettingsSection_row_headline),
    icon = HedvigIcons.Settings,
    onClick = showSettings,
    isLoading = false,
  )
}

@Composable
private fun ProfileRow(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick, enabled = !isLoading)
      .padding(16.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      modifier = Modifier
        .size(24.dp)
        .placeholder(
          isLoading,
          highlight = PlaceholderHighlight.shimmer(),
        ),
    )
    Spacer(Modifier.width(16.dp))
    HedvigText(
      text = title,
      modifier = Modifier
        .placeholder(
          isLoading,
          highlight = PlaceholderHighlight.shimmer(),
        ),
    )
  }
}

@Composable
private fun PreviewProfileItemRows() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PreviewContentWithProvidedParametersAnimatedOnClick(
        parametersList = ProfileUiStateProvider().values.toList(),
      ) { uiState ->
        Column {
          ProfileRows(
            uiState,
            {},
            {},
            {},
            {},
            {},
          )
        }
      }
    }
  }
}

private class ProfileUiStateProvider :
  CollectionPreviewParameterProvider<ProfileUiState>(
    listOf(
      ProfileUiState.Loading,
      ProfileUiState.Success(
        travelCertificateAvailable = true,
      ),
      ProfileUiState.Loading,
      ProfileUiState.Success(
        euroBonus = EuroBonus("jsdhgwmehg"),
        travelCertificateAvailable = true,
      ),
      ProfileUiState.Loading,
      ProfileUiState.Success(
        euroBonus = EuroBonus("jsdhgwmehg"),
        travelCertificateAvailable = false,
      ),
      ProfileUiState.Loading,
    ),
  )
