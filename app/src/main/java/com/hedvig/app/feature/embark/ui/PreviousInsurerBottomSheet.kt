package com.hedvig.app.feature.embark.ui

import androidx.core.os.bundleOf
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerData
import com.hedvig.app.ui.view.ExpandableBottomSheet

class PreviousInsurerBottomSheet : ExpandableBottomSheet() {

    companion object {

        val TAG: String = PreviousInsurerBottomSheet::class.java.name
        private const val PREVIOUS_INSURERS = "PREVIOUS_INSURERS"

        fun newInstance(previousInsurers: List<PreviousInsurerData.PreviousInsurer>) = PreviousInsurerBottomSheet().apply {
            arguments = bundleOf(PREVIOUS_INSURERS to previousInsurers)
        }
    }
}
