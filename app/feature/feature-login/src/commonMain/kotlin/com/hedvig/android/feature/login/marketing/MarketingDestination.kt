package com.hedvig.android.feature.login.marketing

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigToggle
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.IconResource
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDefaultStyleSize
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HedvigLogotype
import com.hedvig.android.design.system.hedvig.icon.flag.FlagSweden
import com.hedvig.android.design.system.hedvig.icon.flag.FlagUk
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.feature.login.marketing.ui.LoginBackgroundVideo
import com.hedvig.android.language.Language
import hedvig.resources.MARKETING_GET_HEDVIG
import hedvig.resources.PROFILE_ABOUT_APP_VERSION
import hedvig.resources.Res
import hedvig.resources.SETTINGS_LANGUAGE_TITLE
import hedvig.resources.SETTINGS_LOGIN_ROW
import hedvig.resources.TALKBACK_OPEN_EXTERNAL_LINK
import hedvig.resources.general_done_button
import hedvig.resources.market_language_screen_choose_language_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MarketingDestination(
  viewModel: MarketingViewModel,
  appVersionName: String,
  openWebOnboarding: () -> Unit,
  navigateToLoginScreen: () -> Unit,
  publicImportantMessages: @Composable (reloadKey: Any?, modifier: Modifier) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  MarketingScreen(
    uiState = uiState,
    appVersionName = appVersionName,
    selectLanguage = { language -> viewModel.emit(MarketingEvent.SelectLanguage(language)) },
    setBankIdSandboxEnabled = { enabled -> viewModel.emit(MarketingEvent.SetBankIdSandboxEnabled(enabled)) },
    openWebOnboarding = openWebOnboarding,
    navigateToLoginScreen = navigateToLoginScreen,
    publicImportantMessages = publicImportantMessages,
  )
}

@Composable
internal fun MarketingScreen(
  uiState: MarketingUiState,
  appVersionName: String,
  selectLanguage: (Language) -> Unit,
  setBankIdSandboxEnabled: (Boolean) -> Unit,
  openWebOnboarding: () -> Unit,
  navigateToLoginScreen: () -> Unit,
  publicImportantMessages: @Composable (reloadKey: Any?, modifier: Modifier) -> Unit,
) {
  val preferencesSheetState = rememberHedvigBottomSheetState<Unit>()
  if (uiState is MarketingUiState.Success) {
    HedvigBottomSheet(
      preferencesSheetState,
      contentPadding = PaddingValues(0.dp),
    ) {
      PreferencesSheetContent(
        chosenLanguage = uiState.language,
        appVersionName = appVersionName,
        selectLanguage = selectLanguage,
        dismissSheet = preferencesSheetState::dismiss,
      )
    }
  }
  HedvigTheme(darkTheme = false) {
    Box(Modifier.fillMaxSize()) {
      LoginBackgroundVideo()
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
            .height(40.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        )
        Column(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .windowInsetsPadding(
              WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
        ) {
          // Measured after the buttons, so the cards get only the height left over and scroll within it.
          val cardsScrollState = rememberScrollState()
          Column(
            Modifier
              .weight(1f, fill = false)
              .fadeTowardScrollableEdges(cardsScrollState)
              .verticalScroll(cardsScrollState),
          ) {
            if (uiState is MarketingUiState.Success) {
              publicImportantMessages(uiState.language, Modifier.padding(top = 16.dp, bottom = 8.dp))
            }
          }
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (uiState is MarketingUiState.Success && uiState.isBankIdSandboxAvailable) {
              HedvigToggle(
                labelText = "BankID sandbox",
                turnedOn = uiState.isBankIdSandboxEnabled,
                onClick = setBankIdSandboxEnabled,
                enabled = true,
                toggleStyle = ToggleStyle.Default(ToggleDefaultStyleSize.Small),
                modifier = Modifier.fillMaxWidth(),
              )
            }
            HedvigButton(
              text = stringResource(Res.string.SETTINGS_LOGIN_ROW),
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
            val linkRoleDescription = stringResource(Res.string.TALKBACK_OPEN_EXTERNAL_LINK)
            HedvigTextButton(
              text = stringResource(Res.string.MARKETING_GET_HEDVIG),
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
      }
      if (uiState is MarketingUiState.Success) {
        val description = stringResource(Res.string.market_language_screen_choose_language_label)
        IconButton(
          onClick = preferencesSheetState::show,
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

/**
 * Fades the content out toward each edge that has more of it beyond, so a cut-off card reads as scrollable. Drawn over
 * any background, as it fades the content itself.
 */
private fun Modifier.fadeTowardScrollableEdges(scrollState: ScrollState): Modifier = this
  .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
  .drawWithContent {
    drawContent()
    val fadeLength = 32.dp.toPx().coerceAtMost(size.height / 2)
    if (scrollState.canScrollBackward) {
      drawRect(
        brush = Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black, endY = fadeLength),
        blendMode = BlendMode.DstIn,
      )
    }
    if (scrollState.canScrollForward) {
      drawRect(
        brush = Brush.verticalGradient(
          0f to Color.Black,
          1f to Color.Transparent,
          startY = size.height - fadeLength,
          endY = size.height,
        ),
        blendMode = BlendMode.DstIn,
      )
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
    text = stringResource(Res.string.SETTINGS_LANGUAGE_TITLE),
    textAlign = TextAlign.Center,
    modifier = Modifier
      .semantics { heading() }
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(24.dp))
  RadioGroup(
    options = Language.entries.map { language ->
      RadioOption(
        id = RadioOptionId(language.name),
        text = stringResource(language.label),
        iconResource = IconResource.Vector(language.flag()),
      )
    },
    selectedOption = RadioOptionId(chosenLanguage.name),
    onRadioOptionSelected = { selectLanguage(Language.valueOf(it.id)) },
    size = RadioGroupSize.Large,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(8.dp))
  HedvigButton(
    text = stringResource(Res.string.general_done_button),
    onClick = dismissSheet,
    enabled = true,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
  HedvigText(
    text = "${stringResource(Res.string.PROFILE_ABOUT_APP_VERSION)}: $appVersionName",
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
      MarketingScreen(
        MarketingUiState.Success(Language.EN_SE, isBankIdSandboxAvailable = true, isBankIdSandboxEnabled = false),
        "X.Y.Z",
        {},
        {},
        {},
        {},
        { _, _ -> },
      )
    }
  }
}

@Preview
@Composable
private fun PreviewMarketingScreenWithImportantMessages() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      MarketingScreen(
        MarketingUiState.Success(Language.EN_SE, isBankIdSandboxAvailable = false, isBankIdSandboxEnabled = false),
        "X.Y.Z",
        {},
        {},
        {},
        {},
        { _, modifier ->
          Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HedvigNotificationCard(
              message = "Logging in with BankID is slower than usual. We are working on it.",
              priority = NotificationPriority.Error,
              modifier = Modifier.fillMaxWidth(),
            )
            HedvigNotificationCard(
              message = "Our phone lines are closed today.",
              priority = NotificationPriority.Info,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        },
      )
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
