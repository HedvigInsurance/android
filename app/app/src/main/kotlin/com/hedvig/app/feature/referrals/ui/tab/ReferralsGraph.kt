package com.hedvig.app.feature.referrals.ui.tab

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.core.ui.appbar.m3.TopAppBarLayoutForActions
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.core.ui.appbar.m3.TopAppBarLayoutForActions
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.app.R
import com.hedvig.app.databinding.ReferralsHeaderBinding
import com.hedvig.app.feature.referrals.data.ReferralsRepository
import com.hedvig.app.feature.referrals.ui.ReferralsInformationActivity
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toWebLocaleTag
import com.hedvig.app.util.extensions.copyToClipboard
import com.hedvig.app.util.extensions.showShareSheet
import com.kiwi.navigationcompose.typed.createRoutePattern
import org.javamoney.moneta.Money
import org.koin.androidx.compose.koinViewModel
import java.util.Locale
import javax.money.MonetaryAmount
import kotlinx.coroutines.launch

internal fun NavGraphBuilder.referralsGraph(
  languageService: LanguageService,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
) {
  animatedNavigation<TopLevelGraph.REFERRALS>(
    startDestination = createRoutePattern<AppDestination.TopLevelDestination.Referrals>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.forever },
    ),
  ) {
    animatedComposable<AppDestination.TopLevelDestination.Referrals>(
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) {
      val viewModel: ReferralsViewModel = koinViewModel()
      ReferralsDestination(
        viewModel = viewModel,
        languageService = languageService,
      )
    }
  }
}

@Composable
private fun ReferralsDestination(
  viewModel: ReferralsViewModel,
  languageService: LanguageService,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  AnimatedContent(targetState = uiState.isLoading) { loading ->
    when (loading) {
      true -> HedvigFullScreenCenterAlignedProgress(show = uiState.isLoading)
      false -> ReferralsScreen(
        uiState = uiState,
        reload = viewModel::reload,
        onCodeChanged = viewModel::onCodeChanged,
        languageService = languageService,
        onSubmitCode = viewModel::onSubmitCode,
        showEditCode = viewModel::showEditCode,
        hideEditCode = viewModel::hideEditCode,
      )
    }
  }
}

