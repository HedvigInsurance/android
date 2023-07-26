package com.hedvig.app.feature.profile.ui.tab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ContactInformation
import com.hedvig.android.core.icons.hedvig.normal.Eurobonus
import com.hedvig.android.core.icons.hedvig.normal.Info
import com.hedvig.android.core.icons.hedvig.normal.Payments
import com.hedvig.android.core.icons.hedvig.normal.Settings

@Composable
internal fun ProfileDestination(
  navigateToEurobonus: () -> Unit,
  navigateToMyInfo: () -> Unit,
  navigateToAboutApp: () -> Unit,
  navigateToSettings: () -> Unit,
  navigateToPayment: () -> Unit,
  viewModel: ProfileViewModel,
) {
  val uiState by viewModel.data.collectAsStateWithLifecycle()

  LaunchedEffect(viewModel) {
    viewModel.reload()
  }
  ProfileScreen(
    uiState = uiState,
    navigateToEurobonus = navigateToEurobonus,
    navigateToMyInfo = navigateToMyInfo,
    navigateToAboutApp = navigateToAboutApp,
    navigateToSettings = navigateToSettings,
    navigateToPayment = navigateToPayment,
    reload = viewModel::reload,
    onLogout = viewModel::onLogout,
  )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProfileScreen(
  uiState: ProfileUiState,
  navigateToEurobonus: () -> Unit,
  navigateToMyInfo: () -> Unit,
  navigateToAboutApp: () -> Unit,
  navigateToSettings: () -> Unit,
  navigateToPayment: () -> Unit,
  reload: () -> Unit,
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
  Box(Modifier.fillMaxSize()) {
    Column(
      Modifier
        .matchParentSize()
        .pullRefresh(pullRefreshState)
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
      Spacer(Modifier.height(64.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.PROFILE_TITLE),
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
      ProfileItemRows(
        profileUiState = uiState,
        showMyInfo = navigateToMyInfo,
        showPaymentInfo = navigateToPayment,
        showSettings = navigateToSettings,
        showAboutApp = navigateToAboutApp,
        navigateToEurobonus = navigateToEurobonus,
      )
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.error) {
        Row(
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLogout)
            .padding(16.dp),
        ) {
          Text(
            text = stringResource(hedvig.resources.R.string.LOGOUT_BUTTON),
            style = MaterialTheme.typography.bodyLarge,
          )
        }
      }
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
    title = stringResource(hedvig.resources.R.string.PROFILE_MY_INFO_ROW_TITLE),
    icon = Icons.Hedvig.ContactInformation,
    onClick = showMyInfo,
  )
  ProfileRow(
    title = stringResource(hedvig.resources.R.string.PROFILE_ROW_PAYMENT_TITLE),
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
      title = stringResource(hedvig.resources.R.string.sas_integration_title),
      icon = Icons.Hedvig.Eurobonus,
      onClick = navigateToEurobonus,
    )
  }
  ProfileRow(
    title = stringResource(hedvig.resources.R.string.PROFILE_ABOUT_ROW),
    icon = Icons.Hedvig.Info,
    onClick = showAboutApp,
  )
  ProfileRow(
    title = stringResource(hedvig.resources.R.string.profile_appSettingsSection_row_headline),
    icon = Icons.Hedvig.Settings,
    onClick = showSettings,
  )
  Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
}

@Composable
private fun ProfileRow(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
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
        uiState = ProfileUiState(
          euroBonus = EuroBonus("ABC-12345678"),
          showBusinessModel = true,
        ),
        navigateToEurobonus = {},
        reload = {},
        onLogout = {},
        navigateToMyInfo = {},
        navigateToAboutApp = {},
        navigateToSettings = {},
        navigateToPayment = {},
      )
    }
  }
}
