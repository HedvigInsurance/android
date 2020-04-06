package com.hedvig.app.feature.dashboard.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.service.DashboardTracker
import com.hedvig.app.ui.decoration.BelowRecyclerViewBottomPaddingItemDecoration
import com.hedvig.app.util.extensions.observe
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private val tracker: DashboardTracker by inject()
    private val dashboardViewModel: DashboardViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        root.adapter = DashboardAdapter(parentFragmentManager)
        root.addItemDecoration(
            BelowRecyclerViewBottomPaddingItemDecoration(
                resources.getDimensionPixelSize(R.dimen.referral_extra_bottom_space)
            )
        )
       
        dashboardViewModel.data.observe(this) { data ->
            data?.let { bind(it) }
        }
    }

    private fun bind(data: Pair<DashboardQuery.Data?, PayinStatusQuery.Data?>) {
        //loadingSpinner.remove()
        val (dashboardData, payinStatusData) = data

        val infoBoxes = mutableListOf<DashboardModel.InfoBox>()

        dashboardData?.importantMessages?.firstOrNull()?.let { importantMessage ->
            infoBoxes.add(
                DashboardModel.InfoBox(
                    InfoBoxModel.ImportantInformation(
                        importantMessage.title ?: "",
                        importantMessage.message ?: "",
                        importantMessage.button ?: "",
                        importantMessage.link ?: ""
                    )
                )
            )
        }

        val renewals = dashboardData?.contracts.orEmpty().mapNotNull { it.upcomingRenewal }

        renewals.forEach {
            infoBoxes.add(
                DashboardModel.InfoBox(
                    InfoBoxModel.Renewal(
                        it.renewalDate,
                        it.draftCertificateUrl
                    )
                )
            )
        }

        if (payinStatusData?.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP) {
            infoBoxes.add(DashboardModel.InfoBox(InfoBoxModel.ConnectPayin))
        }

        val contracts = dashboardData?.contracts.orEmpty().map { DashboardModel.Contract(it) }

        val upsells = mutableListOf<DashboardModel.Upsell>()
        dashboardData?.let { dd ->
            if (isNorway(dd.contracts)) {
                if (doesNotHaveHomeContents(dd.contracts)) {
                    upsells.add(DashboardModel.Upsell(UPSELL_HOME_CONTENTS))
                } else if (doesNotHaveTravelInsurance(dd.contracts)) {
                    upsells.add(DashboardModel.Upsell(UPSELL_TRAVEL))
                }
            }
        }

        (root.adapter as? DashboardAdapter)?.items = infoBoxes + contracts + upsells
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
