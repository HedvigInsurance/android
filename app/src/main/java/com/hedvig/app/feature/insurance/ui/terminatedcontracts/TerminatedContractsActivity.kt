package com.hedvig.app.feature.insurance.ui.terminatedcontracts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.TerminatedContractsActivityBinding
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.insurance.ui.InsuranceAdapter
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class TerminatedContractsActivity : BaseActivity(R.layout.terminated_contracts_activity) {
    private val binding by viewBinding(TerminatedContractsActivityBinding::bind)
    private val model: InsuranceViewModel by viewModel()
    private val tracker: InsuranceTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.recycler.adapter = InsuranceAdapter(tracker, model::load)

        model.data.observe(this) { data ->
            if (data.isFailure) {
                (binding.recycler.adapter as? InsuranceAdapter)?.submitList(listOf(InsuranceModel.Error))
                return@observe
            }

            data.getOrNull()?.contracts?.filter { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }
                ?.let { terminatedContracts ->
                    (binding.recycler.adapter as? InsuranceAdapter)?.submitList(terminatedContracts.map {
                        InsuranceModel.Contract(
                            it
                        )
                    })
                }
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, TerminatedContractsActivity::class.java)
    }
}
