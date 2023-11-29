package com.hedvig.android.feature.chat.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.onInfoElement
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Camera
import com.hedvig.android.core.icons.hedvig.normal.ChevronUp
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.feature.chat.chatfilestate.rememberChatFileState
import com.hedvig.android.logger.logcat
import hedvig.resources.R

@Composable
internal fun ChatInput(
  onSendMessage: (message: String) -> Unit,
  onSendFile: (file: Uri) -> Unit,
  appPackageId: String,
  modifier: Modifier = Modifier,
) {
  val chatFileState = rememberChatFileState(appPackageId = appPackageId) { uri ->
    logcat { "ChatFileState sending uri:$uri" }
    onSendFile(uri)
  }
  var text: String by rememberSaveable { mutableStateOf("") }
  ChatInput(
    text = text,
    setText = { text = it },
    onSendMessage = { message: String ->
      onSendMessage(message)
      text = ""
    },
    takePicture = { chatFileState.startTakePicture() },
    modifier = modifier,
  )
}

@Composable
private fun ChatInput(
  text: String,
  setText: (String) -> Unit,
  onSendMessage: (message: String) -> Unit,
  takePicture: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val buttonSize = 40.dp
  Row(
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier,
  ) {
    val chatShape = MaterialTheme.shapes.squircleMedium
    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
    Box(
      modifier = Modifier
        .size(buttonSize)
        .background(color = MaterialTheme.colorScheme.surface, shape = chatShape)
        .chatInputOutline(chatShape, outlineColor)
        .clip(chatShape)
        .clickable(onClick = takePicture)
        .wrapContentSize(unbounded = true)
        .minimumInteractiveComponentSize(),
      contentAlignment = Alignment.Center,
    ) {
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Icon(
          imageVector = Icons.Hedvig.Camera,
          contentDescription = stringResource(R.string.CHAT_UPLOAD_PRESS_SEND_LABEL),
          modifier = Modifier.size(16.dp),
        )
      }
    }
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shape = chatShape,
      modifier = Modifier
        .heightIn(min = buttonSize)
        .weight(1f)
        .chatInputOutline(chatShape, outlineColor),
    ) {
      BasicTextField(
        value = text,
        onValueChange = { setText(it) },
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Sentences,
          imeAction = ImeAction.Send,
        ),
        keyboardActions = KeyboardActions(
          onSend = { onSendMessage(text) },
        ),
        cursorBrush = SolidColor(LocalContentColor.current),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      ) { innerTextField ->
        if (text.isEmpty()) {
          Text(
            text = stringResource(R.string.CHAT_INPUT_PLACEHOLDER),
            style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
            modifier = Modifier.alpha(0.60f),
          )
        }
        innerTextField()
      }
    }
    IconButton(
      onClick = {
        onSendMessage(text)
      },
      colors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.infoElement,
        disabledContainerColor = MaterialTheme.colorScheme.infoElement,
        contentColor = MaterialTheme.colorScheme.onInfoElement,
        disabledContentColor = MaterialTheme.colorScheme.onInfoElement,
      ),
      enabled = text.isNotBlank(),
      modifier = Modifier.size(buttonSize),
    ) {
      Icon(
        imageVector = Icons.Hedvig.ChevronUp,
        contentDescription = stringResource(R.string.CHAT_UPLOAD_PRESS_SEND_LABEL),
        modifier = Modifier.size(buttonSize / 2),
      )
    }
  }
}

private fun Modifier.chatInputOutline(chatShape: Shape, outlineColor: Color): Modifier = drawWithCache {
  val stroke = Stroke(Dp.Hairline.toPx())
  val outline = chatShape.createOutline(size.copy(size.width, size.height), layoutDirection, this)
  val path = (outline as Outline.Generic).path
  onDrawWithContent {
    drawContent()
    drawPath(path, outlineColor, style = stroke)
  }
}

@HedvigPreview
@Composable
private fun PreviewChatTextInput(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasLongText: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChatInput(
        if (!hasLongText) {
          "Text"
        } else {
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed et congue lacus. Donec ac libero. ".repeat(5)
        },
        {},
        {},
        {},
      )
    }
  }
}
