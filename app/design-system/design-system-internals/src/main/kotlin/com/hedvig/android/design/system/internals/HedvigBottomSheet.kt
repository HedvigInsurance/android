package com.hedvig.android.design.system.internals

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun <T> BottomSheet(
  onDismissRequest: () -> Unit,
  modifier: Modifier,
  sheetState: HedvigBottomSheetState<T>,
  shape: Shape,
  containerColor: Color,
  contentColor: Color,
  scrimColor: Color,
  dragHandle: @Composable (() -> Unit)?,
  contentWindowInsets: @Composable (() -> WindowInsets) = { BottomSheetDefaults.windowInsets },
  content: @Composable (ColumnScope.() -> Unit),
) {
  val state = materialBottomSheetStateImpl(sheetState)
  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    sheetState = state,
    shape = shape,
    containerColor = containerColor,
    contentColor = contentColor,
    scrimColor = scrimColor,
    dragHandle = dragHandle,
    contentWindowInsets = contentWindowInsets,
    content = content,
  )
}

@Composable
fun <T> rememberHedvigBottomSheetState(scope: CoroutineScope): HedvigBottomSheetState<T> {
  val materialState = rememberModalBottomSheetState()
  return remember { HedvigBottomSheetStateImpl(materialState, scope) }
}

private class HedvigBottomSheetStateImpl<T>(
  val materialState: SheetState,
  val scope: CoroutineScope,
) : HedvigBottomSheetState<T> {
  override val isVisible: Boolean
    get() = materialState.isVisible

  override var data: T? by mutableStateOf(null)
    private set

  override fun dismiss() {
    scope.launch {
      materialState.hide()
    }
  }

  override fun show(data: T) {
    this.data = data
    scope.launch {
      materialState.show()
    }
  }
}

@Composable
private fun <T> materialBottomSheetStateImpl(hedvigState: HedvigBottomSheetState<T>): SheetState {
  check(hedvigState is HedvigBottomSheetStateImpl) {
    val message = "Expected HedvigBottomSheetStateImpl, got ${hedvigState::class}"
    logcat { "materialBottomSheetStateImpl failed with: $message" }
    message
  }
  return hedvigState.materialState
}
