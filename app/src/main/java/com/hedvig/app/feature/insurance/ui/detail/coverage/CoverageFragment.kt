package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import coil.ImageLoader
import com.carousell.concatadapterextension.ConcatItemDecoration
import com.carousell.concatadapterextension.ConcatSpanSizeLookup
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailCoverageFragmentBinding
import com.hedvig.app.feature.insurablelimits.InsurableLimitsAdapter
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.perils.PerilsAdapter
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CoverageFragment : Fragment(R.layout.contract_detail_coverage_fragment) {
    private val binding by viewBinding(ContractDetailCoverageFragmentBinding::bind)
    private val model: ContractDetailViewModel by sharedViewModel()
    private val imageLoader: ImageLoader by inject()
    private val trackingFacade: TrackingFacade by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val perilsAdapter = PerilsAdapter(
            fragmentManager = parentFragmentManager,
            imageLoader = imageLoader,
            trackingFacade = trackingFacade,
        )
        val insurableLimitsAdapter = InsurableLimitsAdapter(parentFragmentManager)
        val concatAdapter = ConcatAdapter(perilsAdapter, insurableLimitsAdapter)
        binding.root.apply {
            applyNavigationBarInsets()
            adapter = concatAdapter
            (layoutManager as? GridLayoutManager)?.let { lm ->
                lm.spanSizeLookup = ConcatSpanSizeLookup(lm.spanCount) { concatAdapter.adapters }
            }
            addItemDecoration(ConcatItemDecoration { concatAdapter.adapters })
            model.viewState
                .flowWithLifecycle(lifecycle)
                .onEach { viewState ->
                    when (viewState) {
                        ContractDetailViewModel.ViewState.Error -> {
                            perilsAdapter.submitList(emptyList())
                            insurableLimitsAdapter.submitList(emptyList())
                        }
                        ContractDetailViewModel.ViewState.Loading -> {
                            perilsAdapter.submitList(emptyList())
                            insurableLimitsAdapter.submitList(emptyList())
                        }
                        is ContractDetailViewModel.ViewState.Success -> {
                            perilsAdapter.submitList(viewState.state.coverageViewState.perils)
                            insurableLimitsAdapter.submitList(viewState.state.coverageViewState.insurableLimits)
                        }
                    }
                }
                .launchIn(viewLifecycleScope)
        }
    }
}
