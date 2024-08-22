package com.hedvig.android.design.system.hedvig.freetext

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDefaults.counterPadding
import com.hedvig.android.design.system.hedvig.fromToken
import com.hedvig.android.design.system.hedvig.internal.Decoration
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.BackgroundBlack
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalAmberElement
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SurfacePrimary
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextPrimary
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextTertiary
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens
import com.hedvig.android.design.system.hedvig.value
import hedvig.resources.R

@Composable
fun FreeTextOverlay(
  overlaidContent: @Composable () -> Unit,
  // the content that shows on the screen first and triggers the visibility of full-screen freeTextOverlay -
  // for example, FreeTextDisplay or some Button.
  freeTextValue: String?,
  freeTextHint: String,
  freeTextOnSaveClick: (String?) -> Unit,
  freeTextOnCancelClick: () -> Unit,
  modifier: Modifier = Modifier,
  shouldShowOverlay: Boolean = false,
  freeTextMaxLength: Int = FreeTextDefaults.maxLength,
  cancelButtonText: String? = null,
  confirmButtonText: String? = null,
) {
  Box(modifier) {
    overlaidContent()
    FreeTextOverlayAnimated(
      shouldShowOverlay = shouldShowOverlay,
      freeTextValue = freeTextValue,
      hintText = freeTextHint,
      onSaveClick = freeTextOnSaveClick,
      onCancelClick = freeTextOnCancelClick,
      textMaxLength = freeTextMaxLength,
      cancelButtonText = cancelButtonText,
      confirmButtonText = confirmButtonText,
    )
  }
}

@Composable
private fun FreeTextOverlayAnimated(
  shouldShowOverlay: Boolean,
  freeTextValue: String?,
  hintText: String,
  onSaveClick: (String?) -> Unit,
  onCancelClick: () -> Unit,
  textMaxLength: Int,
  modifier: Modifier = Modifier,
  cancelButtonText: String? = null,
  confirmButtonText: String? = null,
) {
  val showFullScreenEditTextTransition = updateTransition(
    shouldShowOverlay,
    "showFullScreenEditTextTransition",
  )
  showFullScreenEditTextTransition.AnimatedContent(
    transitionSpec = {
      fadeIn() togetherWith fadeOut()
    },
    modifier = modifier.fillMaxSize(),
  ) { showFullScreenEditText: Boolean ->
    Box(Modifier.fillMaxSize(), propagateMinConstraints = true) {
      if (showFullScreenEditText) {
        Surface(color = freeTextColors.backgroundColor) {
          FreeTextOverlayContent(
            freeTextValue = freeTextValue,
            hintText = hintText,
            onSaveClick = onSaveClick,
            onCancelClick = onCancelClick,
            textMaxLength = textMaxLength,
            modifier = Modifier,
            cancelButtonText = cancelButtonText,
            confirmButtonText = confirmButtonText,
          )
        }
      }
    }
  }
}

