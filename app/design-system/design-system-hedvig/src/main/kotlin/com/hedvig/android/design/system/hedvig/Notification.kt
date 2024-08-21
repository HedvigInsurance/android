package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Default
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.NotificationDefaults.paddingNoIcon
import com.hedvig.android.design.system.hedvig.NotificationDefaults.paddingWithIcon
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
  style:  NotificationDefaults.InfoCardStyle = NotificationDefaults.defaultStyle
) {
  val padding = if (withIcon) paddingWithIcon else paddingNoIcon
  Surface(
    modifier = modifier.padding(padding),
    shape = NotificationDefaults.shape,
    color = priority.colors.containerColor,
  ) {
    ProvideTextStyle(NotificationDefaults.textStyle) {
      Row() {

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
    bottom = NotificationsTokens.BottomPadding
  )
  internal val paddingNoIcon = PaddingValues(
    start = NotificationsTokens.StartPadding,
    end = NotificationsTokens.EndPadding,
    top = NotificationsTokens.TopPadding,
    bottom = NotificationsTokens.BottomPadding
  )
  internal val withIconDefault = true
  internal val defaultStyle: NotificationDefaults.InfoCardStyle = Default
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
//  val buttonContainerColor: Color, todo: those don't change according to the dark/light mode in figma
//  val buttonTextColor: Color,
  val iconColor: Color,
)
