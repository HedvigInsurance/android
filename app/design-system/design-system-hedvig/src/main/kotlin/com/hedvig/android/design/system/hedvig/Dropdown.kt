package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle
import com.hedvig.android.design.system.hedvig.DropdownDefaults.LockedState
import com.hedvig.android.design.system.hedvig.DropdownDefaults.LockedState.Locked
import com.hedvig.android.design.system.hedvig.DropdownDefaults.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.DropdownItem.DropdownItemWithIcon
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.ChevronDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Lock
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.tokens.AnimationTokens
import com.hedvig.android.design.system.hedvig.tokens.CommonLargeDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.CommonMediumDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.CommonSmallDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.DropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDefaultDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeIconDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeLabeledDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeDefaultDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeIconDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeLabeledDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDefaultDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeIconDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeLabeledDropdownTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

@Composable
fun DropdownWithDialog(
  style: DropdownStyle,
  size: DropdownDefaults.DropdownSize,
  hintText: String,
  onItemChosen: (chosenIndex: Int) -> Unit,
  onSelectorClick: () -> Unit,
  modifier: Modifier = Modifier,
  chosenItemIndex: Int? = null,
  isEnabled: Boolean = true,
  hasError: Boolean = false,
  errorText: String? = null,
  containerColor: Color? = null,
  lockedState: LockedState = NotLocked,
  dialogProperties: DialogProperties = DialogDefaults.defaultProperties,
  onLockedClick: (() -> Unit)? = null,
  dialogContent: (@Composable (onDismissRequest: () -> Unit) -> Unit)? = null,
) {
  var isDialogVisible by rememberSaveable { mutableStateOf(false) }
  if (isDialogVisible) {
    HedvigDialog(
      applyDefaultPadding = dialogContent == null,
      dialogProperties = dialogProperties,
      onDismissRequest = {
        isDialogVisible = false
      },
      style = DialogDefaults.DialogStyle.NoButtons,
    ) {
      if (dialogContent != null) {
        dialogContent {
          isDialogVisible = false
        }
      } else {
        Column(
          modifier = Modifier.background(
            color = dropdownColors.containerColor(false).value,
            shape = size.shape,
          ),
        ) {
          style.items.forEachIndexed { index, item ->
            DropdownOption(
              item = item,
              size = size,
              style = style,
              onClick = {
                onItemChosen(index)
                isDialogVisible = false
              },
              isSelected = index == chosenItemIndex,
            )
          }
        }
      }
    }
  }
  DropdownSelector(
    text = if (chosenItemIndex == null) hintText else style.items[chosenItemIndex].text,
    size = size,
    isHint = chosenItemIndex == null,
    isEnabled = isEnabled,
    showError = hasError,
    modifier = modifier,
    style = style,
    onClick = {
      onSelectorClick()
      isDialogVisible = true
    },
    errorText = errorText,
    isDialogOpen = isDialogVisible,
    containerColor = containerColor,
    lockedState = lockedState,
    onLockedClick = onLockedClick,
  )
}

