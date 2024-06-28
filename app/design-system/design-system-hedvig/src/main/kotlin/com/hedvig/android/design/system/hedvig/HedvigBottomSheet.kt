package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.hedvig.android.design.system.hedvig.HedvigBottomSheetType.FullScreenWithManyTextInputs
import com.hedvig.android.design.system.hedvig.HedvigBottomSheetType.SimpleHalfScreenSheet
import com.hedvig.android.design.system.hedvig.tokens.BottomSheetTokens
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetSize
import com.skydoves.flexible.core.FlexibleSheetValue.FullyExpanded
import com.skydoves.flexible.core.FlexibleSheetValue.IntermediatelyExpanded
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState

@Composable
fun HedvigBottomSheet(
  onVisibleChange: (Boolean) -> Unit,
  allowNestedScroll: Boolean,
  sheetType: HedvigBottomSheetType,
  onDismissRequest: () -> Unit = { onVisibleChange(false) },
  content: @Composable ColumnScope.() -> Unit,
) {
  when (sheetType) {
    FullScreenWithManyTextInputs -> FullScreenWithManyTextInputsBottomSheet(
      onVisibleChange,
      allowNestedScroll,
      onDismissRequest,
      content,
    )

    SimpleHalfScreenSheet -> SimpleHalfScreenSheetBottomSheet(
      onVisibleChange,
      allowNestedScroll,
      onDismissRequest,
      content,
    )
  }
}

@Composable
private fun FullScreenWithManyTextInputsBottomSheet(
  onVisibleChange: (Boolean) -> Unit,
  allowNestedScroll: Boolean,
  onDismissRequest: (() -> Unit) = { onVisibleChange(false) },
  content: @Composable ColumnScope.() -> Unit,
) {
  var currentSheetTarget by remember {
    mutableStateOf(FullyExpanded)
  }
  val sheetState = rememberFlexibleBottomSheetState(
    skipSlightlyExpanded = true,
    skipIntermediatelyExpanded = true,
    isModal = HedvigBottomSheetDefaults.ISMODAL,
    containSystemBars = HedvigBottomSheetDefaults.containSystemBars,
    allowNestedScroll = allowNestedScroll,
    flexibleSheetSize = HedvigBottomSheetDefaults.flexibleSheetSize,
  )
  FlexibleBottomSheet(
    sheetState = sheetState,
    scrimColor = HedvigBottomSheetDefaults.bottomSheetColors.scrimColor,
    containerColor = HedvigBottomSheetDefaults.bottomSheetColors.bottomSheetBackgroundColor,
    contentColor = HedvigBottomSheetDefaults.bottomSheetColors.contentColor,
    windowInsets = HedvigBottomSheetDefaults.defaultInsets,
    modifier = HedvigBottomSheetDefaults.modifier,
    onDismissRequest = onDismissRequest,
    shape = HedvigBottomSheetDefaults.bottomSheetShape.shape,
    onTargetChanges = { sheetValue ->
      currentSheetTarget = sheetValue
    },
  ) {
    Column {
      content()
    }
  }
}

@Composable
private fun SimpleHalfScreenSheetBottomSheet(
  onVisibleChange: (Boolean) -> Unit,
  allowNestedScroll: Boolean,
  onDismissRequest: (() -> Unit) = { onVisibleChange(false) },
  content: @Composable ColumnScope.() -> Unit,
) {
  var currentSheetTarget by remember {
    mutableStateOf(IntermediatelyExpanded)
  }
  val sheetState = rememberFlexibleBottomSheetState(
    skipSlightlyExpanded = true,
    skipIntermediatelyExpanded = false,
    isModal = HedvigBottomSheetDefaults.ISMODAL,
    containSystemBars = HedvigBottomSheetDefaults.containSystemBars,
    allowNestedScroll = allowNestedScroll,
    flexibleSheetSize = HedvigBottomSheetDefaults.flexibleSheetSize,
  )
  FlexibleBottomSheet(
    sheetState = sheetState,
    scrimColor = HedvigBottomSheetDefaults.bottomSheetColors.scrimColor,
    containerColor = HedvigBottomSheetDefaults.bottomSheetColors.bottomSheetBackgroundColor,
    contentColor = HedvigBottomSheetDefaults.bottomSheetColors.contentColor,
    windowInsets = HedvigBottomSheetDefaults.defaultInsets,
    modifier = HedvigBottomSheetDefaults.modifier,
    onDismissRequest = onDismissRequest,
    shape = HedvigBottomSheetDefaults.bottomSheetShape.shape,
    onTargetChanges = { sheetValue ->
      currentSheetTarget = sheetValue
    },
  ) {
    Column {
      content()
    }
  }
}

private object HedvigBottomSheetDefaults {
  val bottomSheetColors: BottomSheetColors
    @Composable
    get() = with(HedvigTheme.colorScheme) {
      remember(this) {
        BottomSheetColors(
          scrimColor = fromToken(BottomSheetTokens.ScrimColor),
          bottomSheetBackgroundColor = fromToken(BottomSheetTokens.BottomSheetBackgroundColor),
          contentColor = fromToken(BottomSheetTokens.ContentColor),
        )
      }
    }

  val bottomSheetShape: BottomSheetShape
    @Composable
    get() = BottomSheetShape(BottomSheetTokens.ContainerShape.value)

  val modifier: Modifier
    @Composable
    get() = Modifier.positionAwareImePadding()

  const val ISMODAL = true

  val containSystemBars = true

  val flexibleSheetSize: FlexibleSheetSize
    get() = FlexibleSheetSize(
      fullyExpanded = 1.0f,
      intermediatelyExpanded = 0.6f,
      slightlyExpanded = 0.15f,
    )

  val defaultInsets: WindowInsets = WindowInsets(0, 0, 0, 0)
}

@Immutable
internal data class BottomSheetColors(
  val scrimColor: Color,
  val bottomSheetBackgroundColor: Color,
  val contentColor: Color,
)

@Immutable
internal data class BottomSheetShape(
  val shape: Shape,
)

enum class HedvigBottomSheetType {
  FullScreenWithManyTextInputs,

  /** should use this one if there many TextFields with input (ime works better), or a long content */

  SimpleHalfScreenSheet,
  /** should use this one with short content */

  // tb added if needed: dynamic content depending on sheetSize; slightly expanded and always visible etc
}
