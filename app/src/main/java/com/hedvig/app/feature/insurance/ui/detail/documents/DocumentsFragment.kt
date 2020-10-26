package com.hedvig.app.feature.insurance.ui.detail.documents

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailDocumentsFragmentBinding
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import org.koin.android.viewmodel.ext.android.sharedViewModel

class DocumentsFragment : Fragment(R.layout.contract_detail_documents_fragment) {
    private val binding by viewBinding(ContractDetailDocumentsFragmentBinding::bind)
    private val model: ContractDetailViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.root.apply {
            doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            adapter = DocumentsAdapter()
            model.data.observe(viewLifecycleOwner) { data ->
                (adapter as? DocumentsAdapter)?.submitList(
                    listOfNotNull(
                        data.currentAgreement.asAgreementCore?.certificateUrl?.let {
                            DocumentsModel(
                                getString(R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE),
                                it
                            )
                        },
                        DocumentsModel(
                            getString(R.string.MY_DOCUMENTS_INSURANCE_TERMS),
                            data.termsAndConditions.url
                        )
                    )
                )
            }
        }
    }
}
