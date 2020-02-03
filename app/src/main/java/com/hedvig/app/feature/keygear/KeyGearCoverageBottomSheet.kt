package com.hedvig.app.feature.keygear

import android.app.Dialog
import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment

class KeyGearCoverageBottomSheet : RoundedBottomSheetDialogFragment() {

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.dialog_key_gear_coverage)


        return dialog
    }

    companion object {
        const val TAG = "coverageBottomSheet"

        fun newInstance(): KeyGearCoverageBottomSheet {
            return KeyGearCoverageBottomSheet()
        }
    }
}
