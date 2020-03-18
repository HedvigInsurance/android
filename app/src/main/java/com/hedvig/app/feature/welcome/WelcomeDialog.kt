package com.hedvig.app.feature.welcome

import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.feature.dismissiblepager.DismissiblePager
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerPage
import com.hedvig.app.feature.ratings.RatingsDialog
import org.koin.android.ext.android.inject

class WelcomeDialog : DismissiblePager() {

    override val proceedLabel = R.string.NEWS_PROCEED
    override val dismissLabel = R.string.NEWS_DISMISS
    override val animationStyle = R.style.WelcomeDialogAnimation
    override val titleLabel: Nothing? = null

    override val tracker: WelcomeTracker by inject()
    override val items: List<DismissiblePagerPage> by lazy {
        arguments?.getParcelableArrayList<DismissiblePagerPage>(ITEMS)
            ?: throw Error("Cannot create a WelcomeDialog without any items")
    }

    override fun onDismiss() {
        RatingsDialog
            .newInstance()
            .show(parentFragmentManager, RatingsDialog.TAG)
    }

    companion object {
        const val TAG = "WelcomeDialog"
        private const val ITEMS = "items"

        fun newInstance(items: List<DismissiblePagerPage>) = WelcomeDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ITEMS, ArrayList(items))
            }
        }
    }
}
