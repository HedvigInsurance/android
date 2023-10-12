package com.hedvig.android.feature.chat.legacy

import android.view.HapticFeedbackConstants
import android.view.View

internal fun View.setHapticClickListener(onClickListener: (View) -> Unit) {
  setOnClickListener { view ->
    performOnTapHapticFeedback()
    onClickListener(view)
  }
}

internal fun View.performOnTapHapticFeedback() = performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
