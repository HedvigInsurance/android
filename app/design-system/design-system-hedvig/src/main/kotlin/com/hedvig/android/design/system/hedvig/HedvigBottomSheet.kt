package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
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
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.icon.ArrowDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.BottomSheetTokens
import eu.wewox.modalsheet.ExperimentalSheetApi
import eu.wewox.modalsheet.ModalSheet

@OptIn(ExperimentalSheetApi::class)
@Composable
fun HedvigBottomSheet(
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  onSystemBack: (() -> Unit)? = { onVisibleChange(false) },
  sheetPadding: PaddingValues? = null,
  contentPadding: PaddingValues? = null,
  cancelable: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  InternalHedvigBottomSheet(
    isVisible = isVisible,
    onVisibleChange = onVisibleChange,
    onSystemBack = onSystemBack,
    sheetPadding = sheetPadding,
    contentPadding = contentPadding,
    cancelable = cancelable,
    content = content,
  )
}

@OptIn(ExperimentalSheetApi::class)
@Composable
fun HedvigBottomSheet(
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
  bottomButtonText: String,
  onSystemBack: (() -> Unit)? = { onVisibleChange(false) },
  onBottomButtonClick: () -> Unit = { onVisibleChange(false) },
  sheetPadding: PaddingValues? = null,
  cancelable: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  InternalHedvigBottomSheet(
    isVisible = isVisible,
    onVisibleChange = onVisibleChange,
    onSystemBack = onSystemBack,
    sheetPadding = sheetPadding,
    cancelable = cancelable,
  ) {
    content()
    HedvigButton(
      onClick = onBottomButtonClick,
      text = bottomButtonText,
      enabled = true,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(32.dp))
  }
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
  sheetPadding: PaddingValues? = null,
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
  val finalSheetPadding =
    sheetPadding ?: WindowInsets.safeDrawing.only(WindowInsetsSides.Top + Companion.Horizontal).asPaddingValues()
  ModalSheet(
    visible = isVisible,
    onVisibleChange = onVisibleChange,
    cancelable = cancelable,
    scrimColor = bottomSheetColors.scrimColor,
    backgroundColor = bottomSheetColors.bottomSheetBackgroundColor,
    contentColor = bottomSheetColors.contentColor,
    sheetPadding = finalSheetPadding,
    onSystemBack = onSystemBack,
    shape = bottomSheetShape.shape,
  ) {
    Box {
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
        DragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
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
  Surface(
    modifier = modifier
      .width(40.dp)
      .height(4.dp)
      .background(
        shape = HedvigTheme.shapes.cornerSmall,
        color = bottomSheetColors.chipColor,
      )
      .clip(HedvigTheme.shapes.cornerSmall),
  ) {}
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
        scrimColor = fromToken(BottomSheetTokens.ScrimColor),
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