@Composable
private fun ReferralsScreen(
  uiState: ReferralsUiState,
  reload: () -> Unit,
  onSubmitCode: (String) -> Unit,
  onCodeChanged: (String) -> Unit,
  hideEditCode: () -> Unit,
  showEditCode: () -> Unit,
  languageService: LanguageService,
) {
  Box(
    modifier = Modifier.fillMaxSize(),
    propagateMinConstraints = true,
  ) {
    val context = LocalContext.current
    val resources = context.resources

    if (uiState.errorMessage != null) {
      Box {
        Column(Modifier.matchParentSize()) {
          Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
          Spacer(Modifier.height(64.dp))
          HedvigErrorSection(retry = reload)
        }
      }
    } else {
      ForeverScreen(
        uiState = uiState,
        reload = reload,
        onShareCodeClick = { code, incentive ->
          context.showShareSheet(hedvig.resources.R.string.REFERRALS_SHARE_SHEET_TITLE) { intent ->
            intent.putExtra(
              Intent.EXTRA_TEXT,
              resources.getString(
                hedvig.resources.R.string.REFERRAL_SMS_MESSAGE,
                incentive.format(languageService.getLocale()),
                buildString {
                  append(resources.getString(R.string.WEB_BASE_URL))
                  append("/")
                  append(languageService.getGraphQLLocale().toWebLocaleTag())
                  append("/forever/")
                  append(Uri.encode(code))
                },
              ),
            )
            intent.type = "text/plain"
          }
        },
        openReferralsInformation = { referralTermsUrl: String, referralIncentive: MonetaryAmount ->
          context.startActivity(
            ReferralsInformationActivity.newInstance(
              context = context,
              termsUrl = referralTermsUrl,
              incentive = referralIncentive,
            ),
          )
        },
        onCodeChanged = onCodeChanged,
        onSubmitCode = onSubmitCode,
        showEditCode = showEditCode,
        hideEditCode = hideEditCode,
      )
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ForeverScreen(
  uiState: ReferralsUiState,
  reload: () -> Unit,
  onShareCodeClick: (code: String, incentive: MonetaryAmount) -> Unit,
  onCodeChanged: (String) -> Unit,
  onSubmitCode: (String) -> Unit,
  hideEditCode: () -> Unit,
  showEditCode: () -> Unit,
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
  val incentive = uiState.incentive

  val sheetState = rememberModalBottomSheetState()
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(uiState.showEditCode) {
    coroutineScope.launch {
      if (uiState.showEditCode) {
        sheetState.show()
      } else {
        sheetState.hide()
      }
    }
  }

  if (uiState.showEditCode) {
    ModalBottomSheet(
      onDismissRequest = {
        hideEditCode()
      },
      // todo use "https://github.com/c5inco/smoother" for a top only squircle shape here
      sheetState = sheetState,
      tonalElevation = 0.dp,
    ) {
      Text(
        text = stringResource(id = hedvig.resources.R.string.referrals_change_change_code),
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(32.dp))
      HedvigTextField(
        value = uiState.editedCampaignCode ?: "",
        label = {
          Text(stringResource(id = hedvig.resources.R.string.referrals_empty_code_headline))
        },
        onValueChange = onCodeChanged,
        errorText = uiState.codeError.toErrorMessage(),
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigContainedButton(
        text = stringResource(id = hedvig.resources.R.string.general_save_button),
        onClick = {
          onSubmitCode(uiState.editedCampaignCode ?: "")
        },
        isLoading = uiState.isLoadingCode,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(id = hedvig.resources.R.string.general_cancel_button),
        onClick = {
          coroutineScope.launch {
            sheetState.hide()
          }.invokeOnCompletion {
            hideEditCode()
          }
        },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
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
        text = stringResource(hedvig.resources.R.string.PROFILE_REFERRAL_TITLE),
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      ReferralsContent(uiState)
      if (incentive != null && uiState.campaignCode != null) {
        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          text = stringResource(hedvig.resources.R.string.referrals_empty_share_code_button),
          onClick = { onShareCodeClick(uiState.campaignCode, incentive) },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(id = hedvig.resources.R.string.referrals_change_change_code),
          onClick = {
            coroutineScope.launch {
              sheetState.expand()
            }.invokeOnCompletion {
              showEditCode()
            }
          },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    if (incentive != null && uiState.referralUrl != null) {
      TopAppBarLayoutForActions {
        IconButton(
          onClick = { openReferralsInformation(uiState.referralUrl, incentive) },
          colors = IconButtonDefaults.iconButtonColors(),
          modifier = Modifier.size(40.dp),
        ) {
          Icon(
            painter = painterResource(hedvig.resources.R.drawable.ic_info_toolbar),
            contentDescription = stringResource(hedvig.resources.R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
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

private fun ReferralsRepository.ReferralError?.toErrorMessage(): String? = when (this) {
  ReferralsRepository.ReferralError.CodeExists -> "Code exists"
  is ReferralsRepository.ReferralError.CodeTooLong -> "Code too long"
  is ReferralsRepository.ReferralError.CodeTooShort -> "Too short"
  is ReferralsRepository.ReferralError.GeneralError -> "General Error"
  is ReferralsRepository.ReferralError.MaxUpdates -> "Max updates"
  null -> null
}

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.ReferralsContent(
  uiState: ReferralsUiState,
) {
  val locale = uiState.locale ?: Locale.ENGLISH
  val context = LocalContext.current

  CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
      text = uiState.currentDiscountAmount?.format(locale) ?: "-",
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
  }
  Spacer(Modifier.height(16.dp))
  AndroidViewBinding(
    factory = ReferralsHeaderBinding::inflate,
    update = bindPieChart(uiState),
  )
  Spacer(Modifier.height(24.dp))
  if (uiState.referrals.isEmpty() && uiState.incentive != null) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
      Text(
        text = stringResource(
          id = hedvig.resources.R.string.referrals_empty_body,
          uiState.incentive.format(locale),
          Money.of(0, uiState.incentive.currency?.currencyCode).format(locale),
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
  } else {
    Text(
      text = stringResource(id = hedvig.resources.R.string.FOREVER_TAB_MONTLY_COST_LABEL),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
      Text(
        text = stringResource(
          id = hedvig.resources.R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
          uiState.currentNetAmount?.format(locale) ?: "-",
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
  Spacer(Modifier.height(82.dp))
  HedvigBigCard(
    onClick = {
      uiState.campaignCode?.let {
        context.copyToClipboard(uiState.campaignCode)
      }
    },
    hintText = stringResource(id = hedvig.resources.R.string.referrals_empty_code_headline),
    inputText = uiState.campaignCode,
    enabled = true,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
}
