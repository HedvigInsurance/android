package com.hedvig.app.feature.insurance.ui.terminatedcontracts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.core.view.updatePadding
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.TerminatedContractsActivityBinding
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.insurance.ui.InsuranceAdapter
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class TerminatedContractsActivity : BaseActivity(R.layout.terminated_contracts_activity) {
    private val binding by viewBinding(TerminatedContractsActivityBinding::bind)
    private val model: InsuranceViewModel by viewModel()
    private val tracker: InsuranceTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.allowEnterTransitionOverlap = true
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        postponeEnterTransition()
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }
            recycler.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }
            toolbar.setNavigationOnClickListener { onBackPressed() }
            recycler.adapter = InsuranceAdapter(tracker, model::load)
            model.data.observe(this@TerminatedContractsActivity) { data ->
                if (data.isFailure) {
                    (recycler.adapter as? InsuranceAdapter)?.submitList(listOf(InsuranceModel.Error))
                    return@observe
                }

                data
                    .getOrNull()
                    ?.contracts
                    ?.filter { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }
                    ?.let { terminatedContracts ->
                        (recycler.adapter as? InsuranceAdapter)?.submitList(
                            terminatedContracts.map {
                                InsuranceModel.Contract(
                                    it
                                )
                            }
                        )
                        recycler.post {
                            startPostponedEnterTransition()
                        }
                    }
            }
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, TerminatedContractsActivity::class.java)
    }
}
