package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.BigCardDefaults.inputTextStyle
import com.hedvig.android.design.system.hedvig.BigCardDefaults.labelTextStyle
import com.hedvig.android.design.system.hedvig.BigCardDefaults.padding
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.TypographyKeyTokens

/**
 * The card which looks like a TextField, but functions as a button which has the text in the positions of a button.
 * https://www.figma.com/file/qUhLjrKl98PAzHov9ilaDH/New-Web-UI-Kit?type=design&node-id=114-2605&t=NMbwHBp5OhuKjgZ4-4
 */

@Composable
fun HedvigBigCard(
// todo: rename after complete migration to new DS to ButtonWithLabelTextField
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = HedvigTheme.shapes.cornerLarge,
  content: @Composable () -> Unit,
) {
  Surface(
    shape = shape,
    color = bigCardColors.containerColor,
    modifier = modifier
      .clip(shape)
      .clickable(
        onClick = onClick,
        enabled = enabled,
      ),
  ) {
    content()
  }
}

@Composable
fun HedvigBigCard(
// todo: rename after complete migration to new DS to ButtonWithLabelTextField
  onClick: () -> Unit,
  labelText: String,
  inputText: String?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  textStyle: TextStyle = inputTextStyle,
) {
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    color = bigCardColors.containerColor,
    modifier = modifier
      .clip(HedvigTheme.shapes.cornerLarge)
      .clickable(
        onClick = onClick,
        enabled = enabled,
      ),
  ) {
    Column(Modifier.padding(padding)) {
      HedvigText(
        text = labelText,
        style = labelTextStyle,
        color = bigCardColors.labelTextColor(enabled),
      )
      if (inputText != null) {
        HedvigText(
          text = inputText,
          style = textStyle,
          color = bigCardColors.inputTextColor(enabled),
        )
      }
    }
  }
}

private object BigCardDefaults {
  val labelTextStyle: TextStyle
    @Composable
    get() = TypographyKeyTokens.Label.value
  val inputTextStyle: TextStyle
    @Composable
    get() = TypographyKeyTokens.BodySmall.value
  val padding = PaddingValues(
    top = 11.dp,
    bottom = 10.dp,
    start = 16.dp,
    end = 16.dp,
  )
}

private data class BigCardColors(
  val containerColor: Color,
  private val labelTextColor: Color,
  private val inputTextColor: Color,
  private val labelDisabledTextColor: Color,
  private val inputDisabledTextColor: Color,
) {
  fun labelTextColor(enabled: Boolean): Color {
    return if (enabled) labelTextColor else labelDisabledTextColor
  }

  fun inputTextColor(enabled: Boolean): Color {
    return if (enabled) inputTextColor else inputDisabledTextColor
  }
}

private val bigCardColors: BigCardColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      BigCardColors(
        containerColor = fromToken(ColorSchemeKeyTokens.SurfacePrimary),
        labelTextColor = fromToken(ColorSchemeKeyTokens.TextSecondary),
        inputTextColor = fromToken(ColorSchemeKeyTokens.TextPrimary),
        labelDisabledTextColor = fromToken(ColorSchemeKeyTokens.TextTertiary),
        inputDisabledTextColor = fromToken(ColorSchemeKeyTokens.TextSecondary),
      )
    }
  }
