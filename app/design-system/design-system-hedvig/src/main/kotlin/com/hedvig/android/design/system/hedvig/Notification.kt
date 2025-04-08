package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
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
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens
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
import com.hedvig.android.design.system.internals.SnackBarColors
import hedvig.resources.R

@Composable
fun HedvigNotificationCard(
  message: String,
  priority: NotificationPriority,
  modifier: Modifier = Modifier,
  withIcon: Boolean = NotificationDefaults.withIconDefault,
  style: InfoCardStyle = defaultStyle,
  buttonLoading: Boolean = false,
) {
  HedvigNotificationCard(
    content = {
      HedvigText(text = message)
    },
    priority = priority,
    modifier = modifier,
    withIcon = withIcon,
    style = style,
    buttonLoading = buttonLoading,
  )
}

@Composable
fun HedvigNotificationCard(
  content: @Composable () -> Unit,
  priority: NotificationPriority,
  modifier: Modifier = Modifier,
  withIcon: Boolean = NotificationDefaults.withIconDefault,
  style: InfoCardStyle = defaultStyle,
  buttonLoading: Boolean = false,
) {
  val padding = if (withIcon) paddingWithIcon else paddingNoIcon
  val description = when (priority) {
    Attention, Error, Info -> stringResource(R.string.TALKBACK_NOTIFICATION_CARD)
    Campaign, NotificationPriority.InfoInline, NotificationPriority.NeutralToast -> ""
  }

  Surface(
    modifier = modifier.semantics(mergeDescendants = true) {
      contentDescription = description
    },
    shape = NotificationDefaults.shape,
    color = priority.colors.containerColor,
    border = priority.colors.borderColor,
  ) {
    val buttonDarkTheme = if (priority is NotificationPriority.InfoInline) isSystemInDarkTheme() else false
    ProvideTextStyle(textStyle) {
      Row(Modifier.padding(padding)) {
        if (withIcon) {
          LayoutWithoutPlacement(
            sizeAdjustingContent = { HedvigText("H") },
          ) {
            Icon(
              imageVector = priority.icon,
              contentDescription = null,
              tint = priority.colors.iconColor,
              modifier = Modifier.size(18.dp),
            )
          }
          Spacer(Modifier.width(6.dp))
        }
        Column {
          ProvideTextStyle(LocalTextStyle.current.copy(color = priority.colors.textColor)) {
            content()
          }
          when (style) {
            is Buttons -> {
              Spacer(Modifier.height(NotificationsTokens.SpaceBetweenTextAndButtons))
              Row {
                HedvigTheme(darkTheme = buttonDarkTheme) {
                  HedvigButton(
                    enabled = true,
                    onClick = style.onLeftButtonClick,
                    buttonStyle = priority.buttonStyle,
                    buttonSize = Small,
                    modifier = Modifier.weight(1f),
                  ) {
                    HedvigText(style.leftButtonText, style = textStyle)
                  }
                  Spacer(Modifier.width(4.dp))
                  HedvigButton(
                    enabled = true,
                    onClick = style.onRightButtonClick,
                    buttonStyle = priority.buttonStyle,
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
              HedvigTheme(darkTheme = buttonDarkTheme) {
                HedvigButton(
                  enabled = true,
                  onClick = style.onButtonClick,
                  buttonStyle = priority.buttonStyle,
                  buttonSize = Small,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  LayoutWithoutPlacement(
                    sizeAdjustingContent = {
                      HedvigText(style.buttonText, style = textStyle)
                    },
                  ) {
                    if (!buttonLoading) {
                      HedvigText(style.buttonText, style = textStyle)
                    } else {
                      Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                      ) {
                        ThreeDotsLoading()
                      }
                    }
                  }
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
    colors = priority.colors.let { notificationColors ->
      SnackBarColors(
        containerColor = notificationColors.containerColor,
        textColor = notificationColors.textColor,
        iconColor = notificationColors.iconColor,
      )
    },
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

  sealed interface NotificationPriority {
    @get:Composable
    val colors: NotificationColors

    @get:Composable
    val icon: ImageVector

    val buttonStyle: ButtonDefaults.ButtonStyle

    data object InfoInline : NotificationPriority {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(ColorSchemeKeyTokens.FillNegative),
              borderColor = fromToken(ColorSchemeKeyTokens.BorderPrimary),
              textColor = fromToken(ColorSchemeKeyTokens.TextSecondary),
              iconColor = fromToken(FillSecondary),
            )
          }
        }
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.InfoFilled

      override val buttonStyle: ButtonDefaults.ButtonStyle
        get() = ButtonDefaults.ButtonStyle.Secondary
    }

    data object Attention : NotificationPriority {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(SignalAmberFill),
              borderColor = fromToken(SignalAmberFill),
              textColor = fromToken(SignalAmberText),
              iconColor = fromToken(SignalAmberElement),
            )
          }
        }
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.WarningFilled

      override val buttonStyle: ButtonDefaults.ButtonStyle
        get() = SecondaryAlt
    }

    data object Error : NotificationPriority {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(SignalRedFill),
              borderColor = fromToken(SignalRedFill),
              textColor = fromToken(SignalRedText),
              iconColor = fromToken(SignalRedElement),
            )
          }
        }
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.WarningFilled
      override val buttonStyle: ButtonDefaults.ButtonStyle
        get() = SecondaryAlt
    }

    data object Info : NotificationPriority {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(SignalBlueFill),
              borderColor = fromToken(SignalBlueFill),
              textColor = fromToken(SignalBlueText),
              iconColor = fromToken(SignalBlueElement),
            )
          }
        }
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.InfoFilled
      override val buttonStyle: ButtonDefaults.ButtonStyle
        get() = SecondaryAlt
    }

    data object Campaign : NotificationPriority {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(SignalGreenFill),
              borderColor = fromToken(SignalGreenFill),
              textColor = fromToken(SignalGreenText),
              iconColor = fromToken(SignalGreenElement),
            )
          }
        }
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.Campaign
      override val buttonStyle: ButtonDefaults.ButtonStyle
        get() = SecondaryAlt
    }

    data object NeutralToast : NotificationPriority {
      override val colors: NotificationColors
        @Composable
        get() = with(HedvigTheme.colorScheme) {
          remember(this) {
            NotificationColors(
              containerColor = fromToken(SurfacePrimary),
              borderColor = fromToken(SurfacePrimary),
              textColor = fromToken(TextSecondaryTranslucent),
              iconColor = fromToken(FillSecondary),
            )
          }
        }
      override val icon: ImageVector
        @Composable
        get() =
          HedvigIcons.InfoFilled
      override val buttonStyle: ButtonDefaults.ButtonStyle
        get() = SecondaryAlt
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

@Immutable
data class NotificationColors(
  val containerColor: Color,
  val borderColor: Color,
  val textColor: Color,
  val iconColor: Color,
)

@HedvigPreview
@Composable
private fun PreviewNotificationCard(
  @PreviewParameter(NotificationCardPriorityProvider::class) priority: NotificationPriority,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
      NotificationPriority.InfoInline,
    ),
  )
