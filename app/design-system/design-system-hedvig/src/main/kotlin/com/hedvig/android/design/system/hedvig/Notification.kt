package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Small
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.SecondaryAlt
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Button
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Buttons
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Default
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Attention
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Campaign
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Error
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.NotificationDefaults.defaultSnackbarPriority
import com.hedvig.android.design.system.hedvig.NotificationDefaults.defaultStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.paddingNoIcon
import com.hedvig.android.design.system.hedvig.NotificationDefaults.paddingWithIcon
import com.hedvig.android.design.system.hedvig.NotificationDefaults.snackBarShape
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
import com.hedvig.android.design.system.internals.InternalSnackBar
import com.hedvig.android.design.system.internals.NotificationColors

@Composable
fun HedvigNotificationCard(
  message: String,
  priority: NotificationPriority,
  modifier: Modifier = Modifier,
  withIcon: Boolean = NotificationDefaults.withIconDefault,
  style: InfoCardStyle = defaultStyle,
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
          Icon(
            imageVector = priority.icon,
            contentDescription = null,
            tint = priority.colors.iconColor,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(6.dp))
        }
        Column {
          HedvigText(text = message, color = priority.colors.textColor)
          when (style) {
            is Buttons -> {
              Spacer(Modifier.height(NotificationsTokens.SpaceBetweenTextAndButtons))
              Row {
                HedvigTheme(darkTheme = false) {
                  HedvigButton(
                    enabled = true,
                    onClick = style.onLeftButtonClick,
                    buttonStyle = SecondaryAlt,
                    buttonSize = Small,
                    modifier = Modifier.weight(1f),
                  ) {
                    HedvigText(style.leftButtonText, style = textStyle)
                  }
                  Spacer(Modifier.width(4.dp))
                  HedvigButton(
                    enabled = true,
                    onClick = style.onRightButtonClick,
                    buttonStyle = SecondaryAlt,
                    buttonSize = Small,
                    modifier = Modifier.weight(1f),
                  ) {
                    HedvigText(style.rightButtonText, style = textStyle)
                  }
                }
              }
            }

            is Button -> {
              Spacer(Modifier.height(NotificationsTokens.SpaceBetweenTextAndButtons))
              HedvigTheme(darkTheme = false) {
                HedvigButton(
                  enabled = true,
                  onClick = style.onButtonClick,
                  buttonStyle = SecondaryAlt,
                  buttonSize = Small,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  HedvigText(style.buttonText, style = textStyle)
                }
              }
            }

            Default -> {}
          }
        }
      }
    }
  }
}

@Composable
fun HedvigSnackbar(
  snackbarText: String,
  showSnackbar: Boolean,
  showedSnackbar: () -> Unit,
  modifier: Modifier = Modifier,
  priority: NotificationPriority = defaultSnackbarPriority,
  action: (() -> Unit)? = null,
  actionLabel: String? = null,
) {
  InternalSnackBar(
    colors = priority.colors,
    shape = snackBarShape,
    snackbarText = snackbarText,
    showSnackbar = showSnackbar,
    showedSnackbar = showedSnackbar,
    modifier = modifier,
    action = action,
    actionLabel = actionLabel,
    textStyle = textStyle,
    icon = {
      Icon(
        imageVector = priority.icon,
        contentDescription = null,
        tint = priority.colors.iconColor,
        modifier = Modifier.size(18.dp),
      )
    },
  )
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
  internal val defaultSnackbarPriority = NotificationPriority.NeutralToast
  internal val shape
    @Composable
    @ReadOnlyComposable
    get() = NotificationsTokens.ContainerShape.value
  internal val snackBarShape
    @Composable
    @ReadOnlyComposable
    get() = NotificationsTokens.SnackbarContainerShape.value
  internal val textStyle
    @Composable
    @ReadOnlyComposable
    get() = NotificationsTokens.LabelTextFont.value

  sealed class NotificationPriority() {
    @get:Composable
    internal abstract val colors: NotificationColors

    @get:Composable
    internal abstract val icon: ImageVector

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
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.WarningFilled
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
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.WarningFilled
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
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.InfoFilled
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
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.Campaign
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
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.InfoFilled
    }
  }

  sealed class InfoCardStyle {
    data object Default : InfoCardStyle()

    data class Button(
      val buttonText: String,
      val onButtonClick: () -> Unit,
    ) : InfoCardStyle()

    data class Buttons(
      val leftButtonText: String,
      val rightButtonText: String,
      val onLeftButtonClick: () -> Unit,
      val onRightButtonClick: () -> Unit,
    ) : InfoCardStyle()
  }
}

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
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        HedvigNotificationCard(
          priority = priority,
          message = "A short message about something that needs attention.",
          withIcon = false,
          style = Default,
        )
        HedvigNotificationCard(
          priority = priority,
          message = "A short message about something that needs attention.",
          withIcon = true,
          style = Default,
        )
        HedvigNotificationCard(
          priority = priority,
          message = "A short message about something that needs attention.",
          withIcon = false,
          style = Button("Button", {}),
        )
        HedvigNotificationCard(
          priority = priority,
          message = "A short message about something that needs attention.",
          withIcon = true,
          style = Button("Button", {}),
        )
        HedvigNotificationCard(
          priority = priority,
          message = "A short message about something that needs attention.",
          withIcon = false,
          style = Buttons("Left", "Right", {}, {}),
        )
        HedvigNotificationCard(
          priority = priority,
          message = "A short message about something that needs attention.",
          withIcon = true,
          style = Buttons("Left", "Right", {}, {}),
        )
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
