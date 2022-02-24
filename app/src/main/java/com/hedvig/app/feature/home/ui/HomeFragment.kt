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
import com.hedvig.app.R
import com.hedvig.app.databinding.HomeFragmentBinding
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.ScrollPositionListener
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.ui.animator.ViewHolderReusingDefaultItemAnimator
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.home_fragment) {
    private val model: HomeViewModel by viewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val binding by viewBinding(HomeFragmentBinding::bind)
    private var scroll = 0
    private val imageLoader: ImageLoader by inject()
    private val marketManager: MarketManager by inject()

    private val registerForActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            model.reload()
        }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.onScroll(scroll)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        scroll = 0

        val adapter = HomeAdapter(
            fragmentManager = parentFragmentManager,
            retry = model::reload,
            startIntentForResult = ::startEmbarkForResult,
            imageLoader = imageLoader,
            marketManager = marketManager,
            onClaimDetailCardClicked = model::onClaimDetailCardClicked,
            onClaimDetailCardShown = model::onClaimDetailCardShown,
            onPaymentCardShown = model::onPaymentCardShown,
        )

        binding.swipeToRefresh.setOnRefreshListener {
            model.reload()
        }

        binding.recycler.apply {
            applyNavigationBarInsets()
            applyStatusBarInsets()

            itemAnimator = ViewHolderReusingDefaultItemAnimator()
            this.adapter = adapter
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
            addOnScrollListener(
                ScrollPositionListener(
                    { scrollPosition ->
                        scroll = scrollPosition
                        loggedInViewModel.onScroll(scrollPosition)
                    },
                    viewLifecycleOwner
                )
            )
            this.adapter = adapter
        }

        model.viewState
            .flowWithLifecycle(lifecycle)
            .onEach { viewState ->
                binding.swipeToRefresh.isRefreshing = viewState is HomeViewModel.ViewState.Loading

                when (viewState) {
                    is HomeViewModel.ViewState.Error -> adapter.submitList(listOf(HomeModel.Error))
                    HomeViewModel.ViewState.Loading -> binding.swipeToRefresh.isRefreshing = true
                    is HomeViewModel.ViewState.Success -> adapter.submitList(viewState.homeItems)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun startEmbarkForResult(intent: Intent) {
        registerForActivityResult.launch(intent)
    }
}
