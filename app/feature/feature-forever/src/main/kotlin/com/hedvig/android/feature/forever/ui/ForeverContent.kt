package com.hedvig.android.feature.forever.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.apollo.format
import com.hedvig.android.core.ui.appbar.m3.TopAppBarLayoutForActions
import com.hedvig.android.feature.forever.ForeverUiState
import com.hedvig.android.feature.forever.data.toErrorMessage
import hedvig.resources.R
import java.util.*
import javax.money.MonetaryAmount
import kotlinx.coroutines.launch
import org.javamoney.moneta.Money

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ForeverContent(
  uiState: ForeverUiState,
  reload: () -> Unit,
  onShareCodeClick: (code: String, incentive: MonetaryAmount) -> Unit,
  onCodeChanged: (String) -> Unit,
  onSubmitCode: (String) -> Unit,
  openReferralsInformation: (String, MonetaryAmount) -> Unit,
) {
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = uiState.isLoading,
    onRefresh = reload,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )
  val locale = uiState.locale ?: Locale.ENGLISH

  val sheetState = rememberModalBottomSheetState(true)
  val coroutineScope = rememberCoroutineScope()
  var showEditBottomSheet by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(uiState.showEditCode) {
    coroutineScope.launch {
      if (uiState.showEditCode) {
        sheetState.expand()
      } else {
        sheetState.hide()
      }
      showEditBottomSheet = uiState.showEditCode
    }
  }

  if (showEditBottomSheet) {
    EditCodeBottomSheet(
      sheetState = sheetState,
      code = uiState.editedCampaignCode ?: "",
      onCodeChanged = onCodeChanged,
      onDismiss = {
        coroutineScope.launch {
          sheetState.hide()
          showEditBottomSheet = false
        }
      },
      onSubmitCode = { onSubmitCode(uiState.editedCampaignCode ?: "") },
      errorText = uiState.codeError.toErrorMessage(),
      isLoading = uiState.isLoadingCode,
    )
  }

  Box {
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
        text = stringResource(R.string.PROFILE_REFERRAL_TITLE),
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(horizontal = 16.dp),
      )

      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Text(
          text = uiState.currentDiscountAmount?.format(locale) ?: "-",
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(16.dp))
      // TODO Add pie chart
      Spacer(Modifier.height(24.dp))
      if (uiState.referrals.isEmpty() && uiState.incentive != null) {
        Spacer(Modifier.height(32.dp))
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
          Text(
            text = stringResource(
              id = R.string.referrals_empty_body,
              uiState.incentive.format(locale),
              Money.of(0, uiState.incentive.currency?.currencyCode).format(locale),
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
        }
        Spacer(Modifier.height(16.dp))
      } else {
        Text(
          text = stringResource(id = R.string.FOREVER_TAB_MONTLY_COST_LABEL),
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
          Text(
            text = stringResource(
              id = R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
              uiState.currentNetAmount?.format(locale) ?: "-",
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
        }
        Spacer(Modifier.height(82.dp))
      }

      ReferralCodeContent(
        uiState = uiState,
        onChangeCodeClicked = {
          coroutineScope.launch {
            sheetState.hide()
            showEditBottomSheet = true
          }
        },
        onShareCodeClick = onShareCodeClick
      )
    }
    if (uiState.incentive != null && uiState.referralUrl != null) {
      TopAppBarLayoutForActions {
        IconButton(
          onClick = { openReferralsInformation(uiState.referralUrl, uiState.incentive) },
          colors = IconButtonDefaults.iconButtonColors(),
          modifier = Modifier.size(40.dp),
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_info_toolbar),
            contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
            modifier = Modifier.size(24.dp),
          )
        }
      }
    }
    PullRefreshIndicator(
      refreshing = uiState.isLoading,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}
