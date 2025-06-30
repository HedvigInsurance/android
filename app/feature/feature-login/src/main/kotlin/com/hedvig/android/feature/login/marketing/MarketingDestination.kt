package com.hedvig.android.feature.login.marketing

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
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
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HedvigLogotype
import com.hedvig.android.design.system.hedvig.icon.flag.FlagSweden
import com.hedvig.android.design.system.hedvig.icon.flag.FlagUk
import com.hedvig.android.feature.login.marketing.ui.LoginBackgroundImage
import com.hedvig.android.language.Language
import com.hedvig.android.language.label
import hedvig.resources.R

@Composable
internal fun MarketingDestination(
  viewModel: MarketingViewModel,
  appVersionName: String,
  openWebOnboarding: () -> Unit,
  navigateToLoginScreen: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  MarketingScreen(
    uiState = uiState,
    appVersionName = appVersionName,
    selectLanguage = { language -> viewModel.emit(MarketingEvent.SelectLanguage(language)) },
    openWebOnboarding = openWebOnboarding,
    navigateToLoginScreen = navigateToLoginScreen,
  )
}

@Composable
private fun MarketingScreen(
  uiState: MarketingUiState,
  appVersionName: String,
  selectLanguage: (Language) -> Unit,
  openWebOnboarding: () -> Unit,
  navigateToLoginScreen: () -> Unit,
) {
  var showPreferencesSheet by rememberSaveable { mutableStateOf(false) }
  HedvigBottomSheet(
    isVisible = (showPreferencesSheet && uiState is MarketingUiState.Success),
    onVisibleChange = { showPreferencesSheet = it },
    contentPadding = PaddingValues(0.dp),
  ) {
    if (uiState is MarketingUiState.Success) {
      PreferencesSheetContent(
        chosenLanguage = uiState.language,
        appVersionName = appVersionName,
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
                navigateToLoginScreen()
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_button"),
          )
          val linkRoleDescription = stringResource(R.string.TALKBACK_OPEN_EXTERNAL_LINK)
          HedvigTextButton(
            text = stringResource(R.string.MARKETING_GET_HEDVIG),
            onClickLabel = linkRoleDescription,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState is MarketingUiState.Success,
            buttonSize = Large,
            onClick = {
              (uiState as? MarketingUiState.Success)?.run {
                openWebOnboarding()
              }
            },
          )
        }
      }
      if (uiState is MarketingUiState.Success) {
        val description = stringResource(R.string.market_language_screen_choose_language_label)
        IconButton(
          onClick = { showPreferencesSheet = true },
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
          Image(HedvigIcons.FlagSweden, description)
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
  chosenLanguage: Language,
  appVersionName: String,
  selectLanguage: (Language) -> Unit,
  dismissSheet: () -> Unit,
) {
  HedvigText(
    text = stringResource(R.string.SETTINGS_LANGUAGE_TITLE),
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(24.dp))
  RadioGroup(
    radioGroupStyle = RadioGroupDefaults.RadioGroupStyle.Vertical.Icon(
      dataList = Language.entries.map { language ->
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
  Spacer(Modifier.height(8.dp))
  Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
}

private fun Language.flag(): ImageVector {
  return when (this) {
    Language.SV_SE -> HedvigIcons.FlagSweden
    Language.EN_SE -> HedvigIcons.FlagUk
  }
}

@Preview
@Composable
private fun PreviewMarketingScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      MarketingScreen(MarketingUiState.Success(Language.EN_SE), "X.Y.Z", {}, {}, {})
    }
  }
}

@Preview
@Composable
private fun PreviewPreferencesSheetContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column {
        PreferencesSheetContent(Language.EN_SE, "X.Y.Z", {}, {})
      }
    }
  }
}
