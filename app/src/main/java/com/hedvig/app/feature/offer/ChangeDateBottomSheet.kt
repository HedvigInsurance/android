package com.hedvig.app.feature.offer

import android.app.Dialog
import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment

class ChangeDateBottomSheet: RoundedBottomSheetDialogFragment() {

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(R.layout.dialog_change_start_date)

        return dialog
    }

    companion object {

        const val TAG = "changeDateBottomSheet"

        fun newInstance(): ChangeDateBottomSheet {
            return ChangeDateBottomSheet()
        }
    }

}
