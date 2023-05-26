package com.hedvig.app.feature.crossselling.ui.detail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.R

@Composable
fun ClickableListItem(
  onClick: () -> Unit,
  @DrawableRes icon: Int,
  text: String,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clickable(onClick = onClick)
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = null,
      modifier = Modifier.size(24.dp),
    )
    Spacer(Modifier.size(16.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewClickableListItem() {
  HedvigTheme {
    Surface {
      ClickableListItem(
        onClick = {},
        icon = R.drawable.ic_info,
        text = "Full coverage",
      )
    }
  }
}
