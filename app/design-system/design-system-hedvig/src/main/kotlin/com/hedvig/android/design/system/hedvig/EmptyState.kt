package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.Button
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.BANK_ID
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.NO_ICON
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.icon.BankId
import com.hedvig.android.design.system.hedvig.icon.CheckFilled
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.tokens.EmptyStateTokens

@Composable
fun EmptyState(
  text: String,
  description: String?,
  modifier: Modifier = Modifier,
  iconStyle: EmptyStateIconStyle = EmptyStateDefaults.iconStyle,
  buttonStyle: EmptyStateButtonStyle = EmptyStateDefaults.buttonStyle,
) {
  Column(
    modifier = modifier
      .background(emptyStateColors.containerColor)
      .padding(horizontal = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(Modifier.height(24.dp))
    EmptyStateIcon(iconStyle)
    HedvigText(
      text = text,
      style = EmptyStateDefaults.textStyle.copy(
        lineBreak = LineBreak.Heading,
      ),
      color = emptyStateColors.textColor,
      textAlign = TextAlign.Center,
    )
    if (description != null) {
      HedvigText(
        description,
        style = EmptyStateDefaults.textStyle.copy(
          lineBreak = LineBreak.Heading,
        ),
        color = emptyStateColors.descriptionColor,
        textAlign = TextAlign.Center,
      )
    }
    when (buttonStyle) {
      is Button -> {
        Spacer(Modifier.height(24.dp))
        HedvigButton(
          onClick = buttonStyle.onButtonClick,
          enabled = true,
          buttonStyle = Primary,
          buttonSize = Medium,
        ) {
          HedvigText(buttonStyle.buttonText)
        }
      }
      NoButton -> {}
    }
    Spacer(Modifier.height(24.dp))
  }
}

@Composable
private fun ColumnScope.EmptyStateIcon(iconStyle: EmptyStateIconStyle) {
  val sizeModifier = Modifier.size(EmptyStateTokens.IconSize)
  when (iconStyle) {
    ERROR -> {
      Icon(
        HedvigIcons.WarningFilled,
        null,
        tint = emptyStateColors.errorIconColor,
        modifier = sizeModifier,
      )
      Spacer(Modifier.height(16.dp))
    }

    INFO -> {
      Icon(
        HedvigIcons.InfoFilled,
        null,
        tint = emptyStateColors.infoIconColor,
        modifier = sizeModifier,
      )
      Spacer(Modifier.height(16.dp))
    }

    SUCCESS -> {
      Icon(
        HedvigIcons.CheckFilled,
        null,
        tint = emptyStateColors.successIconColor,
        modifier = sizeModifier,
      )
      Spacer(Modifier.height(16.dp))
    }

    BANK_ID -> {
      Icon(
        HedvigIcons.BankId,
        null,
        modifier = sizeModifier,
      )
      Spacer(Modifier.height(16.dp))
    }

    NO_ICON -> {}
  }
}

object EmptyStateDefaults {
  internal val iconStyle: EmptyStateIconStyle = NO_ICON
  internal val buttonStyle: EmptyStateButtonStyle = NoButton

  enum class EmptyStateIconStyle {
    ERROR,
    INFO,
    SUCCESS,
    BANK_ID,
    NO_ICON,
  }

  sealed class EmptyStateButtonStyle {
    data class Button(val buttonText: String, val onButtonClick: () -> Unit) : EmptyStateButtonStyle()

    data object NoButton : EmptyStateButtonStyle()
  }

  internal val textStyle: TextStyle
    @Composable
    @ReadOnlyComposable
    get() = EmptyStateTokens.TextFont.value
}

@Immutable
private data class EmptyStateColors(
  val containerColor: Color,
  val descriptionColor: Color,
  val textColor: Color,
  val errorIconColor: Color,
  val infoIconColor: Color,
  val successIconColor: Color,
)

private val emptyStateColors: EmptyStateColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      EmptyStateColors(
        containerColor = fromToken(EmptyStateTokens.ContainerColor),
        textColor = fromToken(EmptyStateTokens.TextColor),
        descriptionColor = fromToken(EmptyStateTokens.DescriptionColor),
        errorIconColor = fromToken(EmptyStateTokens.ErrorIconColor),
        successIconColor = fromToken(EmptyStateTokens.SuccessIconColor),
        infoIconColor = fromToken(EmptyStateTokens.InfoIconColor),
      )
    }
  }

@Preview
@Composable
private fun EmptyStatePreview() {
  HedvigTheme {
    Surface(color = Color.White) {
      EmptyState(
        text = "Something went wrong",
        description = "Long description description description description description",
        iconStyle = ERROR,
        buttonStyle = Button("Try again", {}),
      )
    }
  }
}
