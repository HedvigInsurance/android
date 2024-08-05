package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.alpha
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

@OptIn(ExperimentalSheetApi::class, ExperimentalLayoutApi::class)
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
  val scrollState = rememberScrollState()
  var scrollDown by remember { mutableStateOf(false) }
  LaunchedEffect(scrollDown) {
    if (scrollDown) {
      scrollState.animateScrollTo(scrollState.maxValue)
    }
  }
  val isImeVisible = WindowInsets.isImeVisible
  val defaultPadding = if (isImeVisible) {
    WindowInsets.ime.asPaddingValues()
  } else {
    WindowInsets.safeDrawing
      .only(WindowInsetsSides.Bottom + WindowInsetsSides.Top).asPaddingValues()
  }
  val finalSheetPadding = sheetPadding ?: defaultPadding

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
        modifier = Modifier.padding(horizontal = bottomSheetShape.contentHorizontalPadding)
          .verticalScroll(scrollState),
      ) {
        Spacer(modifier = Modifier.height(8.dp))
        LittleUpperChip()
        Spacer(modifier = Modifier.height(20.dp))
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
      if (scrollState.canScrollForward && !scrollDown) {
        HintArrowDown {
          scrollDown = true
        }
      }
    }
  }
}

@Composable
private fun ColumnScope.LittleUpperChip() {
  Surface(
    modifier = Modifier
      .width(40.dp)
      .height(4.dp)
      .align(Alignment.CenterHorizontally)
      .background(
        shape = ShapeDefaults.CornerSmall,
        color = bottomSheetColors.chipColor,
      ).clip(ShapeDefaults.CornerSmall),
  ) {}
}

@Composable
private fun BoxScope.HintArrowDown(onClick: () -> Unit) {
  val infiniteTransition = rememberInfiniteTransition(label = "arrowDownAlpha")
  val arrowAlpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = 2000,
        easing = LinearEasing,
      ),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "arrowDownAlpha animation",
  )
  Row(Modifier.align(Alignment.BottomEnd).padding(horizontal = 32.dp, vertical = 16.dp)) {
    Icon(
      HedvigIcons.ArrowDown,
      null,
      Modifier.alpha(arrowAlpha).clickable { onClick() },
    )
  }
}

@Immutable
internal data class BottomSheetColors(
  val scrimColor: Color,
  val bottomSheetBackgroundColor: Color,
  val contentColor: Color,
  val chipColor: Color,
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
