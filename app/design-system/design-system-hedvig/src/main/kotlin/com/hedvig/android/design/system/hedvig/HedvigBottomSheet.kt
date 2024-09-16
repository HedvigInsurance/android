package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.icon.ArrowDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.BottomSheetTokens
import com.hedvig.android.design.system.internals.HedvigBottomSheetInternal

@Composable
fun HedvigBottomSheet(
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  topButtonText: String,
  onTopButtonClick: () -> Unit,
  bottomButtonText: String,
  onBottomButtonClick: () -> Unit = { onVisibleChange(false) },
  content: @Composable ColumnScope.() -> Unit,
) {
  HedvigBottomSheet(
    isVisible = isVisible,
    onVisibleChange = onVisibleChange,
  ) {
    content()
    HedvigButton(
      onClick = onTopButtonClick,
      text = topButtonText,
      enabled = true,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      onClick = onBottomButtonClick,
      text = bottomButtonText,
      enabled = true,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
fun HedvigBottomSheet(
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  bottomButtonText: String,
  onBottomButtonClick: () -> Unit = { onVisibleChange(false) },
  content: @Composable ColumnScope.() -> Unit,
) {
  HedvigBottomSheet(
    isVisible = isVisible,
    onVisibleChange = onVisibleChange,
  ) {
    content()
    HedvigButton(
      onClick = onBottomButtonClick,
      text = bottomButtonText,
      enabled = true,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
fun HedvigBottomSheet(
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  content: @Composable ColumnScope.() -> Unit,
) {
  val sheetStyle = bottomSheetStyle
  val scrollState = rememberScrollState()
  var scrollDown by remember { mutableStateOf(false) }
  LaunchedEffect(scrollDown) {
    if (scrollDown) {
      scrollState.animateScrollTo(scrollState.maxValue)
    }
  }
  HedvigBottomSheetInternal(
    visible = isVisible,
    onVisibleChange = onVisibleChange,
    containerColor = bottomSheetColors.backgroundColor,
    contentColor = bottomSheetColors.contentColor,
    scrimColor = bottomSheetColors.scrimColor,
    shape = sheetStyle.shape,
    dragHandle = { DragHandle(sheetStyle) },
  ) {
    Box {
      Column(
        modifier = Modifier
          .padding(horizontal = sheetStyle.contentHorizontalPadding)
          .verticalScroll(scrollState),
      ) {
        content()
        Spacer(Modifier.height(sheetStyle.contentBottomPadding))
      }
      Crossfade(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(horizontal = 16.dp, vertical = 16.dp),
        targetState = scrollDown,
      ) { animatedScrollDown ->
        if (scrollState.canScrollForward && !animatedScrollDown) {
          HintArrowDown(
            onClick = { scrollDown = true },
          )
        }
      }
    }
  }
}

@Composable
private fun DragHandle(style: BottomSheetStyle, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .padding(style.dragHandlePadding)
      .size(style.dragHandleSize)
      .background(bottomSheetColors.dragHandleColor, style.dragHandleShape),
  )
}

@Composable
private fun HintArrowDown(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(modifier = modifier) {
    IconButton(
      onClick = onClick,
      modifier = Modifier
        .clip(CircleShape)
        .background(bottomSheetColors.arrowBackgroundColor),
    ) {
      Icon(
        HedvigIcons.ArrowDown,
        null,
        tint = bottomSheetColors.arrowColor,
      )
    }
  }
}

@Immutable
private data class BottomSheetColors(
  val scrimColor: Color,
  val backgroundColor: Color,
  val contentColor: Color,
  val dragHandleColor: Color,
  val arrowColor: Color,
  val arrowBackgroundColor: Color,
)

private val bottomSheetColors: BottomSheetColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      BottomSheetColors(
        scrimColor = fromToken(BottomSheetTokens.ScrimColor).copy(alpha = BottomSheetTokens.ScrimOpacity),
        backgroundColor = fromToken(BottomSheetTokens.BackgroundColor),
        contentColor = fromToken(BottomSheetTokens.ContentColor),
        dragHandleColor = fromToken(BottomSheetTokens.DragHandleColor),
        arrowColor = fromToken(BottomSheetTokens.ArrowColor),
        arrowBackgroundColor = fromToken(BottomSheetTokens.ArrowColorBackground),
      )
    }
  }

@Immutable
private data class BottomSheetStyle(
  val shape: Shape,
  val contentBottomPadding: Dp,
  val contentHorizontalPadding: Dp,
  val dragHandleSize: DpSize,
  val dragHandleShape: Shape,
  val dragHandlePadding: PaddingValues,
)

private val bottomSheetStyle: BottomSheetStyle
  @Composable
  get() = BottomSheetStyle(
    shape = BottomSheetTokens.ContainerShape.value,
    contentBottomPadding = BottomSheetTokens.ContentBottomPadding,
    contentHorizontalPadding = BottomSheetTokens.ContentHorizontalPadding,
    dragHandleSize = DpSize(
      BottomSheetTokens.DragHandleWidth,
      BottomSheetTokens.DragHandleHeight,
    ),
    dragHandleShape = BottomSheetTokens.DragHandleShape.value,
    dragHandlePadding = PaddingValues(
      start = BottomSheetTokens.ContentHorizontalPadding,
      end = BottomSheetTokens.ContentHorizontalPadding,
      top = BottomSheetTokens.DragHandleTopPadding,
      bottom = BottomSheetTokens.DragHandleBottomPadding,
    ),
  )
