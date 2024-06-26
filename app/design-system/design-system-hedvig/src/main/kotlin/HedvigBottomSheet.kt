import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.window.Popup
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.fromToken
import com.hedvig.android.design.system.hedvig.tokens.BottomSheetTokens
import com.hedvig.android.logger.logcat
import eu.wewox.modalsheet.ExperimentalSheetApi
import eu.wewox.modalsheet.ModalSheet

@OptIn(ExperimentalSheetApi::class, ExperimentalLayoutApi::class)
@Composable
fun HedvigBottomSheet(
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  content: @Composable ColumnScope.() -> Unit,
  onSystemBack: (() -> Unit)? = null,
  sheetPadding: PaddingValues? = null,
  cancelable: Boolean = true,
) {
  val isImeVisible = WindowInsets.isImeVisible
  val defaultPadding = if (isImeVisible) {
    WindowInsets.ime.asPaddingValues()
  } else {
    WindowInsets.safeDrawing
      .only(WindowInsetsSides.Bottom).asPaddingValues()
  }
  val finalSheetPadding = sheetPadding ?: defaultPadding
  val finalOnSystemBack = onSystemBack ?: { onVisibleChange(false) }
  logcat { "mariia: finalOnSystemBack is onSystemBack: ${finalOnSystemBack == onSystemBack}" }
  ModalSheet(
    visible = isVisible,
    onVisibleChange = onVisibleChange,
    cancelable = cancelable,
    content = content,
    scrimColor = bottomSheetColors.scrimColor,
    backgroundColor = bottomSheetColors.bottomSheetBackgroundColor,
    contentColor = bottomSheetColors.contentColor,
    sheetPadding = finalSheetPadding,
    onSystemBack = finalOnSystemBack,
    shape = bottomSheetShape.shape,
  )
}

@Immutable
internal data class BottomSheetColors(
  val scrimColor: Color,
  val bottomSheetBackgroundColor: Color,
  val contentColor: Color,
)

internal val bottomSheetColors: BottomSheetColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      BottomSheetColors(
        scrimColor = fromToken(BottomSheetTokens.ScrimColor),
        bottomSheetBackgroundColor = fromToken(BottomSheetTokens.BottomSheetBackgroundColor),
        contentColor = fromToken(BottomSheetTokens.ContentColor),
      )
    }
  }

@Immutable
internal data class BottomSheetShape(
  val shape: Shape,
)

internal val bottomSheetShape: BottomSheetShape
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      BottomSheetShape(
        shape = RoundedCornerShape(
          topStart = BottomSheetTokens.TopCornerRadius,
          topEnd = BottomSheetTokens.TopCornerRadius,
          bottomEnd = BottomSheetTokens.BottomCornerRadius,
          bottomStart = BottomSheetTokens.BottomCornerRadius,
        ),
      )
    }
  }

