package com.hedvig.app.feature.welcome

import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.feature.dismissiblepager.DismissiblePager
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import org.koin.android.ext.android.inject

class WelcomeDialog : DismissiblePager() {

    override val proceedLabel = R.string.NEWS_PROCEED
    override val lastButtonText = R.string.NEWS_DISMISS
    override val animationStyle = R.style.WelcomeDialogAnimation
    override val titleLabel: Nothing? = null
    override val shouldShowLogo = true

    override val tracker: WelcomeTracker by inject()
    override val items: List<DismissiblePagerModel>
        get() = requireArguments().getParcelableArrayList<DismissiblePagerModel>(ITEMS).orEmpty()

    companion object {
        const val TAG = "WelcomeDialog"
        private const val ITEMS = "items"

        fun newInstance(items: List<DismissiblePagerModel>) = WelcomeDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ITEMS, ArrayList(items + DismissiblePagerModel.SwipeOffScreen))
            }
        }
    }
}
