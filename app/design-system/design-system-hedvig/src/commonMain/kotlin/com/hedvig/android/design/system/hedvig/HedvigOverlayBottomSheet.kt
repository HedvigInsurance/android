package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.api.HedvigOverlaySheetController
import com.hedvig.android.design.system.internals.OverlayBottomSheet
import com.hedvig.android.design.system.internals.rememberOverlaySheetController

/**
 * A presence-driven Hedvig bottom sheet for content whose visibility is owned by the caller
 * (e.g. a navigation back-stack entry) rather than a [HedvigBottomSheetState]. Styled identically
 * to [HedvigBottomSheet]: BackgroundPrimary container, TextPrimary content, Scrim @ 32 %,
 * CornerXLargeTop shape, the Hedvig drag handle, and 16 dp horizontal content padding.
 *
 * Animate it closed with [controller].hide() before the content leaves composition.
 */
@Composable
fun HedvigOverlayBottomSheet(
  controller: HedvigOverlaySheetController,
  onDismissRequest: () -> Unit,
  content: @Composable ColumnScope.() -> Unit,
) {
  OverlayBottomSheet(
    controller = controller,
    onDismissRequest = onDismissRequest,
    containerColor = bottomSheetColors.bottomSheetBackgroundColor,
    contentColor = bottomSheetColors.contentColor,
    scrimColor = bottomSheetColors.scrimColor,
    shape = bottomSheetShape.shape,
    contentPadding = PaddingValues(horizontal = 16.dp),
    dragHandle = {
      DragHandle(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentWidth(Alignment.CenterHorizontally)
          .padding(top = 8.dp, bottom = 20.dp),
      )
    },
    content = content,
  )
}

@Composable
fun rememberHedvigOverlaySheetController(): HedvigOverlaySheetController = rememberOverlaySheetController()
