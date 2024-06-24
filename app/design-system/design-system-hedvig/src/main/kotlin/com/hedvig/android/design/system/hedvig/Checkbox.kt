package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxStyle
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxStyle.Default
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.LockedState.Locked
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.CheckboxColorTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxTokens.LargeSizeCheckboxTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxTokens.MediumSizeCheckboxTokens
import com.hedvig.android.design.system.hedvig.tokens.SizeCheckboxTokens.SmallSizeCheckboxTokens

@Composable
fun CheckBox(
  optionText: String,
  chosenState: ChosenState,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  lockedState: LockedState = NotLocked,
  checkboxStyle: CheckboxStyle = CheckboxDefaults.checkboxStyle,
  checkboxSize: CheckboxDefaults.CheckboxSize = CheckboxDefaults.checkboxSize,
  interactionSource: MutableInteractionSource? = null,
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  val clickableModifier = if (onClick != null) {
    modifier
      .clip(checkboxSize.size(checkboxStyle).shape)
      .semantics { role = Role.RadioButton }
      .clickable(
        enabled = when (lockedState) {
          Locked -> false
          NotLocked -> true
        },
        interactionSource = interactionSource,
        indication = LocalIndication.current,
      ) {
        onClick()
      }
  } else {
    modifier.semantics { role = Role.RadioButton }
  }
  Surface(
    modifier = clickableModifier,
    shape = checkboxSize.size(checkboxStyle).shape,
    color = checkboxColors.containerColor,
  ) {
    val optionTextColor = checkboxColors.optionTextColor(lockedState)
    val labelTextColor = checkboxColors.labelTextColor(lockedState)
    Row(
      modifier = Modifier.padding(checkboxSize.size(checkboxStyle).contentPadding),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      when (checkboxStyle) {
        Default -> {
          HedvigText(
            optionText,
            style = checkboxSize.size(checkboxStyle).optionTextStyle,
            color = optionTextColor,
            modifier = Modifier.weight(1f),
          )
          Spacer(Modifier.width(8.dp))
          SelectIndicationSquareBox(chosenState = chosenState, lockedState = lockedState, onCheckedChange = null)
        }

        is CheckboxStyle.Icon -> {
          when (checkboxStyle.iconResource) {
            is IconResource.Vector -> {
              Icon(
                imageVector = checkboxStyle.iconResource.imageVector,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                  .size(32.dp),
              )
            }

            is IconResource.Painter -> {
              Image(
                painter = painterResource(id = checkboxStyle.iconResource.painterResId),
                contentDescription = null,
                modifier = Modifier
                  .size(32.dp),
              )
            }
          }
          Spacer(Modifier.width(8.dp))
          HedvigText(
            optionText,
            style = checkboxSize.size(checkboxStyle).optionTextStyle,
            modifier = Modifier.weight(1f),
            color = optionTextColor,
          )
          Spacer(Modifier.width(8.dp))
          SelectIndicationSquareBox(chosenState = chosenState, lockedState = lockedState, onCheckedChange = null)
        }

        is CheckboxStyle.Label -> {
          Column(Modifier.weight(1f)) {
            HedvigText(
              optionText,
              style = checkboxSize.size(checkboxStyle).optionTextStyle,
              color = optionTextColor,
            )
            HedvigText(
              checkboxStyle.labelText,
              style = checkboxSize.size(checkboxStyle).labelTextStyle,
              color = labelTextColor,
            )
          }
          Spacer(Modifier.width(8.dp))
          SelectIndicationSquareBox(chosenState = chosenState, lockedState = lockedState, onCheckedChange = null)
        }

        CheckboxStyle.LeftAligned -> {
          SelectIndicationSquareBox(chosenState = chosenState, lockedState = lockedState, onCheckedChange = null)
          Spacer(Modifier.width(8.dp))
          HedvigText(
            optionText,
            style = checkboxSize.size(checkboxStyle).optionTextStyle,
            color = optionTextColor,
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

@Composable
fun SelectIndicationSquareBox(
  // public here, bc there are checkboxes different in color than in design system, but with the same indicationBox (like termination flow, date step)
  chosenState: ChosenState,
  lockedState: LockedState,
  onCheckedChange: ((ChosenState) -> Unit)?,
  modifier: Modifier = Modifier,
) {
  CheckItemAnimation(chosenState) { currentState: ChosenState ->
    val shape = CheckboxDefaults.CheckboxSize.Large.size(Default).shape // same for all sizes
    val checkIconColor = HedvigTheme.colorScheme.surfacePrimary
    val backgroundColor = when (currentState) {
      Chosen -> {
        when (lockedState) {
          Locked -> checkboxColors.disabledIndicatorColor
          NotLocked -> checkboxColors.chosenIndicatorColor
        }
      }

      NotChosen -> Color.Transparent
    }
    val borderColor = when (currentState) {
      Chosen -> {
        when (lockedState) {
          Locked -> checkboxColors.disabledIndicatorColor
          NotLocked -> checkboxColors.chosenIndicatorColor
        }
      }

      NotChosen -> checkboxColors.notChosenIndicatorColor
    }
    Box(
      modifier = modifier
        .size(24.dp)
        .background(
          color = backgroundColor,
          shape = shape,
        )
        .border(
          width = 2.dp,
          color = borderColor,
          shape = shape,
        )
        .clip(shape)
        .clickable {
          if (onCheckedChange != null) {
            onCheckedChange(currentState)
          }
        },
      contentAlignment = Alignment.Center,
    ) {
      if ((currentState == Chosen)) {
        Icon(HedvigIcons.Checkmark, contentDescription = null, tint = checkIconColor)
      }
    }
  }
}

object CheckboxDefaults {
  internal val checkboxStyle: CheckboxStyle = Default
  internal val checkboxSize: CheckboxSize = CheckboxSize.Large

  sealed interface CheckboxStyle {
    data object Default : CheckboxStyle

    data class Label(val labelText: String) : CheckboxStyle

    data class Icon(val iconResource: IconResource) : CheckboxStyle

    data object LeftAligned : CheckboxStyle
  }

  enum class CheckboxSize {
    Large,
    Medium,
    Small,
  }
}

internal fun CheckboxDefaults.CheckboxSize.size(style: CheckboxStyle): CheckboxSize {
  return when (this) {
    CheckboxDefaults.CheckboxSize.Large -> CheckboxSize.Large(style)
    CheckboxDefaults.CheckboxSize.Medium -> CheckboxSize.Medium(style)
    CheckboxDefaults.CheckboxSize.Small -> CheckboxSize.Small(style)
  }
}

@Immutable
internal data class CheckboxColors(
  val containerColor: Color,
  val optionTextColor: Color,
  val labelTextColor: Color,
  val disabledOptionTextColor: Color,
  val disabledLabelTextColor: Color,
  val chosenIndicatorColor: Color,
  val notChosenIndicatorColor: Color,
  val disabledIndicatorColor: Color,
) {
  @Stable
  fun optionTextColor(state: LockedState): Color = when (state) {
    Locked -> disabledOptionTextColor
    else -> optionTextColor
  }

  @Stable
  fun labelTextColor(state: LockedState): Color = when (state) {
    Locked -> disabledLabelTextColor
    else -> labelTextColor
  }
}

internal val checkboxColors: CheckboxColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      CheckboxColors(
        containerColor = fromToken(CheckboxColorTokens.ContainerColor),
        optionTextColor = fromToken(CheckboxColorTokens.OptionTextColor),
        labelTextColor = fromToken(CheckboxColorTokens.LabelTextColor),
        disabledOptionTextColor = fromToken(CheckboxColorTokens.DisabledOptionTextColor),
        disabledLabelTextColor = fromToken(CheckboxColorTokens.DisabledLabelTextColor),
        chosenIndicatorColor = fromToken(CheckboxColorTokens.ChosenIndicatorColor),
        notChosenIndicatorColor = fromToken(CheckboxColorTokens.NotChosenIndicatorColor),
        disabledIndicatorColor = fromToken(CheckboxColorTokens.DisabledIndicatorColor),
      )
    }
  }

internal sealed interface CheckboxSize {
  val contentPadding: PaddingValues

  @get:Composable
  val optionTextStyle: TextStyle

  @get:Composable
  val labelTextStyle: TextStyle

  @get:Composable
  val shape: Shape

  @get:Composable
  val indicationShape: Shape

  data class Large(val style: CheckboxStyle) : CheckboxSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = LargeSizeCheckboxTokens.verticalPadding(style).calculateTopPadding(),
      bottom = LargeSizeCheckboxTokens.verticalPadding(style).calculateBottomPadding(),
      start = LargeSizeCheckboxTokens.HorizontalPadding,
      end = LargeSizeCheckboxTokens.HorizontalPadding,
    )

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeCheckboxTokens.LabelTextFont.value

    override val optionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeCheckboxTokens.OptionTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeCheckboxTokens.ContainerShape.value

    override val indicationShape: Shape
      @Composable
      @ReadOnlyComposable
      get() = LargeSizeCheckboxTokens.IndicationShape.value
  }

  data class Medium(val style: CheckboxStyle) : CheckboxSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = MediumSizeCheckboxTokens.verticalPadding(style).calculateTopPadding(),
      bottom = MediumSizeCheckboxTokens.verticalPadding(style).calculateBottomPadding(),
      start = MediumSizeCheckboxTokens.HorizontalPadding,
      end = MediumSizeCheckboxTokens.HorizontalPadding,
    )

    override val optionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeCheckboxTokens.OptionTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeCheckboxTokens.ContainerShape.value

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeCheckboxTokens.LabelTextFont.value

    override val indicationShape: Shape
      @Composable
      @ReadOnlyComposable
      get() = MediumSizeCheckboxTokens.IndicationShape.value
  }

  data class Small(val style: CheckboxStyle) : CheckboxSize {
    override val contentPadding: PaddingValues = PaddingValues(
      top = SmallSizeCheckboxTokens.verticalPadding(style).calculateTopPadding(),
      bottom = SmallSizeCheckboxTokens.verticalPadding(style).calculateBottomPadding(),
      start = SmallSizeCheckboxTokens.HorizontalPadding,
      end = SmallSizeCheckboxTokens.HorizontalPadding,
    )

    override val labelTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeCheckboxTokens.LabelTextFont.value

    override val optionTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeCheckboxTokens.OptionTextFont.value

    override val shape: Shape
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeCheckboxTokens.ContainerShape.value

    override val indicationShape: Shape
      @Composable
      @ReadOnlyComposable
      get() = SmallSizeCheckboxTokens.IndicationShape.value
  }
}