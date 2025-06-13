package com.hedvig.android.feature.home.home.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Chat
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredCampaign
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredFirstVet
import hedvig.resources.R

@Composable
fun ToolbarChatIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Icon(
    imageVector = HedvigIcons.Chat,
    contentDescription = stringResource(R.string.DASHBOARD_OPEN_CHAT),
    tint = com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme.signalGreyElement,
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

@HedvigPreview
@Composable
private fun PreviewToolbarChatIcon() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
