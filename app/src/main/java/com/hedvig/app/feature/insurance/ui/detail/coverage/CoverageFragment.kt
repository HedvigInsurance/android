package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestBuilder
import com.carousell.concatadapterextension.ConcatItemDecoration
import com.carousell.concatadapterextension.ConcatSpanSizeLookup
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailCoverageFragmentBinding
import com.hedvig.app.feature.insurablelimits.InsurableLimitsAdapter
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.util.extensions.view.updatePadding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CoverageFragment : Fragment(R.layout.contract_detail_coverage_fragment) {
    private val binding by viewBinding(ContractDetailCoverageFragmentBinding::bind)
    private val model: ContractDetailViewModel by sharedViewModel()
    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val coverageAdapter = CoverageAdapter(requestBuilder, parentFragmentManager)
        val insurableLimitsAdapter = InsurableLimitsAdapter(parentFragmentManager)
        val concatAdapter = ConcatAdapter(coverageAdapter, insurableLimitsAdapter)
        binding.root.apply {
            doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            adapter = concatAdapter
            (layoutManager as? GridLayoutManager)?.let { lm ->
                lm.spanSizeLookup = ConcatSpanSizeLookup(lm.spanCount) { concatAdapter.adapters }
            }
            addItemDecoration(ConcatItemDecoration { concatAdapter.adapters })
            model.coverageViewState.observe(viewLifecycleOwner) { viewState ->
                coverageAdapter.submitList(viewState.perilItems)
                insurableLimitsAdapter.submitList(viewState.insurableLimitItems)
            }
        }
    }
}
