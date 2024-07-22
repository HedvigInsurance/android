package com.hedvig.android.core.ui.appbar.m3

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
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
import com.hedvig.android.core.icons.hedvig.colored.hedvig.ColoredCircleWithCampaign
import com.hedvig.android.core.icons.hedvig.colored.hedvig.FirstVet
import hedvig.resources.R

@Composable
fun ToolbarChatIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Icon(
    imageVector = Icons.Hedvig.Chat,
    contentDescription = stringResource(R.string.DASHBOARD_OPEN_CHAT),
    tint = com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme.fillSecondary,
    modifier = modifier
      .size(40.dp)
      .shadow(4.dp, CircleShape)
      .clip(CircleShape)
      .clickable(onClick = onClick),
  )
}

@Composable
fun ToolbarFirstVetIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Image(
    imageVector = Icons.Hedvig.FirstVet,
    contentDescription = stringResource(R.string.HC_QUICK_ACTIONS_FIRSTVET_TITLE),
    modifier = modifier
      .size(40.dp)
      .shadow(4.dp, CircleShape)
      .clip(CircleShape)
      .clickable(onClick = onClick),
  )
}

@Composable
fun ToolbarCrossSellsIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Image(
    imageVector = Icons.Hedvig.ColoredCircleWithCampaign,
    contentDescription = stringResource(R.string.insurance_tab_cross_sells_title),
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
      Row {
        ToolbarCrossSellsIcon({})
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarFirstVetIcon(onClick = {})
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarChatIcon({})
      }
    }
  }
}
