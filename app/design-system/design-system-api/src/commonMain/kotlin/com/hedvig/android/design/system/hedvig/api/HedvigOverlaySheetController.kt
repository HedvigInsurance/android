package com.hedvig.android.design.system.hedvig.api

import androidx.compose.runtime.Stable

/**
 * Drives the hide animation of a presence-driven overlay bottom sheet (one whose visibility is the
 * presence of a back-stack entry, not an imperative state flag). The renderer binds the underlying
 * material sheet state; callers (e.g. a navigation scene's removal hook) call [hide] to animate the
 * sheet closed before the entry leaves composition.
 */
@Stable
interface HedvigOverlaySheetController {
  suspend fun hide()
}
