package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailYourInfoFragmentBinding
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class YourInfoFragment : Fragment(R.layout.contract_detail_your_info_fragment) {

    private val binding by viewBinding(ContractDetailYourInfoFragmentBinding::bind)
    private val model: ContractDetailViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.root.applyNavigationBarInsets()
        val adapter = YourInfoAdapter(parentFragmentManager)
        binding.recycler.adapter = adapter
        model.yourInfoList.observe(viewLifecycleOwner, adapter::submitList)
    }
}
