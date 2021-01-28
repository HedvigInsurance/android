package com.hedvig.app.feature.embark.ui

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerAdapter
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerFragment
import com.hedvig.app.feature.embark.passages.previousinsurer.PreviousInsurerParameter
import com.hedvig.app.ui.view.ExpandableBottomSheet
import org.koin.android.ext.android.inject

class PreviousInsurerBottomSheet : ExpandableBottomSheet() {

    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()

    private val insurers by lazy {
        requireArguments()
            .getParcelableArrayList<PreviousInsurerParameter.PreviousInsurer>(PREVIOUS_INSURERS)
            ?: throw IllegalArgumentException("No argument passed to ${this.javaClass.name}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.adapter = PreviousInsurerAdapter(insurers, requestBuilder, ::onInsurerSelected)
    }

    private fun onInsurerSelected(id: String) {
        val bundle = bundleOf(EXTRA_INSURER_ID to id)
        setFragmentResult(PreviousInsurerFragment.REQUEST_SELECT_INSURER, bundle)
        dismiss()
    }

    companion object {

        val TAG: String = PreviousInsurerBottomSheet::class.java.name
        const val EXTRA_INSURER_ID = "INSURER_ID"
        private const val PREVIOUS_INSURERS = "PREVIOUS_INSURERS"

        fun newInstance(previousInsurers: List<PreviousInsurerParameter.PreviousInsurer>) = PreviousInsurerBottomSheet().apply {
            arguments = bundleOf(PREVIOUS_INSURERS to previousInsurers)
        }
    }
}
