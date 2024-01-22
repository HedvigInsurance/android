package com.hedvig.android.feature.login.marketing

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.flag.FlagDenmark
import com.hedvig.android.core.icons.hedvig.flag.FlagNorway
import com.hedvig.android.core.icons.hedvig.flag.FlagSweden
import com.hedvig.android.core.icons.hedvig.flag.FlagUk
import com.hedvig.android.core.icons.hedvig.logo.HedvigLogotype
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.feature.login.marketing.ui.LoginBackgroundImage
import com.hedvig.android.language.Language
import com.hedvig.android.market.Market
import hedvig.resources.R
import kotlin.math.max
import kotlinx.coroutines.launch

@Composable
internal fun MarketingDestination(
  viewModel: MarketingViewModel,
  appVersionName: String,
  openWebOnboarding: (Market) -> Unit,
  navigateToLoginScreen: (Market) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  MarketingScreen(
    uiState = uiState,
    appVersionName = appVersionName,
    selectMarket = { market -> viewModel.emit(MarketingEvent.SelectMarket(market)) },
    selectLanguage = { language -> viewModel.emit(MarketingEvent.SelectLanguage(language)) },
    openWebOnboarding = openWebOnboarding,
    navigateToLoginScreen = navigateToLoginScreen,
  )
}

@ExperimentalMaterial3Api
@Composable
private fun MarketingScreen(
  uiState: MarketingUiState,
  appVersionName: String,
  selectMarket: (Market) -> Unit,
  selectLanguage: (Language) -> Unit,
  openWebOnboarding: (Market) -> Unit,
  navigateToLoginScreen: (Market) -> Unit,
) {
  HedvigTheme(darkTheme = false) {
    var showPreferencesSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(true)
    val coroutineScope = rememberCoroutineScope()
    if (showPreferencesSheet && uiState is MarketingUiState.Success) {
      HedvigTheme {
        ModalBottomSheet(
          containerColor = MaterialTheme.colorScheme.background,
          onDismissRequest = { showPreferencesSheet = false },
          sheetState = sheetState,
          windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top),
        ) {
          Column(Modifier.verticalScroll(rememberScrollState())) {
            PreferencesSheetContent(
              chosenMarket = uiState.market,
              chosenLanguage = uiState.language,
              appVersionName = appVersionName,
              selectMarket = selectMarket,
              selectLanguage = selectLanguage,
              dismissSheet = {
                coroutineScope.launch {
                  sheetState.hide()
                }.invokeOnCompletion {
                  showPreferencesSheet = false
                }
              },
            )
          }
        }
      }
    }

    Box(Modifier.fillMaxSize()) {
      LoginBackgroundImage()
      Column(
        verticalArrangement = object : Arrangement.Vertical {
          override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
            val middlePoint = totalSize / 2
            val logoTypeSize = sizes[0]
            val buttonsSize = sizes[1]
            val buttonsTopYPosition = totalSize - buttonsSize
            outPositions[1] = buttonsTopYPosition

            val minSpaceBetween = 16.dp.roundToPx()
            val logoTypePreferredTopYPosition = middlePoint - (logoTypeSize / 2)
            val logoTypeCanBeCentered =
              logoTypePreferredTopYPosition + logoTypeSize <= buttonsTopYPosition - minSpaceBetween
            if (logoTypeCanBeCentered) {
              outPositions[0] = logoTypePreferredTopYPosition
            } else {
              val logoTypeYPositionJustAboveButtons = buttonsTopYPosition - logoTypeSize - minSpaceBetween
              outPositions[0] = max(0, logoTypeYPositionJustAboveButtons)
            }
          }
        },
        modifier = Modifier
          .matchParentSize()
          .windowInsetsPadding(WindowInsets.safeDrawing),
      ) {
        Image(
          Icons.Hedvig.HedvigLogotype,
          null,
          Modifier
            .align(Alignment.CenterHorizontally)
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        )
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .windowInsetsPadding(
              WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
        ) {
          HedvigContainedButton(
            text = stringResource(R.string.SETTINGS_LOGIN_ROW),
            enabled = uiState is MarketingUiState.Success,
            onClick = {
              (uiState as? MarketingUiState.Success)?.run {
                navigateToLoginScreen(market)
              }
            },
            modifier = Modifier.testTag("login_button"),
          )
          HedvigTextButton(
            text = stringResource(R.string.MARKETING_GET_HEDVIG),
            enabled = uiState is MarketingUiState.Success,
            onClick = {
              (uiState as? MarketingUiState.Success)?.run {
                openWebOnboarding(market)
              }
            },
          )
        }
      }
      if (uiState is MarketingUiState.Success) {
        IconButton(
          onClick = { showPreferencesSheet = true },
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
          val flagImageVector = when (uiState.market) {
            Market.SE -> Icons.Hedvig.FlagSweden
            Market.NO -> Icons.Hedvig.FlagNorway
            Market.DK -> Icons.Hedvig.FlagDenmark
          }
          Image(flagImageVector, null)
        }
      }
      if (uiState is MarketingUiState.Loading) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
      }
    }
  }
}

