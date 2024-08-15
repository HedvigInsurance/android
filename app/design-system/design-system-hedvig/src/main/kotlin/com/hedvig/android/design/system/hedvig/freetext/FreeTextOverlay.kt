package com.hedvig.android.design.system.hedvig.freetext

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDecorationBox
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTextFieldSize
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.colors
import com.hedvig.android.design.system.hedvig.configuration
import com.hedvig.android.design.system.hedvig.fromToken
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.BackgroundBlack
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SurfacePrimary
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextPrimary
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextTertiary
import com.hedvig.android.design.system.hedvig.tokens.FreeTextTokens
import com.hedvig.android.design.system.hedvig.value
import hedvig.resources.R

@Composable
fun FreeTextOverlay(
  overlaidContent: @Composable () -> Unit,
  freeTextValue: String?,
  freeTextHint: String,
  freeTextOnSaveClick: (String?) -> Unit,
  freeTextOnCancelClick: () -> Unit,
  shouldShowOverlay: Boolean,
  modifier: Modifier = Modifier,
  freeTextMaxLength: Int = FreeTextDefaults.maxLength,
  cancelButtonText: String? = null,
  confirmButtonText: String? = null,
  // the content that shows on the screen first and triggers the visibility of free text full-screen overlay.
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
    modifier = Modifier.fillMaxSize(),
  ) { showFullScreenEditText: Boolean ->
    Box(Modifier.fillMaxSize()) {
      if (showFullScreenEditText) {
        HedvigTheme(darkTheme = true) {
          Surface(
            color = freeTextColors.backgroundColor,
            modifier = Modifier.fillMaxSize(),
          ) {
            FreeTextOverlayContent(
              freeTextValue = freeTextValue,
              hintText = hintText,
              onSaveClick = onSaveClick,
              onCancelClick = onCancelClick,
              textMaxLength = textMaxLength,
              modifier = modifier,
              cancelButtonText = cancelButtonText,
              confirmButtonText = confirmButtonText,
            )
          }
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
      .imePadding()
      .safeContentPadding()
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
          Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.weight(1f),
          ) {
            HedvigTextFieldDecorationBox(
              value = textValue.text,
              colors = HedvigTextFieldDefaults.colors(
                containerColor = Color.Transparent,
              ), // todo???? indication etc?
              label = {
                if (freeTextValue == null) {
                  HedvigText(
                    text = hintText,
                    modifier = Modifier.fillMaxSize(),
                    style = FreeTextDefaults.textStyle.value,
                  )
                } else {
                }
              },
              innerTextField = innerTextField,
              enabled = true,
              //  singleLine = false,
              interactionSource = remember { MutableInteractionSource() },
              visualTransformation = VisualTransformation.None,
              configuration = HedvigTextFieldDefaults.configuration(), // ??? todo
              size = HedvigTextFieldSize.Large, // ??? todo
            )
          }
          Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
          ) {
            HedvigText(
              text = "${textValue.text.length}/$textMaxLength",
              style = FreeTextDefaults.countLabelStyle.value,
              color = freeTextColors.labelColor,
            )
          }
        }
      },
    )
    Spacer(modifier = Modifier.height(8.dp))
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

fun TextFieldValue.ofMaxLength(maxLength: Int): TextFieldValue {
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
  val textStyle = FreeTextTokens.TextStyle
  val countLabelStyle = FreeTextTokens.countLabel
  val shape
    @Composable
    @ReadOnlyComposable
    get() = FreeTextTokens.ContainerShape.value
}

private data class FreeTextColors(
  val backgroundColor: Color,
  val cursorBrushColor: Color,
  val textFieldColor: Color,
  val textColor: Color,
  val labelColor: Color,
)

private val freeTextColors: FreeTextColors
  @Composable get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      FreeTextColors(
        backgroundColor = fromToken(BackgroundBlack),
        cursorBrushColor = fromToken(TextPrimary),
        textFieldColor = fromToken(SurfacePrimary),
        textColor = fromToken(TextPrimary),
        labelColor = fromToken(TextTertiary),
      )
    }
  }
