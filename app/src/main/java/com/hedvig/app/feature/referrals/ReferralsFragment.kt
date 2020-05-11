package com.hedvig.app.feature.referrals

import android.os.Bundle
import android.view.View
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.BaseTabFragment
import com.hedvig.app.feature.loggedin.ui.BaseTabViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.ui.decoration.BelowRecyclerViewBottomPaddingItemDecoration
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.safeLet
import e
import kotlinx.android.synthetic.main.fragment_new_referral.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ReferralsFragment : BaseTabFragment() {
    private val profileViewModel: ProfileViewModel by sharedViewModel()

    private val tabViewModel: BaseTabViewModel by sharedViewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()

    override val layout = R.layout.fragment_new_referral

    override fun onResume() {
        super.onResume()
        invites.scrollToPosition(0)
        tabViewModel.removeReferralNotification()
        (invites.adapter as? InvitesAdapter)?.startTankAnimation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        invites.addItemDecoration(
            BelowRecyclerViewBottomPaddingItemDecoration(
                resources.getDimensionPixelSize(R.dimen.referral_extra_bottom_space)
            )
        )

        profileViewModel.data.observe(this) { data ->
            safeLet(
                data?.insuranceCost?.fragments?.costFragment?.monthlyGross?.amount?.toBigDecimal()
                    ?.toInt(),
                data?.referralInformation
            ) { monthlyCost, referralCampaign ->
                bindData(monthlyCost, referralCampaign)
            } ?: e { "No data" }
        }

        invites.setupToolbarScrollListener(loggedInViewModel)
    }

    private fun bindData(monthlyCost: Int, data: ProfileQuery.ReferralInformation) {
        invites.adapter = InvitesAdapter(monthlyCost, data)
    }
}