@Suppress("UnusedReceiverParameter")
@ExperimentalFoundationApi
@Composable
private fun ColumnScope.PreferencesSheetContent(
  chosenMarket: Market,
  chosenLanguage: Language,
  appVersionName: String,
  selectMarket: (Market) -> Unit,
  selectLanguage: (Language) -> Unit,
  dismissSheet: () -> Unit,
) {
  val pagerState = rememberPagerState { 2 }
  Text(
    text = stringResource(R.string.LOGIN_MARKET_PICKER_PREFERENCES),
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(24.dp))
  PreferencesPagerSelector(pagerState)
  Spacer(Modifier.height(16.dp))
  HorizontalPager(
    state = pagerState,
    contentPadding = PaddingValues(horizontal = 16.dp),
    beyondBoundsPageCount = 1,
    pageSpacing = 32.dp,
    key = { it },
  ) { pageIndex ->
    if (pageIndex == 0) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (market in Market.entries) {
          PreferenceSelectableRow(
            displayName = stringResource(market.label),
            imageVector = market.flag(),
            isSelected = market == chosenMarket,
            onSelected = { selectMarket(market) },
          )
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (language in chosenMarket.availableLanguages) {
          PreferenceSelectableRow(
            displayName = stringResource(language.label),
            imageVector = language.flag(),
            isSelected = language == chosenLanguage,
            onSelected = { selectLanguage(language) },
          )
        }
      }
    }
  }
  Spacer(Modifier.height(8.dp))
  HedvigContainedButton(
    text = stringResource(R.string.general_done_button),
    onClick = dismissSheet,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
  Text(
    text = "${stringResource(R.string.PROFILE_ABOUT_APP_VERSION)}: $appVersionName",
    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
  Spacer(Modifier.windowInsetsBottomHeight(BottomSheetDefaults.windowInsets))
}

@Composable
private fun PreferencesPagerSelector(pagerState: PagerState) {
  val couroutineScope = rememberCoroutineScope()
  TabRow(
    selectedTabIndex = pagerState.currentPage,
    containerColor = MaterialTheme.colorScheme.background,
  ) {
    Tab(
      selected = pagerState.currentPage == 0,
      onClick = { couroutineScope.launch { pagerState.animateScrollToPage(0) } },
      text = {
        Text(text = stringResource(R.string.market_picker_modal_title), style = MaterialTheme.typography.bodyMedium)
      },
    )
    Tab(
      selected = pagerState.currentPage == 1,
      onClick = { couroutineScope.launch { pagerState.animateScrollToPage(1) } },
      text = {
        Text(text = stringResource(R.string.language_picker_modal_title), style = MaterialTheme.typography.bodyMedium)
      },
    )
  }
}

@Composable
private fun PreferenceSelectableRow(
  displayName: String,
  imageVector: ImageVector,
  isSelected: Boolean,
  onSelected: () -> Unit,
) {
  HedvigCard(
    onClick = { onSelected() },
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    ) {
      Image(imageVector, null)
      Text(
        text = displayName,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.weight(1f),
      )
      SelectIndicationCircle(isSelected)
    }
  }
}

private fun Market.flag(): ImageVector {
  return when (this) {
    Market.SE -> Icons.Hedvig.FlagSweden
    Market.NO -> Icons.Hedvig.FlagNorway
    Market.DK -> Icons.Hedvig.FlagDenmark
  }
}

private fun Language.flag(): ImageVector {
  return when (this) {
    Language.SV_SE -> Icons.Hedvig.FlagSweden
    Language.EN_SE -> Icons.Hedvig.FlagUk
    Language.NB_NO -> Icons.Hedvig.FlagNorway
    Language.EN_NO -> Icons.Hedvig.FlagUk
    Language.DA_DK -> Icons.Hedvig.FlagDenmark
    Language.EN_DK -> Icons.Hedvig.FlagUk
  }
}

@Preview
@Composable
private fun PreviewMarketingScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      MarketingScreen(MarketingUiState.Success(Market.SE, Language.EN_SE), "X.Y.Z", {}, {}, {}, {})
    }
  }
}

@Preview
@Composable
private fun PreviewPreferencesSheetContent() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        PreferencesSheetContent(Market.SE, Language.EN_SE, "X.Y.Z", {}, {}, {})
      }
    }
  }
}
