package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.WindowInsetsSides.Companion
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.icon.ArrowDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.BottomSheetTokens
import com.hedvig.android.design.system.hedvig.tokens.ScrimTokens
import eu.wewox.modalsheet.ExperimentalSheetApi
import eu.wewox.modalsheet.ModalSheet

@OptIn(ExperimentalSheetApi::class)
@Composable
fun HedvigBottomSheet(
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  onSystemBack: (() -> Unit)? = { onVisibleChange(false) },
  contentPadding: PaddingValues? = null,
  cancelable: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  InternalHedvigBottomSheet(
    isVisible = isVisible,
    onVisibleChange = onVisibleChange,
    onSystemBack = onSystemBack,
    contentPadding = contentPadding,
    cancelable = cancelable,
    content = content,
  )
}

@Composable
fun <T> rememberHedvigBottomSheetState(): HedvigBottomSheetState<T> {
  return remember { HedvigBottomSheetStateImpl() }
}

@Stable
interface HedvigBottomSheetState<T> {
  val isVisible: Boolean
  val data: T?

  fun show(data: T)

  fun dismiss()
}

fun HedvigBottomSheetState<Unit>.show() {
  show(Unit)
}

private class HedvigBottomSheetStateImpl<T>() : HedvigBottomSheetState<T> {
  override var isVisible: Boolean by mutableStateOf(false)
    private set
  override var data: T? by mutableStateOf(null)
    private set

  override fun dismiss() {
    isVisible = false
  }

  override fun show(data: T) {
    this.data = data
    isVisible = true
  }
}

@OptIn(ExperimentalSheetApi::class)
@Composable
fun <T> HedvigBottomSheet(
  hedvigBottomSheetState: HedvigBottomSheetState<T>,
  contentPadding: PaddingValues? = null,
  content: @Composable ColumnScope.(T) -> Unit,
) {
  InternalHedvigBottomSheet(
    isVisible = hedvigBottomSheetState.isVisible,
    onVisibleChange = {
      if (!it) {
        hedvigBottomSheetState.dismiss()
      }
    },
    onSystemBack = {
      hedvigBottomSheetState.dismiss()
    },
    contentPadding = contentPadding,
  ) {
    if (hedvigBottomSheetState.data != null) {
      content(hedvigBottomSheetState.data!!)
    }
  }
}

@OptIn(ExperimentalSheetApi::class)
@Composable
private fun InternalHedvigBottomSheet(
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  onSystemBack: (() -> Unit)? = { onVisibleChange(false) },
  contentPadding: PaddingValues? = null,
  cancelable: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  val scrollState = rememberScrollState()
  var scrollDown by remember { mutableStateOf(false) }
  LaunchedEffect(scrollDown) {
    if (scrollDown) {
      scrollState.animateScrollTo(scrollState.maxValue)
    }
  }
  val sheetPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + Companion.Horizontal).asPaddingValues()
  ModalSheet(
    visible = isVisible,
    onVisibleChange = onVisibleChange,
    cancelable = cancelable,
    scrimColor = bottomSheetColors.scrimColor,
    backgroundColor = bottomSheetColors.bottomSheetBackgroundColor,
    contentColor = bottomSheetColors.contentColor,
    sheetPadding = sheetPadding,
    onSystemBack = onSystemBack,
    shape = bottomSheetShape.shape,
  ) {
    // [ModalSheet] automatically requests focus on appearance. This means that if there is a focusable child available,
    // it gets focus immediately. This commonly is a TextField, which also brings the keyboard up. The keyboard ends up
    // coming up way too early, before the sheet manages to show, which results in a very awkward and janky animation
    // where the keyboard and the sheet are coming up in different timings, hiding each other while the insets are not
    // quick enough to catch up to make them go up in sync.
    // Making this box focusable means that it itself grabs the focus instead, not showing the keyboard on appearance.
    Box(Modifier.consumeWindowInsets(sheetPadding).focusable()) {
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
        DragHandle(modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
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
