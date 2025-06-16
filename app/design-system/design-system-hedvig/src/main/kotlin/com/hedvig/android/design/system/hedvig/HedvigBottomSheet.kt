package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.Campaign
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.BottomSheetTokens
import com.hedvig.android.design.system.hedvig.tokens.ScrimTokens
import com.hedvig.android.design.system.internals.BottomSheet
import com.hedvig.android.design.system.internals.rememberInternalHedvigBottomSheetState
import eu.wewox.modalsheet.ExperimentalSheetApi
import hedvig.resources.R

@OptIn(ExperimentalSheetApi::class)
@Composable
fun HedvigBottomSheet(
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
  dragHandle: @Composable (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  val sheetState = rememberHedvigBottomSheetState<Unit>()
  LaunchedEffect(isVisible) {
    if (isVisible) {
      sheetState.show()
    } else {
      sheetState.dismiss()
    }
  }
  InternalHedvigBottomSheet(
    onDismissRequest = { onVisibleChange(false) },
    content = content,
    contentPadding = contentPadding,
    sheetState = sheetState,
    dragHandle = dragHandle,
  )
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
fun <T> HedvigBottomSheet(
  hedvigBottomSheetState: HedvigBottomSheetState<T>,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
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
    dragHandle = dragHandle,
    sheetState = hedvigBottomSheetState,
  ) {
    if (hedvigBottomSheetState.data != null) {
      content(hedvigBottomSheetState.data!!)
    }
  }
}

@OptIn(ExperimentalSheetApi::class)
@Composable
private fun <T> InternalHedvigBottomSheet(
  onDismissRequest: () -> Unit,
  sheetState: HedvigBottomSheetState<T>,
  contentPadding: PaddingValues,
  dragHandle: @Composable (() -> Unit)?,
  content: @Composable ColumnScope.() -> Unit,
) {
  BottomSheet(
    onDismissRequest = onDismissRequest,
    modifier = Modifier,
    sheetState = sheetState,
    shape = bottomSheetShape.shape,
    scrimColor = bottomSheetColors.scrimColor,
    containerColor = bottomSheetColors.bottomSheetBackgroundColor,
    contentColor = bottomSheetColors.contentColor,
    contentPadding = contentPadding,
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
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      content()
    }
  }
}

@Composable
fun CrossSellDragHandle(
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
  text: String? = stringResource(R.string.CROSS_SELL_BANNER_TEXT),
) {
  val direction = LocalLayoutDirection.current
  Box(
    modifier
      .fillMaxWidth()
      .height(40.dp)
      .layout { measurable, constraints ->
        // M3 sheet does not allow us to "break out" of the content padding so we do it through a layout
        val paddingStart = contentPadding.calculateStartPadding(direction).roundToPx()
        val paddingEnd = contentPadding.calculateEndPadding(direction).roundToPx()
        val adjustedConstraints = constraints.copy(
          maxWidth = constraints.maxWidth + paddingEnd + paddingStart,
          minWidth = constraints.minWidth + paddingEnd + paddingStart,
        )
        val placeable = measurable.measure(adjustedConstraints)
        layout(placeable.width, placeable.height) {
          placeable.place(0, 0)
        }
      }
      .background(color = HedvigTheme.colorScheme.signalGreenFill),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        HedvigIcons.Campaign,
        contentDescription = EmptyContentDescription,
        tint = HedvigTheme.colorScheme.signalGreenElement,
        modifier = Modifier.size(20.dp),
      )
      if (text != null) {
        Spacer(Modifier.width(8.dp))
        HedvigText(
          text,
          fontSize = HedvigTheme.typography.label.fontSize,
          color = HedvigTheme.colorScheme.signalGreenText,
        )
      }
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
