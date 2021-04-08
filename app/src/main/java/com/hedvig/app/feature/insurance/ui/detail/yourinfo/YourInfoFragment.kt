package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailYourInfoFragmentBinding
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.util.extensions.view.updatePadding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class YourInfoFragment : Fragment(R.layout.contract_detail_your_info_fragment) {

    private val binding by viewBinding(ContractDetailYourInfoFragmentBinding::bind)
    private val model: ContractDetailViewModel by sharedViewModel()
    private val adapter = YourInfoAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.root.apply {
            doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
        }

        binding.recycler.adapter = adapter
        model.yourInfoList.observe(viewLifecycleOwner, adapter::submitList)
    }
}
