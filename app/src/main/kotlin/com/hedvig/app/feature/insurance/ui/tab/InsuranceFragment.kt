package com.hedvig.app.feature.insurance.ui.tab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import coil.ImageLoader
import com.hedvig.android.market.MarketManager
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentInsuranceBinding
import com.hedvig.app.feature.insurance.ui.InsuranceAdapter
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.ScrollPositionListener
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.openWebBrowser
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class InsuranceFragment : Fragment(R.layout.fragment_insurance) {
  private val insuranceViewModel: InsuranceViewModel by activityViewModel()
  private val loggedInViewModel: LoggedInViewModel by activityViewModel()
  private val marketManager: MarketManager by inject()
  private val imageLoader: ImageLoader by inject()
  private val binding by viewBinding(FragmentInsuranceBinding::bind)
  private var scroll = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val insuranceAdapter = InsuranceAdapter(
      marketManager,
      insuranceViewModel::load,
      insuranceViewModel::onClickCrossSellAction,
      imageLoader,
      insuranceViewModel::onClickCrossSellCard,
    )
    binding.insuranceRecycler.apply {
      scroll = 0
      addOnScrollListener(
        ScrollPositionListener(
          onScroll = { scrollPosition ->
            scroll = scrollPosition
            loggedInViewModel.onScroll(scrollPosition)
          },
          lifecycleOwner = viewLifecycleOwner,
        ),
      )
      itemAnimator = ViewHolderReusingDefaultItemAnimator()
      adapter = insuranceAdapter
    }

    binding.swipeToRefresh.setOnRefreshListener {
      insuranceViewModel.load()
    }

    insuranceViewModel
      .viewState
      .flowWithLifecycle(viewLifecycle)
      .onEach { viewState ->
        with(viewState) {
          binding.swipeToRefresh.isRefreshing = viewState.loading

          viewState.storeUrl
            ?.let { activity?.openWebBrowser(it) }
            ?.also { insuranceViewModel.crossSellActionOpened() }

          when {
            hasError -> insuranceAdapter.submitList(
              listOf(
                InsuranceModel.Header,
                InsuranceModel.Error,
              ),
            )
            items != null -> insuranceAdapter.submitList(items)
          }
        }
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
}
