package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.Buttons
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.NoButtons

@Composable
fun HedvigDialog(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  style: DialogStyle = DialogDefaults.defaultDialogStyle,
  content: @Composable () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogDefaults.defaultProperties,
  ) {
    Box(
        modifier = modifier
            .sizeIn(
                minWidth = DialogDefaults.dialogMinWidth,
                maxWidth = DialogDefaults.dialogMaxWidth,
            ),
        propagateMinConstraints = true,
    ) {
      Surface(
        shape = DialogDefaults.shape,
        color = DialogDefaults.containerColor,
        modifier = Modifier.graphicsLayer(
          shadowElevation =
          with(LocalDensity.current) { DialogDefaults.elevation.toPx() },
          shape = DialogDefaults.shape, clip = false)
      ) {
        Column {
          when (style) {
            is Buttons -> {
              content()
              Row {
                HedvigButton(
                  modifier = Modifier.weight(1f),
                  onClick = style.onDismissRequest,
                  text = style.dismissButtonText,
                  enabled = true,
                  buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
                  buttonSize = ButtonDefaults.ButtonSize.Medium,
                )
                Spacer(Modifier.width(8.dp))
                HedvigButton(
                  modifier = Modifier.weight(1f),
                  onClick = style.onConfirmButtonClick,
                  text = style.confirmButtonText,
                  enabled = true,
                  buttonStyle = ButtonDefaults.ButtonStyle.Primary,
                  buttonSize = ButtonDefaults.ButtonSize.Medium,
                )
              }
            }
            NoButtons -> content()
          }
        }
      }
    }
  }
}

object DialogDefaults {
  internal val dialogMinWidth: Dp = 100.dp //todo: get a token here instead
  internal val dialogMaxWidth: Dp = 300.dp //todo: get a token here instead
  internal val defaultButtonSize = ButtonSize.SMALL
  internal val defaultDialogStyle = NoButtons
  internal val defaultProperties = DialogProperties()
  internal val shape = RoundedCornerShape(12.dp) //todo: get a token here instead
  internal val containerColor: Color = Color.White //todo: get a token here instead
  internal val elevation = 2.dp //todo: get a token here instead


  sealed class DialogStyle {

    data class Buttons(
      val onDismissRequest: () -> Unit,
      val dismissButtonText: String,
      val onConfirmButtonClick: () -> Unit,
      val confirmButtonText: String,
      val buttonSize: ButtonSize = defaultButtonSize,
    ) : DialogStyle()

    data object NoButtons : DialogStyle()
  }

  enum class ButtonSize {
    BIG,
    SMALL
  }
}
