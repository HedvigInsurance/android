package com.hedvig.android.feature.profile.aboutapp

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.icons.HedvigIcons
import com.hedvig.android.core.icons.hedvig.small.hedvig.ArrowNorthEast
import com.hedvig.android.core.locale.CommonLocale
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigAlertDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.withHedvigLink
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import hedvig.resources.HC_CHAT_BUTTON
import hedvig.resources.LEGAL_A11Y
import hedvig.resources.LEGAL_INFORMATION
import hedvig.resources.LEGAL_PRIVACY_POLICY
import hedvig.resources.PROFILE_ABOUT_APP_LICENSE_ATTRIBUTIONS
import hedvig.resources.PROFILE_ABOUT_APP_MEMBER_ID
import hedvig.resources.PROFILE_ABOUT_APP_TITLE
import hedvig.resources.PROFILE_ABOUT_APP_VERSION
import hedvig.resources.PROFILE_INFO_LABEL
import hedvig.resources.Res
import hedvig.resources.app_info_submit_bug_button
import hedvig.resources.app_info_submit_bug_continue
import hedvig.resources.app_info_submit_bug_go_back
import hedvig.resources.app_info_submit_bug_prefilled_letter_body
import hedvig.resources.app_info_submit_bug_prefilled_letter_subject
import hedvig.resources.app_info_submit_bug_warning_with_chat_link_1
import hedvig.resources.app_info_submit_bug_warning_with_chat_link_2
import hedvig.resources.app_info_submit_bug_warning_with_chat_link_3
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InformationDestination(
  viewModel: AboutAppViewModel,
  onBackPressed: () -> Unit,
  showOpenSourceLicenses: () -> Unit,
  navigateToNewConversation: () -> Unit,
  hedvigBuildConstants: HedvigBuildConstants,
  languageService: LanguageService,
  openUrl: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  InformationScreen(
    uiState = uiState,
    onBackPressed = onBackPressed,
    showOpenSourceLicenses = showOpenSourceLicenses,
    isProduction = hedvigBuildConstants.isProduction,
    appVersionName = hedvigBuildConstants.appVersionName,
    appVersionCode = hedvigBuildConstants.appVersionCode,
    navigateToNewConversation = navigateToNewConversation,
    openUrl = openUrl,
    languageService = languageService,
  )
}

@Composable
private fun InformationScreen(
  uiState: AboutAppUiState,
  isProduction: Boolean,
  appVersionName: String,
  appVersionCode: String,
  onBackPressed: () -> Unit,
  showOpenSourceLicenses: () -> Unit,
  navigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  languageService: LanguageService,
) {
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.PROFILE_INFO_LABEL),
    navigateUp = onBackPressed,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    when (uiState) {
      AboutAppUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced(
          Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      is AboutAppUiState.Content -> {
        InformationContent(
          memberId = uiState.memberId,
          deviceId = uiState.deviceId,
          showOpenSourceLicenses = showOpenSourceLicenses,
          isProduction = isProduction,
          appVersionName = appVersionName,
          appVersionCode = appVersionCode,
          navigateToNewConversation = navigateToNewConversation,
          openUrl = openUrl,
          languageService = languageService,
        )
      }
    }
  }
}

