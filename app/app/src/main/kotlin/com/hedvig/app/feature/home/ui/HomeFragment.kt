package com.hedvig.app.feature.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import coil.ImageLoader
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.MarketManager
import com.hedvig.app.R
import com.hedvig.app.databinding.HomeFragmentBinding
import com.hedvig.app.feature.claims.ui.startClaimsFlow
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.PaymentType
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.home_fragment) {
  private val viewModel: HomeViewModel by viewModel()
  private val binding by viewBinding(HomeFragmentBinding::bind)
  private val imageLoader: ImageLoader by inject()
  private val marketManager: MarketManager by inject()
  private val hAnalytics: HAnalytics by inject()
  private val featureManager: FeatureManager by inject()

  private val registerForActivityResult: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      viewModel.reload()
    }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val homeAdapter = HomeAdapter(
      fragmentManager = parentFragmentManager,
      retry = viewModel::reload,
      imageLoader = imageLoader,
      marketManager = marketManager,
      onClaimDetailCardClicked = viewModel::onClaimDetailCardClicked,
      onClaimDetailCardShown = viewModel::onClaimDetailCardShown,
      onPaymentCardShown = viewModel::onPaymentCardShown,
      onPaymentCardClicked = ::onPaymentCardClicked,
      onStartClaimClicked = ::onStartClaimClicked,
      onStartMovingFlow = ::onStartMovingFlow,
    )

    binding.swipeToRefresh.setOnRefreshListener {
      viewModel.reload()
    }

    binding.recycler.apply {
      applyNavigationBarInsets()
      applyStatusBarInsets()

      itemAnimator = ViewHolderReusingDefaultItemAnimator()
      adapter = homeAdapter
      (layoutManager as? GridLayoutManager)?.spanSizeLookup =
        object : GridLayoutManager.SpanSizeLookup() {
          override fun getSpanSize(position: Int): Int {
            (binding.recycler.adapter as? HomeAdapter)?.currentList?.getOrNull(position)
              ?.let { item ->
                return when (item) {
                  is HomeModel.CommonClaim -> 1
                  else -> 2
                }
              }
            return 2
          }
        }
      addItemDecoration(HomeItemDecoration(context))
    }

    viewModel.viewState
      .flowWithLifecycle(lifecycle)
      .onEach { viewState ->
        binding.swipeToRefresh.isRefreshing = viewState is HomeViewModel.ViewState.Loading

        when (viewState) {
          is HomeViewModel.ViewState.Error -> homeAdapter.submitList(listOf(HomeModel.Error))
          HomeViewModel.ViewState.Loading -> binding.swipeToRefresh.isRefreshing = true
          is HomeViewModel.ViewState.Success -> homeAdapter.submitList(viewState.homeItems)
        }
      }
      .launchIn(lifecycleScope)
  }

  private fun onStartClaimClicked() {
    lifecycleScope.launch {
      hAnalytics.beginClaim(AppScreen.HOME)
      startClaimsFlow(
        fragmentManager = parentFragmentManager,
        registerForResult = ::registerForResult,
        featureManager = featureManager,
        context = requireContext(),
        commonClaimId = null,
      )
    }
  }

  private fun onPaymentCardClicked(paymentType: PaymentType) {
    viewModel.onPaymentCardClicked()
    val market = marketManager.market ?: return
    startActivity(
      connectPayinIntent(
        requireContext(),
        paymentType,
        market,
        false,
      ),
    )
  }

  private fun onStartMovingFlow() {
    lifecycleScope.launch {
      if (featureManager.isFeatureEnabled(Feature.NEW_MOVING_FLOW)) {
        context?.startActivity(
          Intent(
            requireContext(),
            com.hedvig.android.feature.changeaddress.ChangeAddressActivity::class.java,
          ),
        )
      } else {
        context?.startActivity(ChangeAddressActivity.newInstance(requireContext()))
      }
    }
  }

  private fun registerForResult(intent: Intent) {
    registerForActivityResult.launch(intent)
  }
}
