package com.hedvig.app.feature.profile.ui.charity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.BottomSheetCharityExplanationBinding
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.viewBinding

class CharityExplanationBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(BottomSheetCharityExplanationBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_charity_explanation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.body.setMarkdownText(getString(R.string.PROFILE_MY_CHARITY_INFO_BODY))
    }

    companion object {
        const val TAG = "charity_explanation_bottom_sheet"

        fun newInstance() = CharityExplanationBottomSheet()
    }
}
