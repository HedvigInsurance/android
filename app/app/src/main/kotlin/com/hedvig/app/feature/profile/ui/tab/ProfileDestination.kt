package com.hedvig.app.feature.profile.ui.tab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.ToolbarChatIcon
import com.hedvig.android.core.ui.appbar.m3.TopAppBarLayoutForActions
import com.hedvig.android.core.ui.getLocale
import com.hedvig.app.R
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.extensions.startChat
import org.javamoney.moneta.Money
import java.math.BigDecimal

@Composable
internal fun ProfileDestination(
  navigateToEurobonus: () -> Unit,
  navigateToBusinessModel: () -> Unit,
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
    navigateToBusinessModel = navigateToBusinessModel,
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
  navigateToBusinessModel: () -> Unit,
  navigateToMyInfo: () -> Unit,
  navigateToAboutApp: () -> Unit,
  navigateToSettings: () -> Unit,
  navigateToPayment: () -> Unit,
  reload: () -> Unit,
  onLogout: () -> Unit,
) {
  val context = LocalContext.current
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
      ProfileItemRows(
        profileUiState = uiState,
        showMyInfo = navigateToMyInfo,
        showBusinessModel = navigateToBusinessModel,
        showPaymentInfo = navigateToPayment,
        showSettings = navigateToSettings,
        showAboutApp = navigateToAboutApp,
        navigateToEurobonus = navigateToEurobonus,
        logout = onLogout,
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    TopAppBarLayoutForActions {
      ToolbarChatIcon(
        onClick = {
          context.startChat()
        },
      )
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
  showBusinessModel: () -> Unit,
  showPaymentInfo: () -> Unit,
  showSettings: () -> Unit,
  showAboutApp: () -> Unit,
  navigateToEurobonus: () -> Unit,
  logout: () -> Unit,
) {
  ProfileRow(
    title = stringResource(hedvig.resources.R.string.PROFILE_MY_INFO_ROW_TITLE),
    caption = profileUiState.contactInfoName,
    iconPainter = painterResource(R.drawable.ic_contact_information),
    onClick = showMyInfo,
  )
  AnimatedVisibility(
    visible = profileUiState.paymentInfo != null,
    enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
    label = "Payment info",
  ) {
    val paymentCaption = profileUiState.paymentInfo?.let {
      getPriceCaption(it)
    } ?: ""
    ProfileRow(
      title = stringResource(hedvig.resources.R.string.PROFILE_ROW_PAYMENT_TITLE),
      caption = paymentCaption,
      iconPainter = painterResource(R.drawable.ic_payment),
      onClick = showPaymentInfo,
    )
  }

  AnimatedVisibility(
    visible = profileUiState.euroBonus != null,
    enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
    label = "Eurobonus",
  ) {
    ProfileRow(
      title = stringResource(hedvig.resources.R.string.sas_integration_title),
      caption = when {
        profileUiState.euroBonus == null -> ""
        profileUiState.euroBonus.code != null -> profileUiState.euroBonus.code
        else -> stringResource(hedvig.resources.R.string.sas_integration_connect_your_number)
      },
      iconPainter = when {
        profileUiState.euroBonus == null -> ColorPainter(Color.Transparent)
        profileUiState.euroBonus.code != null -> {
          painterResource(com.hedvig.android.core.design.system.R.drawable.ic_checkmark_in_circle)
        }

        else -> painterResource(com.hedvig.android.core.design.system.R.drawable.ic_info)
      },
      onClick = navigateToEurobonus,
    )
  }

  AnimatedVisibility(
    visible = profileUiState.showBusinessModel,
    enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
    label = "Business model",
  ) {
    ProfileRow(
      title = stringResource(hedvig.resources.R.string.BUSINESS_MODEL_PROFILE_ROW),
      caption = null,
      iconPainter = painterResource(R.drawable.ic_profile_business_model),
      onClick = showBusinessModel,
    )
  }
  Spacer(Modifier.height(56.dp))
  Text(
    text = stringResource(hedvig.resources.R.string.profile_appSettingsSection_title),
    style = MaterialTheme.typography.headlineSmall,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(4.dp))
  ProfileRow(
    title = stringResource(hedvig.resources.R.string.profile_appSettingsSection_row_headline),
    caption = stringResource(hedvig.resources.R.string.profile_appSettingsSection_row_subheadline),
    iconPainter = painterResource(R.drawable.ic_profile_settings),
    onClick = showSettings,
  )
  ProfileRow(
    title = stringResource(hedvig.resources.R.string.PROFILE_ABOUT_ROW),
    caption = stringResource(hedvig.resources.R.string.profile_tab_about_row_subtitle),
    iconPainter = painterResource(hedvig.resources.R.drawable.ic_info_toolbar),
    onClick = showAboutApp,
  )
  CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.error) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = logout)
        .padding(16.dp),
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_power),
        contentDescription = null,
      )
      Spacer(Modifier.width(16.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.LOGOUT_BUTTON),
        style = MaterialTheme.typography.bodyLarge,
      )
    }
  }
  Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
}

@ReadOnlyComposable
@Composable
private fun getPriceCaption(paymentInfo: PaymentInfo): String? {
  paymentInfo.priceCaptionResId ?: return null
  val locale = getLocale()
  val localizedAmount = paymentInfo.monetaryMonthlyNet.format(locale)
  return stringResource(paymentInfo.priceCaptionResId, localizedAmount)
}

@Composable
private fun ProfileRow(
  title: String,
  caption: String?,
  iconPainter: Painter,
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
      painter = iconPainter,
      contentDescription = null,
      modifier = Modifier.size(24.dp),
    )
    Spacer(Modifier.width(16.dp))
    Box(
      contentAlignment = Alignment.CenterStart,
      modifier = Modifier.weight(1f),
    ) {
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
        )
        if (caption != null) {
          Spacer(Modifier.height(4.dp))
          Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      // Another instance of the content, with the caption always rendering, so that the item takes that much space
      // regardless. Used to make the entire ProfileRow as big as the other rows are.
      Column(Modifier.alpha(0f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(4.dp))
        Text(
          text = caption ?: "",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    Spacer(Modifier.width(16.dp))
    Icon(
      painter = painterResource(hedvig.resources.R.drawable.ic_arrow_forward),
      contentDescription = null,
      modifier = Modifier.size(24.dp),
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
          contactInfoName = "Contact info name",
          paymentInfo = PaymentInfo(
            Money.of(BigDecimal(123), "SEK"),
            hedvig.resources.R.string.Direct_Debit_Connected,
          ),
          euroBonus = EuroBonus("ABC-12345678"),
          showBusinessModel = true,
        ),
        navigateToEurobonus = {},
        navigateToBusinessModel = {},
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
