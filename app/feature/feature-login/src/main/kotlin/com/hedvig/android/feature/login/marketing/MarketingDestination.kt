package com.hedvig.android.feature.login.marketing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigTabRowMaxSixTabs
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.IconResource
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TabDefaults
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HedvigLogotype
import com.hedvig.android.design.system.hedvig.icon.flag.FlagDenmark
import com.hedvig.android.design.system.hedvig.icon.flag.FlagNorway
import com.hedvig.android.design.system.hedvig.icon.flag.FlagSweden
import com.hedvig.android.design.system.hedvig.icon.flag.FlagUk
import com.hedvig.android.feature.login.marketing.ui.LoginBackgroundImage
import com.hedvig.android.language.Language
import com.hedvig.android.market.Market
import hedvig.resources.R

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

@Composable
private fun MarketingScreen(
  uiState: MarketingUiState,
  appVersionName: String,
  selectMarket: (Market) -> Unit,
  selectLanguage: (Language) -> Unit,
  openWebOnboarding: (Market) -> Unit,
  navigateToLoginScreen: (Market) -> Unit,
) {
  var showPreferencesSheet by rememberSaveable { mutableStateOf(false) }
  HedvigBottomSheet(
    isVisible = (showPreferencesSheet && uiState is MarketingUiState.Success),
    onVisibleChange = { showPreferencesSheet = it },
    sheetPadding = PaddingValues(0.dp),
    contentPadding = PaddingValues(0.dp),
  ) {
    if (uiState is MarketingUiState.Success) {
      PreferencesSheetContent(
        chosenMarket = uiState.market,
        chosenLanguage = uiState.language,
        appVersionName = appVersionName,
        selectMarket = selectMarket,
        selectLanguage = selectLanguage,
        dismissSheet = { showPreferencesSheet = false },
      )
    }
  }
  HedvigTheme(darkTheme = false) {
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
              outPositions[0] = logoTypeYPositionJustAboveButtons.coerceAtLeast(0)
            }
          }
        },
        modifier = Modifier
          .matchParentSize()
          .windowInsetsPadding(WindowInsets.safeDrawing),
      ) {
        Image(
          HedvigIcons.HedvigLogotype,
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
          HedvigButton(
            text = stringResource(R.string.SETTINGS_LOGIN_ROW),
            enabled = uiState is MarketingUiState.Success,
            onClick = {
              (uiState as? MarketingUiState.Success)?.run {
                navigateToLoginScreen(market)
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_button"),
          )
          HedvigTextButton(
            text = stringResource(R.string.MARKETING_GET_HEDVIG),
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState is MarketingUiState.Success,
            buttonSize = Large,
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
            Market.SE -> HedvigIcons.FlagSweden
            Market.NO -> HedvigIcons.FlagNorway
            Market.DK -> HedvigIcons.FlagDenmark
          }
          Image(flagImageVector, null)
        }
      }
      if (uiState is MarketingUiState.Loading) {
        HedvigCircularProgressIndicator(Modifier.align(Alignment.Center))
      }
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.PreferencesSheetContent(
  chosenMarket: Market,
  chosenLanguage: Language,
  appVersionName: String,
  selectMarket: (Market) -> Unit,
  selectLanguage: (Language) -> Unit,
  dismissSheet: () -> Unit,
) {
  var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
  HedvigText(
    text = stringResource(R.string.LOGIN_MARKET_PICKER_PREFERENCES),
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(24.dp))
  PreferencesPagerSelector(selectedTabIndex, { selectedTabIndex = it }, Modifier.padding(horizontal = 16.dp))
  Spacer(Modifier.height(16.dp))
  AnimatedContent(
    targetState = selectedTabIndex,
    transitionSpec = {
      val spec = tween<IntOffset>(durationMillis = 600, easing = FastOutSlowInEasing)
      if (initialState == 0) {
        slideIntoContainer(SlideDirection.Start, spec) togetherWith slideOutOfContainer(SlideDirection.Start, spec)
      } else {
        slideIntoContainer(SlideDirection.End, spec) togetherWith slideOutOfContainer(SlideDirection.End, spec)
      }
    },
  ) { index ->
    if (index == 0) {
      RadioGroup(
        radioGroupStyle = RadioGroupDefaults.RadioGroupStyle.Vertical.Icon(
          dataList = Market.entries.map { market ->
            RadioOptionGroupData.RadioOptionGroupDataWithIcon(
              RadioOptionData(
                id = market.name,
                optionText = stringResource(market.label),
                chosenState = if (market == chosenMarket) ChosenState.Chosen else ChosenState.NotChosen,
              ),
              IconResource.Vector(market.flag()),
            )
          },
        ),
        onOptionClick = { selectMarket(Market.valueOf(it)) },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    } else {
      RadioGroup(
        radioGroupStyle = RadioGroupDefaults.RadioGroupStyle.Vertical.Icon(
          dataList = chosenMarket.availableLanguages.map { language ->
            RadioOptionGroupData.RadioOptionGroupDataWithIcon(
              RadioOptionData(
                id = language.name,
                optionText = stringResource(language.label),
                chosenState = if (language == chosenLanguage) ChosenState.Chosen else ChosenState.NotChosen,
              ),
              IconResource.Vector(language.flag()),
            )
          },
        ),
        onOptionClick = { selectLanguage(Language.valueOf(it)) },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
  Spacer(Modifier.height(8.dp))
  HedvigButton(
    text = stringResource(R.string.general_done_button),
    onClick = dismissSheet,
    enabled = true,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
  HedvigText(
    text = "${stringResource(R.string.PROFILE_ABOUT_APP_VERSION)}: $appVersionName",
    style = HedvigTheme.typography.finePrint.copy(color = HedvigTheme.colorScheme.textSecondary),
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
  Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
}

@Composable
private fun PreferencesPagerSelector(
  selectedTabIndex: Int,
  selectTabIndex: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigTabRowMaxSixTabs(
    tabTitles = listOf(
      stringResource(R.string.market_picker_modal_title),
      stringResource(R.string.language_picker_modal_title),
    ),
    tabStyle = TabDefaults.TabStyle.Filled,
    selectedTabIndex = selectedTabIndex,
    onTabChosen = { selectTabIndex(it) },
    modifier = modifier,
  )
}

private fun Market.flag(): ImageVector {
  return when (this) {
    Market.SE -> HedvigIcons.FlagSweden
    Market.NO -> HedvigIcons.FlagNorway
    Market.DK -> HedvigIcons.FlagDenmark
  }
}

private fun Language.flag(): ImageVector {
  return when (this) {
    Language.SV_SE -> HedvigIcons.FlagSweden
    Language.EN_SE -> HedvigIcons.FlagUk
    Language.NB_NO -> HedvigIcons.FlagNorway
    Language.EN_NO -> HedvigIcons.FlagUk
    Language.DA_DK -> HedvigIcons.FlagDenmark
    Language.EN_DK -> HedvigIcons.FlagUk
  }
}

@Preview
@Composable
private fun PreviewMarketingScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      MarketingScreen(MarketingUiState.Success(Market.SE, Language.EN_SE), "X.Y.Z", {}, {}, {}, {})
    }
  }
}

@Preview
@Composable
private fun PreviewPreferencesSheetContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column {
        PreferencesSheetContent(Market.SE, Language.EN_SE, "X.Y.Z", {}, {}, {})
      }
    }
  }
}
