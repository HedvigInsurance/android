import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import eu.wewox.modalsheet.ExperimentalSheetApi
import eu.wewox.modalsheet.ModalSheet

@OptIn(ExperimentalSheetApi::class)
@Composable
fun HedvigBottomSheet(
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  sheetPadding: PaddingValues,
  content: @Composable ColumnScope.() -> Unit,
  onSystemBack: (() -> Unit)?,
  cancelable: Boolean = true,
) {
  val scrimColor = HedvigTheme.colorScheme.surfacePrimary  //todo: move to defaults
  val bottomSheetBackgroundColor = HedvigTheme.colorScheme.backgroundPrimary  //todo: move to defaults
  val contentColor = HedvigTheme.colorScheme.textPrimary  // todo: move to defaults
  val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 0.dp, bottomStart = 0.dp) // todo: move to defaults
  ModalSheet(
      visible = isVisible,
      onVisibleChange = onVisibleChange,
      cancelable = cancelable,
      content = content,
      scrimColor = scrimColor,
      backgroundColor = bottomSheetBackgroundColor,
      contentColor = contentColor,
      sheetPadding = sheetPadding,
      onSystemBack = onSystemBack,
      shape = shape,
  )
}


//todo: make overloading HedvigBottomSheet with data
