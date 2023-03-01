package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import com.hedvig.android.feature.cancelinsurance.CancelInsuranceActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailYourInfoFragmentBinding
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.table.TableAdapter
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class YourInfoFragment : Fragment(R.layout.contract_detail_your_info_fragment) {

  private val binding by viewBinding(ContractDetailYourInfoFragmentBinding::bind)
  private val viewModel: ContractDetailViewModel by activityViewModel()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.root.applyNavigationBarInsets()
    val topYourInfoAdapter = YourInfoAdapter(parentFragmentManager, ::openCancelInsuranceScreen)
    val tableAdapter = TableAdapter()
    val bottomYourInfoAdapter = YourInfoAdapter(parentFragmentManager, ::openCancelInsuranceScreen)

    binding.recycler.adapter = ConcatAdapter(
      topYourInfoAdapter,
      tableAdapter,
      bottomYourInfoAdapter,
    )

    viewModel.viewState
      .flowWithLifecycle(lifecycle)
      .onEach { viewState ->
        when (viewState) {
          ContractDetailViewModel.ViewState.Error -> {
            topYourInfoAdapter.submitList(emptyList())
            bottomYourInfoAdapter.submitList(emptyList())
          }
          ContractDetailViewModel.ViewState.Loading -> {
            topYourInfoAdapter.submitList(emptyList())
            bottomYourInfoAdapter.submitList(emptyList())
          }
          is ContractDetailViewModel.ViewState.Success -> {
            val state = viewState.state.memberDetailsViewState
            tableAdapter.setTable(state.detailsTable)
            topYourInfoAdapter.submitList(listOfNotNull(state.pendingAddressChange))
            bottomYourInfoAdapter.submitList(
              listOfNotNull(
                state.changeAddressButton,
                state.change,
                state.cancelInsurance,
              ),
            )
          }
        }
      }
      .launchIn(viewLifecycleScope)
  }

  private fun openCancelInsuranceScreen(insuranceId: String) {
    startActivity(CancelInsuranceActivity.newInstance(requireContext(), insuranceId))
  }
}
