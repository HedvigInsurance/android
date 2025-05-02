package com.hedvig.android.design.system.internals

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun <T> BottomSheet(
  onDismissRequest: () -> Unit,
  modifier: Modifier,
  sheetState: HedvigBottomSheetState<T>,
  contentPadding: PaddingValues,
  shape: Shape,
  containerColor: Color,
  contentColor: Color,
  scrimColor: Color,
  dragHandle: @Composable (() -> Unit)?,
  content: @Composable (ColumnScope.() -> Unit),
) {
  val state = sheetState.materialState()
  if (sheetState.isVisible) {
    ModalBottomSheet(
      onDismissRequest = onDismissRequest,
      modifier = modifier,
      sheetState = state,
      contentWindowInsets = {
        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom) + contentPadding
      },
      shape = shape,
      containerColor = containerColor,
      contentColor = contentColor,
      scrimColor = scrimColor,
      dragHandle = dragHandle,
      content = content,
    )
  }
}

@Composable
fun <T> rememberInternalHedvigBottomSheetState(): HedvigBottomSheetState<T> {
  val materialState = rememberModalBottomSheetState(true)
  val scope = rememberCoroutineScope()
  val hedvigBottomSheetState: HedvigBottomSheetStateImpl<T> = remember(materialState, scope) {
    HedvigBottomSheetStateImpl(materialState, scope)
  }
  LaunchedEffect(hedvigBottomSheetState, materialState) {
    snapshotFlow { materialState.isVisible }
      .distinctUntilChanged()
      .collect { isVisible ->
        if (!isVisible) {
          hedvigBottomSheetState.isVisible = false
        }
      }
  }
  return hedvigBottomSheetState
}

private class HedvigBottomSheetStateImpl<T>(
  val materialState: SheetState,
  val scope: CoroutineScope,
) : HedvigBottomSheetState<T> {
  override var isVisible: Boolean by mutableStateOf(materialState.isVisible)

  override var data: T? by mutableStateOf(null)
    private set

  override fun dismiss() {
    scope.launch { materialState.hide() }
  }

  override fun show(data: T) {
    this.data = data
    isVisible = true
  }
}

@Composable
private fun <T> HedvigBottomSheetState<T>.materialState(): SheetState {
  check(this is HedvigBottomSheetStateImpl) {
    val message = "Expected HedvigBottomSheetStateImpl, got ${this::class}"
    logcat { "materialBottomSheetStateImpl failed with: $message" }
    message
  }
  return materialState
}