@Composable
private fun DropdownSelector(
  text: String,
  size: DropdownDefaults.DropdownSize,
  isHint: Boolean,
  isEnabled: Boolean,
  isDialogOpen: Boolean,
  showError: Boolean,
  errorText: String?,
  style: DropdownStyle,
  onClick: () -> Unit,
  lockedState: LockedState,
  modifier: Modifier = Modifier,
  containerColor: Color? = null,
  onLockedClick: (() -> Unit)? = null,
) {
  val conditionedOnClick = when (lockedState) {
    is Locked -> onLockedClick ?: {}
    NotLocked -> onClick
  }
  Column(
    modifier = modifier,
  ) {
    Surface(
      shape = size.shape,
      color = containerColor ?: dropdownColors.containerColor(showError).value,
      modifier = Modifier
        .clip(size.shape)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(
            bounded = true,
            radius = 1000.dp,
          ),
          onClick = conditionedOnClick,
        ),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          val textColor = if (isHint) {
            dropdownColors.hintColor(showError, isEnabled).value
          } else {
            dropdownColors.textColor(
              showError = showError,
              isEnabled = isEnabled,
            ).value
          }
          val labelColor = dropdownColors.labelColor(showError, isEnabled).value
          when (style) {
            is DropdownStyle.Default -> DefaultStyleStartSlot(
              textColor = textColor,
              text = text,
              textStyle = size.textStyle,
            )

            is DropdownStyle.Icon -> IconStyleStartSlot(
              textColor = textColor,
              text = text,
              textStyle = size.textStyle,
              icon = style.defaultIcon,
            )

            is DropdownStyle.Label -> LabelStyleStartSlot(
              textColor = textColor,
              text = text,
              textStyle = size.textStyle,
              labelText = style.label,
              labelTextColor = labelColor,
              labelTextStyle = size.labelTextStyle,
            )
          }
        },
        endSlot = {
          val fullRotation by animateFloatAsState(
            targetValue = if (isDialogOpen) -180f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
          )
          Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            AnimatedVisibility(showError, enter = fadeIn(), exit = fadeOut()) {
              Icon(
                HedvigIcons.WarningFilled,
                "",
                tint = dropdownColors.errorIconColor,
              )
              Spacer(Modifier.width(4.dp))
            }
            IconButton(
              onClick = conditionedOnClick,
              enabled = isEnabled,
              modifier = Modifier
                .size(DropdownTokens.ChevronSize)
                .graphicsLayer {
                  rotationZ = fullRotation
                },
            ) {
              val icon = when (lockedState) {
                is Locked -> HedvigIcons.Lock
                NotLocked -> HedvigIcons.ChevronDown
              }
              Icon(
                icon,
                "",
                tint = dropdownColors.chevronColor(isEnabled),
              )
            }
          }
        },
        spaceBetween = 4.dp,
        modifier = Modifier.padding(size.contentPadding(style)),
      )
    }

    if (errorText != null) {
      AnimatedVisibility(showError) {
        HedvigText(
          text = errorText,
          color = dropdownColors.errorTextColor,
          style = size.errorTextStyle,
          modifier = Modifier.padding(size.errorTextPadding),
          textAlign = TextAlign.Start,
        )
      }
    }
  }
}

@Composable
fun DefaultStyleStartSlot(text: String, textStyle: TextStyle, textColor: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    HedvigText(
      text = text,
      style = textStyle,
      modifier = Modifier,
      color = textColor,
    )
  }
}

@Composable
fun IconStyleStartSlot(text: String, textStyle: TextStyle, textColor: Color, icon: IconResource) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    when (icon) {
      is IconResource.Painter -> Icon(
        painterResource(icon.painterResId),
        "",
        Modifier.size(DropdownTokens.IconSize),
        tint = Color.Unspecified,
      )
      is IconResource.Vector -> Icon(
        icon.imageVector,
        "",
        Modifier.size(DropdownTokens.IconSize),
        tint = Color.Unspecified,
      )
    }
    Spacer(Modifier.width(8.dp))
    HedvigText(
      text = text,
      style = textStyle,
      color = textColor,
    )
  }
}

@Composable
fun LabelStyleStartSlot(
  labelText: String,
  labelTextStyle: TextStyle,
  labelTextColor: Color,
  text: String,
  textStyle: TextStyle,
  textColor: Color,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Column(verticalArrangement = Arrangement.spacedBy((-2).dp)) {
      HedvigText(
        text = labelText,
        style = labelTextStyle,
        color = labelTextColor,
      )
      HedvigText(
        text = text,
        style = textStyle,
        color = textColor,
      )
    }
  }
}

