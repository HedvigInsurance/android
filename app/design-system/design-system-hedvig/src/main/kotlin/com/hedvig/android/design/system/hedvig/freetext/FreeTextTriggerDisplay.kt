package com.hedvig.android.design.system.hedvig.freetext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Height
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Height.Limited
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Height.Unlimited
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Style
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Style.Labeled
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.contentPadding
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.defaultHeight
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.defaultStyle
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.supportingTextPadding
import com.hedvig.android.design.system.hedvig.fromToken
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextPrimary
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextSecondaryTranslucent
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextTertiary
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens.DisplayContentPaddingBottom
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens.DisplayContentPaddingEnd
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens.DisplayContentPaddingStart
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens.DisplayContentPaddingTop
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens.DisplaySupportingTextPaddingBottom
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens.DisplaySupportingTextPaddingEnd
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens.DisplaySupportingTextPaddingStart
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens.DisplaySupportingTextPaddingTop
import com.hedvig.android.design.system.hedvig.value

@Composable
fun FreeTextDisplay(
  onClick: () -> Unit,
  freeTextValue: String?,
  freeTextPlaceholder: String,
  modifier: Modifier = Modifier,
  maxLength: Int = FreeTextDisplayDefaults.maxLength,
  height: Height = defaultHeight,
  style: Style = defaultStyle,
  hasError: Boolean = false,
  supportingText: String? = null,
) {
  val textValue = freeTextValue ?: ""

  Column(modifier) {
    Surface(
      onClick = onClick,
      modifier =
        when (height) {
          is Limited -> Modifier.height(
            height.requiredHeight,
          )

          Unlimited -> Modifier
        },
      shape = FreeTextDisplayDefaults.shape,
      color = freeTextColors.displayContainerColor,
    ) {
      Column(
        Modifier.padding(contentPadding),
      ) {
        if (style is Labeled && freeTextValue != null) {
          Row(Modifier.fillMaxWidth()) {
            HedvigText(
              text = style.labelText,
              color = displayColors.labelColor,
              style = FreeTextDisplayDefaults.countLabelStyle.value,
            )
          }
        }
        Row(
          modifier = if (height is Limited) Modifier.weight(1f) else Modifier,
        ) {
          val text = freeTextValue ?: if (style is Labeled) style.labelText else freeTextPlaceholder
          HedvigText(
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
            text = text,
            style = FreeTextDisplayDefaults.textStyle.value,
            color = if (freeTextValue != null) displayColors.textColor else displayColors.placeHolderColor,
          )
          AnimatedVisibility(hasError) {
            if (hasError) {
              Icon(HedvigIcons.WarningFilled, null, tint = freeTextColors.warningIconColor)
            }
          }
        }
        Spacer(Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth(),
        ) {
          AnimatedVisibility(textValue.length >= maxLength) {
            Icon(HedvigIcons.WarningFilled, null, tint = freeTextColors.warningIconColor)
          }
          Spacer(Modifier.width(2.dp))
          HedvigText(
            text = "${textValue.length}/$maxLength",
            style = FreeTextDisplayDefaults.countLabelStyle.value,
            color = displayColors.counterColor,
          )
        }
      }
    }
    AnimatedVisibility(hasError) {
      if (hasError && supportingText != null) {
        HedvigText(
          text = supportingText,
          color = displayColors.supportingTextColor,
          style = FreeTextDisplayDefaults.countLabelStyle.value,
          modifier = Modifier.padding(supportingTextPadding),
        )
      }
    }
  }
}

object FreeTextDisplayDefaults {
  val defaultHeight = Limited()
  val defaultStyle = Style.Default
  internal val textStyle = FreeTextTokens.TextStyle
  internal val maxLength: Int = FreeTextTokens.TextDefaultMaxLength
  internal val countLabelStyle = FreeTextTokens.CountLabel
  internal val supportingTextPadding = PaddingValues(
    bottom = DisplaySupportingTextPaddingBottom,
    top = DisplaySupportingTextPaddingTop,
    start = DisplaySupportingTextPaddingStart,
    end = DisplaySupportingTextPaddingEnd,
  )
  internal val contentPadding = PaddingValues(
    bottom = DisplayContentPaddingBottom,
    top = DisplayContentPaddingTop,
    start = DisplayContentPaddingStart,
    end = DisplayContentPaddingEnd,
  )

  val shape: Shape
    @Composable
    @ReadOnlyComposable
    get() = FreeTextTokens.DisplayContainerShape.value

  sealed class Height {
    data object Unlimited : Height()

    data class Limited(
      val requiredHeight: Dp = FreeTextTokens.DisplayDefaultHeight,
    ) : Height()
  }

  sealed class Style {
    data object Default : Style()

    data class Labeled(
      val labelText: String,
    ) : Style()
  }
}

private data class FreeTextDisplayColors(
  val placeHolderColor: Color,
  val textColor: Color,
  val supportingTextColor: Color,
  val labelColor: Color,
  val counterColor: Color,
)

private val displayColors
  @Composable get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      FreeTextDisplayColors(
        textColor = fromToken(TextPrimary),
        labelColor = fromToken(TextSecondaryTranslucent),
        placeHolderColor = fromToken(TextTertiary),
        counterColor = fromToken(TextTertiary),
        supportingTextColor = fromToken(TextSecondaryTranslucent),
      )
    }
  }
