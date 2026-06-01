package com.hedvig.android.design.system.hedvig.api

/**
 * Implemented by Swift. While a pointer is down on a marked region we
 * disable the system swipe-back so Compose's horizontal scroll can win;
 * on touch end we re-enable it.
 */
interface IosSwipeBackController {
  fun setSwipeBackEnabled(isEnabled: Boolean)
}