@Composable
private fun DropdownOption(
  item: DropdownItem,
  size: DropdownDefaults.DropdownSize,
  style: DropdownStyle,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val containerColor = dropdownColors.containerColor(showError = false).value
  val checkSymbolColor = dropdownColors.chevronColor(isEnabled = true)
  Column(
    modifier = modifier,
  ) {
    Surface(
      shape = size.shape,
      color = containerColor,
      modifier = Modifier
        .clip(size.shape)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(
            bounded = true,
            radius = 1000.dp,
          ),
          onClick = onClick,
        ),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        modifier = Modifier.padding(size.optionContentPadding(style)),
        startSlot = {
          val textColor = dropdownColors.textColor(
            showError = false,
            isEnabled = true,
          ).value
          when (style) {
            is DropdownStyle.Default, is DropdownStyle.Label ->
              DefaultStyleStartSlot(
                textColor = textColor,
                text = item.text,
                textStyle = size.textStyle,
              )
            is DropdownStyle.Icon -> IconStyleStartSlot(
              textColor = textColor,
              text = item.text,
              textStyle = size.textStyle,
              icon = (item as DropdownItemWithIcon).painter,
            )
          }
        },
        endSlot = {
          Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            AnimatedVisibility(isSelected) {
              if (isSelected) {
                Icon(
                  HedvigIcons.Checkmark,
                  "",
                  tint = checkSymbolColor,
                  modifier = Modifier.size(DropdownTokens.IconSize),
                )
              }
            }
          }
        },
        spaceBetween = 8.dp,
      )
    }
  }
}

object DropdownDefaults {
  sealed class LockedState {
    data object NotLocked : LockedState()

    data object Locked : LockedState()
  }

  sealed class DropdownSize {
    protected abstract val defaultPadding: PaddingValues
    protected abstract val labelContentPadding: PaddingValues
    protected abstract val iconContentPadding: PaddingValues
    internal abstract val errorTextPadding: PaddingValues
    internal val errorTextStyle: TextStyle
      @Composable
      @ReadOnlyComposable
      get() = DropdownTokens.ErrorTextFont.value

    @get:Composable
    internal abstract val labelTextStyle: TextStyle

    @get:Composable
    internal abstract val shape: Shape

    @get:Composable
    internal abstract val textStyle: TextStyle

    internal fun optionContentPadding(style: DropdownStyle): PaddingValues {
      return when (style) {
        is DropdownStyle.Default -> defaultPadding
        is DropdownStyle.Icon -> iconContentPadding
        is DropdownStyle.Label -> defaultPadding
      }
    }

    internal fun contentPadding(style: DropdownStyle): PaddingValues {
      return when (style) {
        is DropdownStyle.Default -> defaultPadding
        is DropdownStyle.Icon -> iconContentPadding
        is DropdownStyle.Label -> labelContentPadding
      }
    }

    data object Large : DropdownSize() {
      override val defaultPadding: PaddingValues
        get() = PaddingValues(
          top = LargeSizeDefaultDropdownTokens.TopPadding,
          bottom = LargeSizeDefaultDropdownTokens.BottomPadding,
          start = LargeSizeDefaultDropdownTokens.HorizontalPadding,
          end = LargeSizeDefaultDropdownTokens.HorizontalPadding,
        )
      override val labelContentPadding: PaddingValues
        get() = PaddingValues(
          top = LargeSizeLabeledDropdownTokens.TopPadding,
          bottom = LargeSizeLabeledDropdownTokens.BottomPadding,
          start = LargeSizeLabeledDropdownTokens.HorizontalPadding,
          end = LargeSizeLabeledDropdownTokens.HorizontalPadding,
        )
      override val iconContentPadding: PaddingValues
        get() = PaddingValues(
          top = LargeSizeIconDropdownTokens.TopPadding,
          bottom = LargeSizeIconDropdownTokens.BottomPadding,
          start = LargeSizeIconDropdownTokens.HorizontalPadding,
          end = LargeSizeIconDropdownTokens.HorizontalPadding,
        )
      override val errorTextPadding: PaddingValues
        get() = PaddingValues(horizontal = LargeSizeLabeledDropdownTokens.HorizontalPadding)
      override val labelTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LargeSizeLabeledDropdownTokens.LabelTextFont.value
      override val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = DropdownTokens.ContainerShape.value
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = CommonLargeDropdownTokens.TextFont.value
    }

