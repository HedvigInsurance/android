package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DemoModeLabel(buttonText: String, onButtonClick: () -> Unit, modifier: Modifier = Modifier) {
  HedvigButton(
    modifier = modifier,
    text = buttonText,
    onClick = onButtonClick,
    buttonStyle = ButtonDefaults.ButtonStyle.Red,
    buttonSize = ButtonDefaults.ButtonSize.Mini,
    enabled = true,
  )
}

@HedvigPreview
@Composable
private fun PreviewHedvigDialogContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DemoModeLabel(
        "Exit demo mode",
        {},
      )
    }
  }
}
