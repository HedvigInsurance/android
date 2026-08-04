package com.hedvig.android.feature.home.home.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Campaign
import com.hedvig.android.design.system.hedvig.icon.ChatNoCircle
import com.hedvig.android.design.system.hedvig.icon.Clock
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredChat
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredFirstVetNoCircle
import hedvig.resources.DASHBOARD_OPEN_CHAT
import hedvig.resources.HC_QUICK_ACTIONS_FIRSTVET_SUBTITLE
import hedvig.resources.Res
import hedvig.resources.home_tab_claim_button_text
import hedvig.resources.insurance_tab_cross_sells_title
import org.jetbrains.compose.resources.stringResource

// Diameter shared by every circular button in the home top app bar, and the glyph centered in it.
private val toolbarButtonSize = 48.dp
private val toolbarGlyphSize = 24.dp

/**
 * The circular surface every home top app bar button sits on: a [toolbarButtonSize] circle holding a
 * centered [toolbarGlyphSize] glyph.
 */
@Composable
private fun ToolbarIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, glyph: @Composable () -> Unit) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(toolbarButtonSize)
      .shadow(4.dp, CircleShape)
      .clip(CircleShape)
      .background(HedvigTheme.colorScheme.surfacePrimary)
      .clickable(role = Role.Button, onClick = onClick),
    content = { glyph() },
  )
}

@Composable
fun ToolbarChatIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
  ToolbarIconButton(onClick = onClick, modifier = modifier) {
    Icon(
      imageVector = HedvigIcons.ChatNoCircle,
      contentDescription = stringResource(Res.string.DASHBOARD_OPEN_CHAT),
      tint = HedvigTheme.colorScheme.fillPrimary,
      modifier = Modifier.size(toolbarGlyphSize),
    )
  }
}

@Composable
fun ToolbarFirstVetIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
  ToolbarIconButton(onClick = onClick, modifier = modifier) {
    Image(
      imageVector = HedvigIcons.ColoredFirstVetNoCircle,
      contentDescription = stringResource(Res.string.HC_QUICK_ACTIONS_FIRSTVET_SUBTITLE),
      modifier = Modifier.size(toolbarGlyphSize),
    )
  }
}

@Composable
fun ToolbarCrossSellsIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
  val isRotated by produceState(false) { value = true }
  val fullRotation by animateFloatAsState(
    targetValue = if (isRotated) 360f else 0f,
    animationSpec = tween(1500, 50),
  )
  ToolbarIconButton(
    onClick = onClick,
    // Spin the whole circular icon as one unit (outermost transform), so the rotation stays within
    // the circular clip and shadow.
    modifier = modifier.graphicsLayer { rotationZ = fullRotation },
  ) {
    Icon(
      imageVector = HedvigIcons.Campaign,
      contentDescription = stringResource(Res.string.insurance_tab_cross_sells_title),
      tint = HedvigTheme.colorScheme.signalGreenElement,
      modifier = Modifier.size(toolbarGlyphSize),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewToolbarChatIcon() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Row {
        ToolbarFirstVetIcon(onClick = {})
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarCrossSellsIcon({})
        Spacer(modifier = Modifier.width(8.dp))

        ToolbarChatIcon({})
      }
    }
  }
}
