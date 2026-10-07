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
import com.hedvig.android.feature.chat.data.InChatCrossSell
import hedvig.resources.ADDON_FLOW_SEE_PRICE_BUTTON
import hedvig.resources.GENERAL_NO_THANKS
import hedvig.resources.ONBOARDING_CROSS_SELL_TITLE
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InChatCrossSellCard(
  crossSell: InChatCrossSell,
  onDismissClick: (String) -> Unit,
  onSeePriceClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigNotificationCard(
    content = {
      // A campaign card keeps its light green fill in dark mode, which is why the design system forces
      // a light theme on its buttons. Its text has to come from the light scheme for the same reason.
      HedvigTheme(darkTheme = false) {
        Column {
          HedvigText(text = crossSell.title,
            color = HedvigTheme.colorScheme.textBlackTranslucent,
            style = HedvigTheme.typography.label)
          HedvigText(
            text = crossSell.description,
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
          )
        }
      }
    },
    priority = Campaign,
    style = InfoCardStyle.Buttons(
      leftButtonText = stringResource(Res.string.GENERAL_NO_THANKS),
      rightButtonText = crossSell.buttonTitle,
      onLeftButtonClick = {
        onDismissClick(crossSell.storeUrl)
      },
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
        crossSell = InChatCrossSell(
          title = "Få rabatt",
          description = "Aktivera din rabatt genom att teckna en till försäkring för hem, djur eller bil.",
          buttonTitle = "Se ditt pris",
          storeUrl = "",
        ),
        onDismissClick = {},
        onSeePriceClick = {},
      )
    }
  }
}
