package com.hedvig.android.feature.chat.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.photo.capture.state.rememberPhotoCaptureState
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.onInfoElement
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Camera
import com.hedvig.android.core.icons.hedvig.normal.ChevronRight
import com.hedvig.android.core.icons.hedvig.normal.ChevronUp
import com.hedvig.android.core.icons.hedvig.normal.Document
import com.hedvig.android.core.icons.hedvig.normal.Pictures
import com.hedvig.android.logger.logcat
import hedvig.resources.R

@Composable
internal fun ChatInput(
  onSendMessage: (message: String) -> Unit,
  onSendPhoto: (file: Uri) -> Unit,
  onSendMedia: (file: Uri) -> Unit,
  appPackageId: String,
  modifier: Modifier = Modifier,
) {
  var expandChatOptions by rememberSaveable { mutableStateOf(true) }
  var text: String by rememberSaveable { mutableStateOf("") }
  val photoCaptureState = rememberPhotoCaptureState(appPackageId = appPackageId) { uri ->
    logcat { "ChatFileState sending uri:$uri" }
    onSendPhoto(uri)
  }
  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
  ) { resultingUri: Uri? ->
    if (resultingUri != null) {
      onSendMedia(resultingUri)
    }
  }
  val filePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { resultingUri: Uri? ->
    if (resultingUri != null) {
      onSendMedia(resultingUri)
    }
  }
  ChatInput(
    text = text,
    setText = {
      text = it
      expandChatOptions = false
    },
    areChatOptionsExpanded = expandChatOptions,
    expandChatOptions = { expandChatOptions = true },
    onSendMessage = { message: String ->
      onSendMessage(message)
      text = ""
    },
    takePicture = { photoCaptureState.launchTakePhotoRequest() },
    selectMedia = { photoPicker.launch(PickVisualMediaRequest()) },
    selectFile = { filePicker.launch("*/*") },
    modifier = modifier,
  )
}

@Composable
private fun ChatInput(
  text: String,
  setText: (String) -> Unit,
  areChatOptionsExpanded: Boolean,
  expandChatOptions: () -> Unit,
  onSendMessage: (message: String) -> Unit,
  takePicture: () -> Unit,
  selectMedia: () -> Unit,
  selectFile: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier,
  ) {
    val chatShape = MaterialTheme.shapes.squircleMedium
    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
    ChatOptions(
      areChatOptionsExpanded = areChatOptionsExpanded,
      takePicture = takePicture,
      shape = chatShape,
      outlineColor = outlineColor,
      selectMedia = selectMedia,
      selectFile = selectFile,
      expandChatOptions = expandChatOptions,
    )
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shape = MaterialTheme.shapes.squircleMedium,
      modifier = Modifier
        .heightIn(min = buttonSize)
        .weight(1f)
        .chatInputOutline(chatShape, outlineColor),
    ) {
      Row(verticalAlignment = Alignment.Bottom) {
        BasicTextField(
          value = text,
          onValueChange = { setText(it) },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.None,
          ),
          cursorBrush = SolidColor(LocalContentColor.current),
          textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
          modifier = Modifier.weight(1f).padding(vertical = 8.dp).padding(start = 12.dp),
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
        Box(
          modifier = Modifier
            .size(buttonSize)
            .wrapContentSize()
            .size(24.dp)
            .background(color = MaterialTheme.colorScheme.infoElement, shape = chatShape)
            .chatInputOutline(chatShape, outlineColor)
            .clip(CircleShape)
            .clickable(enabled = text.isNotBlank()) { onSendMessage(text) }
            .wrapContentSize(unbounded = true)
            .minimumInteractiveComponentSize(),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Hedvig.ChevronUp,
            contentDescription = stringResource(R.string.CHAT_UPLOAD_PRESS_SEND_LABEL),
            tint = MaterialTheme.colorScheme.onInfoElement,
            modifier = Modifier.size(12.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun ChatOptions(
  areChatOptionsExpanded: Boolean,
  takePicture: () -> Unit,
  shape: Shape,
  outlineColor: Color,
  selectMedia: () -> Unit,
  selectFile: () -> Unit,
  expandChatOptions: () -> Unit,
) {
  val showExpandMoreTransition = updateTransition(targetState = areChatOptionsExpanded)
  showExpandMoreTransition.AnimatedContent(
    transitionSpec = {
      val enter = fadeIn(animationSpec = tween(220, delayMillis = 90))
      val exit = fadeOut(animationSpec = tween(90))
      ContentTransform(
        enter,
        exit,
        sizeTransform = SizeTransform(clip = false),
      )
    },
    contentAlignment = Alignment.CenterStart,
  ) { showExpandedOptions ->
    if (showExpandedOptions) {
      Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ChatClickableSquare(
          onClick = takePicture,
          imageVector = Icons.Hedvig.Camera,
          contentDescription = stringResource(R.string.KEY_GEAR_ADD_ITEM_ADD_PHOTO_BUTTON),
          chatShape = shape,
          outlineColor = outlineColor,
        )
        ChatClickableSquare(
          onClick = selectMedia,
          imageVector = Icons.Hedvig.Pictures,
          contentDescription = stringResource(R.string.KEY_GEAR_ADD_ITEM_ADD_PHOTO_BUTTON),
          chatShape = shape,
          outlineColor = outlineColor,
        )
        ChatClickableSquare(
          onClick = selectFile,
          imageVector = Icons.Hedvig.Document,
          contentDescription = stringResource(R.string.KEY_GEAR_ADD_ITEM_ADD_PHOTO_BUTTON),
          chatShape = shape,
          outlineColor = outlineColor,
        )
      }
    } else {
      IconButton(
        onClick = expandChatOptions,
        colors = IconButtonDefaults.iconButtonColors(
          containerColor = Color.Transparent,
          contentColor = LocalContentColor.current,
        ),
        modifier = Modifier.size(buttonSize),
      ) {
        Icon(
          imageVector = Icons.Hedvig.ChevronRight,
          contentDescription = null,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun ChatClickableSquare(
  onClick: () -> Unit,
  imageVector: ImageVector,
  contentDescription: String?,
  chatShape: Shape,
  outlineColor: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .size(buttonSize)
      .background(color = MaterialTheme.colorScheme.surface, shape = chatShape)
      .chatInputOutline(chatShape, outlineColor)
      .clip(chatShape)
      .clickable(onClick = onClick)
      .wrapContentSize(unbounded = true)
      .minimumInteractiveComponentSize(),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = imageVector,
      contentDescription = contentDescription,
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(16.dp),
    )
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

private val buttonSize = 40.dp

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
        true,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
