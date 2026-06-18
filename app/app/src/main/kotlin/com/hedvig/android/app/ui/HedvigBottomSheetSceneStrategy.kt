package com.hedvig.android.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.BottomSheetSceneStrategy

/**
 * Creates and remembers a [BottomSheetSceneStrategy] styled with Hedvig design tokens, matching
 * the appearance of [com.hedvig.android.design.system.hedvig.HedvigBottomSheet] exactly:
 * - containerColor: BackgroundPrimary (BottomSheetTokens.BottomSheetBackgroundColor)
 * - contentColor: TextPrimary (BottomSheetTokens.ContentColor)
 * - scrimColor: Scrim at 32 % opacity (ScrimTokens.ContainerColor × ContainerOpacity)
 * - shape: CornerXLargeTop (BottomSheetTokens.ContainerShape)
 * - dragHandle: Hedvig chip (40×4 dp, SurfaceSecondary, CornerSmall, padded 8/20 dp)
 */
@Composable
internal fun rememberHedvigBottomSheetSceneStrategy(): BottomSheetSceneStrategy<HedvigNavKey> {
  val containerColor = HedvigTheme.colorScheme.backgroundPrimary
  val contentColor = HedvigTheme.colorScheme.textPrimary
  // ScrimTokens.ContainerColor = Scrim, ScrimTokens.ContainerOpacity = 0.32f
  val scrimColor = HedvigTheme.colorScheme.scrim.copy(alpha = 0.32f)
  val shape = HedvigTheme.shapes.cornerXLargeTop
  // BottomSheetTokens.UpperChipColor = SurfaceSecondary
  val chipColor = HedvigTheme.colorScheme.surfaceSecondary
  val chipShape = HedvigTheme.shapes.cornerSmall
  return remember(containerColor, contentColor, scrimColor, shape, chipColor, chipShape) {
    BottomSheetSceneStrategy(
      containerColor = containerColor,
      contentColor = contentColor,
      scrimColor = scrimColor,
      shape = shape,
      dragHandle = {
        Box(
          modifier = Modifier
            .wrapContentWidth(Alignment.CenterHorizontally)
            .padding(top = 8.dp, bottom = 20.dp)
            .size(width = 40.dp, height = 4.dp)
            .background(shape = chipShape, color = chipColor),
        )
      },
    )
  }
}
