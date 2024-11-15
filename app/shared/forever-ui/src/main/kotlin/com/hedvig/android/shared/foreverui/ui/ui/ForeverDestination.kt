package com.hedvig.android.shared.foreverui.ui.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
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
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.placeholder
import com.hedvig.android.placeholder.shimmer
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.PullRefreshState
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import com.hedvig.android.shared.foreverui.ui.data.ForeverData
import com.hedvig.android.shared.foreverui.ui.data.Referral
import com.hedvig.android.shared.foreverui.ui.data.ReferralState
import com.hedvig.android.shared.foreverui.ui.ui.ForeverUiState.Loading
import com.hedvig.android.shared.foreverui.ui.ui.ForeverUiState.Success
import hedvig.resources.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ForeverDestination(
  viewModel: ForeverViewModel,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  LocalConfiguration.current
  val resources = context.resources
  val shareSheetTitle = stringResource(R.string.REFERRALS_SHARE_SHEET_TITLE)
  ForeverScreen(
    uiState = uiState,
    reload = { viewModel.emit(ForeverEvent.RetryLoadReferralData) },
    onSubmitCode = { viewModel.emit(ForeverEvent.SubmitNewReferralCode(it)) },
    showedReferralCodeSubmissionError = { viewModel.emit(ForeverEvent.ShowedReferralCodeSubmissionError) },
    openEditCodeBottomSheet = {
      viewModel.emit(ForeverEvent.OpenEditCodeBottomSheet)
    },
    showedReferralCodeSuccessfulChangeMessage = {
      viewModel.emit(ForeverEvent.ShowedReferralCodeSuccessfulChangeMessage)
    },
    closeEditCodeBottomSheet = {
      viewModel.emit(ForeverEvent.CloseEditCodeBottomSheet)
    },
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
  )
}

@Composable
private fun ForeverScreen(
  uiState: ForeverUiState,
  reload: () -> Unit,
  onSubmitCode: (String) -> Unit,
  showedReferralCodeSubmissionError: () -> Unit,
  openEditCodeBottomSheet: () -> Unit,
  closeEditCodeBottomSheet: () -> Unit,
  showedReferralCodeSuccessfulChangeMessage: () -> Unit,
  onShareCodeClick: (String, UiMoney) -> Unit,
) {
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = (uiState as? ForeverUiState.Success)?.reloading == true,
    onRefresh = reload,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )
  Column(Modifier.fillMaxSize()) {
    AnimatedContent(
      targetState = uiState,
      label = "forever_ui_state",
      transitionSpec = { fadeIn() togetherWith fadeOut() },
      contentKey = { uiState ->
        when (uiState) {
          ForeverUiState.Error -> "Error"
          Loading -> "Loading"
          is Success -> "Success"
        }
      },
    ) { uiStateAnimated ->
      when (uiStateAnimated) {
        ForeverUiState.Error -> HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier.fillMaxSize(),
        )
        Loading -> LoadingForeverContent()
        is Success -> {
          ForeverContent(
            uiState = uiStateAnimated,
            pullRefreshState = pullRefreshState,
            onShareCodeClick = onShareCodeClick,
            onSubmitCode = onSubmitCode,
            showedReferralCodeSubmissionError = showedReferralCodeSubmissionError,
            openEditCodeBottomSheet = openEditCodeBottomSheet,
            closeEditCodeBottomSheet = closeEditCodeBottomSheet,
            showedReferralCodeSuccessfulChangeMessage = showedReferralCodeSuccessfulChangeMessage,
          )
        }
      }
    }
  }
}

@Composable
internal fun LoadingForeverContent() {
  Column(
    Modifier
      .fillMaxSize()
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
    }
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = "-",
      textAlign = TextAlign.Center,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier
        .withoutPlacement()
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    DiscountPieChart(
      totalPrice = 0f,
      totalExistingDiscount = 0f,
      incentive = 0f,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .clip(CircleShape)
        .size(215.dp)
        .placeholder(
          visible = true,
          highlight = PlaceholderHighlight.shimmer(),
        ),
    )
    Spacer(Modifier.weight(1f))
  }
}

