
package com.hedvig.android.feature.profile.tab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import com.hedvig.android.core.icons.hedvig.normal.Payments
import com.hedvig.android.core.icons.hedvig.normal.Settings
import com.hedvig.android.core.ui.dialog.HedvigAlertDialog
import com.hedvig.android.core.ui.plus
import com.hedvig.android.memberreminders.ui.MemberReminderCards
import com.hedvig.android.notification.permission.NotificationPermissionDialog
import com.hedvig.android.notification.permission.rememberNotificationPermissionState
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
  navigateToPayment: () -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToAddMissingInfo: (contractId: String) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  viewModel: ProfileViewModel,
) {
  val uiState by viewModel.data.collectAsStateWithLifecycle()
  ProfileScreen(
    uiState = uiState,
    reload = viewModel::reload,
    navigateToEurobonus = navigateToEurobonus,
    navigateToMyInfo = navigateToMyInfo,
    navigateToAboutApp = navigateToAboutApp,
    navigateToSettings = navigateToSettings,
    navigateToPayment = navigateToPayment,
    navigateToConnectPayment = navigateToConnectPayment,
    navigateToAddMissingInfo = navigateToAddMissingInfo,
    openAppSettings = openAppSettings,
    openUrl = openUrl,
    snoozeNotificationPermission = viewModel::snoozeNotificationPermission,
    onLogout = viewModel::onLogout,
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
  navigateToPayment: () -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToAddMissingInfo: (contractId: String) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  snoozeNotificationPermission: () -> Unit,
  onLogout: () -> Unit,
) {
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = uiState.isLoading,
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
      ProfileItemRows(
        profileUiState = uiState,
        showMyInfo = navigateToMyInfo,
        showPaymentInfo = navigateToPayment,
        showSettings = navigateToSettings,
        showAboutApp = navigateToAboutApp,
        navigateToEurobonus = navigateToEurobonus,
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.weight(1f))
      val notificationPermissionState = rememberNotificationPermissionState()
      val memberReminders =
        uiState.memberReminders.onlyApplicableReminders(notificationPermissionState.status.isGranted)
      NotificationPermissionDialog(notificationPermissionState, openAppSettings)
      var consumedWindowInsets by remember { mutableStateOf(WindowInsets(0.dp)) }

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
      )
      if (memberReminders.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
      }
      HedvigTextButton(
        text = stringResource(R.string.LOGOUT_BUTTON),
        colors = ButtonDefaults.textButtonColors(
          contentColor = MaterialTheme.colorScheme.error,
        ),
        onClick = {
          showLogoutDialog = true
        },
        modifier = Modifier.padding(horizontal = 16.dp).testTag("logout"),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    PullRefreshIndicator(
      refreshing = uiState.isLoading,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

@Composable
private fun ColumnScope.ProfileItemRows(
  profileUiState: ProfileUiState,
  showMyInfo: () -> Unit,
  showPaymentInfo: () -> Unit,
  showSettings: () -> Unit,
  showAboutApp: () -> Unit,
  navigateToEurobonus: () -> Unit,
) {
  ProfileRow(
    title = stringResource(R.string.PROFILE_MY_INFO_ROW_TITLE),
    icon = Icons.Hedvig.ContactInformation,
    onClick = showMyInfo,
  )
  ProfileRow(
    title = stringResource(R.string.PROFILE_ROW_PAYMENT_TITLE),
    icon = Icons.Hedvig.Payments,
    onClick = showPaymentInfo,
  )
  AnimatedVisibility(
    visible = profileUiState.euroBonus != null,
    enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
    label = "Eurobonus",
  ) {
    ProfileRow(
      title = stringResource(R.string.sas_integration_title),
      icon = Icons.Hedvig.Eurobonus,
      onClick = navigateToEurobonus,
    )
  }
  ProfileRow(
    title = stringResource(R.string.PROFILE_ABOUT_ROW),
    icon = Icons.Hedvig.Info,
    onClick = showAboutApp,
  )
  ProfileRow(
    title = stringResource(R.string.profile_appSettingsSection_row_headline),
    icon = Icons.Hedvig.Settings,
    onClick = showSettings,
  )
}

@Composable
private fun ProfileRow(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(16.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      modifier = Modifier.size(24.dp),
    )
    Spacer(Modifier.width(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewProfileSuccessScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ProfileScreen(
        uiState = ProfileUiState(euroBonus = EuroBonus("ABC-12345678")),
        reload = {},
        navigateToEurobonus = {},
        navigateToMyInfo = {},
        navigateToAboutApp = {},
        navigateToSettings = {},
        navigateToPayment = {},
        navigateToConnectPayment = {},
        navigateToAddMissingInfo = {},
        openAppSettings = {},
        openUrl = {},
        snoozeNotificationPermission = {},
      ) {}
    }
  }
}
