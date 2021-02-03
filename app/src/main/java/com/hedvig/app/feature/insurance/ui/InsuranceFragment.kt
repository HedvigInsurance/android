package com.hedvig.app.feature.insurance.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentInsuranceBinding
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.ScrollPositionListener
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class InsuranceFragment : Fragment(R.layout.fragment_insurance) {
    private val insuranceViewModel: InsuranceViewModel by sharedViewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private val tracker: InsuranceTracker by inject()
    private val marketManager: MarketManager by inject()
    private val binding by viewBinding(FragmentInsuranceBinding::bind)
    private var scroll = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.onScroll(scroll)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scroll = 0

        binding.insuranceRecycler.apply {
            val scrollInitialTopPadding = paddingTop

            var hasInsetForToolbar = false

            loggedInViewModel.toolbarInset.observe(viewLifecycleOwner) { toolbarInsets ->
                updatePadding(top = scrollInitialTopPadding + toolbarInsets)
                if (!hasInsetForToolbar) {
                    hasInsetForToolbar = true
                    scrollToPosition(0)
                }
            }

            val scrollInitialBottomPadding = paddingBottom
            loggedInViewModel.bottomTabInset.observe(viewLifecycleOwner) { bottomTabInset ->
                updatePadding(bottom = scrollInitialBottomPadding + bottomTabInset)
            }
            addOnScrollListener(
                ScrollPositionListener(
                    { scrollPosition ->
                        scroll = scrollPosition
                        loggedInViewModel.onScroll(scrollPosition)
                    },
                    viewLifecycleOwner
                )
            )
            adapter = InsuranceAdapter(tracker, marketManager, insuranceViewModel::load)
        }
        insuranceViewModel.data.observe(viewLifecycleOwner) { data ->
            bind(data)
        }
    }

    private fun bind(data: Result<InsuranceQuery.Data>) {
        binding.loadSpinner.root.remove()

        if (data.isFailure) {
            (binding.insuranceRecycler.adapter as? InsuranceAdapter)?.submitList(
                listOf(
                    InsuranceModel.Header,
                    InsuranceModel.Error
                )
            )
            return
        }

        val successData = data.getOrNull() ?: return

        val contracts = successData.contracts.map(InsuranceModel::Contract).let { contractModels ->
            if (hasNotOnlyTerminatedContracts(successData.contracts)) {
                contractModels.filter { it.inner.status.fragments.contractStatusFragment.asTerminatedStatus == null }
            } else {
                contractModels
            }
        }

        val upsells = mutableListOf<InsuranceModel.Upsell>()

        if (isNorway(successData.contracts)) {
            if (doesNotHaveHomeContents(successData.contracts)) {
                upsells.add(UPSELL_HOME_CONTENTS)
            } else if (doesNotHaveTravelInsurance(successData.contracts)) {
                upsells.add(UPSELL_TRAVEL)
            }
        }

        (binding.insuranceRecycler.adapter as? InsuranceAdapter)?.submitList(
            listOf(InsuranceModel.Header) + contracts + terminatedRow(successData.contracts) + upsells
        )
    }

    private fun terminatedRow(contracts: List<InsuranceQuery.Contract>): List<InsuranceModel> {
        val terminatedContracts = amountOfTerminatedContracts(contracts)
        return if (hasNotOnlyTerminatedContracts(contracts)) {
            listOf(
                InsuranceModel.TerminatedContractsHeader,
                InsuranceModel.TerminatedContracts(terminatedContracts)
            )
        } else {
            emptyList()
        }
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
            contracts.any {
                it.currentAgreement.asNorwegianTravelAgreement != null ||
                    it.currentAgreement.asNorwegianHomeContentAgreement != null
            }

        fun doesNotHaveHomeContents(contracts: List<InsuranceQuery.Contract>) =
            contracts.none { it.currentAgreement.asNorwegianHomeContentAgreement != null }

        fun doesNotHaveTravelInsurance(contracts: List<InsuranceQuery.Contract>) =
            contracts.none { it.currentAgreement.asNorwegianTravelAgreement != null }

        fun amountOfTerminatedContracts(contracts: List<InsuranceQuery.Contract>) =
            contracts.filter { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }.size

        fun hasNotOnlyTerminatedContracts(contracts: List<InsuranceQuery.Contract>): Boolean {
            val terminatedContracts = amountOfTerminatedContracts(contracts)
            return (terminatedContracts > 0 && contracts.size != terminatedContracts)
        }
    }
}
