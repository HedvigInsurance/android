package com.hedvig.app.feature.referrals

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.BaseTabFragment
import com.hedvig.app.feature.loggedin.ui.BaseTabViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.ui.decoration.BelowRecyclerViewBottomPaddingItemDecoration
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.safeLet
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import e
import kotlinx.android.synthetic.main.fragment_new_referral.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ReferralsFragment : BaseTabFragment() {
    private val profileViewModel: ProfileViewModel by sharedViewModel()

    private val tabViewModel: BaseTabViewModel by sharedViewModel()

    override val layout = R.layout.fragment_new_referral

    private var toolbarRoot: LinearLayout? = null
    private var toolbar: androidx.appcompat.widget.Toolbar? = null

    override fun onResume() {
        super.onResume()
        invites.scrollToPosition(0)
        tabViewModel.removeReferralNotification()
        (invites.adapter as? InvitesAdapter)?.startTankAnimation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarRoot = activity?.findViewById(R.id.toolbarTest)
        toolbar = activity?.findViewById(R.id.hedvigToolbar)

        invites.doOnApplyWindowInsets { view, insets, initialState ->
            val navbar = activity?.findViewById<BottomNavigationView>(R.id.bottomTabs)
            safeLet(toolbarRoot, navbar) { toolbar, navbar ->
                view.updatePadding(
                    top = initialState.paddings.top + toolbar.measuredHeight,
                    bottom = initialState.paddings.bottom + navbar.measuredHeight + insets.systemWindowInsetBottom
                )
            }
        }

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
        val toolbarText = activity?.findViewById<TextView>(R.id.toolbarText)
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
                        toolbarText?.offsetTopAndBottom(-dy)
                        toolbar.elevation = percentage * 10
                    } else {
                        // scroll down
                        toolbarText?.offsetTopAndBottom(-dy)
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
