package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailYourInfoFragmentBinding
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class YourInfoFragment : Fragment(R.layout.contract_detail_your_info_fragment) {
    private val binding by viewBinding(ContractDetailYourInfoFragmentBinding::bind)
    private val model: ContractDetailViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.root.apply {
            adapter = YourInfoAdapter()

            model.data.observe(viewLifecycleOwner) { data ->
                data.currentAgreement.asNorwegianTravelAgreement?.let {
                    (adapter as? YourInfoAdapter)?.submitList(
                        coinsuredSection(it.numberCoInsured)
                    )
                }
            }
        }
    }

    private fun coinsuredSection(amount: Int) = listOf(
        YourInfoModel.Header.Coinsured,
        YourInfoModel.Row(
            getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER),
            when (amount) {
                0 -> getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT_ZERO_COINSURED)
                1 -> getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT_ONE_COINSURED)
                else -> getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT, amount)
            }
        )
    )
}
