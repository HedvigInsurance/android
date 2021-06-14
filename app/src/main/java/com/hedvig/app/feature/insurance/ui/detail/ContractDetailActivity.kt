package com.hedvig.app.feature.insurance.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailActivityBinding
import com.hedvig.app.feature.insurance.ui.bindTo
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageFragment
import com.hedvig.app.feature.insurance.ui.detail.documents.DocumentsFragment
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoFragment
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContractDetailActivity : BaseActivity(R.layout.contract_detail_activity) {
    private val binding by viewBinding(ContractDetailActivityBinding::bind)
    private val model: ContractDetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        postponeEnterTransition()
        window.apply {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            sharedElementEnterTransition = sharedElementTransition()
            sharedElementExitTransition = sharedElementTransition()
        }
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(ID)

        if (id == null) {
            e { "Programmer error: ID not provided to ${this.javaClass.name}" }
            return
        }

        binding.apply {
            window.compatSetDecorFitsSystemWindows(false)
            toolbar.applyStatusBarInsets()
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            tabContent.adapter = ContractDetailTabAdapter(this@ContractDetailActivity)
            TabLayoutMediator(tabContainer, tabContent) { tab, position ->
                when (position) {
                    0 -> {
                        tab.setText(R.string.insurance_details_view_tab_1_title)
                    }
                    1 -> {
                        tab.setText(R.string.insurance_details_view_tab_2_title)
                    }
                    2 -> {
                        tab.setText(R.string.insurance_details_view_tab_3_title)
                    }
                    else -> {
                        e { "Invalid tab index: $position" }
                    }
                }
            }.attach()
            cardContainer.arrow.isInvisible = true
            cardContainer.card.transitionName = "contract_card"
            error.retry.setHapticClickListener {
                model.loadContract(id)
            }
            model
                .data
                .flowWithLifecycle(lifecycle)
                .onEach { viewState ->
                    when (viewState) {
                        ContractDetailViewModel.ViewState.Error -> {
                            content.isVisible = false
                            error.root.apply {
                                isVisible = true
                                setBackgroundColor(context.colorAttr(R.attr.colorSurface))
                            }
                        }
                        ContractDetailViewModel.ViewState.Loading -> {
                        }
                        is ContractDetailViewModel.ViewState.Success -> {
                            content.isVisible = true
                            error.root.isVisible = false
                            val contract = viewState.data
                            contract.bindTo(cardContainer, marketManager)
                            terminationInfo.isVisible =
                                contract.status.fragments.contractStatusFragment.asTerminatedStatus != null ||
                                contract.status.fragments.contractStatusFragment.asTerminatedTodayStatus != null
                        }
                    }
                    startPostponedEnterTransition()
                }
                .launchIn(lifecycleScope)
        }

        model.loadContract(id)
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

class ContractDetailTabAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 3
    override fun createFragment(position: Int) = when (position) {
        0 -> YourInfoFragment()
        1 -> CoverageFragment()
        2 -> DocumentsFragment()
        else -> Fragment()
    }
}
