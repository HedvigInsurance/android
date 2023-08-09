package com.hedvig.app.feature.chat.ui

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronUp

class ChatTextInput : AbstractComposeView {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr,
  )

  var placeholderText: String by mutableStateOf(resources.getString(hedvig.resources.R.string.CHAT_INPUT_PLACEHOLDER))
  var text: String by mutableStateOf("")
  var onSendMessageListener: (() -> Unit)? by mutableStateOf(null)

  override fun onSaveInstanceState(): Parcelable {
    val root = super.onSaveInstanceState()
    return bundleOf(
      "root" to root,
      "text" to text,
    )
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state is Bundle && state.containsKey("text")) {
      text = state.getString("text")!!
      val root: Parcelable? = state.parcelable("root")
      super.onRestoreInstanceState(root)
    } else {
      super.onRestoreInstanceState(state)
    }
  }

  @Composable
  override fun Content() {
    HedvigTheme {
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = contentColorFor(MaterialTheme.colorScheme.onSurfaceVariant),
        shape = MaterialTheme.shapes.squircle,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          val showPlaceholder by remember { derivedStateOf { text.isEmpty() } }
          BasicTextField(
            value = text,
            onValueChange = { text = it },
            keyboardOptions = KeyboardOptions(
              capitalization = KeyboardCapitalization.Sentences,
              imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(
              onSend = { onSendMessageListener?.invoke() },
            ),
            cursorBrush = SolidColor(LocalContentColor.current),
            textStyle = LocalTextStyle.current.copy(
              color = LocalContentColor.current,
            ),
            modifier = Modifier
              .weight(1f)
              .padding(
                start = 16.dp,
                top = 8.dp,
                bottom = 8.dp,
              )
              .graphicsLayer {
                alpha = if (showPlaceholder) {
                  0.60f
                } else {
                  1f
                }
              },
          ) { innerTextField ->
            if (showPlaceholder) {
              Text(
                text = placeholderText,
                fontSize = 15.sp,
                textAlign = TextAlign.Start,
              )
            }
            innerTextField()
          }
          IconButton(
            onClick = {
              onSendMessageListener?.invoke()
            },
            colors = IconButtonDefaults.iconButtonColors(
              containerColor = MaterialTheme.colorScheme.infoElement,
              disabledContainerColor = MaterialTheme.colorScheme.infoElement,
              contentColor = MaterialTheme.colorScheme.surface,
              disabledContentColor = MaterialTheme.colorScheme.surface,
            ),
            enabled = onSendMessageListener != null && text.isNotBlank(),
            modifier = Modifier
              .align(Alignment.CenterVertically)
              .size(24.dp),
          ) {
            Icon(
              imageVector = Icons.Hedvig.ChevronUp,
              contentDescription = stringResource(hedvig.resources.R.string.CHAT_UPLOAD_PRESS_SEND_LABEL),
              modifier = Modifier.size(14.dp),
            )
          }
          Spacer(Modifier.width(8.dp))
        }
      }
    }
  }
}
