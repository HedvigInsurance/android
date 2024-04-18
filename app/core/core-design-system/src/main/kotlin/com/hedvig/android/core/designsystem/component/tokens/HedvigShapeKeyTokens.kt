package com.hedvig.android.core.designsystem.component.tokens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import com.hedvig.android.core.designsystem.material3.fromToken

@Suppress("unused")
internal enum class HedvigShapeKeyTokens {
  CornerExtraLarge,
  CornerExtraLargeTop,
  CornerExtraSmall,
  CornerExtraSmallTop,
  CornerFull,
  CornerLarge,
  CornerLargeTop,
  CornerMedium,
  CornerNone,
  CornerSmall,
}

internal val HedvigShapeKeyTokens.value: Shape
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.shapes.fromToken(this)
