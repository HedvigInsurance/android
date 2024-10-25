package com.hedvig.android.compose.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit

fun stringWithShiftedLabel(
  text: String,
  labelText: String,
  textFontSize: TextUnit,
  labelFontSize: TextUnit,
  textColor: Color,
): AnnotatedString {
  return buildAnnotatedString {
    withStyle(
      SpanStyle(
        fontSize = textFontSize,
        color = textColor,
      ),
    ) {
      append(text)
    }
    withStyle(
      SpanStyle(
        baselineShift = BaselineShift(0.3f),
        fontSize = labelFontSize,
        color = textColor,
      ),
    ) {
      append(labelText)
    }
  }
}
