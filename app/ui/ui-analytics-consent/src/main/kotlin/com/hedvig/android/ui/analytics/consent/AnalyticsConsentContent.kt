package com.hedvig.android.ui.analytics.consent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import hedvig.resources.LEGAL_PRIVACY_POLICY_APP_SHORT
import hedvig.resources.ONBOARDING_ANALYTICS_ALLOW_BUTTON
import hedvig.resources.ONBOARDING_ANALYTICS_DENY_BUTTON
import hedvig.resources.ONBOARDING_ANALYTICS_SUBTITLE
import hedvig.resources.ONBOARDING_ANALYTICS_TITLE
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

/**
 * The body of an analytics-consent screen, from the explanation down to the Allow and Don't allow
 * buttons. It fills the remaining height of the column, so the caller only adds its own chrome around
 * it.
 */
@Composable
fun ColumnScope.AnalyticsConsentContent(
  badge: ConsentBadge?,
  buttonsEnabled: Boolean,
  onBadgeSettled: (badge: ConsentBadge?) -> Unit,
  onAllow: () -> Unit,
  onDeny: () -> Unit,
  onPrivacyPolicy: () -> Unit,
) {
  Column(Modifier.padding(horizontal = 16.dp)) {
    HedvigText(text = stringResource(Res.string.ONBOARDING_ANALYTICS_TITLE))
    Spacer(Modifier.height(4.dp))
    HedvigText(
      text = stringResource(Res.string.ONBOARDING_ANALYTICS_SUBTITLE),
      color = HedvigTheme.colorScheme.textSecondary,
    )
  }
  Spacer(Modifier.weight(1f))
  Spacer(Modifier.height(24.dp))
  AnalyticsConsentCard(
    badge = badge,
    onBadgeSettled = onBadgeSettled,
    modifier = Modifier.align(Alignment.CenterHorizontally),
  )
  Spacer(Modifier.weight(1f))
  Spacer(Modifier.height(24.dp))
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .align(Alignment.CenterHorizontally)
      .clip(CircleShape)
      .clickable(onClick = onPrivacyPolicy)
      .padding(horizontal = 8.dp, vertical = 4.dp),
  ) {
    HedvigText(
      text = stringResource(Res.string.LEGAL_PRIVACY_POLICY_APP_SHORT),
      style = HedvigTheme.typography.label,
      textDecoration = TextDecoration.Underline,
    )
    Icon(
      imageVector = HedvigIcons.ArrowNorthEast,
      contentDescription = null,
      modifier = Modifier.size(18.dp),
    )
  }
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(Res.string.ONBOARDING_ANALYTICS_ALLOW_BUTTON),
    onClick = onAllow,
    enabled = buttonsEnabled,
    buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .clip(CircleShape),
  )
  Spacer(Modifier.height(8.dp))
  HedvigButton(
    text = stringResource(Res.string.ONBOARDING_ANALYTICS_DENY_BUTTON),
    onClick = onDeny,
    enabled = buttonsEnabled,
    buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .clip(CircleShape),
  )
  Spacer(Modifier.height(16.dp))
}

@HedvigPreview
@Composable
private fun PreviewAnalyticsConsentContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(Modifier.fillMaxSize()) {
        AnalyticsConsentContent(
          badge = ConsentBadge.Accepted,
          buttonsEnabled = true,
          onBadgeSettled = {},
          onAllow = {},
          onDeny = {},
          onPrivacyPolicy = {},
        )
      }
    }
  }
}
