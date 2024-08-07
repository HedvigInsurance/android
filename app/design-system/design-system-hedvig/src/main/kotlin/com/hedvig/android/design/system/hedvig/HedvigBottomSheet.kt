package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
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
  val defaultPadding = WindowInsets.safeDrawing.asPaddingValues()
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
        DragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
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
      Crossfade(
        modifier = Modifier.align(Alignment.BottomEnd).padding(horizontal = 32.dp, vertical = 16.dp),
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
        shape = ShapeDefaults.CornerSmall,
        color = bottomSheetColors.chipColor,
      ).clip(ShapeDefaults.CornerSmall),
  ) {}
}

@Composable
private fun HintArrowDown(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(modifier = modifier) {
    IconButton(
      onClick = onClick,
      modifier = Modifier.clip(CircleShape).background(bottomSheetColors.arrowBackgroundColor),
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
