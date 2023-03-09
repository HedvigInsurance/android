package com.hedvig.app.feature.insurance.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import coil.ImageLoader
import com.google.android.material.tabs.TabLayoutMediator
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.market.MarketManager
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailActivityBinding
import com.hedvig.app.feature.insurance.ui.bindTo
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageFragment
import com.hedvig.app.feature.insurance.ui.detail.documents.DocumentsFragment
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoFragment
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import slimber.log.e

class ContractDetailActivity : AppCompatActivity(R.layout.contract_detail_activity) {

  private val binding by viewBinding(ContractDetailActivityBinding::bind)
  private val contractId: String
    get() = intent.getStringExtra(ID)
      ?: throw IllegalArgumentException("Programmer error: ID not provided to ${this.javaClass.name}")
  private val viewModel: ContractDetailViewModel by viewModel { parametersOf(contractId) }
  private val marketManager: MarketManager by inject()
  private val imageLoader: ImageLoader by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    postponeEnterTransition()
    window.apply {
      requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
      sharedElementEnterTransition = sharedElementTransition()
      sharedElementExitTransition = sharedElementTransition()
    }
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    binding.apply {
      window.compatSetDecorFitsSystemWindows(false)
      toolbar.applyStatusBarInsets()
      toolbar.setNavigationOnClickListener {
        onBackPressedDispatcher.onBackPressed()
      }
      tabContent.offscreenPageLimit = 1
      tabContent.adapter = ContractDetailTabAdapter(this@ContractDetailActivity, contractId)
      TabLayoutMediator(tabContainer, tabContent) { tab, position ->
        when (position) {
          0 -> {
            tab.setText(hedvig.resources.R.string.insurance_details_view_tab_1_title)
          }
          1 -> {
            tab.setText(hedvig.resources.R.string.insurance_details_view_tab_2_title)
          }
          2 -> {
            tab.setText(hedvig.resources.R.string.insurance_details_view_tab_3_title)
          }
          else -> {
            e { "Invalid tab index: $position" }
          }
        }
      }.attach()
      cardContainer.arrow.isInvisible = true
      cardContainer.card.transitionName = "contract_card"
      error.onClick = { viewModel.loadContract(contractId) }

      viewModel
        .viewState
        .flowWithLifecycle(lifecycle)
        .onEach { viewState ->
          when (viewState) {
            ContractDetailViewModel.ViewState.Error -> {
              content.remove()
              error.show()
            }
            ContractDetailViewModel.ViewState.Loading -> {}
            is ContractDetailViewModel.ViewState.Success -> {
              content.show()
              error.remove()
              val contract = viewState.state.contractCardViewState
              contract.bindTo(cardContainer, marketManager, imageLoader)
            }
          }
          startPostponedEnterTransition()
        }
        .launchIn(lifecycleScope)
    }
  }

  private fun sharedElementTransition() = ChangeBounds().apply {
    duration = 200
    interpolator = AccelerateDecelerateInterpolator()
  }

  companion object {
    private const val ID = "ID"
    fun newInstance(context: Context, id: String) =
      Intent(context, ContractDetailActivity::class.java).apply {
        putExtra(ID, id)
      }
  }
}

class ContractDetailTabAdapter(
  activity: AppCompatActivity,
  private val contractId: String,
) : FragmentStateAdapter(activity) {
  override fun getItemCount() = 3
  override fun createFragment(position: Int) = when (position) {
    0 -> YourInfoFragment()
    1 -> CoverageFragment.newInstance(contractId)
    2 -> DocumentsFragment()
    else -> Fragment()
  }
}