    data object Medium : DropdownSize() {
      override val defaultPadding: PaddingValues
        get() = PaddingValues(
          top = MediumSizeDefaultDropdownTokens.TopPadding,
          bottom = MediumSizeDefaultDropdownTokens.BottomPadding,
          start = MediumSizeDefaultDropdownTokens.HorizontalPadding,
          end = MediumSizeDefaultDropdownTokens.HorizontalPadding,
        )
      override val labelContentPadding: PaddingValues
        get() = PaddingValues(
          top = MediumSizeLabeledDropdownTokens.TopPadding,
          bottom = MediumSizeLabeledDropdownTokens.BottomPadding,
          start = MediumSizeLabeledDropdownTokens.HorizontalPadding,
          end = MediumSizeLabeledDropdownTokens.HorizontalPadding,
        )
      override val iconContentPadding: PaddingValues
        get() = PaddingValues(
          top = MediumSizeIconDropdownTokens.TopPadding,
          bottom = MediumSizeIconDropdownTokens.BottomPadding,
          start = MediumSizeIconDropdownTokens.HorizontalPadding,
          end = MediumSizeIconDropdownTokens.HorizontalPadding,
        )
      override val labelTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MediumSizeLabeledDropdownTokens.LabelTextFont.value
      override val errorTextPadding: PaddingValues
        get() = PaddingValues(horizontal = MediumSizeLabeledDropdownTokens.HorizontalPadding)
      override val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = DropdownTokens.ContainerShape.value
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = CommonMediumDropdownTokens.TextFont.value
    }

    data object Small : DropdownSize() {
      override val defaultPadding: PaddingValues
        get() = PaddingValues(
          top = SmallSizeDefaultDropdownTokens.TopPadding,
          bottom = SmallSizeDefaultDropdownTokens.BottomPadding,
          start = SmallSizeDefaultDropdownTokens.HorizontalPadding,
          end = SmallSizeDefaultDropdownTokens.HorizontalPadding,
        )
      override val labelContentPadding: PaddingValues
        get() = PaddingValues(
          top = SmallSizeLabeledDropdownTokens.TopPadding,
          bottom = SmallSizeLabeledDropdownTokens.BottomPadding,
          start = SmallSizeLabeledDropdownTokens.HorizontalPadding,
          end = SmallSizeLabeledDropdownTokens.HorizontalPadding,
        )
      override val iconContentPadding: PaddingValues
        get() = PaddingValues(
          top = SmallSizeIconDropdownTokens.TopPadding,
          bottom = SmallSizeIconDropdownTokens.BottomPadding,
          start = SmallSizeIconDropdownTokens.HorizontalPadding,
          end = SmallSizeIconDropdownTokens.HorizontalPadding,
        )
      override val labelTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = SmallSizeLabeledDropdownTokens.LabelTextFont.value
      override val errorTextPadding: PaddingValues
        get() = PaddingValues(horizontal = SmallSizeLabeledDropdownTokens.HorizontalPadding)
      override val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = DropdownTokens.ContainerShape.value
      override val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = CommonSmallDropdownTokens.TextFont.value
    }
  }

  sealed class DropdownStyle {
    abstract val items: List<DropdownItem>

    data class Default(override val items: List<SimpleDropdownItem>) : DropdownStyle()

    data class Icon(override val items: List<DropdownItemWithIcon>, val defaultIcon: IconResource) : DropdownStyle()

    data class Label(override val items: List<SimpleDropdownItem>, val label: String) : DropdownStyle()
  }
}

