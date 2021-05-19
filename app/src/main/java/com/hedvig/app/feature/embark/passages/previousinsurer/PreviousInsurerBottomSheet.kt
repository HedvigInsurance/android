package com.hedvig.app.feature.embark.passages.previousinsurer

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.ui.view.ExpandableBottomSheet
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PreviousInsurerBottomSheet : ExpandableBottomSheet() {

    private val viewModel: PreviousInsurerViewModel by sharedViewModel()
    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()

    private val insurers by lazy {
        requireArguments()
            .getParcelableArrayList<PreviousInsurerParameter.PreviousInsurer>(PREVIOUS_INSURERS)
            ?: throw IllegalArgumentException("No argument passed to ${this.javaClass.name}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.adapter = PreviousInsurerAdapter(
            context = requireContext(),
            previousInsurers = insurers,
            requestBuilder = requestBuilder,
            onInsurerClicked = ::onInsurerSelected
        )
    }

    private fun onInsurerSelected(item: PreviousInsurerItem.Insurer) {
        viewModel.setPreviousInsurer(item)
        dismiss()
    }

    companion object {

        val TAG: String = PreviousInsurerBottomSheet::class.java.name
        private const val PREVIOUS_INSURERS = "PREVIOUS_INSURERS"

        fun newInstance(previousInsurers: List<PreviousInsurerParameter.PreviousInsurer>) = PreviousInsurerBottomSheet().apply {
            arguments = bundleOf(PREVIOUS_INSURERS to previousInsurers)
        }
    }
}
