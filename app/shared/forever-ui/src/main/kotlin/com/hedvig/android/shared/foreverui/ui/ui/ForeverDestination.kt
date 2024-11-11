package com.hedvig.android.shared.foreverui.ui.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigBigCard
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigSnackbar
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Copy
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoOutline
import com.hedvig.android.language.LanguageService
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.PullRefreshState
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import com.hedvig.android.shared.foreverui.ui.data.ForeverData
import com.hedvig.android.shared.foreverui.ui.data.Referral
import com.hedvig.android.shared.foreverui.ui.data.ReferralState
import hedvig.resources.R

@Composable
fun ForeverDestination(
  viewModel: ForeverViewModel,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ForeverScreen(
    uiState = uiState,
    reload = { viewModel.emit(ForeverEvent.RetryLoadReferralData) },
    onSubmitCode = { viewModel.emit(ForeverEvent.SubmitNewReferralCode(it)) },
    showedReferralCodeSubmissionError = { viewModel.emit(ForeverEvent.ShowedReferralCodeSubmissionError) },
    showedReferralCodeSuccessfulChangeMessage = {
      viewModel.emit(ForeverEvent.ShowedReferralCodeSuccessfulChangeMessage)
    },
    languageService = languageService,
    hedvigBuildConstants = hedvigBuildConstants,
  )
}

