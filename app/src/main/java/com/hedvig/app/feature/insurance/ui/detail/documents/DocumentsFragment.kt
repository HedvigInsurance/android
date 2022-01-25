package com.hedvig.app.feature.insurance.ui.detail.documents

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailDocumentsFragmentBinding
import com.hedvig.app.feature.documents.DocumentAdapter
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DocumentsFragment : Fragment(R.layout.contract_detail_documents_fragment) {
    private val binding by viewBinding(ContractDetailDocumentsFragmentBinding::bind)
    private val model: ContractDetailViewModel by sharedViewModel()
    private val trackingFacade: TrackingFacade by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.root.apply {
            applyNavigationBarInsets()
            val documentsAdapter = DocumentAdapter(trackingFacade)
            adapter = documentsAdapter
            model.viewState
                .flowWithLifecycle(lifecycle)
                .onEach { viewState ->
                    when (viewState) {
                        ContractDetailViewModel.ViewState.Error -> {}
                        ContractDetailViewModel.ViewState.Loading -> {}
                        is ContractDetailViewModel.ViewState.Success -> {
                            documentsAdapter.submitList(viewState.state.documentsViewState.documents)
                        }
                    }
                }.launchIn(viewLifecycleScope)
        }
    }
}
