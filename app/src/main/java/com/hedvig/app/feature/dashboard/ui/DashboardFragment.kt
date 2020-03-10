package com.hedvig.app.feature.dashboard.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.UpsellAdapter
import com.hedvig.app.UpsellModel
import com.hedvig.app.feature.dashboard.service.DashboardTracker
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private val tracker: DashboardTracker by inject()
    private val dashboardViewModel: DashboardViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contracts.adapter = ContractAdapter(parentFragmentManager)
        upsells.adapter = UpsellAdapter()

        dashboardViewModel.data.observe(this) { dashboardData ->
            dashboardData?.let { bindDashboardData(it) }
        }
    }

    private fun bindDashboardData(data: DashboardData) {
        loadingSpinner.remove()
        (contracts.adapter as? ContractAdapter)?.items = data.contracts

        if (isNorway(data.contracts)) {
            if (doesNotHaveHomeContents(data.contracts)) {
                (upsells.adapter as? UpsellAdapter)?.items = listOf(
                    UpsellModel( // TODO: Translations and Icon
                        android.R.drawable.btn_plus,
                        android.R.string.ok,
                        android.R.string.ok,
                        android.R.string.ok
                    )
                )
            }
            if (doesNotHaveTravelInsurance(data.contracts)) {
                (upsells.adapter as? UpsellAdapter)?.items = listOf(
                    UpsellModel( // TODO: Translations and Icon
                        android.R.drawable.btn_minus,
                        android.R.string.no,
                        android.R.string.no,
                        android.R.string.no
                    )
                )
            }
        }
    }

    companion object {
        fun isNorway(contracts: List<Contract>) = contracts.any { it.currentAgreement.asNorwegianTravelAgreement != null || it.currentAgreement.asNorwegianHomeContentAgreement != null }

        fun doesNotHaveHomeContents(contracts: List<Contract>) = contracts.none { it.currentAgreement.asNorwegianHomeContentAgreement != null }
        fun doesNotHaveTravelInsurance(contracts: List<Contract>) = contracts.none { it.currentAgreement.asNorwegianTravelAgreement != null }
    }
}
