package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailYourInfoFragmentBinding
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.table.TableAdapter
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class YourInfoFragment : Fragment(R.layout.contract_detail_your_info_fragment) {

    private val binding by viewBinding(ContractDetailYourInfoFragmentBinding::bind)
    private val model: ContractDetailViewModel by sharedViewModel()
    private val trackingFacade: TrackingFacade by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.root.applyNavigationBarInsets()
        val topYourInfoAdapter = YourInfoAdapter(parentFragmentManager, trackingFacade)
        val tableAdapter = TableAdapter()
        val bottomYourInfoAdapter = YourInfoAdapter(parentFragmentManager, trackingFacade)

        binding.recycler.adapter = ConcatAdapter(
            topYourInfoAdapter,
            tableAdapter,
            bottomYourInfoAdapter,
        )
        model
            .yourInfoList
            .flowWithLifecycle(viewLifecycle)
            .onEach { viewState ->
                when (viewState) {
                    is ContractDetailViewModel.YourInfoViewState.Success -> {
                        topYourInfoAdapter.submitList(viewState.topItems)
                        tableAdapter.setTable(viewState.detailsTable)
                        bottomYourInfoAdapter.submitList(viewState.bottomItems)
                    }
                    ContractDetailViewModel.YourInfoViewState.Error -> {
                    }
                    ContractDetailViewModel.YourInfoViewState.Loading -> {
                    }
                }
            }
            .launchIn(viewLifecycleScope)
    }
}
