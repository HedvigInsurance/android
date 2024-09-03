package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle
import com.hedvig.android.design.system.hedvig.DropdownItem.DropdownItemWithIcon
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.tokens.AnimationTokens
import com.hedvig.android.design.system.hedvig.tokens.DropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeDefaultDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.LargeSizeLabeledDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeDefaultDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.MediumSizeLabeledDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeDefaultDropdownTokens
import com.hedvig.android.design.system.hedvig.tokens.SmallSizeLabeledDropdownTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

@Composable
fun DropdownWithDialog(
  style: DropdownStyle,
  size: DropdownDefaults.DropdownSize,
  hintText: String,
  modifier: Modifier = Modifier,
  chosenItemIndex: Int? = null,
  isEnabled: Boolean = true,
  hasError: Boolean = false,
  errorText: String? = null,
) {
  val containerColor = dropdownColors.containerColor(showError = hasError)
  val selectorLabelColor = dropdownColors.labelColor(showError = hasError, isEnabled = isEnabled)
  val selectorTextColor = dropdownColors.textColor(showError = hasError, isEnabled = isEnabled)
  val chevronColor = dropdownColors.chevronColor(isEnabled = isEnabled)
  val errorTextColor = dropdownColors.errorTextColor
  val errorIconColor = dropdownColors.errorIconColor
  
  var isDialogVisible by rememberSaveable { mutableStateOf(false) }
  if (isDialogVisible) {
    HedvigDialog(
      onDismissRequest = {
        isDialogVisible = false
      },
      style = DialogDefaults.DialogStyle.NoButtons
    ) {
      EmptyState(
        text = "Are you sure?",
        description = "Long description description description description description",
        iconStyle = BANK_ID,
        buttonStyle = NoButton,
      )
    }
}

object DropdownDefaults {

  sealed class DropdownSize {
    protected abstract val defaultPadding: PaddingValues
    protected abstract val labelContentPadding: PaddingValues
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

    internal fun contentPadding(style: DropdownStyle): PaddingValues {
      return when (style) {
        is DropdownStyle.Default, is DropdownStyle.Icon -> defaultPadding
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
        get() = LargeSizeLabeledDropdownTokens.TextFont.value
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
        get() = MediumSizeLabeledDropdownTokens.TextFont.value
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
        get() = SmallSizeLabeledDropdownTokens.TextFont.value
    }
  }

  sealed class DropdownStyle {
    data class Default(val items: List<SimpleDropdownItem>) : DropdownStyle()
    data class Icon(val items: List<DropdownItemWithIcon>, val defaultIcon: IconResource) : DropdownStyle()
    data class Label(val items: List<SimpleDropdownItem>, val label: String) : DropdownStyle()
  }
}

sealed class DropdownItem {
  abstract val text: String
  abstract val enabled: Boolean

  data class SimpleDropdownItem(
    override val text: String,
    override val enabled: Boolean = true,
  ) : DropdownItem()

  data class DropdownItemWithIcon(
    override val text: String,
    val iconResource: IconResource,
    override val enabled: Boolean = true,
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
        hintColor = fromToken(DropdownTokens.HintColor)
      )
    }
  }
  
