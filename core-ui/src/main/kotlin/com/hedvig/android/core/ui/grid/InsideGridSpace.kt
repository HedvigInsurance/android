package com.hedvig.android.core.ui.grid

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp

@Immutable
class InsideGridSpace private constructor(
  @Stable
  val horizontal: Dp,
  @Stable
  val vertical: Dp,
) {
  companion object {
    operator fun invoke(both: Dp): InsideGridSpace = InsideGridSpace(both, both)

    operator fun invoke(horizontal: Dp, vertical: Dp): InsideGridSpace = InsideGridSpace(horizontal, vertical)
  }
}
