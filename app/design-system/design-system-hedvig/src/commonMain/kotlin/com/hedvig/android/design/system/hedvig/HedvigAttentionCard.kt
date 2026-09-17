package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.WarningFilled

/**
 * Presents something the member has to resolve: what is wrong, what that means for them, and the
 * single action that fixes it. The leading icon is always the red warning, as the card is reserved
 * for states that interrupt the member's cover until they act.
 *
 * @param subtitle The consequence or status, read as a continuation of [title].
 */
@Composable
fun HedvigAttentionCard(
  title: String,
  subtitle: String,
  body: String,
  buttonText: String,
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    color = HedvigTheme.colorScheme.fillNegative,
    // fillNegative matches the surface this sits on in both themes, and the drop shadow that would
    // otherwise separate them is light-only, so the outline is what gives the card an edge in dark mode.
    borderColor = HedvigTheme.colorScheme.borderPrimary,
    modifier = modifier
      .fillMaxWidth()
      .hedvigDropShadow(HedvigTheme.shapes.cornerXLarge),
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.padding(16.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(width = 40.dp, height = 33.dp)
              .background(HedvigTheme.colorScheme.signalRedFill, CircleShape),
          ) {
            Icon(
              imageVector = HedvigIcons.WarningFilled,
              contentDescription = null,
              tint = HedvigTheme.colorScheme.signalRedElement,
              modifier = Modifier.size(24.dp),
            )
          }
          Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
          ) {
            HedvigText(
              text = title,
              style = HedvigTheme.typography.label,
              color = HedvigTheme.colorScheme.textPrimary,
            )
            HedvigText(
              text = subtitle,
              style = HedvigTheme.typography.label,
              color = HedvigTheme.colorScheme.textSecondary,
            )
          }
        }
        HedvigText(
          text = body,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      HedvigButton(
        text = buttonText,
        onClick = onButtonClick,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Primary,
        buttonSize = ButtonDefaults.ButtonSize.Small,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigAttentionCard() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HedvigAttentionCard(
        title = "Missing payment method",
        subtitle = "Requires action",
        body = "For your insurance to stay active, you need to connect a payment method",
        buttonText = "Connect payment",
        onButtonClick = {},
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}
