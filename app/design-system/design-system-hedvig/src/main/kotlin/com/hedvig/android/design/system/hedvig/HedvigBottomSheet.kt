package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.tokens.BottomSheetTokens
import com.hedvig.android.design.system.hedvig.tokens.ScrimTokens
import com.hedvig.android.design.system.internals.BottomSheet
import com.hedvig.android.design.system.internals.rememberInternalHedvigBottomSheetState
import eu.wewox.modalsheet.ExperimentalSheetApi

@OptIn(ExperimentalSheetApi::class)
@Composable
fun <T> HedvigBottomSheet(
  hedvigBottomSheetState: HedvigBottomSheetState<T>,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
  sheetPadding: PaddingValues = PaddingValues(0.dp),
  style: BottomSheetStyle = BottomSheetDefaults.bottomSheetStyle,
  dragHandle: @Composable (() -> Unit)? = null,
  content: @Composable ColumnScope.(T) -> Unit,
) {
  InternalHedvigBottomSheet(
    onDismissRequest = {
      // Purposefully left empty, as this callback is called *after* the sheet has finished animating to the hidden
      // state. HedvigBottomSheetState has logic internally which observes that state to turn the isVisible flag to
      // false automaticaly.
    },
    contentPadding = contentPadding,
    sheetPadding = sheetPadding,
    style = style,
    dragHandle = dragHandle,
    sheetState = hedvigBottomSheetState,
  ) {
    if (hedvigBottomSheetState.data != null) {
      content(hedvigBottomSheetState.data!!)
    }
  }
}

fun HedvigBottomSheetState<Unit>.show() {
  show(Unit)
}

@Composable
fun <T> rememberHedvigBottomSheetState(): HedvigBottomSheetState<T> {
  return rememberInternalHedvigBottomSheetState()
}

@OptIn(ExperimentalSheetApi::class)
@Composable
private fun <T> InternalHedvigBottomSheet(
  onDismissRequest: () -> Unit,
  sheetState: HedvigBottomSheetState<T>,
  contentPadding: PaddingValues,
  sheetPadding: PaddingValues,
  style: BottomSheetStyle,
  dragHandle: @Composable (() -> Unit)?,
  content: @Composable ColumnScope.() -> Unit,
) {
  BottomSheet(
    onDismissRequest = onDismissRequest,
    modifier = Modifier,
    sheetState = sheetState,
    shape = bottomSheetShape.shape,
    scrimColor = style.scrimColor ?: bottomSheetColors.scrimColor,
    containerColor = if (style.transparentBackground) {
      Color.Transparent
    } else {
      bottomSheetColors.bottomSheetBackgroundColor
    },
    contentColor = bottomSheetColors.contentColor,
    contentPadding = contentPadding,
    sheetPadding = sheetPadding,
    dragHandle = dragHandle
      ?: {
        DragHandle(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .padding(top = 8.dp, bottom = 20.dp),
        )
      },
  ) {
    Column(
      modifier = if (style.automaticallyScrollableContent) {
        Modifier.verticalScroll(rememberScrollState())
      } else {
        Modifier
      },
    ) {
      content()
    }
  }
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .width(40.dp)
      .height(4.dp)
      .background(
        shape = HedvigTheme.shapes.cornerSmall,
        color = bottomSheetColors.chipColor,
      ),
  )
}

@Immutable
internal data class BottomSheetColors(
  val scrimColor: Color,
  val bottomSheetBackgroundColor: Color,
  val contentColor: Color,
  val chipColor: Color,
  val arrowColor: Color,
  val arrowBackgroundColor: Color,
)

internal val bottomSheetColors: BottomSheetColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      BottomSheetColors(
        scrimColor = fromToken(ScrimTokens.ContainerColor).copy(ScrimTokens.ContainerOpacity),
        bottomSheetBackgroundColor = fromToken(BottomSheetTokens.BottomSheetBackgroundColor),
        contentColor = fromToken(BottomSheetTokens.ContentColor),
        chipColor = fromToken(BottomSheetTokens.UpperChipColor),
        arrowColor = fromToken(BottomSheetTokens.ArrowColor),
        arrowBackgroundColor = fromToken(BottomSheetTokens.ArrowColorBackground),
      )
    }
  }

@Immutable
internal data class BottomSheetShape(
  val shape: Shape,
  val contentHorizontalPadding: Dp,
)

internal val bottomSheetShape: BottomSheetShape
  @Composable
  get() = BottomSheetShape(
    shape = BottomSheetTokens.ContainerShape.value,
    contentHorizontalPadding = BottomSheetTokens.ContentPadding,
  )

@Immutable
data class BottomSheetStyle(
  val transparentBackground: Boolean,
  val automaticallyScrollableContent: Boolean,
  val scrimColor: Color?,
)

object BottomSheetDefaults {
  val bottomSheetStyle: BottomSheetStyle = BottomSheetStyle(
    transparentBackground = false,
    automaticallyScrollableContent = true,
    scrimColor = null,
  )
}
