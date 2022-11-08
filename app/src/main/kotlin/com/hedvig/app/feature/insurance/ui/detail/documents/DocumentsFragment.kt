package com.hedvig.app.feature.insurance.ui.detail.documents

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailDocumentsFragmentBinding
import com.hedvig.app.feature.documents.DocumentAdapter
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class DocumentsFragment : Fragment(R.layout.contract_detail_documents_fragment) {
  private val binding by viewBinding(ContractDetailDocumentsFragmentBinding::bind)
  private val viewModel: ContractDetailViewModel by activityViewModel()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.root.apply {
      applyNavigationBarInsets()
      val documentsAdapter = DocumentAdapter()
      adapter = documentsAdapter
      viewModel.viewState
        .flowWithLifecycle(lifecycle)
        .onEach { viewState ->
          val listItems = when (viewState) {
            ContractDetailViewModel.ViewState.Error -> emptyList()
            ContractDetailViewModel.ViewState.Loading -> emptyList()
            is ContractDetailViewModel.ViewState.Success -> viewState.state.documentsViewState.documents
          }
          documentsAdapter.submitList(listItems)
        }.launchIn(viewLifecycleScope)
    }
  }
}
