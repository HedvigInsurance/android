package com.hedvig.app.feature.dashboard.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.service.DashboardTracker
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.safeLet
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private val tracker: DashboardTracker by inject()
    private val dashboardViewModel: DashboardViewModel by sharedViewModel()
    private var toolbar: LinearLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        root.doOnApplyWindowInsets { view, insets, initialState ->
            toolbar = activity?.findViewById(R.id.toolbarTest)
            val navbar = activity?.findViewById<BottomNavigationView>(R.id.bottomTabs)
            safeLet(toolbar, navbar) { toolbar, navbar ->
                view.updatePadding(
                    top = initialState.paddings.top + toolbar.measuredHeight,
                    bottom = initialState.paddings.bottom + navbar.measuredHeight + insets.systemWindowInsetBottom
                )
            }
        }
        setupScrollListener()
        root.adapter = DashboardAdapter(parentFragmentManager)

        dashboardViewModel.data.observe(this) { data ->
            data?.let { bind(it) }
        }
    }

    private fun setupScrollListener() {
        val toolbarText = activity?.findViewById<TextView>(R.id.toolbarText)
        root.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val dy = oldScrollY - scrollY
            toolbar?.let { toolbar ->
                val toolbarHeight = toolbar.height.toFloat()
                val offset = root.computeVerticalScrollOffset().toFloat()
                val percentage = if (offset < toolbarHeight) {
                    offset / toolbarHeight
                } else {
                    1f
                }
                if (dy < 0) {
                    // Scroll up
                    toolbarText?.offsetTopAndBottom(dy)
                    toolbar.elevation = percentage * 10
                } else {
                    // scroll down
                    toolbarText?.offsetTopAndBottom(dy)
                    toolbar.elevation = percentage * 10
                }
            }
        }
    }

    private fun bind(data: Pair<DashboardQuery.Data?, PayinStatusQuery.Data?>) {
        loadingSpinner.remove()
        val (dashboardData, payinStatusData) = data

        val infoBoxes = mutableListOf<DashboardModel.InfoBox>()

        dashboardData?.importantMessages?.firstOrNull()?.let { importantMessage ->
            infoBoxes.add(
                DashboardModel.InfoBox.ImportantInformation(
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
                DashboardModel.InfoBox.Renewal(
                    it.renewalDate,
                    it.draftCertificateUrl
                )
            )
        }

        if (payinStatusData?.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP) {
            infoBoxes.add(DashboardModel.InfoBox.ConnectPayin)
        }

        val contracts = dashboardData?.contracts.orEmpty().map { DashboardModel.Contract(it) }

        val upsells = mutableListOf<DashboardModel.Upsell>()
        dashboardData?.let { dd ->
            if (isNorway(dd.contracts)) {
                if (doesNotHaveHomeContents(dd.contracts)) {
                    upsells.add(UPSELL_HOME_CONTENTS)
                } else if (doesNotHaveTravelInsurance(dd.contracts)) {
                    upsells.add(UPSELL_TRAVEL)
                }
            }
        }

        (root.adapter as? DashboardAdapter)?.items = infoBoxes + contracts + upsells
    }

    companion object {
        private val UPSELL_HOME_CONTENTS =
            DashboardModel.Upsell(
                R.string.UPSELL_NOTIFICATION_CONTENT_TITLE,
                R.string.UPSELL_NOTIFICATION_CONTENT_DESCRIPTION,
                R.string.UPSELL_NOTIFICATION_CONTENT_CTA
            )

        private val UPSELL_TRAVEL =
            DashboardModel.Upsell(
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
