package com.hedvig.app.feature.dashboard.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.service.DashboardTracker
import com.hedvig.app.feature.trustly.TrustlyActivity
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
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

        dashboardViewModel.directDebitStatus.observe(this) { directDebitStatusData ->
            directDebitStatusData?.let { bindDirectDebiStatusData(it) }
        }
    }

    private fun bindDirectDebiStatusData(data: DirectDebitQuery.Data) {
        if (data.directDebitStatus == DirectDebitStatus.NEEDS_SETUP) {
            infoBoxTitle.text = getString(R.string.DASHBOARD_SETUP_DIRECT_DEBIT_TITLE)
            infoBoxBody.text = getString(R.string.DASHBOARD_DIRECT_DEBIT_STATUS_NEED_SETUP_DESCRIPTION)
            infoBoxButton.text = getString(R.string.DASHBOARD_DIRECT_DEBIT_STATUS_NEED_SETUP_BUTTON_LABEL)
            infoBoxButton.setHapticClickListener {
                startActivity(TrustlyActivity.newInstance(requireContext()))
            }
            infoBox.show()
        } else {
            infoBox.remove()
        }
    }

    private fun bindDashboardData(data: DashboardQuery.Data) {
        loadingSpinner.remove()
        (contracts.adapter as? ContractAdapter)?.items = data.contracts

        if (isNorway(data.contracts)) {
            if (doesNotHaveHomeContents(data.contracts)) {
                (upsells.adapter as? UpsellAdapter)?.items = listOf(
                    UPSELL_HOME_CONTENTS
                )
            }
            if (doesNotHaveTravelInsurance(data.contracts)) {
                (upsells.adapter as? UpsellAdapter)?.items = listOf(
                    UPSELL_TRAVEL
                )
            }
        }
    }

    companion object {
        private val UPSELL_HOME_CONTENTS =
            UpsellModel(
                R.string.UPSELL_NOTIFICATION_CONTENT_TITLE,
                R.string.UPSELL_NOTIFICATION_CONTENT_DESCRIPTION,
                R.string.UPSELL_NOTIFICATION_CONTENT_CTA
            )

        private val UPSELL_TRAVEL =
            UpsellModel(
                R.string.UPSELL_NOTIFICATION_TRAVEL_TITLE,
                R.string.UPSELL_NOTIFICATION_TRAVEL_DESCRIPTION,
                R.string.UPSELL_NOTIFICATION_TRAVEL_CTA
            )

        fun isNorway(contracts: List<DashboardQuery.Contract>) = contracts.any { it.currentAgreement.asNorwegianTravelAgreement != null || it.currentAgreement.asNorwegianHomeContentAgreement != null }

        fun doesNotHaveHomeContents(contracts: List<DashboardQuery.Contract>) = contracts.none { it.currentAgreement.asNorwegianHomeContentAgreement != null }
        fun doesNotHaveTravelInsurance(contracts: List<DashboardQuery.Contract>) = contracts.none { it.currentAgreement.asNorwegianTravelAgreement != null }
    }
}
