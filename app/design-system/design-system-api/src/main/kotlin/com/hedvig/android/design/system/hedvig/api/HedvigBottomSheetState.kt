package com.hedvig.android.design.system.hedvig.api

import androidx.compose.runtime.Stable

@Stable
interface HedvigBottomSheetState<T> {
  val isVisible: Boolean
  val data: T?

  fun show(data: T)

  fun dismiss()
}
