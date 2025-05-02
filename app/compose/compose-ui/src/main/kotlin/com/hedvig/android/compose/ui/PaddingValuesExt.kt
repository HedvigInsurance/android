package com.hedvig.android.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

operator fun PaddingValues.plus(that: PaddingValues): PaddingValues = object : PaddingValues {
  override fun calculateBottomPadding(): Dp = this@plus.calculateBottomPadding() + that.calculateBottomPadding()

  override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
    this@plus.calculateLeftPadding(layoutDirection) + that.calculateLeftPadding(layoutDirection)

  override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
    this@plus.calculateRightPadding(layoutDirection) + that.calculateRightPadding(layoutDirection)

  override fun calculateTopPadding(): Dp = this@plus.calculateTopPadding() + that.calculateTopPadding()
}

operator fun WindowInsets.plus(that: PaddingValues): WindowInsets = CombiningPaddingValuesInsets(this, that)

@Stable
private class CombiningPaddingValuesInsets(
  private val originalWindowInsets: WindowInsets,
  private val paddingValues: PaddingValues,
) : WindowInsets {
  override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int {
    return with(density) {
      paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }
  }

  override fun getTop(density: Density): Int {
    return with(density) {
      paddingValues.calculateTopPadding().roundToPx()
    }
  }

  override fun getRight(density: Density, layoutDirection: LayoutDirection): Int {
    return with(density) {
      paddingValues.calculateRightPadding(layoutDirection).roundToPx()
    }
  }

  override fun getBottom(density: Density): Int {
    return with(density) {
      paddingValues.calculateBottomPadding().roundToPx()
    }
  }

  override fun toString(): String {
    val layoutDirection = LayoutDirection.Ltr
    val start = paddingValues.calculateLeftPadding(layoutDirection)
    val top = paddingValues.calculateTopPadding()
    val end = paddingValues.calculateRightPadding(layoutDirection)
    val bottom = paddingValues.calculateBottomPadding()
    return "$originalWindowInsets + PaddingValues($start, $top, $end, $bottom)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other !is CombiningPaddingValuesInsets) {
      return false
    }

    return other.originalWindowInsets == originalWindowInsets && other.paddingValues == paddingValues
  }

  override fun hashCode(): Int = originalWindowInsets.hashCode() + paddingValues.hashCode() * 31
}
