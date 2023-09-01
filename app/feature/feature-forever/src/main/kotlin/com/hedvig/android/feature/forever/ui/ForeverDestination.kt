package com.hedvig.android.feature.forever.ui

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.apollo.format
import com.hedvig.android.apollo.toWebLocaleTag
import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Copy
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.data.forever.toErrorMessage
import com.hedvig.android.feature.forever.ForeverUiState
import com.hedvig.android.feature.forever.ForeverViewModel
import com.hedvig.android.feature.forever.copyToClipboard
import com.hedvig.android.feature.forever.showShareSheet
import com.hedvig.android.language.LanguageService
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.PullRefreshState
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import hedvig.resources.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.javamoney.moneta.Money
import javax.money.MonetaryAmount

@Composable
internal fun ForeverDestination(
  viewModel: ForeverViewModel,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ForeverScreen(
    uiState = uiState,
    reload = viewModel::reload,
    onSubmitCode = viewModel::onSubmitCode,
    languageService = languageService,
    hedvigBuildConstants = hedvigBuildConstants,
  )
}

@Composable
private fun ForeverScreen(
  uiState: ForeverUiState,
  reload: () -> Unit,
  onSubmitCode: (String) -> Unit,
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
    refreshing = uiState.isLoading,
    onRefresh = reload,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )
  Box(Modifier.fillMaxSize()) {
    val transition = updateTransition(targetState = uiState, label = "home ui state")
    transition.AnimatedContent(
      modifier = Modifier.fillMaxSize(),
      contentKey = { it.errorMessage },
    ) { uiState ->
      if (uiState.errorMessage != null) {
        Column {
          Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
          HedvigErrorSection(retry = reload)
        }
      } else {
        val shareSheetTitle = stringResource(R.string.REFERRALS_SHARE_SHEET_TITLE)
        ForeverContent(
          uiState = uiState,
          pullRefreshState = pullRefreshState,
          onShareCodeClick = { code, incentive ->
            context.showShareSheet(shareSheetTitle) { intent ->
              intent.putExtra(
                Intent.EXTRA_TEXT,
                resources.getString(
                  R.string.REFERRAL_SMS_MESSAGE,
                  incentive.format(languageService.getLocale()),
                  buildString {
                    append(hedvigBuildConstants.urlBaseWeb)
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
          onSubmitCode = onSubmitCode,
        )
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

@Composable
internal fun ForeverContent(
  uiState: ForeverUiState,
  pullRefreshState: PullRefreshState,
  onShareCodeClick: (code: String, incentive: MonetaryAmount) -> Unit,
  onSubmitCode: (String) -> Unit,
) {
  val locale = getLocale()

  val editSheetState = rememberModalBottomSheetState(true)
  val referralExplanationSheetState = rememberModalBottomSheetState(true)
  val coroutineScope = rememberCoroutineScope()
  var showEditBottomSheet by rememberSaveable { mutableStateOf(false) }
  var showReferralExplanationBottomSheet by rememberSaveable { mutableStateOf(false) }
  val focusRequester = remember { FocusRequester() }
  var textFieldValueState by remember(uiState.campaignCode) {
    mutableStateOf(TextFieldValue(text = uiState.campaignCode ?: ""))
  }

  LaunchedEffect(uiState.showEditCode) {
    if (uiState.showEditCode) {
      editSheetState.expand()
    } else {
      editSheetState.hide()
    }
    showEditBottomSheet = uiState.showEditCode
  }

  if (showEditBottomSheet) {
    EditCodeBottomSheet(
      sheetState = editSheetState,
      code = textFieldValueState,
      onCodeChanged = { textFieldValueState = it },
      onDismiss = {
        coroutineScope.launch {
          editSheetState.hide()
          showEditBottomSheet = false
        }
      },
      onSubmitCode = { onSubmitCode(textFieldValueState.text) },
      errorText = uiState.codeError.toErrorMessage(),
      isLoading = uiState.isLoadingCode,
      focusRequester = focusRequester,
    )
  }

  if (showReferralExplanationBottomSheet && uiState.incentive != null) {
    ForeverExplanationBottomSheet(
      discount = uiState.incentive.format(locale),
      onDismiss = {
        coroutineScope.launch {
          referralExplanationSheetState.hide()
          showReferralExplanationBottomSheet = false
        }
      },
      sheetState = referralExplanationSheetState,
    )
  }

  Column(
    Modifier
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
        style = MaterialTheme.typography.titleLarge,
      )
      if (uiState.incentive != null && uiState.referralUrl != null) {
        IconButton(
          onClick = { showReferralExplanationBottomSheet = true },
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
    Spacer(Modifier.height(16.dp))
    Text(
      text = uiState.currentDiscountAmount?.format(locale) ?: "-",
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    DiscountPieChart(
      totalPrice = uiState.grossPriceAmount?.abs()?.number?.toFloat() ?: 0f,
      totalExistingDiscount = uiState.currentDiscountAmount?.abs()?.number?.toFloat() ?: 0f,
      incentive = uiState.incentive?.abs()?.number?.toFloat() ?: 0f,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.height(24.dp))
    if (uiState.referrals.isEmpty() && uiState.incentive != null) {
      Text(
        text = stringResource(
          id = R.string.referrals_empty_body,
          uiState.incentive.format(locale),
          Money.of(0, uiState.incentive.currency?.currencyCode).format(locale),
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
          uiState.currentNetAmount?.format(locale) ?: "-",
        ),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))

    ReferralCodeContent(
      uiState = uiState,
      onChangeCodeClicked = {
        coroutineScope.launch {
          editSheetState.hide()
          showEditBottomSheet = true
          delay(400)
          focusRequester.requestFocus()
          textFieldValueState = textFieldValueState.copy(selection = TextRange(textFieldValueState.text.length))
        }
      },
      onShareCodeClick = onShareCodeClick,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    if (uiState.referrals.isNotEmpty()) {
      Spacer(Modifier.height(8.dp))
      ReferralList(uiState, Modifier.padding(horizontal = 16.dp))
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
internal fun ReferralCodeContent(
  uiState: ForeverUiState,
  onChangeCodeClicked: () -> Unit,
  onShareCodeClick: (code: String, incentive: MonetaryAmount) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  Column(modifier) {
    HedvigBigCard(
      onClick = {
        uiState.campaignCode?.let {
          context.copyToClipboard(uiState.campaignCode)
        }
      },
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
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

    if (uiState.incentive != null && uiState.campaignCode != null) {
      Spacer(Modifier.height(16.dp))
      HedvigContainedButton(
        text = stringResource(R.string.referrals_empty_share_code_button),
        onClick = { onShareCodeClick(uiState.campaignCode, uiState.incentive) },
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(id = R.string.referrals_change_change_code),
        onClick = { onChangeCodeClicked() },
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewForeverContent() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ForeverContent(
        ForeverUiState(
          referrals = listOf(
            ForeverUiState.Referral("Name#1", ForeverUiState.ReferralState.ACTIVE, null),
            ForeverUiState.Referral("Name#2", ForeverUiState.ReferralState.IN_PROGRESS, null),
            ForeverUiState.Referral("Name#3", ForeverUiState.ReferralState.TERMINATED, null),
          ),
        ),
        rememberPullRefreshState(false, {}),
        { _, _ -> },
        {},
      )
    }
  }
}
