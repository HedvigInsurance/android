package com.hedvig.app.feature.embark.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.PreviousInsurerBottomSheetLayoutBinding
import com.hedvig.app.util.extensions.viewBinding

class PreviousInsurerBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(PreviousInsurerBottomSheetLayoutBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.previous_insurer_bottom_sheet_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    companion object {
        val TAG = PreviousInsurerBottomSheet::class.java.name
        fun newInstance() = PreviousInsurerBottomSheet()
    }
}
