package com.hedvig.android.ui.claimflow

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.icon.WarningFilled

@Composable
fun WarningTextWithIcon(text: String, modifier: Modifier = Modifier) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = HedvigIcons.InfoFilled,
      contentDescription = null,
      tint = HedvigTheme.colorScheme.signalAmberElement,
      modifier = Modifier.size(16.dp),
    )
    Spacer(Modifier.width(8.dp))
    HedvigText(text)
  }
}

@Composable
fun WarningTextWithIconForInput(text: String, modifier: Modifier = Modifier) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = HedvigIcons.WarningFilled,
      contentDescription = null,
      tint = HedvigTheme.colorScheme.signalAmberElement,
      modifier = Modifier.size(16.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    HedvigText(
      text = text,
      color = HedvigTheme.colorScheme.signalAmberElement,
      style = HedvigTheme.typography.finePrint,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewWarningTextWithIcon() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      WarningTextWithIcon("Text")
    }
  }
}