@Composable
private fun FreeTextOverlayContent(
  freeTextValue: String?,
  hintText: String,
  onSaveClick: (String?) -> Unit,
  onCancelClick: () -> Unit,
  textMaxLength: Int,
  modifier: Modifier = Modifier,
  cancelButtonText: String? = null,
  confirmButtonText: String? = null,
) {
  val focusManager = LocalFocusManager.current
  val focusRequester = remember { FocusRequester() }
  var textValue by remember {
    mutableStateOf(
      TextFieldValue(
        text = freeTextValue ?: "",
        selection = TextRange((freeTextValue ?: "").length),
      ),
    )
  }
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }
  BackHandler(true) {
    focusManager.clearFocus()
    onCancelClick()
  }
  Column(
    modifier
      .fillMaxSize()
      .safeDrawingPadding()
      .padding(FreeTextDefaults.fieldPadding),
  ) {
    BasicTextField(
      value = textValue,
      onValueChange = {
        textValue = it.ofMaxLength(textMaxLength)
      },
      cursorBrush = SolidColor(freeTextColors.cursorBrushColor),
      modifier = Modifier
        .weight(1f)
        .focusRequester(focusRequester)
        .background(
          color = freeTextColors.textFieldColor,
          shape = FreeTextDefaults.shape,
        ),
      textStyle = FreeTextDefaults.textStyle.value.copy(color = freeTextColors.textColor),
      decorationBox = @Composable { innerTextField ->
        Column {
          HedvigFreeTextDecorationBox(
            value = textValue.text,
            placeholder = {
              HedvigText(
                text = hintText,
                modifier = Modifier.fillMaxSize(),
                style = FreeTextDefaults.textStyle.value,
              )
            },
            innerTextField = innerTextField,
            visualTransformation = VisualTransformation.None,
            contentPadding = FreeTextDefaults.textPadding,
            container = {
              Box(
                Modifier.background(
                  color = freeTextColors.textFieldColor,
                  shape = FreeTextDefaults.shape,
                ),
              )
            },
            modifier = Modifier
              .weight(1f)
              .wrapContentSize(Alignment.TopStart),
          )

          HedvigText(
            text = "${textValue.text.length}/$textMaxLength",
            style = FreeTextDefaults.countLabelStyle.value,
            color = freeTextColors.labelColor,
            modifier = Modifier
              .fillMaxWidth()
              .padding(counterPadding)
              .wrapContentWidth(Alignment.End),
          )
        }
      },
    )
    Spacer(modifier = Modifier.height(8.dp))
    HedvigTheme(
      darkTheme = true,
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
      ) {
        HedvigButton(
          enabled = true,
          text = cancelButtonText ?: stringResource(id = R.string.general_cancel_button),
          onClick = {
            focusManager.clearFocus()
            onCancelClick()
          },
          buttonSize = ButtonSize.Medium,
          buttonStyle = Secondary,
          modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        HedvigButton(
          enabled = true,
          text = confirmButtonText ?: stringResource(id = R.string.general_save_button),
          onClick = {
            focusManager.clearFocus()
            val valueToSave = textValue.text.ifEmpty { null }
            onSaveClick(valueToSave)
          },
          buttonSize = ButtonSize.Medium,
          buttonStyle = Primary,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

private fun TextFieldValue.ofMaxLength(maxLength: Int): TextFieldValue {
  val overLength = text.length - maxLength
  return if (overLength > 0) {
    val headIndex = selection.end - overLength
    val trailIndex = selection.end
    if (headIndex >= 0) {
      copy(
        text = text.substring(0, headIndex) + text.substring(trailIndex, text.length),
        selection = TextRange(headIndex),
      )
    } else {
      copy(text.take(maxLength), selection = TextRange(maxLength))
    }
  } else {
    this
  }
}

internal object FreeTextDefaults {
  val maxLength: Int = FreeTextTokens.TextDefaultMaxLength
  val fieldPadding: PaddingValues = PaddingValues(
    top = FreeTextTokens.FieldTopPadding,
    bottom = FreeTextTokens.FieldPadding,
    start = FreeTextTokens.FieldPadding,
    end = FreeTextTokens.FieldPadding,
  )
  val textPadding = PaddingValues(FreeTextTokens.TextPadding)
  val counterPadding = PaddingValues(
    start = FreeTextTokens.OverlayCounterPaddingStart,
    bottom = FreeTextTokens.OverlayCounterPaddingBottom,
    end = FreeTextTokens.OverlayCounterPaddingEnd,
    top = FreeTextTokens.OverlayCounterPaddingTop,
  )
  val textStyle = FreeTextTokens.TextStyle
  val countLabelStyle = FreeTextTokens.CountLabel
  val shape
    @Composable
    @ReadOnlyComposable
    get() = FreeTextTokens.ContainerShape.value
}

internal data class FreeTextColors(
  val backgroundColor: Color,
  val cursorBrushColor: Color,
  val textFieldColor: Color,
  val textColor: Color,
  val labelColor: Color,
  val hintColor: Color,
  val displayContainerColor: Color,
  val warningIconColor: Color,
)

internal val freeTextColors: FreeTextColors
  @Composable get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      FreeTextColors(
        backgroundColor = fromToken(BackgroundBlack),
        cursorBrushColor = fromToken(TextPrimary),
        textFieldColor = fromToken(SurfacePrimary),
        textColor = fromToken(TextPrimary),
        labelColor = fromToken(TextTertiary),
        hintColor = fromToken(TextTertiary),
        displayContainerColor = fromToken(SurfacePrimary),
        warningIconColor = fromToken(SignalAmberElement),
      )
    }
  }

@Composable
private fun HedvigFreeTextDecorationBox(
  value: String,
  innerTextField: @Composable () -> Unit,
  visualTransformation: VisualTransformation,
  contentPadding: PaddingValues,
  container: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  placeholder: @Composable (() -> Unit)? = null,
) {
  val transformedText = remember(value, visualTransformation) {
    visualTransformation.filter(AnnotatedString(value))
  }.text.text

  val decoratedPlaceholder: @Composable ((Modifier) -> Unit)? =
    if (placeholder != null && transformedText.isEmpty()) {
      @Composable { modifier ->
        Box(modifier) {
          Decoration(
            contentColor = freeTextColors.hintColor,
            typography = FreeTextDefaults.textStyle.value,
            content = placeholder,
          )
        }
      }
    } else {
      null
    }
  Box(
    modifier.padding(contentPadding),
    content = {
      container()
      if (decoratedPlaceholder != null) {
        decoratedPlaceholder(
          Modifier.layoutId("placeholder"),
        )
      }
      Box(
        modifier = Modifier.layoutId("inner text field"),
        propagateMinConstraints = true,
      ) {
        innerTextField()
      }
    },
  )
}
