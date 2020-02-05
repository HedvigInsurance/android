package com.hedvig.app.feature.keygear.ui

import android.app.Dialog
import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment

class KeyGearValuationBottomSheet : RoundedBottomSheetDialogFragment() {

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(R.layout.dialog_key_gear_valuation)


        return dialog
    }

    companion object {
        const val TAG = "keyGearValuationBottomSheet"

        fun newInstance() = KeyGearValuationBottomSheet()
    }
}
