package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Buttons
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Default
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Attention
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Campaign
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Error
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.NeutralToast
import com.hedvig.android.design.system.hedvig.NotificationDefaults.paddingNoIcon
import com.hedvig.android.design.system.hedvig.NotificationDefaults.paddingWithIcon
import com.hedvig.android.design.system.hedvig.NotificationDefaults.textStyle
import com.hedvig.android.design.system.hedvig.icon.Campaign
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.FillSecondary
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalAmberElement
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalAmberFill
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalAmberText
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalBlueElement
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalBlueFill
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalBlueText
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalGreenElement
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalGreenFill
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalGreenText
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalRedElement
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalRedFill
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SignalRedText
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SurfacePrimary
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextSecondaryTranslucent
import com.hedvig.android.design.system.hedvig.tokens.NotificationsTokens

@Composable
fun NotificationCard(
  message: String,
  priority: NotificationPriority,
  modifier: Modifier = Modifier,
  withIcon: Boolean = NotificationDefaults.withIconDefault,
  style: InfoCardStyle = NotificationDefaults.defaultStyle,
) {
  val padding = if (withIcon) paddingWithIcon else paddingNoIcon
  Surface(
    modifier = modifier,
    shape = NotificationDefaults.shape,
    color = priority.colors.containerColor,
  ) {
    ProvideTextStyle(textStyle) {
      Row(Modifier.padding(padding)) {
        if (withIcon) {
          val icon = when (priority) {
            Attention, Error -> HedvigIcons.WarningFilled
            Campaign -> HedvigIcons.Campaign
            Info, NeutralToast -> HedvigIcons.InfoFilled
          }
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = priority.colors.iconColor,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(6.dp))
        }
        Column {
          HedvigText(text = message)
          if (style is Buttons) {
            Row {
              HedvigTheme(darkTheme  = false) {
                HedvigButton(
                  enabled = true,
                  onClick = style.onLeftButtonClick,
                  buttonStyle = ButtonDefaults.ButtonStyle.SecondaryAlt,
                  buttonSize = ButtonDefaults.ButtonSize.Small,
                  modifier = Modifier.weight(1f),
                ) {
                  HedvigText(style.leftButtonText, style = textStyle)
                }
                Spacer(Modifier.width(4.dp))
                HedvigButton(
                  enabled = true,
                  onClick = style.onRightButtonClick,
                  buttonStyle = ButtonDefaults.ButtonStyle.SecondaryAlt,
                  buttonSize = ButtonDefaults.ButtonSize.Small,
                  modifier = Modifier.weight(1f),
                ) {
                  HedvigText(style.rightButtonText, style = textStyle)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun NotificationToast() {
}

object NotificationDefaults {
  internal val paddingWithIcon = PaddingValues(
    start = NotificationsTokens.StartPaddingWithIcon,
    end = NotificationsTokens.EndPadding,
    top = NotificationsTokens.TopPadding,
    bottom = NotificationsTokens.BottomPadding,
  )
  internal val paddingNoIcon = PaddingValues(
    start = NotificationsTokens.StartPadding,
    end = NotificationsTokens.EndPadding,
    top = NotificationsTokens.TopPadding,
    bottom = NotificationsTokens.BottomPadding,
  )
  internal val withIconDefault = true
  internal val defaultStyle: InfoCardStyle = Default
  internal val shape
    @Composable
    @ReadOnlyComposable
    get() = NotificationsTokens.ContainerShape.value
  internal val textStyle
    @Composable
    @ReadOnlyComposable
    get() = NotificationsTokens.LabelTextFont.value

  sealed class NotificationPriority() {
    @get:Composable
    internal abstract val colors: NotificationColors

    data object Attention : NotificationPriority() {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(SignalAmberFill),
              textColor = fromToken(SignalAmberText),
              iconColor = fromToken(SignalAmberElement),
            )
          }
        }
    }

    data object Error : NotificationPriority() {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(SignalRedFill),
              textColor = fromToken(SignalRedText),
              iconColor = fromToken(SignalRedElement),
            )
          }
        }
    }

    data object Info : NotificationPriority() {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(SignalBlueFill),
              textColor = fromToken(SignalBlueText),
              iconColor = fromToken(SignalBlueElement),
            )
          }
        }
    }

    data object Campaign : NotificationPriority() {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(SignalGreenFill),
              textColor = fromToken(SignalGreenText),
              iconColor = fromToken(SignalGreenElement),
            )
          }
        }
    }

    data object NeutralToast : NotificationPriority() {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(SurfacePrimary),
              textColor = fromToken(TextSecondaryTranslucent),
              iconColor = fromToken(FillSecondary),
            )
          }
        }
    }
  }

  sealed class InfoCardStyle {
    data object Default : InfoCardStyle()

    data class Buttons(
      val leftButtonText: String,
      val rightButtonText: String,
      val onLeftButtonClick: () -> Unit,
      val onRightButtonClick: () -> Unit,
    ) : InfoCardStyle()
  }
}

internal data class NotificationColors(
  val containerColor: Color,
  val textColor: Color,
  val iconColor: Color,
)

@Preview
@Composable
private fun PreviewNotificationCard(
  @PreviewParameter(NotificationCardPriorityProvider::class) priority: NotificationPriority,
) {
  HedvigTheme(darkTheme = true) {
    Surface(color = HedvigTheme.colorScheme.backgroundWhite) {
      Column(
        Modifier
          .width(330.dp)
          .padding(16.dp),
      ) {
        NotificationCard(
          priority = priority,
          message = "A short message about something that needs attention.",
          withIcon = false,
          style = Default,
        )
        Spacer(Modifier.height(16.dp))
        NotificationCard(
          priority = priority,
          message = "A short message about something that needs attention.",
          withIcon = true,
          style = Default,
        )
        Spacer(Modifier.height(16.dp))
        NotificationCard(
          priority = priority,
          message = "A short message about something that needs attention.",
          withIcon = false,
          style = Buttons("Left", "Right", {}, {}),
        )
        Spacer(Modifier.height(16.dp))
        NotificationCard(
          priority = priority,
          message = "A short message about something that needs attention.",
          withIcon = true,
          style = Buttons("Left", "Right", {}, {}),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

private class NotificationCardPriorityProvider :
  CollectionPreviewParameterProvider<NotificationPriority>(
    listOf(
      Info,
      Error,
      Campaign,
      Attention,
    ),
  )