@Composable
private fun ColumnScope.InformationContent(
  memberId: String?,
  deviceId: String?,
  showOpenSourceLicenses: () -> Unit,
  isProduction: Boolean,
  appVersionName: String,
  appVersionCode: String,
  navigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  languageService: LanguageService,
) {
  Spacer(Modifier.height(16.dp))
  LegalInfoSection(
    openUrl = openUrl,
    languageService = languageService,
  )
  Spacer(Modifier.height(24.dp))
  var showSubmitBugWarning by remember { mutableStateOf(false) }
  if (showSubmitBugWarning) {
    SubmitBugWarningDialog(
      memberId = memberId,
      deviceId = deviceId,
      appVersionName = appVersionName,
      navigateToNewConversation = navigateToNewConversation,
      onDismissRequest = {
        showSubmitBugWarning = false
      },
    )
  }
  HighlightLabel(
    stringResource(Res.string.PROFILE_ABOUT_APP_TITLE),
    size = HighlightLabelDefaults.HighLightSize.Medium,
    color = HighlightLabelDefaults.HighlightColor.Blue(
      HighlightLabelDefaults.HighlightShade.LIGHT,
    ),
    modifier = Modifier.padding(horizontal = 16.dp),
  )
  if (memberId != null) {
    HorizontalItemsWithMaximumSpaceTaken(
      spaceBetween = 8.dp,
      modifier = horizontalDividerModifier.then(
        Modifier
          .fillMaxWidth()
          .padding(16.dp),
      ),
      startSlot = {
        HedvigText(stringResource(Res.string.PROFILE_ABOUT_APP_MEMBER_ID))
      },
      endSlot = {
        HedvigText(
          memberId,
          color = HedvigTheme.colorScheme.textSecondary,
          textAlign = TextAlign.End,
        )
      },
    )
  }
  HorizontalItemsWithMaximumSpaceTaken(
    modifier = horizontalDividerModifier.then(
      Modifier
        .fillMaxWidth()
        .padding(16.dp),
    ),
    startSlot = {
      HedvigText(stringResource(Res.string.PROFILE_ABOUT_APP_VERSION))
    },
    endSlot = {
      HedvigText(
        text = buildString {
          append(appVersionName)
          if (!isProduction) {
            append(" (")
            append(appVersionCode)
            append(")")
          }
        },
        color = HedvigTheme.colorScheme.textSecondary,
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth(),
      )
    },
    spaceBetween = 8.dp,
  )
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = showOpenSourceLicenses)
      .padding(16.dp),
  ) {
    HedvigText(stringResource(Res.string.PROFILE_ABOUT_APP_LICENSE_ATTRIBUTIONS))
  }
  Spacer(Modifier.height(16.dp))
  Spacer(Modifier.weight(1f))
  HedvigButton(
    text = stringResource(Res.string.app_info_submit_bug_button),
    enabled = true,
    buttonStyle = Secondary,
    onClick = { showSubmitBugWarning = true },
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
}

@Composable
private fun SubmitBugWarningDialog(
  memberId: String?,
  deviceId: String?,
  appVersionName: String,
  navigateToNewConversation: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  val localContext = LocalContext.current
  val letterSubject = stringResource(Res.string.app_info_submit_bug_prefilled_letter_subject)
  val letterBody = String.format(
    stringResource(Res.string.app_info_submit_bug_prefilled_letter_body),
    memberId,
    deviceId,
    appVersionName,
    "Android ${Build.VERSION.SDK_INT}",
  )
  HedvigAlertDialog(
    title = buildAnnotatedString {
      append(stringResource(Res.string.app_info_submit_bug_warning_with_chat_link_1))
      append(" ")
      withHedvigLink(
        tag = stringResource(Res.string.HC_CHAT_BUTTON),
        onClick = navigateToNewConversation,
      ) {
        append(stringResource(Res.string.app_info_submit_bug_warning_with_chat_link_2))
      }
      append(" ")
      append(stringResource(Res.string.app_info_submit_bug_warning_with_chat_link_3))
    },
    text = null,
    onDismissRequest = onDismissRequest,
    onConfirmClick = {
      openEmailClientWithPrefilledData(
        localContext,
        "android@hedvig.com",
        letterSubject,
        letterBody,
      )
    },
    confirmButtonLabel = stringResource(Res.string.app_info_submit_bug_continue),
    dismissButtonLabel = stringResource(Res.string.app_info_submit_bug_go_back),
  )
}

private fun openEmailClientWithPrefilledData(
  context: Context,
  email: String,
  letterSubject: String,
  letterBody: String,
) {
  val sendLetterIntent: Intent = Intent().apply {
    action = Intent.ACTION_SENDTO
    data = "mailto:".toUri()
    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    putExtra(Intent.EXTRA_SUBJECT, letterSubject)
    putExtra(Intent.EXTRA_TEXT, letterBody)
  }
  context.startActivity(
    Intent.createChooser(
      sendLetterIntent,
      letterSubject,
    ),
  )
}