@Composable
private fun ForeverScreen(
  uiState: ForeverUiState,
  reload: () -> Unit,
  onSubmitCode: (String) -> Unit,
  showedReferralCodeSubmissionError: () -> Unit,
  showedReferralCodeSuccessfulChangeMessage: () -> Unit,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  val context = LocalContext.current
  LocalConfiguration.current
  val resources = context.resources
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = uiState.isLoadingForeverData,
    onRefresh = reload,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )
  Box(Modifier.fillMaxSize()) {
    val transition = updateTransition(targetState = uiState, label = "home ui state")
    transition.AnimatedContent(
      modifier = Modifier.fillMaxSize(),
      contentKey = { it.foreverDataErrorMessage },
    ) { uiState ->
      if (uiState.foreverDataErrorMessage != null) {
        HedvigErrorSection(onButtonClick = reload)
      } else {
        val shareSheetTitle = stringResource(R.string.REFERRALS_SHARE_SHEET_TITLE)
        ForeverContent(
          uiState = uiState,
          pullRefreshState = pullRefreshState,
          onShareCodeClick = { code: String, incentive: UiMoney ->
            context.showShareSheet(shareSheetTitle) { intent ->
              intent.putExtra(
                Intent.EXTRA_TEXT,
                resources.getString(
                  R.string.REFERRAL_SMS_MESSAGE,
                  incentive.toString(),
                  buildString {
                    append(hedvigBuildConstants.urlBaseWeb)
                    append("/")
                    append(languageService.getLanguage().webPath())
                    append("/forever/")
                    append(Uri.encode(code))
                  },
                ),
              )
              intent.type = "text/plain"
            }
          },
          onSubmitCode = onSubmitCode,
          showedReferralCodeSubmissionError = showedReferralCodeSubmissionError,
          showedCampaignCodeSuccessfulChangeMessage = showedReferralCodeSuccessfulChangeMessage,
        )
      }
    }
    PullRefreshIndicator(
      refreshing = uiState.isLoadingForeverData,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

@Composable
internal fun ForeverContent(
  uiState: ForeverUiState,
  pullRefreshState: PullRefreshState,
  onShareCodeClick: (code: String, incentive: UiMoney) -> Unit,
  onSubmitCode: (String) -> Unit,
  showedReferralCodeSubmissionError: () -> Unit,
  showedCampaignCodeSuccessfulChangeMessage: () -> Unit,
) {
  var textFieldValue by remember(uiState.foreverData?.campaignCode) {
    mutableStateOf(uiState.foreverData?.campaignCode ?: "")
  }

  var showEditReferralCodeBottomSheet by rememberSaveable { mutableStateOf(false) }

  EditCodeBottomSheet(
    isVisible = showEditReferralCodeBottomSheet,
    code = textFieldValue,
    onCodeChanged = { textFieldValue = it },
    onDismiss = {
      showEditReferralCodeBottomSheet = false
    },
    onSubmitCode = {
      onSubmitCode(textFieldValue)
      showEditReferralCodeBottomSheet = false
    },
    referralCodeUpdateError = uiState.referralCodeErrorMessage,
    showedReferralCodeSubmissionError = showedReferralCodeSubmissionError,
    isLoading = uiState.referralCodeLoading,
  )

  var showReferralExplanationBottomSheet by rememberSaveable { mutableStateOf(false) }
  if (uiState.foreverData?.incentive != null) {
    ForeverExplanationBottomSheet(
      discount = uiState.foreverData.incentive.toString(),
      onDismiss = { showReferralExplanationBottomSheet = false },
      isVisible = showReferralExplanationBottomSheet,
    )
  }

  LaunchedEffect(textFieldValue) {
    showedReferralCodeSubmissionError() // Clear error on new referral code input
  }
  LaunchedEffect(showEditReferralCodeBottomSheet) {
    if (!showEditReferralCodeBottomSheet) {
      showedReferralCodeSubmissionError() // Clear error when the dialog is dismissed
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
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .height(64.dp)
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      ) {
        HedvigText(
          text = stringResource(R.string.TAB_REFERRALS_TITLE),
          style = HedvigTheme.typography.headlineSmall,
        )
        if (uiState.foreverData?.incentive != null) {
          IconButton(
            onClick = { showReferralExplanationBottomSheet = true },
            modifier = Modifier.size(40.dp),
          ) {
            Icon(
              imageVector = HedvigIcons.InfoOutline,
              contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
              modifier = Modifier.size(24.dp),
            )
          }
        }
      }
      Spacer(Modifier.height(16.dp))
      HedvigText(
        text = uiState.foreverData?.currentDiscount?.toString()?.let { "-$it" } ?: "-",
        textAlign = TextAlign.Center,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
      DiscountPieChart(
        totalPrice = uiState.foreverData?.currentGrossCost?.amount?.toFloat() ?: 0f,
        totalExistingDiscount = uiState.foreverData?.currentDiscount?.amount?.toFloat() ?: 0f,
        incentive = uiState.foreverData?.incentive?.amount?.toFloat() ?: 0f,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth()
          .wrapContentWidth(Alignment.CenterHorizontally),
      )
      Spacer(Modifier.height(24.dp))
      if (uiState.foreverData?.referrals?.isEmpty() == true && uiState.foreverData.incentive != null) {
        HedvigText(
          text = stringResource(
            id = R.string.referrals_empty_body,
            uiState.foreverData.incentive.toString(),
          ),
          style = HedvigTheme.typography.bodySmall.copy(
            textAlign = TextAlign.Center,
            lineBreak = LineBreak.Heading,
          ),
          color = HedvigTheme.colorScheme.textSecondary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      } else {
        HedvigText(
          text = stringResource(id = R.string.FOREVER_TAB_MONTLY_COST_LABEL),
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        HedvigText(
          text = stringResource(
            id = R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
            uiState.foreverData?.currentNetCost?.toString() ?: "-",
          ),
          textAlign = TextAlign.Center,
          color = HedvigTheme.colorScheme.textSecondary,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      if (uiState.foreverData?.campaignCode != null) {
        Spacer(Modifier.height(16.dp))
        ReferralCodeCard(
          campaignCode = uiState.foreverData.campaignCode,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
      Spacer(Modifier.weight(1f))
      if (uiState.foreverData?.incentive != null && uiState.foreverData.campaignCode != null) {
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(R.string.referrals_empty_share_code_button),
          enabled = true,
          onClick = {
            onShareCodeClick(
              uiState.foreverData.campaignCode,
              uiState.foreverData.incentive,
            )
          },
          modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(id = R.string.referrals_change_change_code),
          onClick = {
            showEditReferralCodeBottomSheet = true
          },
          buttonSize = Large,
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        )
      }
      if (uiState.foreverData?.referrals?.isNotEmpty() == true) {
        Spacer(Modifier.height(16.dp))
        ReferralList(
          referrals = uiState.foreverData.referrals,
          grossPriceAmount = uiState.foreverData.currentGrossCost,
          currentNetAmount = uiState.foreverData.currentNetCost,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    HedvigSnackbar(
      snackbarText = stringResource(R.string.referrals_change_code_changed),
      priority = NotificationPriority.Info,
      showSnackbar = uiState.showReferralCodeSuccessfullyChangedMessage,
      showedSnackbar = showedCampaignCodeSuccessfulChangeMessage,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .windowInsetsPadding(WindowInsets.safeDrawing),
    )
  }
}

@Composable
internal fun ReferralCodeCard(campaignCode: String, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  HedvigBigCard(
    onClick = {
      context.copyToClipboard(campaignCode)
    },
    enabled = true,
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .heightIn(min = 72.dp)
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Column {
        HedvigText(
          text = stringResource(id = R.string.referrals_empty_code_headline),
          style = HedvigTheme.typography.bodySmall.copy(color = HedvigTheme.colorScheme.textSecondary),
        )
        HedvigText(
          text = campaignCode,
          style = HedvigTheme.typography.headlineSmall,
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      Icon(
        imageVector = HedvigIcons.Copy,
        contentDescription = "Copy",
        modifier = Modifier
          .align(Alignment.Bottom)
          .padding(bottom = 8.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewForeverContent(
  @PreviewParameter(ForeverUiStateProvider::class) foreverUiState: ForeverUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ForeverContent(
        foreverUiState,
        rememberPullRefreshState(false, {}),
        { _, _ -> },
        {},
        {},
        {},
      )
    }
  }
}

private class ForeverUiStateProvider : CollectionPreviewParameterProvider<ForeverUiState>(
  listOf(
    ForeverUiState(
      foreverDataErrorMessage = ErrorMessage("Error message", null),
      foreverData = null,
      isLoadingForeverData = false,
      referralCodeLoading = false,
      referralCodeErrorMessage = null,
      showReferralCodeSuccessfullyChangedMessage = false,
    ),
    ForeverUiState(
      foreverData = ForeverData(
        referrals = listOf(
          Referral("Name#1", ReferralState.ACTIVE, null),
          Referral("Name#2", ReferralState.IN_PROGRESS, null),
          Referral("Name#3", ReferralState.TERMINATED, null),
        ),
        campaignCode = null,
        incentive = null,
        currentNetCost = null,
        currentDiscount = null,
        currentGrossCost = null,
        currentDiscountAmountExcludingReferrals = null,
      ),
      isLoadingForeverData = false,
      foreverDataErrorMessage = null,
      referralCodeLoading = false,
      referralCodeErrorMessage = null,
      showReferralCodeSuccessfullyChangedMessage = false,
    ),
  ),
)
