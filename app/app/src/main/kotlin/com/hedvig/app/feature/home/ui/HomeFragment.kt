package com.hedvig.app.feature.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import coil.ImageLoader
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.MarketManager
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
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {
  private val viewModel: HomeViewModel by viewModel()
  private val imageLoader: ImageLoader by inject()
  private val marketManager: MarketManager by inject()
  private val hAnalytics: HAnalytics by inject()
  private val featureManager: FeatureManager by inject()

  private val registerForActivityResult: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      viewModel.reload()
    }

  @OptIn(ExperimentalMaterialApi::class)
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        HedvigTheme {
          Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
          ) {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isLoading = uiState.isLoading

            val pullRefreshState = rememberPullRefreshState(
              refreshing = isLoading,
              onRefresh = viewModel::reload,
            )
            Box() {
              Column(
                Modifier
                  .matchParentSize()
                  .pullRefresh(pullRefreshState)
                  .verticalScroll(rememberScrollState())
                  .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
              ) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
                Spacer(Modifier.height(64.dp))

                when (uiState) {
                  HomeUiState.Loading -> {}
                  is HomeUiState.Error -> {
                    GenericErrorScreen(
                      onRetryButtonClick = viewModel::reload,
                      modifier = Modifier
                        .padding(16.dp)
                        .padding(top = (80 - 16).dp),
                    )
                  }
                  is HomeUiState.Success -> {
                    // todo add success items
                    // homeAdapter.submitList(viewState.homeItems)
                  }
                }

                Spacer(Modifier.height(16.dp))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
              }
              PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                scale = true,
                modifier = Modifier.align(Alignment.TopCenter),
              )
            }
          }
        }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val homeAdapter = HomeAdapter(
      fragmentManager = parentFragmentManager,
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
