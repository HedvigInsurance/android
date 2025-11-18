package com.hedvig.android.feature.home.home.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Chat
import com.hedvig.android.design.system.hedvig.icon.Clock
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredCampaign
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredChat
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredFirstVet
import hedvig.resources.R

@Composable
fun ToolbarChatIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Icon(
    imageVector = HedvigIcons.Chat,
    contentDescription = stringResource(R.string.DASHBOARD_OPEN_CHAT),
    tint = HedvigTheme.colorScheme.signalGreyElement,
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
    imageVector = HedvigIcons.ColoredFirstVet,
    contentDescription = stringResource(R.string.HC_QUICK_ACTIONS_FIRSTVET_SUBTITLE),
    modifier = modifier
      .size(40.dp)
      .shadow(4.dp, CircleShape)
      .clip(CircleShape)
      .clickable(onClick = onClick),
  )
}

@Composable
fun ToolbarCrossSellsIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
  val isRotated by produceState(false) { value = true }
  val fullRotation by animateFloatAsState(
    targetValue = if (isRotated) 360f else 0f,
    animationSpec = tween(1500, 50),
  )
  Image(
    imageVector = HedvigIcons.ColoredCampaign,
    contentDescription = stringResource(R.string.insurance_tab_cross_sells_title),
    modifier = modifier
      .size(40.dp)
      .shadow(4.dp, CircleShape)
      .clip(CircleShape)
      .clickable(onClick = onClick)
      .graphicsLayer {
        rotationZ = fullRotation
      },
  )
}

@Composable
fun ToolbarClaimChatIcon(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isDev: Boolean = false) {
  Box{
    Icon(
      imageVector = HedvigIcons.ColoredChat,
      contentDescription = stringResource(R.string.home_tab_claim_button_text),
      tint = Color.Unspecified,
      modifier = modifier
        .size(40.dp)
        .shadow(4.dp, CircleShape)
        .clip(CircleShape)
        .clickable(onClick = onClick),
    )
    if (isDev) {
      HedvigText(
        "dev",
        style = HedvigTheme.typography.label,
        modifier = Modifier.align(Alignment.Center))
    }
  }

}

@HedvigPreview
@Composable
private fun PreviewToolbarChatIcon() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Row {
        ToolbarClaimChatIcon({}, isDev = true)
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarClaimChatIcon({})
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarCrossSellsIcon({})
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarFirstVetIcon(onClick = {})
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarChatIcon({})
      }
    }
  }
}
