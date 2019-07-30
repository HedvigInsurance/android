package com.hedvig.app.feature.profile.ui.charity

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment

class CharityExplanationBottomSheet : RoundedBottomSheetDialogFragment() {

    override fun getTheme() = R.style.NoTitleBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_charity_explanation, null)
        dialog.setContentView(view)

        return dialog
    }

    companion object {

        fun newInstance() = CharityExplanationBottomSheet()
    }
}
