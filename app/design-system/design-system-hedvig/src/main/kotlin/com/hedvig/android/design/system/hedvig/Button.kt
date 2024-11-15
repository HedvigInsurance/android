package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.tokens.GhostStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.MiniSizeButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.PrimaryAltStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.PrimaryStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.SecondaryAltStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.SecondaryStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeButtonTokens

@Composable
fun HedvigButton(
  text: String,
  onClick: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  buttonStyle: ButtonDefaults.ButtonStyle = ButtonDefaults.buttonStyle,
  buttonSize: ButtonDefaults.ButtonSize = ButtonDefaults.buttonSize,
  interactionSource: MutableInteractionSource? = null,
  isLoading: Boolean = false,
) {
  HedvigButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    buttonStyle = buttonStyle,
    buttonSize = buttonSize,
    interactionSource = interactionSource,
  ) {
    val buttonColors = buttonStyle.style.buttonColors
    val loadingTransition = updateTransition(isLoading, label = "loading transition")
    loadingTransition.AnimatedContent(
      transitionSpec = {
        fadeIn(tween(durationMillis = 220, delayMillis = 90)) togetherWith fadeOut(tween(90))
      },
      contentAlignment = Alignment.Center,
    ) { loading ->
      if (loading) {
        LayoutWithoutPlacement(
          sizeAdjustingContent = { HedvigText(text = text, modifier = Modifier.withoutPlacement()) },
        ) {
          ThreeDotsLoading(
            stableColor = buttonColors.activeLoadingIndicatorColor,
            temporaryColor = buttonColors.inactiveLoadingIndicatorColor,
            modifier = Modifier.wrapContentSize(Alignment.Center),
          )
        }
      } else {
        HedvigText(text = text)
      }
    }
  }
}

@Composable
fun HedvigButton(
  onClick: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  buttonStyle: ButtonDefaults.ButtonStyle = ButtonDefaults.buttonStyle,
  buttonSize: ButtonDefaults.ButtonSize = ButtonDefaults.buttonSize,
  interactionSource: MutableInteractionSource? = null,
  content: @Composable RowScope.() -> Unit,
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  val buttonColors = buttonStyle.style.buttonColors
  val containerColor = buttonColors.containerColor(enabled)
  val contentColor = buttonColors.contentColor(enabled)
  val isHovered by interactionSource.collectIsHoveredAsState()
  val hoverColor = buttonColors.hoverColor(isHovered)
  val color by animateColorAsState(
    if (isHovered) {
      hoverColor.compositeOver(containerColor)
    } else {
      containerColor
    },
  )
  Surface(
    onClick = onClick,
    modifier = modifier.semantics { role = Role.Button },
    enabled = enabled,
    shape = buttonSize.size.shape,
    color = color,
    contentColor = contentColor,
    interactionSource = interactionSource,
  ) {
    ProvideTextStyle(buttonSize.size.textStyle) {
      Row(
        modifier = Modifier.padding(buttonSize.size.contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
      )
    }
  }
}

@Composable
fun HedvigTextButton(
  text: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  interactionSource: MutableInteractionSource? = null,
  buttonSize: ButtonSize,
  onClick: () -> Unit,
) {
  HedvigButton(
    text = text,
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
    buttonSize = buttonSize,
    interactionSource = interactionSource,
  )
}

@Composable
fun HedvigRedTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  interactionSource: MutableInteractionSource? = null,
) {
  HedvigButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
    buttonSize = ButtonDefaults.ButtonSize.Large,
    interactionSource = interactionSource,
  ) {
    HedvigText(text, color = ButtonDefaults.ButtonStyle.Ghost.style.buttonColors.redTextColor)
  }
}

object ButtonDefaults {
  internal val buttonStyle: ButtonStyle = ButtonStyle.Primary
  internal val buttonSize: ButtonSize = ButtonSize.Large

  enum class ButtonStyle {
    Primary,
    PrimaryAlt,
    Secondary,
    SecondaryAlt,
    Ghost,
  }

  enum class ButtonSize {
    Large,
    Medium,
    Small,
    Mini,
  }
}

private val ButtonDefaults.ButtonStyle.style: Style
  get() = when (this) {
    ButtonDefaults.ButtonStyle.Primary -> Style.Primary
    ButtonDefaults.ButtonStyle.PrimaryAlt -> Style.PrimaryAlt
    ButtonDefaults.ButtonStyle.Secondary -> Style.Secondary
    ButtonDefaults.ButtonStyle.SecondaryAlt -> Style.SecondaryAlt
    ButtonDefaults.ButtonStyle.Ghost -> Style.Ghost
  }

