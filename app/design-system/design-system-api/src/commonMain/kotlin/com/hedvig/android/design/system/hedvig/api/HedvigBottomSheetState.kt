package com.hedvig.android.design.system.hedvig.api

import androidx.compose.runtime.Stable

/**
 * State for a bottom sheet carrying a payload of [T].
 *
 * [T] is non-null by design: the sheet renders its content only while [data] is set, so a nullable payload
 * would let you build a sheet that shows itself and draws nothing. Sheets that carry nothing use [Unit].
 */
@Stable
interface HedvigBottomSheetState<T : Any> {
  /**
   * Controls whether the sheet should be in composition or not
   * When the sheet enters composition, it internally takes care of animating itself from being hidden to being shown
   */
  val isVisible: Boolean
  val data: T?

  fun show(data: T)

  fun dismiss(onDismissed: () -> Unit = {})
}
