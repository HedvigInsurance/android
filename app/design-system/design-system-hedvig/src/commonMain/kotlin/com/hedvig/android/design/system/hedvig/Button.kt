package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.tokens.ButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.GhostStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.LiquidGlassButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.MiniSizeButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.PrimaryAltStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.PrimaryStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.RedStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.RoundedLiquidGlassStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.RoundedPrimaryStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.SecondaryAltStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.SecondaryStyleButtonTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeButtonTokens
import hedvig.resources.Res
import hedvig.resources.TALKBACK_LOADING_STATE_BUTTON
import org.jetbrains.compose.resources.stringResource

/**
 * @param isLoading Swaps the label for a loading indicator and stops the button accepting clicks, so an in-flight
 *  action cannot be triggered a second time. Colors keep following [enabled], leaving a loading button's fill
 *  unchanged.
 */
@Composable
fun HedvigButton(
  text: String,
  onClick: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  buttonStyle: ButtonDefaults.ButtonStyle = ButtonDefaults.buttonStyle,
  buttonSize: ButtonSize = ButtonDefaults.buttonSize,
  interactionSource: MutableInteractionSource? = null,
  border: Color? = null,
  onClickLabel: String? = null,
  isLoading: Boolean = false,
) {
  HedvigButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    buttonStyle = buttonStyle,
    buttonSize = buttonSize,
    interactionSource = interactionSource,
    border = border,
    onClickLabel = onClickLabel,
    isLoading = isLoading,
  ) {
    ButtonLabel(text = text, isLoading = isLoading, buttonColors = buttonStyle.style.buttonColors)
  }
}

/**
 * A pill of the iOS liquid glass material, lifted off the content behind it by a drop shadow.
 * Unlike [HedvigButton] it takes no [ButtonSize]: Figma draws these at exactly one size, so there
 * is nothing to choose. See [ButtonDefaults.LiquidGlassButtonStyle].
 *
 * @param isLoading Behaves as it does on [HedvigButton].
 */
@Composable
fun HedvigLiquidGlassButton(
  text: String,
  onClick: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  glassStyle: ButtonDefaults.LiquidGlassButtonStyle = ButtonDefaults.LiquidGlassButtonStyle.Tinted,
  interactionSource: MutableInteractionSource? = null,
  border: Color? = null,
  onClickLabel: String? = null,
  isLoading: Boolean = false,
) {
  val style = glassStyle.style
  ButtonImpl(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    style = style,
    size = Size.LiquidGlass,
    interactionSource = interactionSource,
    border = border,
    onClickLabel = onClickLabel,
    isLoading = isLoading,
  ) {
    ButtonLabel(text = text, isLoading = isLoading, buttonColors = style.buttonColors)
  }
}

/** The label slot shared by [HedvigButton] and [HedvigLiquidGlassButton], including the loading swap. */
@Composable
private fun ButtonLabel(text: String, isLoading: Boolean, buttonColors: ButtonColors) {
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
        val desc = stringResource(Res.string.TALKBACK_LOADING_STATE_BUTTON)
        ThreeDotsLoading(
          stableColor = buttonColors.activeLoadingIndicatorColor,
          temporaryColor = buttonColors.inactiveLoadingIndicatorColor,
          modifier = Modifier.wrapContentSize(Alignment.Center)
            .semantics {
              contentDescription = desc
            },
        )
      }
    } else {
      HedvigText(text = text, textAlign = TextAlign.Center)
    }
  }
}

@Composable
fun HedvigButton(
  onClick: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  buttonStyle: ButtonDefaults.ButtonStyle = ButtonDefaults.buttonStyle,
  buttonSize: ButtonSize = ButtonDefaults.buttonSize,
  interactionSource: MutableInteractionSource? = null,
  border: Color? = null,
  onClickLabel: String? = null,
  isLoading: Boolean = false,
  content: @Composable RowScope.() -> Unit,
) {
  ButtonImpl(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    style = buttonStyle.style,
    size = buttonSize.size,
    interactionSource = interactionSource,
    border = border,
    onClickLabel = onClickLabel,
    isLoading = isLoading,
    content = content,
  )
}

/**
 * The one rendering path behind [HedvigButton] and [HedvigLiquidGlassButton]. [style] and [size] arrive
 * already resolved, which is what keeps the two axes independent: a caller's [ButtonStyle] cannot
 * reach in and change the metrics its [ButtonSize] asked for.
 */
