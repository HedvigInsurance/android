package com.hedvig.app.feature.insurance.ui.tab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.SimpleItemAnimator
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentInsuranceBinding
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.insurance.ui.InsuranceAdapter
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.ScrollPositionListener
import com.hedvig.app.feature.settings.MarketManager
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
    private val tracker: InsuranceTracker by inject()
    private val marketManager: MarketManager by inject()
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
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            adapter = InsuranceAdapter(tracker, marketManager, insuranceViewModel::load)
        }

        binding.swipeToRefresh.setOnRefreshListener {
            insuranceViewModel.load()
        }

        insuranceViewModel
            .viewState
            .flowWithLifecycle(viewLifecycle)
            .onEach { bind(it) }
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
