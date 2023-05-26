package com.hedvig.android.core.ui.appbar.m3

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import hedvig.resources.R

@Composable
fun ToolbarChatIcon(
  onClick: () -> Unit,
) {
  Surface(
    shape = CircleShape,
    border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.onSurfaceVariant),
  ) {
    IconButton(
      onClick = onClick,
      colors = IconButtonDefaults.iconButtonColors(),
      modifier = Modifier.size(40.dp),
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_chat),
        contentDescription = stringResource(R.string.DASHBOARD_OPEN_CHAT),
        modifier = Modifier.size(24.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewToolbarChatIcon() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ToolbarChatIcon({})
    }
  }
}
