package com.hedvig.android.design.system.hedvig.freetext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Height
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Height.Limited
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Height.Unlimited
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Style
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.defaultHeight
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.defaultStyle
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens
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
      Column {
        if (style is Style.Labeled) {
          //todo: label
        }
        HedvigText(
          text = freeTextValue ?: freeTextPlaceholder,
          style = FreeTextDisplayDefaults.textStyle.value,
          color = if (freeTextValue != null) Color.Black else Color.Gray, //todo: real colors here
        )
      }

      Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 13.dp),  // todo: real value and into token here
      ) {
        AnimatedVisibility(textValue.length == maxLength) {
          Icon(HedvigIcons.WarningFilled, null, tint = freeTextColors.warningIconColor)
        }
        Spacer(Modifier.width(2.dp))
        HedvigText(
          text = "${textValue.length}/$maxLength",
          style = FreeTextDisplayDefaults.countLabelStyle.value,
          color = Color.Black, // todo: real value and into token here
        )
      }
    }
    AnimatedVisibility(hasError) {
      if (hasError && supportingText != null) {
        HedvigText(
          text = supportingText,
          color = Color.Black, // todo: real value and into token here
          style = FreeTextDisplayDefaults.countLabelStyle.value,
          modifier = Modifier.padding(16.dp), // todo: real value and into token here
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

  val shape: Shape
    @Composable
    @ReadOnlyComposable
    get() = FreeTextTokens.DisplayContainerShape.value

  sealed class Height {
    data object Unlimited : Height()
    data class Limited(
      val requiredHeight: Dp = 124.dp, // todo: real value and into token here
    ) : Height()
  }

  sealed class Style {
    data object Default : Style()
    data class Labeled(
      val labelText: String,
    ) : Style()
  }
}