private val ButtonDefaults.ButtonSize.size: Size
  get() = when (this) {
    ButtonDefaults.ButtonSize.Large -> Size.Large
    ButtonDefaults.ButtonSize.Medium -> Size.Medium
    ButtonDefaults.ButtonSize.Small -> Size.Small
    ButtonDefaults.ButtonSize.Mini -> Size.Mini
  }

@Immutable
private data class ButtonColors(
  val containerColor: Color,
  val contentColor: Color,
  val disabledContainerColor: Color,
  val disabledContentColor: Color,
  val hoverContainerColor: Color,
  val hoverContentColor: Color,
  val activeLoadingIndicatorColor: Color,
  val inactiveLoadingIndicatorColor: Color,
  val redTextColor: Color,
) {
  @Stable
  internal fun containerColor(enabled: Boolean): Color = when {
    !enabled -> disabledContainerColor
    else -> containerColor
  }

  @Stable
  internal fun contentColor(enabled: Boolean): Color = when {
    !enabled -> disabledContentColor
    else -> contentColor
  }

  internal fun hoverColor(isHovered: Boolean): Color = when {
    isHovered -> hoverContainerColor
    else -> Color.Unspecified
  }
}

private sealed interface Size {
  val contentPadding: PaddingValues

  @get:Composable
  val textStyle: TextStyle

  @get:Composable
  val shape: Shape

  object Large : Size {
    override val contentPadding: PaddingValues = PaddingValues(
      top = LargeSizeButtonTokens.TopPadding,
      bottom = LargeSizeButtonTokens.BottomPadding,
      start = LargeSizeButtonTokens.HorizontalPadding,
      end = LargeSizeButtonTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeButtonTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeButtonTokens.ContainerShape.value
  }

  object Medium : Size {
    override val contentPadding: PaddingValues = PaddingValues(
      top = MediumSizeButtonTokens.TopPadding,
      bottom = MediumSizeButtonTokens.BottomPadding,
      start = MediumSizeButtonTokens.HorizontalPadding,
      end = MediumSizeButtonTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeButtonTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeButtonTokens.ContainerShape.value
  }

  object Small : Size {
    override val contentPadding: PaddingValues = PaddingValues(
      top = SmallSizeButtonTokens.TopPadding,
      bottom = SmallSizeButtonTokens.BottomPadding,
      start = SmallSizeButtonTokens.HorizontalPadding,
      end = SmallSizeButtonTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeButtonTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeButtonTokens.ContainerShape.value
  }

  object Mini : Size {
    override val contentPadding: PaddingValues = PaddingValues(
      top = MiniSizeButtonTokens.TopPadding,
      bottom = MiniSizeButtonTokens.BottomPadding,
      start = MiniSizeButtonTokens.HorizontalPadding,
      end = MiniSizeButtonTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MiniSizeButtonTokens.LabelTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = MiniSizeButtonTokens.ContainerShape.value
  }
}

private sealed interface Style {
  @get:Composable
  val buttonColors: ButtonColors

  data object Primary : Style {
    override val buttonColors: ButtonColors
      @Composable
      get() = with(HedvigTheme.colorScheme) {
        remember(this) {
          ButtonColors(
            containerColor = fromToken(PrimaryStyleButtonTokens.ContainerColor),
            contentColor = fromToken(PrimaryStyleButtonTokens.ContentColor),
            disabledContainerColor = fromToken(PrimaryStyleButtonTokens.DisabledContainerColor),
            disabledContentColor = fromToken(PrimaryStyleButtonTokens.DisabledContentColor),
            hoverContainerColor = fromToken(PrimaryStyleButtonTokens.HoverContainerColor),
            hoverContentColor = fromToken(PrimaryStyleButtonTokens.HoverContentColor),
            activeLoadingIndicatorColor = fromToken(PrimaryStyleButtonTokens.ActiveLoadingIndicatorColor),
            inactiveLoadingIndicatorColor = fromToken(PrimaryStyleButtonTokens.InactiveLoadingIndicatorColor),
            redTextColor = fromToken(PrimaryStyleButtonTokens.RedContentColor),
          )
        }
      }
  }

