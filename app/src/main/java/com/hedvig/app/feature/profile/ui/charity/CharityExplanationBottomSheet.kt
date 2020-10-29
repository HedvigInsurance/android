package com.hedvig.app.feature.profile.ui.charity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R

class CharityExplanationBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_charity_explanation, container, false)

    companion object {
        const val TAG = "charity_explanation_bottom_sheet"

        fun newInstance() = CharityExplanationBottomSheet()
    }
}
