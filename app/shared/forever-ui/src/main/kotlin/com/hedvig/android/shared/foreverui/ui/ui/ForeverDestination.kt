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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Copy
import com.hedvig.android.core.icons.hedvig.normal.Info
import com.hedvig.android.core.ui.snackbar.HedvigSnackbar
import com.hedvig.android.core.uidata.UiMoney
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

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

@ExperimentalMaterial3Api
@Composable
internal fun ForeverContent(
  uiState: ForeverUiState,
  pullRefreshState: PullRefreshState,
  onShareCodeClick: (code: String, incentive: UiMoney) -> Unit,
  onSubmitCode: (String) -> Unit,
  showedReferralCodeSubmissionError: () -> Unit,
  showedCampaignCodeSuccessfulChangeMessage: () -> Unit,
) {
  val coroutineScope = rememberCoroutineScope()
  var textFieldValueState by remember(uiState.foreverData?.campaignCode) {
    mutableStateOf(TextFieldValue(text = uiState.foreverData?.campaignCode ?: ""))
  }

  LaunchedEffect(Unit) {
    snapshotFlow { textFieldValueState }.collectLatest {
      showedReferralCodeSubmissionError() // clear error after the member edits the code manually
    }
  }

  val editReferralCodeBottomSheetState = rememberModalBottomSheetState(true)
  var showEditReferralCodeBottomSheet by rememberSaveable { mutableStateOf(false) }
  val dismissEditReferralCodeBottomSheetState: CoroutineScope.() -> Unit = {
    launch {
      editReferralCodeBottomSheetState.hide()
    }.invokeOnCompletion {
      showEditReferralCodeBottomSheet = false
    }
  }

  LaunchedEffect(Unit) {
    // Sheet does not dismiss on click outside. So we clear the state ourselves when it gets hidden.
    snapshotFlow { editReferralCodeBottomSheetState.currentValue }
      .filter { it == SheetValue.Hidden }
      .drop(1)
      .collect {
        coroutineScope { dismissEditReferralCodeBottomSheetState() }
      }
  }

  LaunchedEffect(uiState.showReferralCodeSuccessfullyChangedMessage) {
    if (!uiState.showReferralCodeSuccessfullyChangedMessage) return@LaunchedEffect
    // Hide the bottom sheet if we've successfully changed the code
    dismissEditReferralCodeBottomSheetState()
  }

  if (showEditReferralCodeBottomSheet) {
    EditCodeBottomSheet(
      sheetState = editReferralCodeBottomSheetState,
      code = textFieldValueState,
      onCodeChanged = { textFieldValueState = it },
      onDismiss = {
        showedReferralCodeSubmissionError()
        coroutineScope.dismissEditReferralCodeBottomSheetState()
      },
      onSubmitCode = {
        onSubmitCode(textFieldValueState.text)
      },
      referralCodeUpdateError = uiState.referralCodeErrorMessage,
      showedReferralCodeSubmissionError = showedReferralCodeSubmissionError,
      isLoading = uiState.referralCodeLoading,
    )
  }

  val referralExplanationSheetState = rememberModalBottomSheetState(true)
  var showReferralExplanationBottomSheet by rememberSaveable { mutableStateOf(false) }
  if (showReferralExplanationBottomSheet && uiState.foreverData?.incentive != null) {
    ForeverExplanationBottomSheet(
      discount = uiState.foreverData.incentive.toString(),
      onDismiss = {
        coroutineScope.launch {
          referralExplanationSheetState.hide()
        }.invokeOnCompletion {
          showReferralExplanationBottomSheet = false
        }
      },
      sheetState = referralExplanationSheetState,
    )
  }

  LaunchedEffect(textFieldValueState) {
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
        Text(
          text = stringResource(R.string.TAB_REFERRALS_TITLE),
          style = com.hedvig.android.design.system.hedvig.HedvigTheme.typography.headlineSmall,
        )
        if (uiState.foreverData?.incentive != null) {
          IconButton(
            onClick = { showReferralExplanationBottomSheet = true },
            modifier = Modifier.size(40.dp),
          ) {
            Icon(
              imageVector = Icons.Hedvig.Info,
              contentDescription = stringResource(R.string.REFERRALS_INFO_BUTTON_CONTENT_DESCRIPTION),
              modifier = Modifier.size(24.dp),
            )
          }
        }
      }
      Spacer(Modifier.height(16.dp))
      Text(
        text = uiState.foreverData?.currentDiscount?.toString()?.let { "-$it" } ?: "-",
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Text(
          text = stringResource(
            id = R.string.referrals_empty_body,
            uiState.foreverData.incentive.toString(),
          ),
          style = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center,
            lineBreak = LineBreak.Heading,
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      } else {
        Text(
          text = stringResource(id = R.string.FOREVER_TAB_MONTLY_COST_LABEL),
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Text(
          text = stringResource(
            id = R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
            uiState.foreverData?.currentNetCost?.toString() ?: "-",
          ),
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        HedvigContainedButton(
          text = stringResource(R.string.referrals_empty_share_code_button),
          onClick = {
            onShareCodeClick(
              uiState.foreverData.campaignCode,
              uiState.foreverData.incentive,
            )
          },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(id = R.string.referrals_change_change_code),
          onClick = {
            coroutineScope.launch {
              showEditReferralCodeBottomSheet = true
              textFieldValueState = textFieldValueState.copy(
                selection = TextRange(textFieldValueState.text.length),
              )
            }
          },
          modifier = Modifier.padding(horizontal = 16.dp),
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
        Text(
          text = stringResource(id = R.string.referrals_empty_code_headline),
          style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
        Text(
          text = campaignCode,
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

@HedvigPreview
@Composable
private fun PreviewForeverContent(
  @PreviewParameter(ForeverUiStateProvider::class) foreverUiState: ForeverUiState,
) {
  com.hedvig.android.core.designsystem.theme.HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
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
