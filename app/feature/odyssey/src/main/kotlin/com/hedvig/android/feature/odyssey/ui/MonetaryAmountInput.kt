package com.hedvig.android.feature.odyssey.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.preview.DoubleBooleanCollectionPreviewParameterProvider
import java.text.DecimalFormatSymbols

/**
 * [onInput] guarantees that it either returns a valid double, or null
 */
@Composable
internal fun MonetaryAmountInput(
  value: String?,
  hintText: String,
  canInteract: Boolean,
  onInput: (String?) -> Unit,
  currency: String,
  maximumFractionDigits: Int,
  focusRequester: FocusRequester,
  modifier: Modifier = Modifier,
) {
  val locale = getLocale()
  val decimalSeparator = remember(locale) { DecimalFormatSymbols.getInstance(locale).decimalSeparator }

  var text by rememberSaveable { mutableStateOf(value ?: "") }
  val isError by remember { derivedStateOf { text.lastOrNull() == decimalSeparator } }
  val focusManager = LocalFocusManager.current

  val cursorColor = if (isError) MaterialTheme.colorScheme.error else LocalContentColor.current
  CompositionLocalProvider(
    LocalTextSelectionColors.provides(
      TextSelectionColors(
        handleColor = cursorColor,
        backgroundColor = cursorColor.copy(alpha = DisabledAlpha),
      ),
    ),
    LocalContentColor.provides(
      if (canInteract) {
        LocalContentColor.current
      } else {
        LocalContentColor.current.copy(alpha = DisabledAlpha).compositeOver(MaterialTheme.colorScheme.surface)
      },
    ),
  ) {
    ProvideTextStyle(LocalTextStyle.current.copy(color = LocalContentColor.current)) {
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
            onInput(newValue.ifBlank { null })
          }
        },
        modifier = modifier
          .fillMaxWidth()
          .focusRequester(focusRequester),
        textStyle = LocalTextStyle.current,
        keyboardOptions = KeyboardOptions(
          autoCorrect = false,
          keyboardType = if (allowsDecimals(maximumFractionDigits)) KeyboardType.Number else KeyboardType.Decimal,
          imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
          onDone = {
            focusManager.clearFocus()
          },
        ),
        enabled = canInteract,
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
          }
          TransformedText(
            transformedString,
            MonetaryAmountOffsetMapping(annotatedString.text, decimalSeparator),
          )
        },
        cursorBrush = SolidColor(cursorColor),
        decorationBox = { innerTextField ->
          DecoratingBox(
            textField = innerTextField,
            text = text,
            hintText = hintText,
            currencyCode = currency,
            showTrailingIcon = text.isNotEmpty(),
            canInteract = canInteract,
            onClick = {
              text = ""
              focusManager.clearFocus()
            },
          )
        },
      )
    }
  }
}

@Composable
private fun DecoratingBox(
  textField: @Composable () -> Unit,
  text: String?,
  hintText: String,
  currencyCode: String,
  showTrailingIcon: Boolean,
  canInteract: Boolean,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 16.dp),
  ) {
    Box {
      if (text.isNullOrEmpty()) {
        Text(text = hintText, modifier = Modifier.matchParentSize())
      }
      textField()
    }
    Spacer(modifier = Modifier.weight(1f))
    Spacer(modifier = Modifier.width(8.dp))
    AnimatedVisibility(text.isNullOrEmpty() == false) {
      Row {
        Text(text = currencyCode)
        Spacer(modifier = Modifier.width(8.dp))
      }
    }
    AnimatedVisibility(showTrailingIcon) {
      IconButton(
        onClick = onClick,
        enabled = canInteract,
      ) {
        Icon(Icons.Default.Clear, "Clear Selection")
      }
    }
  }
}

private fun allowsDecimals(maximumFractionDigits: Int): Boolean = maximumFractionDigits != 0

@HedvigPreview
@Composable
private fun PreviewMonetaryAmountInput(
  @PreviewParameter(DoubleBooleanCollectionPreviewParameterProvider::class) input: Pair<Boolean, Boolean>,
) {
  val (hasInput: Boolean, canInteract: Boolean) = input
  HedvigTheme {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineSmall) {
      Surface(color = MaterialTheme.colorScheme.background) {
        MonetaryAmountInput(
          value = if (hasInput) "1234" else "",
          hintText = "Hint",
          canInteract = canInteract,
          onInput = {},
          currency = "SEK",
          maximumFractionDigits = 2,
          focusRequester = remember { FocusRequester() },
        )
      }
    }
  }
}
