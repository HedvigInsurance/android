package com.hedvig.app.feature.insurance.ui.detail.documents

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailDocumentsFragmentBinding
import com.hedvig.app.feature.documents.DocumentAdapter
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
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
            model.documentsList.observe(viewLifecycleOwner, documentsAdapter::submitList)
        }
    }
}
