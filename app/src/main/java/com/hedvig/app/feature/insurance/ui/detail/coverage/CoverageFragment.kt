package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailCoverageFragmentBinding
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class CoverageFragment : Fragment(R.layout.contract_detail_coverage_fragment) {
    private val binding by viewBinding(ContractDetailCoverageFragmentBinding::bind)
    private val model: ContractDetailViewModel by sharedViewModel()
    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.root.apply {
            doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            adapter = CoverageAdapter(requestBuilder, parentFragmentManager)
            (layoutManager as? GridLayoutManager)?.spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) =
                        when ((adapter as? CoverageAdapter)?.currentList?.get(position)) {
                            is CoverageModel.Peril -> 1
                            else -> 2
                        }
                }
            addItemDecoration(CoverageItemDecoration())

            model.data.observe(viewLifecycleOwner) { data ->
                (adapter as? CoverageAdapter)?.submitList(listOf(CoverageModel.Header.Perils(data.typeOfContract))
                    + data.perils.map {
                    CoverageModel.Peril(
                        it.fragments.perilFragment
                    )
                }
                    + CoverageModel.Header.InsurableLimits + data.insurableLimits.map {
                    CoverageModel.InsurableLimit(
                        it.fragments.insurableLimitsFragment
                    )
                })
            }
        }
    }
}
