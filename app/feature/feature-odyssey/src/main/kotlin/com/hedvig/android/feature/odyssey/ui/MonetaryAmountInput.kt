package com.hedvig.android.feature.odyssey.ui

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.text.isDigitsOnly
import com.hedvig.android.compose.ui.preview.DoubleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface

/**
 * [onInput] guarantees that it either returns a valid [Int], or null
 */
@Composable
internal fun MonetaryAmountInput(
  value: String?,
  hintText: String,
  canInteract: Boolean,
  onInput: (String?) -> Unit,
  currency: String,
  focusRequester: FocusRequester,
  modifier: Modifier = Modifier,
) {
  var text by rememberSaveable { mutableStateOf(value ?: "") }
  val focusManager = LocalFocusManager.current
  HedvigTextField(
    text = text,
    onValueChange = onValueChange@{ newValue ->
      if (newValue.length > 10) return@onValueChange
      if (!newValue.isDigitsOnly()) return@onValueChange
      text = newValue
      onInput(newValue.ifBlank { null })
    },
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    labelText = hintText,
    modifier = modifier.focusRequester(focusRequester),
    enabled = canInteract,
    suffix = { HedvigText(currency) },
    keyboardOptions = KeyboardOptions(
      autoCorrectEnabled = false,
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions(
      onDone = {
        focusManager.clearFocus()
      },
    ),
    visualTransformation = visualTransformation@{ annotatedString: AnnotatedString ->
      val transformedString = buildAnnotatedString {
        val numberOfNonTripletNumbers = annotatedString.length % 3
        append(annotatedString.take(numberOfNonTripletNumbers))
        if (numberOfNonTripletNumbers != 0 && annotatedString.length > 3) {
          append(" ")
        }
        annotatedString.drop(numberOfNonTripletNumbers).chunked(3).forEachIndexed { index, triplet ->
          if (index != 0) append(" ")
          append(triplet)
        }
      }
      TransformedText(
        transformedString,
        MonetaryAmountOffsetMapping(annotatedString.text),
      )
    },
  )
}

@HedvigPreview
@Composable
private fun PreviewMonetaryAmountInput(
  @PreviewParameter(DoubleBooleanCollectionPreviewParameterProvider::class) input: Pair<Boolean, Boolean>,
) {
  val (hasInput: Boolean, canInteract: Boolean) = input
  HedvigTheme {
    CompositionLocalProvider(LocalTextStyle provides HedvigTheme.typography.headlineSmall) {
      Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
        MonetaryAmountInput(
          value = if (hasInput) "1234" else "",
          hintText = "Purchase price",
          canInteract = canInteract,
          onInput = {},
          currency = "SEK",
          focusRequester = remember { FocusRequester() },
        )
      }
    }
  }
}
