package com.hedvig.app.feature.welcome

import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.feature.dismissiblepager.DismissiblePager
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerPage
import com.hedvig.app.feature.ratings.RatingsDialog
import org.koin.android.ext.android.inject

class WelcomeDialog : DismissiblePager() {

    override val proceedLabel = R.string.NEWS_PROCEED
    override val lastButtonText = R.string.NEWS_DISMISS
    override val animationStyle = R.style.WelcomeDialogAnimation
    override val titleLabel: Nothing? = null

    override val tracker: WelcomeTracker by inject()
    override val items: List<DismissiblePagerPage>
        get() = requireArguments().getParcelableArrayList<DismissiblePagerPage>(ITEMS).orEmpty()

    override fun onDismiss() {
        RatingsDialog
            .newInstance()
            .show(parentFragmentManager, RatingsDialog.TAG)
    }

    override fun onLastSwipe() {
        dismiss()
        RatingsDialog
            .newInstance()
            .show(parentFragmentManager, RatingsDialog.TAG)
    }

    override fun onLastPageButton() {

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