private val horizontalDividerModifier = Modifier.horizontalDivider(
  DividerPosition.Bottom, horizontalPadding = 16.dp,
)

@Composable
internal fun LegalInfoSection(
  openUrl: (String) -> Unit,
  languageService: LanguageService,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    val linkContainer = LinkContainer(languageService)

    LinkRow(
      modifier = horizontalDividerModifier,
      text = stringResource(Res.string.LEGAL_PRIVACY_POLICY),
      link = linkContainer.getPrivacyPolicyLink(),
      onLinkClick = openUrl,
    )
    LinkRow(
      modifier = horizontalDividerModifier,
      text = stringResource(Res.string.LEGAL_INFORMATION),
      link = linkContainer.getLegalInfoLink(),
      onLinkClick = openUrl,
    )
    LinkRow(
      text = stringResource(Res.string.LEGAL_A11Y),
      link = linkContainer.getA11yLink(),
      onLinkClick = openUrl,
    )
  }
}

private class LinkContainer(
  val languageService: LanguageService,
) {
  private val privacyPolicyLinkEn = "https://www.hedvig.com/se-en/hedvig/privacy-policy"
  private val privacyPolicyLinkSe = "https://www.hedvig.com/se/hedvig/personuppgifter"
  fun getPrivacyPolicyLink(): String {
    return when (languageService.getLanguage()) {
      Language.SV_SE -> privacyPolicyLinkSe
      Language.EN_SE -> privacyPolicyLinkEn
    }
  }

  private val legalInfoLinkEn = "https://www.hedvig.com/se-en/hedvig/legal"
  private val legalInfoLinkSe = "https://www.hedvig.com/se/hedvig/legal"
  fun getLegalInfoLink(): String {
    return when (languageService.getLanguage()) {
      Language.SV_SE -> legalInfoLinkSe
      Language.EN_SE -> legalInfoLinkEn
    }
  }

  private val a11yLinkEn = "https://www.hedvig.com/se-en/help/accessibility"
  private val a11yLinkSe = "https://www.hedvig.com/se/hjalp/tillganglighet"
  fun getA11yLink(): String {
    return when (languageService.getLanguage()) {
      Language.SV_SE -> a11yLinkSe
      Language.EN_SE -> a11yLinkEn
    }
  }
}


@Composable
private fun LinkRow(
  link: String,
  text: String,
  onLinkClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .clickable(
        onClick = {
          onLinkClick(link)
        },
        enabled = true,
      )
      .padding(16.dp)
      .semantics(mergeDescendants = true) {},
  ) {
    HedvigText(
      text = text,
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.width(8.dp))
    Icon(
      imageVector = HedvigIcons.ArrowNorthEast,
      contentDescription = EmptyContentDescription,
      modifier = Modifier
        .size(16.dp),
    )
  }
}


@HedvigPreview
@Composable
private fun PreviewLinkRow() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      LinkRow(
        link = "",
        text = "Legal information",
        onLinkClick = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInformationScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InformationScreen(
        uiState = AboutAppUiState.Content(memberId = "123", deviceId = "123456"),
        onBackPressed = {},
        showOpenSourceLicenses = {},
        isProduction = false,
        appVersionName = "11.3.2",
        appVersionCode = "43",
        navigateToNewConversation = {},
        openUrl = {},
        languageService = previewLanguageService,
      )
    }
  }
}

private val previewLanguageService = object : LanguageService {
  override fun setLanguage(language: Language) {}
  override fun getSelectedLanguage(): Language {
    return Language.EN_SE
  }
  override fun getLanguage(): Language {
    return Language.EN_SE
  }
  override fun getLocale(): CommonLocale {
    return CommonLocale.getDefault()
  }
}