@Composable
private fun ButtonImpl(
  onClick: () -> Unit,
  enabled: Boolean,
  modifier: Modifier,
  style: Style,
  size: Size,
  interactionSource: MutableInteractionSource?,
  border: Color?,
  onClickLabel: String?,
  isLoading: Boolean,
  content: @Composable RowScope.() -> Unit,
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  val buttonColors = style.buttonColors
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

  val shape = ButtonTokens.ContainerShape.value
  val glass = style.glassMaterial.takeIf { enabled }
  Surface(
    onClick = onClick,
    modifier = if (glass == null) {
      modifier
    } else {
      modifier.glassMaterial(glass, color, shape)
    },
    onClickLabel = onClickLabel,
    role = Role.Button,
    // Colors above are already resolved from `enabled`, so withholding the click while loading leaves the
    // button's appearance untouched.
    enabled = enabled && !isLoading,
    shape = shape,
    border = border,
    // The rim shadows have to sit on top of the container fill, which is only reachable from outside
    // this Surface, so the fill moves into the modifier above and the Surface itself stays see-through.
    color = if (glass == null) color else Color.Transparent,
    contentColor = contentColor,
    interactionSource = interactionSource,
  ) {
    ProvideTextStyle(size.textStyle) {
      Row(
        modifier = Modifier.padding(size.contentPadding),
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
  buttonSize: ButtonSize = ButtonSize.Large,
  isLoading: Boolean = false,
  onClickLabel: String? = null,
  onClick: () -> Unit,
) {
  HedvigButton(
    text = text,
    onClick = onClick,
    onClickLabel = onClickLabel,
    enabled = enabled,
    modifier = modifier,
    buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
    buttonSize = buttonSize,
    interactionSource = interactionSource,
    isLoading = isLoading,
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
    buttonSize = ButtonSize.Large,
    interactionSource = interactionSource,
  ) {
    HedvigText(text, color = ButtonDefaults.ButtonStyle.Ghost.style.buttonColors.redTextColor)
  }
}

/**
 * A secondary-fill button with a destructive (red) label, e.g. a "Delete" action that sits next to
 * a primary action. Reuses the secondary style's red content token rather than a bespoke color.
 */
@Composable
fun HedvigSecondaryRedTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  buttonSize: ButtonSize = ButtonSize.Medium,
  interactionSource: MutableInteractionSource? = null,
) {
  HedvigButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
    buttonSize = buttonSize,
    interactionSource = interactionSource,
  ) {
    HedvigText(text, color = ButtonDefaults.ButtonStyle.Secondary.style.buttonColors.redTextColor)
  }
}

@Composable
fun HedvigButtonGhostWithBorder(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  interactionSource: MutableInteractionSource? = null,
  size: ButtonSize = ButtonSize.Medium,
) {
  HedvigTextButton(
    text = text,
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.border(
      width = 1.dp,
      color = HedvigTheme.colorScheme.borderPrimary,
      shape = ButtonTokens.ContainerShape.value,
    ),
    buttonSize = size,
    interactionSource = interactionSource,
  )
}

@HedvigPreview
@Composable
private fun PreviewRoundedButtons() {
  HedvigTheme {
    // A tinted backdrop, so that the translucent liquid glass container is distinguishable from it.
    Surface(color = HedvigTheme.colorScheme.surfaceSecondary) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp),
      ) {
        for (glassStyle in ButtonDefaults.LiquidGlassButtonStyle.entries) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HedvigLiquidGlassButton("Make a claim", {}, enabled = true, glassStyle = glassStyle)
            HedvigLiquidGlassButton("Disabled", {}, enabled = false, glassStyle = glassStyle)
            HedvigLiquidGlassButton("Loading", {}, enabled = true, glassStyle = glassStyle, isLoading = true)
          }
        }
      }
    }
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
    Red,
  }

  /**
   * The two variants of [HedvigLiquidGlassButton]. Figma models them as one component with a
   * boolean Tinted variant, so they are a pair here rather than two entries in [ButtonStyle]: both
   * carry the same drop shadow and the same fixed padding, and neither takes a [ButtonSize].
   */
  enum class LiquidGlassButtonStyle {
    /**
     * An opaque pill, Figma's `Tinted = True`. Renders in the same colors as [ButtonStyle.Primary]
     * at every state; the drop shadow is the only thing that distinguishes the two.
     */
    Tinted,

    /** A translucent pill, Figma's `Tinted = False`, letting the backdrop show through. */
    Regular,
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
    ButtonDefaults.ButtonStyle.Red -> Style.Red
  }

private val ButtonDefaults.LiquidGlassButtonStyle.style: Style
  get() = when (this) {
    ButtonDefaults.LiquidGlassButtonStyle.Tinted -> Style.LiquidGlassTinted
    ButtonDefaults.LiquidGlassButtonStyle.Regular -> Style.LiquidGlassRegular
  }

private val ButtonSize.size: Size
  get() = when (this) {
    ButtonSize.Large -> Size.Large
    ButtonSize.Medium -> Size.Medium
    ButtonSize.Small -> Size.Small
    ButtonSize.Mini -> Size.Mini
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
  fun containerColor(enabled: Boolean): Color = when {
    !enabled -> disabledContainerColor
    else -> containerColor
  }

  @Stable
  fun contentColor(enabled: Boolean): Color = when {
    !enabled -> disabledContentColor
    else -> contentColor
  }

  fun hoverColor(isHovered: Boolean): Color = when {
    isHovered -> hoverContainerColor
    else -> Color.Unspecified
  }
}

