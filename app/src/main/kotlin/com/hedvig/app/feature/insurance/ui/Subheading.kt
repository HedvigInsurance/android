package com.hedvig.app.feature.insurance.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun Subheading(
  text: String,
) {
  Text(
    text = text,
    style = MaterialTheme.typography.h6,
    modifier = Modifier
      .fillMaxWidth()
      .padding(
        start = 16.dp,
        top = 48.dp,
        end = 16.dp,
        bottom = 8.dp,
      ),
  )
}

@HedvigPreview
@Composable
private fun PreviewSubheading() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      Subheading("Add more coverage")
    }
  }
}
