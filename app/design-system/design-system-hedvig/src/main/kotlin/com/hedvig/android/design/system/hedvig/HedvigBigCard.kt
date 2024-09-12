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
// or something more descriptive mb. And it's not big now! It's medium
  onClick: () -> Unit,
  labelText: String,
  inputText: String?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
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
        color = bigCardColors.labelTextColor,
      )
      if (inputText != null) {
        HedvigText(
          text = inputText,
          style = inputTextStyle,
          color = bigCardColors.inputTextColor,
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
  val labelTextColor: Color,
  val inputTextColor: Color,
  val containerColor: Color,
)

private val bigCardColors: BigCardColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      BigCardColors(
        labelTextColor = fromToken(ColorSchemeKeyTokens.TextSecondary),
        inputTextColor = fromToken(ColorSchemeKeyTokens.TextPrimary),
        containerColor = fromToken(ColorSchemeKeyTokens.SurfacePrimary),
      )
    }
  }