private sealed interface Size {
  val contentPadding: PaddingValues

  @get:Composable
  val textStyle: TextStyle

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
  }

  object LiquidGlass : Size {
    override val contentPadding: PaddingValues = PaddingValues(
      top = LiquidGlassButtonTokens.TopPadding,
      bottom = LiquidGlassButtonTokens.BottomPadding,
      start = LiquidGlassButtonTokens.HorizontalPadding,
      end = LiquidGlassButtonTokens.HorizontalPadding,
    )

    override val textStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LiquidGlassButtonTokens.LabelTextFont.value
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
  }
}

private sealed interface Style {
  @get:Composable
  val buttonColors: ButtonColors

  /** Non-null on the styles that render the iOS glass material. */
  val glassMaterial: GlassMaterial?
    @Composable
    get() = null

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

  data object LiquidGlassTinted : Style {
    // The fill inverts between themes: near-black on light, opaque white on dark.
    override val glassMaterial: GlassMaterial
      @Composable
      get() = if (HedvigTheme.colorScheme.isLight) opaqueDarkGlassMaterial else regularGlassMaterial

    override val buttonColors: ButtonColors
      @Composable
      get() = with(HedvigTheme.colorScheme) {
        remember(this) {
          ButtonColors(
            containerColor = fromToken(RoundedPrimaryStyleButtonTokens.ContainerColor),
            contentColor = fromToken(RoundedPrimaryStyleButtonTokens.ContentColor),
            disabledContainerColor = fromToken(RoundedPrimaryStyleButtonTokens.DisabledContainerColor),
            disabledContentColor = fromToken(RoundedPrimaryStyleButtonTokens.DisabledContentColor),
            hoverContainerColor = fromToken(RoundedPrimaryStyleButtonTokens.HoverContainerColor),
            hoverContentColor = fromToken(RoundedPrimaryStyleButtonTokens.HoverContentColor),
            activeLoadingIndicatorColor = fromToken(RoundedPrimaryStyleButtonTokens.ActiveLoadingIndicatorColor),
            inactiveLoadingIndicatorColor = fromToken(RoundedPrimaryStyleButtonTokens.InactiveLoadingIndicatorColor),
            redTextColor = fromToken(RoundedPrimaryStyleButtonTokens.RedContentColor),
          )
        }
      }
  }

  data object LiquidGlassRegular : Style {
    override val glassMaterial: GlassMaterial
      @Composable
      get() = liquidGlassMaterial

    override val buttonColors: ButtonColors
      @Composable
      get() = with(HedvigTheme.colorScheme) {
        remember(this) {
          ButtonColors(
            containerColor = fromToken(RoundedLiquidGlassStyleButtonTokens.ContainerColor),
            contentColor = fromToken(RoundedLiquidGlassStyleButtonTokens.ContentColor),
            disabledContainerColor = fromToken(RoundedLiquidGlassStyleButtonTokens.DisabledContainerColor),
            disabledContentColor = fromToken(RoundedLiquidGlassStyleButtonTokens.DisabledContentColor),
            hoverContainerColor = fromToken(RoundedLiquidGlassStyleButtonTokens.HoverContainerColor),
            hoverContentColor = fromToken(RoundedLiquidGlassStyleButtonTokens.HoverContentColor),
            activeLoadingIndicatorColor = fromToken(RoundedLiquidGlassStyleButtonTokens.ActiveLoadingIndicatorColor),
            inactiveLoadingIndicatorColor =
              fromToken(RoundedLiquidGlassStyleButtonTokens.InactiveLoadingIndicatorColor),
            redTextColor = fromToken(RoundedLiquidGlassStyleButtonTokens.RedContentColor),
          )
        }
      }
  }

  data object Red : Style {
    override val buttonColors: ButtonColors
      @Composable
      get() = with(HedvigTheme.colorScheme) {
        remember(this) {
          ButtonColors(
            containerColor = fromToken(RedStyleButtonTokens.ContainerColor),
            contentColor = fromToken(RedStyleButtonTokens.ContentColor),
            disabledContainerColor = fromToken(RedStyleButtonTokens.DisabledContainerColor),
            disabledContentColor = fromToken(RedStyleButtonTokens.DisabledContentColor),
            hoverContainerColor = fromToken(RedStyleButtonTokens.HoverContainerColor),
            hoverContentColor = fromToken(RedStyleButtonTokens.HoverContentColor),
            activeLoadingIndicatorColor = fromToken(RedStyleButtonTokens.ActiveLoadingIndicatorColor),
            inactiveLoadingIndicatorColor = fromToken(RedStyleButtonTokens.InactiveLoadingIndicatorColor),
            redTextColor = fromToken(RedStyleButtonTokens.RedContentColor),
          )
        }
      }
  }
}
