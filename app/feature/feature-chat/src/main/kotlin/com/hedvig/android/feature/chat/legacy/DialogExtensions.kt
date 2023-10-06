package com.hedvig.android.feature.chat.legacy

import android.app.Dialog
import android.view.WindowManager
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

internal fun Dialog.makeKeyboardAware() {
  window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
  setOnShowListener {
    (this as? BottomSheetDialog)?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let {
      BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
    }
  }
}
