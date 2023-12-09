package com.hedvig.android.core.ui.appbar.m3

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.colored.hedvig.Chat
import hedvig.resources.R

@Composable
fun ToolbarChatIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Image(
    imageVector = Icons.Hedvig.Chat,
    contentDescription = stringResource(R.string.DASHBOARD_OPEN_CHAT),
    modifier = modifier
      .size(40.dp)
      .shadow(4.dp, CircleShape)
      .clip(CircleShape)
      .clickable(onClick = onClick),
  )
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
