package com.hedvig.app.feature.home.ui

import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet
import com.hedvig.app.feature.dismissiblepager.DismissiblePager
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.welcome.WelcomeTracker
import org.koin.android.ext.android.inject

class HowClaimsWorkDialog : DismissiblePager() {
    override val proceedLabel = R.string.claims_explainer_button_next
    override val lastButtonText = R.string.claims_explainer_button_start_claim
    override val animationStyle = R.style.DialogSlideInSlideOut
    override val titleLabel: Nothing? = null
    override val shouldShowLogo = false

    override val tracker: WelcomeTracker by inject()
    override val items: List<DismissiblePagerModel>
        get() = requireArguments().getParcelableArrayList<DismissiblePagerModel>(ITEMS).orEmpty()

    override fun onDismiss() {
    }

    override fun onLastSwipe() {
    }

    override fun onLastPageButton() {
        HonestyPledgeBottomSheet
            .newInstance()
            .show(parentFragmentManager, HonestyPledgeBottomSheet.TAG)
    }

    companion object {
        const val TAG = "HowClaimsWorkDialog"
        private const val ITEMS = "items"

        fun newInstance(items: List<DismissiblePagerModel>) =
            HowClaimsWorkDialog().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ITEMS, ArrayList(items))
                }
            }
    }
}
