package com.hedvig.app.feature.dashboard.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.R
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
        infoCards.adapter = InfoBoxAdapter()

        dashboardViewModel.data.observe(this) { data ->
            data?.let { bind(it) }
        }
    }

    private fun bind(data: Pair<DashboardQuery.Data?, PayinStatusQuery.Data?>) {
        loadingSpinner.remove()
        val (dashboardData, payinStatusData) = data
        dashboardData?.let { bindDashboardData(it) }

        val infoBoxes = mutableListOf<InfoBoxModel>()

        dashboardData?.importantMessages?.firstOrNull()?.let { importantMessage ->
            infoBoxes.add(
                InfoBoxModel.ImportantInformation(
                    importantMessage.title ?: "",
                    importantMessage.message ?: "",
                    importantMessage.button ?: "",
                    importantMessage.link ?: ""
                )
            )
        }

        val renewals = dashboardData?.contracts.orEmpty().mapNotNull { it.upcomingRenewal }

        renewals.forEach {
            infoBoxes.add(
                InfoBoxModel.Renewal(
                    it.renewalDate,
                    it.draftCertificateUrl
                )
            )
        }

        if (payinStatusData?.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP) {
            infoBoxes.add(InfoBoxModel.ConnectPayin)
        }

        (infoCards.adapter as? InfoBoxAdapter)?.items = infoBoxes
    }

    private fun bindDashboardData(data: DashboardQuery.Data) {
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

        fun isNorway(contracts: List<DashboardQuery.Contract>) =
            contracts.any { it.currentAgreement.asNorwegianTravelAgreement != null || it.currentAgreement.asNorwegianHomeContentAgreement != null }

        fun doesNotHaveHomeContents(contracts: List<DashboardQuery.Contract>) =
            contracts.none { it.currentAgreement.asNorwegianHomeContentAgreement != null }

        fun doesNotHaveTravelInsurance(contracts: List<DashboardQuery.Contract>) =
            contracts.none { it.currentAgreement.asNorwegianTravelAgreement != null }
    }
}
