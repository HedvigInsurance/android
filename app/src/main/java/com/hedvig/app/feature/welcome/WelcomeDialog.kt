package com.hedvig.app.feature.welcome

import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.feature.dismissablepager.DismissablePager
import com.hedvig.app.feature.dismissablepager.DismissablePagerPage
import org.koin.android.ext.android.inject
import timber.log.Timber

class WelcomeDialog : DismissablePager() {

    override val proceedLabel = R.string.NEWS_PROCEED
    override val dismissLabel = R.string.NEWS_DISMISS
    override val animationStyle = R.style.WelcomeDialogAnimation
    override val titleLabel: Nothing? = null

    override val tracker: WelcomeTracker by inject()
    override val items: List<DismissablePagerPage> by lazy {
        arguments!!.getParcelableArrayList<DismissablePagerPage>(ITEMS)
    }

    override fun onDismiss() {
        Timber.i("Dismissed!")
    }

    companion object {
        const val TAG = "WelcomeDialog"
        private const val ITEMS = "items"

        fun newInstance(items: List<DismissablePagerPage>) = WelcomeDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ITEMS, ArrayList(items))
            }
        }
    }
}
