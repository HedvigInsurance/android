package com.hedvig.android.odyssey.input.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.core.text.isDigitsOnly
import com.hedvig.odyssey.compose.MonetaryAmountOffsetMapping
import com.hedvig.odyssey.compose.getLocale
import com.hedvig.odyssey.remote.components.decorators.Color
import com.hedvig.odyssey.remote.money.MonetaryAmount
import com.hedvig.odyssey.renderers.ComposeRenderer
import com.hedvig.odyssey.renderers.utils.composeColor
import java.text.DecimalFormatSymbols

@Composable
@ComposeRenderer
fun MonetaryAmountInput(
  onInput: (MonetaryAmount?) -> Unit,
  currency: String,
  maximumFractionDigits: Int,
  focusRequester: FocusRequester,
  modifier: Modifier = Modifier,
) {
  val locale = getLocale()
  val decimalSeparator = remember(locale) { DecimalFormatSymbols.getInstance(locale).decimalSeparator }

  var text by rememberSaveable { mutableStateOf("") }
  val isError by remember { derivedStateOf { text.lastOrNull() == decimalSeparator } }

  val cursorColor = composeColor(if (isError) Color.content.destructive else Color.button.primaryBackground)
  CompositionLocalProvider(
    LocalTextSelectionColors.provides(
      TextSelectionColors(
        handleColor = cursorColor,
        backgroundColor = cursorColor.copy(alpha = 0.4f),
      ),
    ),
  ) {
    val mediumContentAlpha = ContentAlpha.medium
    val localContentColor = LocalContentColor.current
    BasicTextField(
      value = text,
      onValueChange = onValueChange@{ newValue ->
        if (newValue.length > 10) return@onValueChange
        if (!allowsDecimals(maximumFractionDigits) && !newValue.isDigitsOnly()) return@onValueChange
        if (!newValue.all { it.isDigit() || it == decimalSeparator }) return@onValueChange
        if (newValue.count { it == decimalSeparator } > 1) return@onValueChange
        if (newValue.length == 1 && newValue.first() == decimalSeparator) {
          text = ""
          return@onValueChange
        }
        val numberOfDecimalDigits = newValue.substringAfter(decimalSeparator, missingDelimiterValue = "").length
        if (numberOfDecimalDigits > maximumFractionDigits) return@onValueChange
        text = newValue
        if (newValue.last() != decimalSeparator) {
          val monetaryAmount = if (newValue.isBlank()) {
            null
          } else {
            MonetaryAmount(
              newValue.ifBlank { null },
              currency,
            )
          }
          onInput(monetaryAmount)
        }
      },
      modifier = modifier
        .fillMaxWidth()
        .focusRequester(focusRequester),
      textStyle = LocalTextStyle.current.copy(
        textAlign = TextAlign.End,
        color = localContentColor,
      ),
      keyboardOptions = KeyboardOptions(
        autoCorrect = false,
        keyboardType = if (allowsDecimals(maximumFractionDigits)) KeyboardType.Number else KeyboardType.Decimal,
        imeAction = ImeAction.Done,
      ),
      keyboardActions = KeyboardActions(),
      singleLine = true,
      visualTransformation = visualTransformation@{ annotatedString: AnnotatedString ->
        val transformedString = buildAnnotatedString {
          val numbersBeforeDecimal = annotatedString.split(decimalSeparator).first()
          val hasDecimalSeparator = annotatedString.contains(decimalSeparator)
          val numbersAfterDecimal = annotatedString.split(decimalSeparator).getOrNull(1)

          val numberOfNonTripletNumbers = numbersBeforeDecimal.length % 3
          append(numbersBeforeDecimal.take(numberOfNonTripletNumbers))
          if (numberOfNonTripletNumbers != 0 && numbersBeforeDecimal.length > 3) {
            append(" ")
          }
          numbersBeforeDecimal.drop(numberOfNonTripletNumbers).chunked(3).forEachIndexed { index, triplet ->
            if (index != 0) append(" ")
            append(triplet)
          }
          if (hasDecimalSeparator) {
            append(decimalSeparator)
            if (numbersAfterDecimal != null) {
              append(numbersAfterDecimal)
            }
          }
          append(" ")
          withStyle(SpanStyle(localContentColor.copy(mediumContentAlpha))) {
            append(currency)
          }
        }
        TransformedText(
          transformedString,
          MonetaryAmountOffsetMapping(annotatedString.text, decimalSeparator),
        )
      },
      cursorBrush = SolidColor(cursorColor),
      decorationBox = { innerTextField ->
        BoxWithTrailingIcon(
          textField = innerTextField,
          showTrailingIcon = text.isNotEmpty(),
          onClick = {
            text = ""
            focusRequester.freeFocus()
          },
        )
      },
    )
  }
}

@Composable
private fun BoxWithTrailingIcon(
  textField: @Composable () -> Unit,
  showTrailingIcon: Boolean,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.End,
    modifier = Modifier.fillMaxWidth(),
  ) {
    textField()
    AnimatedVisibility(showTrailingIcon) {
      IconButton(onClick = onClick) {
        Icon(Icons.Default.Clear, "Clear Selection")
      }
    }
  }
}

private fun allowsDecimals(maximumFractionDigits: Int): Boolean = maximumFractionDigits != 0