@Composable
internal fun ForeverContent(
  uiState: ForeverUiState.Success,
  pullRefreshState: PullRefreshState,
  onShareCodeClick: (code: String, incentive: UiMoney) -> Unit,
  onSubmitCode: (String) -> Unit,
  showedReferralCodeSubmissionError: () -> Unit,
  showedReferralCodeSuccessfulChangeMessage: () -> Unit,
  openEditCodeBottomSheet: () -> Unit,
  closeEditCodeBottomSheet: () -> Unit,
) {
  val initialTextValue = TextFieldValue(
    text = uiState.foreverData?.campaignCode ?: "",
    selection = TextRange((uiState.foreverData?.campaignCode ?: "").length),
  )
  var textFieldValue by remember(uiState.foreverData?.campaignCode) {
    mutableStateOf(initialTextValue)
  }

  LaunchedEffect(textFieldValue) {
    showedReferralCodeSubmissionError() // Clear error on new referral code input
  }
  LaunchedEffect(uiState.showEditReferralCodeBottomSheet) {
    if (!uiState.showEditReferralCodeBottomSheet) {
      delay(500) // to avoid extra animation effects on closing bottom sheet
      textFieldValue = initialTextValue
      showedReferralCodeSubmissionError()
    }
  }
  LaunchedEffect(uiState.showReferralCodeSuccessfullyChangedMessage) {
    if (uiState.showReferralCodeSuccessfullyChangedMessage) {
      delay(3000)
      showedReferralCodeSuccessfulChangeMessage()
    }
  }
  LaunchedEffect(Unit) {
    snapshotFlow { textFieldValue }.collectLatest {
      if (uiState.referralCodeErrorMessage != null) {
        showedReferralCodeSubmissionError()
      }
      // clear error after the member edits the code manually
    }
  }

  EditCodeBottomSheet(
    isVisible = uiState.showEditReferralCodeBottomSheet,
    code = textFieldValue,
    onCodeChanged = { textFieldValue = it },
    onDismiss = closeEditCodeBottomSheet,
    onSubmitCode = {
      onSubmitCode(textFieldValue.text.trim())
    },
    referralCodeUpdateError = uiState.referralCodeErrorMessage,
    showedReferralCodeSubmissionError = showedReferralCodeSubmissionError,
    isLoading = uiState.referralCodeLoading,
  )

  var showReferralExplanationBottomSheet by rememberSaveable { mutableStateOf(false) }

  ForeverExplanationBottomSheet(
    discount = uiState.foreverData?.incentive.toString(),
    onDismiss = { showReferralExplanationBottomSheet = false },
    isVisible = showReferralExplanationBottomSheet && uiState.foreverData?.incentive != null,
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
            onClick = {
              showReferralExplanationBottomSheet = true
            },
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
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(id = R.string.referrals_change_change_code),
          onClick = openEditCodeBottomSheet,
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
    PullRefreshIndicator(
      refreshing = uiState.reloading == true,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
    HedvigSnackbar(
      snackbarText = stringResource(R.string.referrals_change_code_changed),
      showSnackbar = uiState.showReferralCodeSuccessfullyChangedMessage,
      showedSnackbar = showedReferralCodeSuccessfulChangeMessage,
      priority = NotificationPriority.Info,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(16.dp),
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
      ForeverScreen(
        foreverUiState,
        onShareCodeClick = { _, _ -> },
        reload = {},
        onSubmitCode = {},
        showedReferralCodeSubmissionError = {},
        openEditCodeBottomSheet = {},
        closeEditCodeBottomSheet = {},
        showedReferralCodeSuccessfulChangeMessage = {},
      )
    }
  }
}

private class ForeverUiStateProvider : CollectionPreviewParameterProvider<ForeverUiState>(
  listOf(
    ForeverUiState.Error,
    ForeverUiState.Success(
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
      referralCodeLoading = false,
      referralCodeErrorMessage = null,
      showEditReferralCodeBottomSheet = false,
      reloading = false,
      showReferralCodeSuccessfullyChangedMessage = true,
    ),
    ForeverUiState.Loading,
  ),
)
