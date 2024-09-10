package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.hedvig.android.design.system.hedvig.DialogDefaults.ButtonSize.BIG
import com.hedvig.android.design.system.hedvig.DialogDefaults.ButtonSize.SMALL
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.Buttons
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.NoButtons
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.tokens.DialogTokens
import hedvig.resources.R

@Composable
fun HedvigDialogError(
  titleText: String,
  descriptionText: String,
  buttonText: String,
  onButtonClick: () -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigDialog(
    style = NoButtons,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
  ) {
    EmptyState(
      text = titleText,
      description = descriptionText,
      iconStyle = ERROR,
      buttonStyle = EmptyStateButtonStyle.Button(
        buttonText = buttonText,
        onButtonClick = onButtonClick,
      ),
    )
  }
}

@Composable
fun HedvigDialogAlertWithButtons(
  titleText: String,
  descriptionText: String,
  confirmButtonText: String,
  dismissButtonText: String,
  onConfirmClick: () -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigDialog (
    style = DialogStyle.Buttons(
      onConfirmButtonClick = onConfirmClick,
      onDismissRequest = onDismissRequest,
      confirmButtonText = confirmButtonText,
      dismissButtonText = dismissButtonText,
    ),
    onDismissRequest = onDismissRequest,
    modifier = modifier
  ) {
    HedvigText(
      titleText,
      style = HedvigTheme.typography.bodySmall,
      color = HedvigTheme.colorScheme.textPrimary)
    HedvigText(
      descriptionText,
      style = HedvigTheme.typography.bodySmall,
      color = HedvigTheme.colorScheme.textSecondary)
  }
}

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
    (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(0.2f)
    // a workaround to stop the overlay from dimming background too much, otherwise in the dark theme the overlay color
    // becomes the same as the background color of the dialog itself.
    Surface(
      shape = DialogDefaults.shape,
      color = DialogDefaults.containerColor,
      modifier = modifier,
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
private fun SmallHorizontalButtons(
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
private fun BigVerticalButtons(
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
