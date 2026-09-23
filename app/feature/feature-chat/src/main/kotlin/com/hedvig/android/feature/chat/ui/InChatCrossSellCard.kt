package com.hedvig.android.feature.chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewFontScale
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Campaign
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.ADDON_FLOW_SEE_PRICE_BUTTON
import hedvig.resources.ONBOARDING_CROSS_SELL_TITLE
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InChatCrossSellCard(
  discountPercent: Int?,
  onDismissClick: () -> Unit,
  onSeePriceClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigNotificationCard(
    content = {
      // A campaign card keeps its light green fill in dark mode, which is why the design system forces
      // a light theme on its buttons. Its text has to come from the light scheme for the same reason.
      HedvigTheme(darkTheme = false) {
        Column {
          val title = if (discountPercent != null) {
            // TODO: Add "Get %d%% bundle discount" / "Få %d%% samlingsrabatt" to Lokalise
            "Get $discountPercent% bundle discount"
          } else {
            stringResource(Res.string.ONBOARDING_CROSS_SELL_TITLE)
          }
          HedvigText(text = title, color = HedvigTheme.colorScheme.textBlackTranslucent)
          HedvigText(
            // TODO: Add "Activate your discount by taking out one more insurance for home, pet or car."
            //  / "Aktivera din rabatt genom att teckna en till försäkring för hem, djur eller bil." to Lokalise
            text = "Activate your discount by taking out one more insurance for home, pet or car.",
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
          )
        }
      }
    },
    priority = Campaign,
    style = InfoCardStyle.Buttons(
      // TODO: Add "No thanks" / "Nej tack" to Lokalise
      leftButtonText = "No thanks",
      rightButtonText = stringResource(Res.string.ADDON_FLOW_SEE_PRICE_BUTTON),
      onLeftButtonClick = onDismissClick,
      onRightButtonClick = onSeePriceClick,
    ),
    modifier = modifier,
  )
}

@HedvigPreview
@PreviewFontScale
@Composable
private fun PreviewInChatCrossSellCard() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InChatCrossSellCard(
        discountPercent = 15,
        onDismissClick = {},
        onSeePriceClick = {},
      )
    }
  }
}
