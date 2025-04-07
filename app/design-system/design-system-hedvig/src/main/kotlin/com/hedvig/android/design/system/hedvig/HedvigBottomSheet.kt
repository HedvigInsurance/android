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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.ArrowDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.BottomSheetTokens
import com.hedvig.android.design.system.hedvig.tokens.ScrimTokens
import com.hedvig.android.design.system.internals.BottomSheet
import com.hedvig.android.design.system.internals.rememberHedvigBottomSheetState
import eu.wewox.modalsheet.ExperimentalSheetApi

@OptIn(ExperimentalSheetApi::class)
@Composable
fun HedvigBottomSheet( // todo: this one works fine
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  contentPadding: PaddingValues? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  val scope = rememberCoroutineScope()
  val sheetState = rememberHedvigBottomSheetState<Unit>(scope)
  if (isVisible) {
    InternalHedvigBottomSheet(
      onVisibleChange = onVisibleChange,
      contentPadding = contentPadding,
      content = content,
      sheetState = sheetState,
    )
  }
}

fun HedvigBottomSheetState<Unit>.show() {
  show(Unit)
}

@Composable
fun <T> rememberHedvigBottomSheetState(): HedvigBottomSheetState<T> {
  val scope = rememberCoroutineScope()
  return rememberHedvigBottomSheetState(scope)
}

@OptIn(ExperimentalSheetApi::class)
@Composable
fun <T> HedvigBottomSheet( // todo: this one is 1 frame late, so it doesn't roll up properly
  hedvigBottomSheetState: HedvigBottomSheetState<T>,
  contentPadding: PaddingValues? = null,
  content: @Composable ColumnScope.(T) -> Unit,
) {
  if (hedvigBottomSheetState.isVisible) {
    InternalHedvigBottomSheet(
      onVisibleChange = {},
      contentPadding = contentPadding,
      sheetState = hedvigBottomSheetState,
    ) {
      if (hedvigBottomSheetState.data != null) {
        content(hedvigBottomSheetState.data!!)
      }
    }
  }
}

@OptIn(ExperimentalSheetApi::class)
@Composable
private fun <T> InternalHedvigBottomSheet(
  onVisibleChange: (Boolean) -> Unit,
  contentPadding: PaddingValues? = null,
  sheetState: HedvigBottomSheetState<T>,
  content: @Composable ColumnScope.() -> Unit,
) {
  val scrollState = rememberScrollState()
  var scrollDown by remember { mutableStateOf(false) }
  LaunchedEffect(scrollDown) {
    if (scrollDown) {
      scrollState.animateScrollTo(scrollState.maxValue)
    }
  }
  BottomSheet(
    onDismissRequest = {
      onVisibleChange(false)
    },
    modifier = Modifier,
    sheetState = sheetState,
    shape = bottomSheetShape.shape,
    scrimColor = bottomSheetColors.scrimColor,
    containerColor = bottomSheetColors.bottomSheetBackgroundColor,
    contentColor = bottomSheetColors.contentColor,
    dragHandle = null,
  ) {
    Box(Modifier) {
      Column(
        modifier = Modifier
          .then(
            if (contentPadding != null) {
              Modifier.padding(contentPadding)
            } else {
              Modifier.padding(horizontal = bottomSheetShape.contentHorizontalPadding)
            },
          )
          .verticalScroll(scrollState),
      ) {
        Spacer(modifier = Modifier.height(8.dp))
        DragHandle(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(20.dp))
        content()
      }
      Crossfade(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(horizontal = 32.dp, vertical = 16.dp),
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
