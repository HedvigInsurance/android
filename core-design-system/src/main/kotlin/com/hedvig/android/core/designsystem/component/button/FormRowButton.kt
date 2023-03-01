package com.hedvig.android.core.designsystem.component.button

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun FormRowButton(mainText: String, secondaryText: String, onClick: () -> Unit) {
  LargeContainedButton(
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.onSurface,
    ),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(text = mainText, maxLines = 1)
      Text(text = secondaryText, maxLines = 1)
    }
  }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewFormRowButton() {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
    ) {
      FormRowButton("Date of Incident", "2023-03-14", {})
    }
  }
}
