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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ContactInformation
import com.hedvig.android.core.icons.hedvig.normal.Eurobonus
import com.hedvig.android.core.icons.hedvig.normal.Info
import com.hedvig.android.core.icons.hedvig.normal.MultipleDocuments
import com.hedvig.android.core.icons.hedvig.normal.Settings
import com.hedvig.android.core.ui.dialog.HedvigAlertDialog
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.preview.PreviewContentWithProvidedParametersAnimatedOnClick
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
  openChat: () -> Unit,
  viewModel: ProfileViewModel,
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
    openChat = openChat,
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
  openChat: () -> Unit,
  snoozeNotificationPermission: () -> Unit,
  onLogout: () -> Unit,
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
      title = null,
      text = stringResource(id = R.string.PROFILE_LOGOUT_DIALOG_MESSAGE),
      dismissButtonLabel = stringResource(id = R.string.general_cancel_button),
      onDismissRequest = { showLogoutDialog = false },
      onConfirmClick = onLogout,
    )
  }

  Box(Modifier.fillMaxSize()) {
    Column(
      Modifier
        .matchParentSize()
        .pullRefresh(pullRefreshState)
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .height(64.dp)
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      ) {
        Text(
          text = stringResource(id = R.string.PROFILE_TITLE),
          style = MaterialTheme.typography.titleLarge,
        )
      }
      Spacer(Modifier.height(16.dp))
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
        MemberReminderCards(
          memberReminders = memberReminders,
          navigateToConnectPayment = navigateToConnectPayment,
          navigateToAddMissingInfo = navigateToAddMissingInfo,
          openUrl = openUrl,
          notificationPermissionState = notificationPermissionState,
          snoozeNotificationPermissionReminder = snoozeNotificationPermission,
          contentPadding = PaddingValues(horizontal = 16.dp) + WindowInsets.safeDrawing
            .exclude(consumedWindowInsets)
            .only(WindowInsetsSides.Horizontal)
            .asPaddingValues(),
          modifier = Modifier.onConsumedWindowInsetsChanged { consumedWindowInsets = it },
          openChat = openChat,
        )
        if (memberReminders.isNotEmpty()) {
          Spacer(Modifier.height(16.dp))
        }
      }
      HedvigTextButton(
        text = stringResource(R.string.LOGOUT_BUTTON),
        colors = ButtonDefaults.textButtonColors(
          contentColor = MaterialTheme.colorScheme.error,
        ),
        onClick = {
          showLogoutDialog = true
        },
        modifier = Modifier
          .padding(horizontal = 16.dp)
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
    icon = Icons.Hedvig.Info,
    isLoading = true,
    onClick = {},
  )
  ProfileRow(
    title = stringResource(R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
    icon = Icons.Hedvig.Info,
    isLoading = true,
    onClick = {},
  )
  ProfileRow(
    title = stringResource(R.string.PROFILE_ABOUT_ROW),
    icon = Icons.Hedvig.Info,
    isLoading = true,
    onClick = {},
  )
  ProfileRow(
    title = stringResource(R.string.profile_appSettingsSection_row_headline),
    icon = Icons.Hedvig.Info,
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
    icon = Icons.Hedvig.ContactInformation,
    onClick = showMyInfo,
    isLoading = false,
  )
  if (profileUiState.travelCertificateAvailable) {
    ProfileRow(
      title = stringResource(R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
      icon = Icons.Hedvig.MultipleDocuments,
      onClick = navigateToTravelCertificate,
      isLoading = false,
    )
  }
  if (profileUiState.euroBonus != null) {
    ProfileRow(
      title = stringResource(R.string.sas_integration_title),
      icon = Icons.Hedvig.Eurobonus,
      onClick = navigateToEurobonus,
      isLoading = false,
    )
  }
  ProfileRow(
    title = stringResource(R.string.PROFILE_ABOUT_ROW),
    icon = Icons.Hedvig.Info,
    onClick = showAboutApp,
    isLoading = false,
  )
  ProfileRow(
    title = stringResource(R.string.profile_appSettingsSection_row_headline),
    icon = Icons.Hedvig.Settings,
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
        .placeholder(isLoading, highlight = PlaceholderHighlight.shimmer()),
    )
    Spacer(Modifier.width(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier
        .placeholder(isLoading, highlight = PlaceholderHighlight.shimmer()),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewProfileItemRows() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PreviewContentWithProvidedParametersAnimatedOnClick(
        parametersList = ProfileUiStateProvider().values.toList(),
        content = {
            uiState ->
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
        },
      )
    }
  }
}

private class ProfileUiStateProvider : CollectionPreviewParameterProvider<ProfileUiState>(
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
