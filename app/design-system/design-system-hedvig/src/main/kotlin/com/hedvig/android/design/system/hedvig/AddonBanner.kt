package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FeatureAddonBanner(
  buttonText: String,
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val containerColor = HedvigTheme.colorScheme.fillNegative
  val borderColor = HedvigTheme.colorScheme.borderPrimary
  Surface(
    modifier = modifier,
    shape = HedvigTheme.shapes.cornerLarge,
    color = containerColor,
    border = borderColor,
  ) {
    Row(Modifier.padding()) {
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        text = buttonText,
        enabled = true,
        onClick = onButtonClick,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        buttonSize = ButtonDefaults.ButtonSize.Small,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

