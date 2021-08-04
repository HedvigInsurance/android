package com.hedvig.app.feature.insurance.ui.terminatedcontracts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.TerminatedContractsActivityBinding
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.insurance.ui.InsuranceAdapter
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TerminatedContractsActivity : BaseActivity(R.layout.terminated_contracts_activity) {
    private val binding by viewBinding(TerminatedContractsActivityBinding::bind)
    private val model: InsuranceViewModel by viewModel()
    private val tracker: InsuranceTracker by inject()
    private val marketManager: MarketManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.allowEnterTransitionOverlap = true
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        postponeEnterTransition()
        super.onCreate(savedInstanceState)

        binding.apply {
            window.compatSetDecorFitsSystemWindows(false)
            toolbar.applyStatusBarInsets()
            recycler.applyNavigationBarInsets()
            toolbar.setNavigationOnClickListener { onBackPressed() }
            val adapter = InsuranceAdapter(tracker, marketManager, model::load)
            recycler.adapter = adapter
            model
                .data
                .flowWithLifecycle(lifecycle)
                .onEach { viewState ->
                    when (viewState) {
                        InsuranceViewModel.ViewState.Error -> {
                            adapter.submitList(listOf(InsuranceModel.Error))
                        }
                        InsuranceViewModel.ViewState.Loading -> {
                        }
                        is InsuranceViewModel.ViewState.Success -> {
                            val terminatedContracts = viewState
                                .data
                                .contracts
                                .filter { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }

                            adapter.submitList(
                                terminatedContracts.map { InsuranceModel.Contract(it) }
                            )
                            recycler.post { startPostponedEnterTransition() }
                        }
                    }
                }
                .launchIn(lifecycleScope)
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, TerminatedContractsActivity::class.java)
    }
}
