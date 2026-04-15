package com.hedvig.android.feature.profile.legal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.core.icons.HedvigIcons
import com.hedvig.android.core.icons.hedvig.small.hedvig.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import hedvig.resources.LEGAL_A11Y
import hedvig.resources.LEGAL_CATEGORY_LABEL
import hedvig.resources.LEGAL_INFORMATION
import hedvig.resources.LEGAL_PRIVACY_POLICY
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LegalInfoDestination(
  openUrl: (String) -> Unit,
  navigateUp: () -> Unit,
  languageService: LanguageService,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(Res.string.LEGAL_CATEGORY_LABEL),
  ) {
    val linkContainer = LinkContainer(languageService)
    val horizontalDividerModifier = Modifier.horizontalDivider(DividerPosition.Bottom, horizontalPadding = 16.dp)
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
        .size(24.dp),
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
