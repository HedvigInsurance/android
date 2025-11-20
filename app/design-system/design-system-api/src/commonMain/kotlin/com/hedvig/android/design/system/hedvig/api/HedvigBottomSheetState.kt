package com.hedvig.android.design.system.hedvig.api

import androidx.compose.runtime.Stable

@Stable
interface HedvigBottomSheetState<T> {
  /**
   * Controls whether the sheet should be in composition or not
   * When the sheet enters composition, it internally takes care of animating itself from being hidden to being shown
   */
  val isVisible: Boolean
  val data: T?

  fun show(data: T)

  fun dismiss()
}
