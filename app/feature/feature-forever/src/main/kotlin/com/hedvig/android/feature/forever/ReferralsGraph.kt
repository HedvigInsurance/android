package com.hedvig.android.feature.forever

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.apollo.format
import com.hedvig.android.apollo.toWebLocaleTag
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Copy
import com.hedvig.android.core.ui.appbar.m3.TopAppBarLayoutForActions
import com.hedvig.android.feature.forever.data.ReferralsRepository
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.TopLevelGraph
import com.kiwi.navigationcompose.typed.createRoutePattern
import hedvig.resources.R
import java.util.*
import javax.money.MonetaryAmount
import kotlinx.coroutines.launch
import org.javamoney.moneta.Money
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.referralsGraph(
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
  AnimatedContent(targetState = uiState.isLoading, label = "") { loading ->
    when (loading) {
      true -> HedvigFullScreenCenterAlignedProgress(show = uiState.isLoading)
      false -> ReferralsScreen(
        uiState = uiState,
        reload = viewModel::reload,
        onSubmitCode = viewModel::onSubmitCode,
        onCodeChanged = viewModel::onCodeChanged,
        languageService = languageService,
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
      val shareSheetTitle = stringResource(R.string.REFERRALS_SHARE_SHEET_TITLE)
      ForeverScreen(
        uiState = uiState,
        reload = reload,
        onShareCodeClick = { code, incentive ->
          context.showShareSheet(shareSheetTitle) { intent ->
            intent.putExtra(
              Intent.EXTRA_TEXT,
              resources.getString(
                R.string.REFERRAL_SMS_MESSAGE,
                incentive.format(languageService.getLocale()),
                buildString {
                  append("https://www.dev.hedvigit.com") // TODO Get from resources
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
        onCodeChanged = onCodeChanged,
        onSubmitCode = onSubmitCode,
      ) { referralTermsUrl: String, referralIncentive: MonetaryAmount ->
        // show bottom sheet
      }
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
    ModalBottomSheet(
      containerColor = MaterialTheme.colorScheme.background,
      onDismissRequest = {
        coroutineScope.launch {
          sheetState.hide()
          showEditBottomSheet = false
        }
      },
      // todo use "https://github.com/c5inco/smoother" for a top only squircle shape here
      sheetState = sheetState,
      tonalElevation = 0.dp,
    ) {
      Text(
        text = stringResource(id = R.string.referrals_change_change_code),
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(32.dp))
      HedvigTextField(
        value = uiState.editedCampaignCode ?: "",
        label = {
          Text(stringResource(id = R.string.referrals_empty_code_headline))
        },
        onValueChange = onCodeChanged,
        errorText = uiState.codeError.toErrorMessage(),
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigContainedButton(
        text = stringResource(id = R.string.general_save_button),
        onClick = {
          onSubmitCode(uiState.editedCampaignCode ?: "")
        },
        isLoading = uiState.isLoadingCode,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(id = R.string.general_cancel_button),
        onClick = {
          coroutineScope.launch {
            sheetState.hide()
            showEditBottomSheet = false
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
        text = stringResource(R.string.PROFILE_REFERRAL_TITLE),
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      ReferralsContent(uiState)
      if (incentive != null && uiState.campaignCode != null) {
        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          text = stringResource(R.string.referrals_empty_share_code_button),
          onClick = { onShareCodeClick(uiState.campaignCode, incentive) },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(id = R.string.referrals_change_change_code),
          onClick = {
            coroutineScope.launch {
              sheetState.hide()
              showEditBottomSheet = true
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
  /*
  AndroidViewBinding(
    factory = ReferralsHeaderBinding::inflate,
    update = bindPieChart(uiState),
  )
   */
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
  HedvigBigCard(
    onClick = {
      uiState.campaignCode?.let {
        context.copyToClipboard(uiState.campaignCode)
      }
    },
    enabled = true,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    Row(
      modifier = Modifier
        .heightIn(min = 72.dp)
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Column {
        Text(
          text = stringResource(id = R.string.referrals_empty_code_headline),
          style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
        Text(
          text = uiState.campaignCode ?: "",
          style = MaterialTheme.typography.headlineSmall,
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      Icon(
        imageVector = Icons.Hedvig.Copy,
        contentDescription = "Copy",
        modifier = Modifier
          .align(Alignment.Bottom)
          .padding(bottom = 8.dp),
      )
    }
  }
}
