package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hedvig.android.design.system.hedvig.DialogDefaults.ButtonSize.BIG
import com.hedvig.android.design.system.hedvig.DialogDefaults.ButtonSize.SMALL
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.Buttons
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.NoButtons
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SurfacePrimary
import com.hedvig.android.design.system.hedvig.tokens.DialogTokens
import kotlin.math.ln

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
      Surface(
        shape = DialogDefaults.shape,
        color = DialogDefaults.containerColor,
        modifier = modifier
          .graphicsLayer(
            shadowElevation =
            with(LocalDensity.current) { DialogDefaults.elevation.toPx() },
            shape = DialogDefaults.shape,
            clip = false,
          )
//          .shadow(
//            elevation = DialogDefaults.elevation,
//          ),
      ) {
        Column(
          Modifier.padding(DialogDefaults.padding),
        ) {
          when (style) {
            is Buttons -> {
              content()
              Spacer(Modifier.height(16.dp))
              when (style.buttonSize) {
                BIG -> {
                  BigVerticalButtons(
                    onDismissRequest = style.onDismissRequest,
                    dismissButtonText = style.dismissButtonText,
                    onConfirmButtonClick = style.onConfirmButtonClick,
                    confirmButtonText = style.confirmButtonText,
                  )
                }
                SMALL -> {
                  SmallHorizontalButtons(
                    onDismissRequest = style.onDismissRequest,
                    dismissButtonText = style.dismissButtonText,
                    onConfirmButtonClick = style.onConfirmButtonClick,
                    confirmButtonText = style.confirmButtonText,
                  )
                }
              }
            }
            NoButtons -> content()
          }
        }
      }
    }
}

@Composable
internal fun SmallHorizontalButtons(
  onDismissRequest: () -> Unit,
  dismissButtonText: String,
  onConfirmButtonClick: () -> Unit,
  confirmButtonText: String,
) {
  Row {
    HedvigButton(
      modifier = Modifier.weight(1f),
      onClick = onDismissRequest,
      text = dismissButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      buttonSize = ButtonDefaults.ButtonSize.Medium,
    )
    Spacer(Modifier.width(8.dp))
    HedvigButton(
      modifier = Modifier.weight(1f),
      onClick = onConfirmButtonClick,
      text = confirmButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Primary,
      buttonSize = ButtonDefaults.ButtonSize.Medium,
    )
  }
}

@Composable
internal fun BigVerticalButtons(
  onDismissRequest: () -> Unit,
  dismissButtonText: String,
  onConfirmButtonClick: () -> Unit,
  confirmButtonText: String,
) {
  Column {
    HedvigButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = onConfirmButtonClick,
      text = confirmButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Primary,
      buttonSize = ButtonDefaults.ButtonSize.Large,
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = onDismissRequest,
      text = dismissButtonText,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      buttonSize = ButtonDefaults.ButtonSize.Large,
    )
  }
}

object DialogDefaults {
  internal val defaultButtonSize = SMALL
  internal val defaultDialogStyle = NoButtons
  internal val defaultProperties = DialogProperties()

  internal val shape: Shape
    @Composable
    @ReadOnlyComposable
    get() = DialogTokens.ContainerShape.value

  internal val containerColor: Color
    @Composable
    get() = with(HedvigTheme.colorScheme) {
      remember(this) {
        fromToken(DialogTokens.ContainerColor)
      }
    }

  internal val elevation = DialogTokens.ShadowElevation
  internal val padding = PaddingValues(DialogTokens.Padding)

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
    SMALL,
  }
}

@Composable
internal fun elevationColor(): Color {
  return Color.White
//  return with(HedvigTheme.colorScheme) {
//    val elevation = DialogTokens.TonalElevation
//    val surfaceColor = fromToken(SurfacePrimary)
//    val surfaceTint = fromToken(DialogTokens.ElevationColor)
//    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
//    surfaceTint.copy(alpha = alpha).compositeOver(surfaceColor)
//  }
}