sealed class DropdownItem {
  abstract val text: String

  data class SimpleDropdownItem(
    override val text: String,
  ) : DropdownItem()

  data class DropdownItemWithIcon(
    override val text: String,
    val painter: IconResource,
  ) : DropdownItem()
}

@Immutable
private data class DropdownColors(
  private val containerColor: Color,
  private val labelColor: Color,
  private val textColor: Color,
  private val hintColor: Color,
  private val enabledChevronColor: Color,
  private val disabledChevronColor: Color,
  private val disabledTextColor: Color,
  private val pulsatingContainerColor: Color,
  private val pulsatingContentColor: Color,
  private val pulsatingChevronColor: Color,
  val errorIconColor: Color,
  val errorTextColor: Color,
) {
  @Composable
  fun chevronColor(isEnabled: Boolean): Color {
    return when {
      !isEnabled -> disabledChevronColor
      else -> enabledChevronColor
    }
  }

  @Composable
  fun containerColor(showError: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(showError)
    val targetValue = when {
      shouldPulsate -> pulsatingContainerColor
      else -> containerColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().errorPulsatingDuration,
      ),
      label = "",
    )
  }

  @Composable
  fun hintColor(showError: Boolean, isEnabled: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(showError)
    val targetValue = when {
      shouldPulsate -> pulsatingContentColor
      !isEnabled -> disabledTextColor
      else -> hintColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().errorPulsatingDuration,
      ),
      label = "",
    )
  }

  @Composable
  fun labelColor(showError: Boolean, isEnabled: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(showError)
    val targetValue = when {
      shouldPulsate -> pulsatingContentColor
      !isEnabled -> disabledTextColor
      else -> labelColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().errorPulsatingDuration,
      ),
      label = "",
    )
  }

  @Composable
  fun textColor(showError: Boolean, isEnabled: Boolean): State<Color> {
    val shouldPulsate = shouldPulsate(showError)
    val targetValue = when {
      shouldPulsate -> pulsatingContentColor
      !isEnabled -> disabledTextColor
      else -> textColor
    }
    return animateColorAsState(
      targetValue = targetValue,
      animationSpec = tween(
        durationMillis = AnimationTokens().errorPulsatingDuration,
      ),
      label = "",
    )
  }

  @Composable
  private fun shouldPulsate(isError: Boolean): Boolean {
    var shouldPulsate by remember { mutableStateOf(false) }
    val updatedValue by rememberUpdatedState(isError)
    LaunchedEffect(Unit) {
      snapshotFlow { updatedValue }
        .drop(1)
        .collectLatest { latest ->
          if (latest) {
            shouldPulsate = true
            delay(AnimationTokens().errorPulsatingDuration.toLong())
            shouldPulsate = false
          }
        }
    }
    return shouldPulsate
  }
}

private val dropdownColors: DropdownColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      DropdownColors(
        containerColor = fromToken(DropdownTokens.ContainerColor),
        labelColor = fromToken(DropdownTokens.LabelColor),
        textColor = fromToken(DropdownTokens.TextColor),
        pulsatingContainerColor = fromToken(DropdownTokens.PulsatingContainerColor),
        pulsatingContentColor = fromToken(DropdownTokens.PulsatingContentColor),
        errorTextColor = fromToken(DropdownTokens.ErrorTextColor),
        enabledChevronColor = fromToken(DropdownTokens.EnabledChevronColor),
        disabledChevronColor = fromToken(DropdownTokens.DisabledChevronColor),
        disabledTextColor = fromToken(DropdownTokens.DisabledTextColor),
        pulsatingChevronColor = fromToken(DropdownTokens.PulsatingChevronColor),
        errorIconColor = fromToken(DropdownTokens.ErrorIconColor),
        hintColor = fromToken(DropdownTokens.HintColor),
      )
    }
  }
