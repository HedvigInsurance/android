package com.hedvig.android.design.system.internals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.hedvig.android.design.system.hedvig.api.HedvigOverlaySheetController

/**
 * A presence-driven [ModalBottomSheet]: it is shown for as long as it is composed, and animated
 * closed via [HedvigOverlaySheetController.hide]. Unlike [BottomSheet], it has no visibility/data
 * state of its own — the caller controls its lifetime (e.g. a navigation back-stack entry).
 *
 * Styling is passed in as plain values; `design-system-hedvig` supplies the Hedvig tokens.
 */
@Composable
fun OverlayBottomSheet(
  controller: HedvigOverlaySheetController,
  onDismissRequest: () -> Unit,
  containerColor: Color,
  contentColor: Color,
  scrimColor: Color,
  shape: Shape,
  contentPadding: PaddingValues,
  dragHandle: @Composable () -> Unit,
  content: @Composable ColumnScope.() -> Unit,
) {
  check(controller is OverlaySheetControllerImpl) {
    "Expected OverlaySheetControllerImpl, got ${controller::class}. Use rememberOverlaySheetController()."
  }
  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = controller.materialState,
    containerColor = containerColor,
    contentColor = contentColor,
    scrimColor = scrimColor,
    shape = shape,
    dragHandle = dragHandle,
  ) {
    Column(Modifier.padding(contentPadding)) {
      content()
    }
  }
}

@Composable
fun rememberOverlaySheetController(): HedvigOverlaySheetController {
  val materialState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  return remember(materialState) { OverlaySheetControllerImpl(materialState) }
}

private class OverlaySheetControllerImpl(
  val materialState: SheetState,
) : HedvigOverlaySheetController {
  override suspend fun hide() {
    materialState.hide()
  }
}
