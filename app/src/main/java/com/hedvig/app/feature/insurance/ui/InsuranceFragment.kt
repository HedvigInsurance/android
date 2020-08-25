package com.hedvig.app.feature.insurance.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentInsuranceBinding
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setupToolbarAlphaScrollListener
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class InsuranceFragment : Fragment(R.layout.fragment_insurance) {
    private val insuranceViewModel: InsuranceViewModel by sharedViewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val tracker: InsuranceTracker by inject()
    private val binding by viewBinding(FragmentInsuranceBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            val scrollInitialTopPadding = recycler.paddingTop
            loggedInViewModel.toolbarInset.observe(this@InsuranceFragment) { tbi ->
                tbi?.let { toolbarInsets ->
                    recycler.updatePadding(top = scrollInitialTopPadding + toolbarInsets)
                }
            }

            val scrollInitialBottomPadding = recycler.paddingBottom
            loggedInViewModel.bottomTabInset.observe(this@InsuranceFragment) { bti ->
                bti?.let { bottomTabInset ->
                    recycler.updatePadding(bottom = scrollInitialBottomPadding + bottomTabInset)
                }
            }

            recycler.setupToolbarAlphaScrollListener(loggedInViewModel)
            recycler.adapter = InsuranceAdapter(parentFragmentManager, tracker)
        }
        insuranceViewModel.data.observe(this) { data ->
            data?.let { bind(it) }
        }
    }

    private fun bind(data: Pair<InsuranceQuery.Data?, PayinStatusQuery.Data?>) {
        binding.loadSpinner.root.remove()
        val (dashboardData, payinStatusData) = data

        val infoBoxes = mutableListOf<InsuranceModel.Renewal>()

        val renewals = dashboardData?.contracts.orEmpty().mapNotNull { it.upcomingRenewal }

        renewals.forEach {
            infoBoxes.add(
                InsuranceModel.Renewal(
                    it.renewalDate,
                    it.draftCertificateUrl
                )
            )
        }

        val contracts = dashboardData?.contracts.orEmpty().map { InsuranceModel.Contract(it) }

        val upsells = mutableListOf<InsuranceModel.Upsell>()
        dashboardData?.let { dd ->
            if (isNorway(dd.contracts)) {
                if (doesNotHaveHomeContents(dd.contracts)) {
                    upsells.add(UPSELL_HOME_CONTENTS)
                } else if (doesNotHaveTravelInsurance(dd.contracts)) {
                    upsells.add(UPSELL_TRAVEL)
                }
            }
        }

        (binding.recycler.adapter as? InsuranceAdapter)?.items =
            listOf(InsuranceModel.Header) + infoBoxes + contracts + upsells
    }

    override fun onResume() {
        super.onResume()
        binding.recycler.scrollToPosition(0)
    }

    companion object {
        private val UPSELL_HOME_CONTENTS =
            InsuranceModel.Upsell(
                R.string.UPSELL_NOTIFICATION_CONTENT_TITLE,
                R.string.UPSELL_NOTIFICATION_CONTENT_DESCRIPTION,
                R.string.UPSELL_NOTIFICATION_CONTENT_CTA
            )

        private val UPSELL_TRAVEL =
            InsuranceModel.Upsell(
                R.string.UPSELL_NOTIFICATION_TRAVEL_TITLE,
                R.string.UPSELL_NOTIFICATION_TRAVEL_DESCRIPTION,
                R.string.UPSELL_NOTIFICATION_TRAVEL_CTA
            )

        fun isNorway(contracts: List<InsuranceQuery.Contract>) =
            contracts.any { it.currentAgreement.asNorwegianTravelAgreement != null || it.currentAgreement.asNorwegianHomeContentAgreement != null }

        fun doesNotHaveHomeContents(contracts: List<InsuranceQuery.Contract>) =
            contracts.none { it.currentAgreement.asNorwegianHomeContentAgreement != null }

        fun doesNotHaveTravelInsurance(contracts: List<InsuranceQuery.Contract>) =
            contracts.none { it.currentAgreement.asNorwegianTravelAgreement != null }
    }
}
