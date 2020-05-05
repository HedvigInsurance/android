package com.hedvig.app.feature.referrals

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.BaseTabFragment
import com.hedvig.app.feature.loggedin.ui.BaseTabViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.ui.decoration.BelowRecyclerViewBottomPaddingItemDecoration
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.safeLet
import e
import kotlinx.android.synthetic.main.fragment_new_referral.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ReferralsFragment : BaseTabFragment() {
    private val profileViewModel: ProfileViewModel by sharedViewModel()

    private val tabViewModel: BaseTabViewModel by sharedViewModel()

    override val layout = R.layout.fragment_new_referral

    private var toolbar: androidx.appcompat.widget.Toolbar? = null

    override fun onResume() {
        super.onResume()
        invites.scrollToPosition(0)
        tabViewModel.removeReferralNotification()
        (invites.adapter as? InvitesAdapter)?.startTankAnimation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = activity?.findViewById(R.id.hedvigToolbar)

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

        setupScrollListener()
    }

    private fun setupScrollListener() {
        invites.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                toolbar?.let { toolbar ->
                    val toolbarHeight = toolbar.height.toFloat()
                    val offset = invites.computeVerticalScrollOffset().toFloat()
                    val percentage = if (offset < toolbarHeight) {
                        offset / toolbarHeight
                    } else {
                        1f
                    }
                    if (dy < 0) {
                        // Scroll up
                        toolbar.elevation = percentage * 10
                    } else {
                        // scroll down
                        toolbar.elevation = percentage * 10
                    }
                }
            }
        })
    }

    private fun bindData(monthlyCost: Int, data: ProfileQuery.ReferralInformation) {
        invites.adapter = InvitesAdapter(monthlyCost, data)
    }
}
