package com.hedvig.app.ui.compose.composables.banner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvig_black
import com.hedvig.android.core.designsystem.theme.hedvig_black12percent
import com.hedvig.app.R

@Composable
fun InfoBanner(
  onClick: () -> Unit,
  text: String,
  modifier: Modifier = Modifier,
) {
  val backgroundColor = if (isSystemInDarkTheme()) {
    colorResource(R.color.lavender_400)
  } else {
    colorResource(R.color.lavender_200)
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() },
    color = backgroundColor,
    border = BorderStroke(1.dp, hedvig_black12percent),
  ) {
    Text(
      modifier = Modifier.padding(16.dp),
      text = text,
      color = hedvig_black.copy(alpha = 0.5f),
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewInfoBanner() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      InfoBanner(onClick = { }, text = "Test info banner text")
    }
  }
}
