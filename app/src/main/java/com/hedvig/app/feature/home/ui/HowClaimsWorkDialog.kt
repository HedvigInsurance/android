package com.hedvig.app.feature.home.ui

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet
import com.hedvig.app.feature.dismissiblepager.DismissiblePager
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerPage
import com.hedvig.app.feature.welcome.WelcomeTracker
import org.koin.android.ext.android.inject

class HowClaimsWorkDialog : DismissiblePager() {
    override val proceedLabel = R.string.claims_explainer_02_button_next
    override val lastButtonText = R.string.NEWS_DISMISS
    override val animationStyle = R.style.DialogSlideInSlideOut
    override val titleLabel: Nothing? = null
    override val shouldShowLogo = false
    override val shouldCloseOnLastSwipe = false

    override val tracker: WelcomeTracker by inject()
    override val items: List<DismissiblePagerPage>
        get() = requireArguments().getParcelableArrayList<DismissiblePagerPage>(ITEMS).orEmpty()

    override fun onDismiss() {
    }

    override fun onLastSwipe() {
    }

    override fun onLastPageButton() {
        HonestyPledgeBottomSheet
            .newInstance("test")
            .show(parentFragmentManager, HonestyPledgeBottomSheet.TAG)
    }

    companion object {
        const val TAG = "WelcomeDialog"
        private const val ITEMS = "items"

        fun newInstance(items: List<DismissiblePagerPage>) =
            HowClaimsWorkDialog().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ITEMS, ArrayList(items))
                }
            }
    }
}
