package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.hedvig.android.design.system.hedvig.tokens.IconButtonTokens

@Composable
fun IconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable () -> Unit,
) {
  Box(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .size(IconButtonTokens.StateLayerSize)
      .clip(IconButtonTokens.StateLayerShape.value)
      .background(color = colors.containerColor(enabled))
      .clickable(
        onClick = onClick,
        enabled = enabled,
        role = Role.Button,
        interactionSource = interactionSource,
        indication = rememberRipple(
          bounded = false,
          radius = IconButtonTokens.StateLayerSize / 2,
        ),
      ),
    contentAlignment = Alignment.Center,
  ) {
    val contentColor = colors.contentColor(enabled)
    CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
  }
}

object IconButtonDefaults {
  @Composable
  fun iconButtonColors(): IconButtonColors {
    val contentColor = LocalContentColor.current
    val colors = IconButtonColors(
      containerColor = Color.Transparent,
      contentColor = contentColor,
      disabledContainerColor = Color.Transparent,
      disabledContentColor = contentColor.copy(alpha = IconButtonTokens.DisabledIconOpacity),
    )
    if (colors.contentColor == contentColor) {
      return colors
    } else {
      return colors.copy(
        contentColor = contentColor,
        disabledContentColor = contentColor.copy(alpha = IconButtonTokens.DisabledIconOpacity),
      )
    }
  }
}

@Immutable
data class IconButtonColors(
  val containerColor: Color,
  val contentColor: Color,
  val disabledContainerColor: Color,
  val disabledContentColor: Color,
) {
  @Stable
  internal fun containerColor(enabled: Boolean): Color = if (enabled) containerColor else disabledContainerColor

  @Stable
  internal fun contentColor(enabled: Boolean): Color = if (enabled) contentColor else disabledContentColor
}