  data object PrimaryAlt : Style {
    override val buttonColors: ButtonColors
      @Composable
      get() = with(HedvigTheme.colorScheme) {
        remember(this) {
          ButtonColors(
            containerColor = fromToken(PrimaryAltStyleButtonTokens.ContainerColor),
            contentColor = fromToken(PrimaryAltStyleButtonTokens.ContentColor),
            disabledContainerColor = fromToken(PrimaryAltStyleButtonTokens.DisabledContainerColor),
            disabledContentColor = fromToken(PrimaryAltStyleButtonTokens.DisabledContentColor),
            hoverContainerColor = fromToken(PrimaryAltStyleButtonTokens.HoverContainerColor),
            hoverContentColor = fromToken(PrimaryAltStyleButtonTokens.HoverContentColor),
            activeLoadingIndicatorColor = fromToken(PrimaryAltStyleButtonTokens.ActiveLoadingIndicatorColor),
            inactiveLoadingIndicatorColor = fromToken(PrimaryAltStyleButtonTokens.InactiveLoadingIndicatorColor),
            redTextColor = fromToken(PrimaryStyleButtonTokens.RedContentColor),
          )
        }
      }
  }

  data object Secondary : Style {
    override val buttonColors: ButtonColors
      @Composable
      get() = with(HedvigTheme.colorScheme) {
        remember(this) {
          ButtonColors(
            containerColor = fromToken(SecondaryStyleButtonTokens.ContainerColor),
            contentColor = fromToken(SecondaryStyleButtonTokens.ContentColor),
            disabledContainerColor = fromToken(SecondaryStyleButtonTokens.DisabledContainerColor),
            disabledContentColor = fromToken(SecondaryStyleButtonTokens.DisabledContentColor),
            hoverContainerColor = fromToken(SecondaryStyleButtonTokens.HoverContainerColor),
            hoverContentColor = fromToken(SecondaryStyleButtonTokens.HoverContentColor),
            activeLoadingIndicatorColor = fromToken(SecondaryStyleButtonTokens.ActiveLoadingIndicatorColor),
            inactiveLoadingIndicatorColor = fromToken(SecondaryStyleButtonTokens.InactiveLoadingIndicatorColor),
            redTextColor = fromToken(PrimaryStyleButtonTokens.RedContentColor),
          )
        }
      }
  }

  data object SecondaryAlt : Style {
    override val buttonColors: ButtonColors
      @Composable
      get() = with(HedvigTheme.colorScheme) {
        remember(this) {
          ButtonColors(
            containerColor = fromToken(SecondaryAltStyleButtonTokens.ContainerColor),
            contentColor = fromToken(SecondaryAltStyleButtonTokens.ContentColor),
            disabledContainerColor = fromToken(SecondaryAltStyleButtonTokens.DisabledContainerColor),
            disabledContentColor = fromToken(SecondaryAltStyleButtonTokens.DisabledContentColor),
            hoverContainerColor = fromToken(SecondaryAltStyleButtonTokens.HoverContainerColor),
            hoverContentColor = fromToken(SecondaryAltStyleButtonTokens.HoverContentColor),
            activeLoadingIndicatorColor = fromToken(SecondaryAltStyleButtonTokens.ActiveLoadingIndicatorColor),
            inactiveLoadingIndicatorColor = fromToken(SecondaryAltStyleButtonTokens.InactiveLoadingIndicatorColor),
            redTextColor = fromToken(PrimaryStyleButtonTokens.RedContentColor),
          )
        }
      }
  }

  data object Ghost : Style {
    override val buttonColors: ButtonColors
      @Composable
      get() = with(HedvigTheme.colorScheme) {
        remember(this) {
          ButtonColors(
            containerColor = fromToken(GhostStyleButtonTokens.ContainerColor),
            contentColor = fromToken(GhostStyleButtonTokens.ContentColor),
            disabledContainerColor = fromToken(GhostStyleButtonTokens.DisabledContainerColor),
            disabledContentColor = fromToken(GhostStyleButtonTokens.DisabledContentColor),
            hoverContainerColor = fromToken(GhostStyleButtonTokens.HoverContainerColor),
            hoverContentColor = fromToken(GhostStyleButtonTokens.HoverContentColor),
            activeLoadingIndicatorColor = fromToken(GhostStyleButtonTokens.ActiveLoadingIndicatorColor),
            inactiveLoadingIndicatorColor = fromToken(GhostStyleButtonTokens.InactiveLoadingIndicatorColor),
            redTextColor = fromToken(PrimaryStyleButtonTokens.RedContentColor),
          )
        }
      }
  }
}
