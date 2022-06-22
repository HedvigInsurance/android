package com.hedvig.app.feature.home.ui

import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.feature.dismissiblepager.DismissiblePager
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel

class HowClaimsWorkDialog : DismissiblePager() {
    override val proceedLabel = R.string.claims_explainer_button_next
    override val lastButtonText = R.string.general_close_button
    override val animationStyle = R.style.DialogSlideInSlideOut
    override val titleLabel: Nothing? = null
    override val shouldShowLogo = false

    override val items: List<DismissiblePagerModel>
        get() = requireArguments().getParcelableArrayList<DismissiblePagerModel>(ITEMS).orEmpty()

    override fun onLastPageButton() {
        dismiss()
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
