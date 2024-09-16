package com.hedvig.android.design.system.internals

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import com.hedvig.android.logger.logcat

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HedvigBottomSheetInternal(
  visible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  containerColor: Color,
  contentColor: Color,
  scrimColor: Color,
  shape: Shape,
  dragHandle: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  if (visible) {
    val density = LocalDensity.current
    val imeAnimationTarget = WindowInsets.ime
    val closeKeyboardOnBack = imeAnimationTarget.getBottom(density) > 0
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    SideEffect {
      logcat { "Stelios closeKeyboardOnBack:$closeKeyboardOnBack" }
    }
    ModalBottomSheet(
      onDismissRequest = {
        onVisibleChange(false)
      },
      sheetState = modalBottomSheetState,
      shape = shape,
      containerColor = containerColor,
      contentColor = contentColor,
      scrimColor = scrimColor,
      dragHandle = dragHandle,
      properties = ModalBottomSheetProperties(shouldDismissOnBackPress = !closeKeyboardOnBack),
      modifier = modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
      content = content,
    )
  }
}
