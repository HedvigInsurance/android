package com.hedvig.app.feature.insurance.ui.tab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import coil.ImageLoader
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentInsuranceBinding
import com.hedvig.app.feature.crossselling.ui.detail.handleAction
import com.hedvig.app.feature.insurance.ui.InsuranceAdapter
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.ScrollPositionListener
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class InsuranceFragment : Fragment(R.layout.fragment_insurance) {
    private val insuranceViewModel: InsuranceViewModel by sharedViewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val marketManager: MarketManager by inject()
    private val imageLoader: ImageLoader by inject()
    private val binding by viewBinding(FragmentInsuranceBinding::bind)
    private var scroll = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.insuranceRecycler.apply {
            scroll = 0
            addOnScrollListener(
                ScrollPositionListener(
                    onScroll = { scrollPosition ->
                        scroll = scrollPosition
                        loggedInViewModel.onScroll(scrollPosition)
                    },
                    lifecycleOwner = viewLifecycleOwner
                )
            )
            itemAnimator = ViewHolderReusingDefaultItemAnimator()
            adapter = InsuranceAdapter(
                marketManager,
                insuranceViewModel::load,
                insuranceViewModel::onClickCrossSellAction,
                imageLoader,
                insuranceViewModel::onClickCrossSellCard,
            )
        }

        binding.swipeToRefresh.setOnRefreshListener {
            insuranceViewModel.load()
        }

        insuranceViewModel
            .viewState
            .flowWithLifecycle(viewLifecycle)
            .onEach { viewState ->
                val action = (viewState as? InsuranceViewModel.ViewState.Success)?.action
                if (action != null) {
                    insuranceViewModel.crossSellActionOpened()
                    handleAction(requireContext(), action)
                }
                bind(viewState)
            }
            .launchIn(viewLifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.onScroll(scroll)
        insuranceViewModel.load()
    }

    override fun onPause() {
        super.onPause()
        insuranceViewModel.markCardCrossSellsAsSeen()
    }

    private fun bind(viewState: InsuranceViewModel.ViewState) {
        val adapter = binding.insuranceRecycler.adapter as? InsuranceAdapter ?: return
        binding.swipeToRefresh.isRefreshing = viewState is InsuranceViewModel.ViewState.Loading

        when (viewState) {
            InsuranceViewModel.ViewState.Error -> {
                adapter.submitList(
                    listOf(
                        InsuranceModel.Header,
                        InsuranceModel.Error
                    )
                )
            }
            InsuranceViewModel.ViewState.Loading -> {
            }
            is InsuranceViewModel.ViewState.Success -> {
                adapter.submitList(viewState.items)
            }
        }
    }
}
